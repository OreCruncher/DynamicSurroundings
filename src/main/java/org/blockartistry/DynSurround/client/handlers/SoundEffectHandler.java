/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.DynSurround.client.handlers;

import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.gui.ConfigSound;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.sound.AdhocSound;
import org.blockartistry.DynSurround.client.sound.Emitter;
import org.blockartistry.DynSurround.client.sound.PlayerEmitter;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.client.sound.SoundEngine;
import org.blockartistry.DynSurround.client.sound.SoundState;
import org.blockartistry.DynSurround.client.sound.Sounds;
import org.blockartistry.DynSurround.event.DiagnosticEvent;
import org.blockartistry.DynSurround.event.PlayDistributedSoundEvent;
import org.blockartistry.DynSurround.event.ReloadEvent;
import org.blockartistry.DynSurround.network.Network;
import org.blockartistry.DynSurround.network.PacketPlaySound;
import org.blockartistry.lib.collections.ObjectArray;
import org.blockartistry.lib.compat.PositionedSoundUtil;
import org.blockartistry.lib.sound.BasicSound;

import com.google.common.collect.Sets;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SoundEffectHandler extends EffectHandlerBase {

	private static final int AGE_THRESHOLD_TICKS = 10;
	public static final SoundEffectHandler INSTANCE = new SoundEffectHandler();

	private static final Predicate<PendingSound> PENDING_SOUNDS = input -> {
		if (input.getTickAge() >= AGE_THRESHOLD_TICKS) {
			input.getSound().setState(SoundState.ERROR);
			return true;
		}
		if (input.getTickAge() >= 0) {
			return INSTANCE.playSound(input.getSound()) != null;
		}
		return false;
	};

	private final static class PendingSound {

		private final int timeMark;
		private final BasicSound<?> sound;

		public PendingSound(@Nonnull final BasicSound<?> sound, final int delay) {
			this.timeMark = EnvironState.getTickCounter() + delay;
			this.sound = sound;
		}

		public int getTickAge() {
			return EnvironState.getTickCounter() - this.timeMark;
		}

		public BasicSound<?> getSound() {
			return this.sound;
		}
	}

	private final THashMap<SoundEffect, Emitter> emitters = new THashMap<>();
	private final THashMap<String, SoundEvent> replacements = new THashMap<>();
	private final ObjectArray<PendingSound> pending = new ObjectArray<>();
	private final ObjectArray<BasicSound<?>> sendToServer = new ObjectArray<>();
	private final Set<String> soundsToBlock = Sets.newHashSet();
	private final TObjectIntHashMap<String> soundCull = new TObjectIntHashMap<String>();

	private SoundEffectHandler() {
		super("Sound Effects");
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {
		this.emitters.values().forEach(Emitter::update);
		this.pending.removeIf(PENDING_SOUNDS);
		this.sendToServer.forEach(sound -> Network.sendToServer(new PacketPlaySound(player, sound)));
		this.sendToServer.clear();
	}

	@Override
	public void onConnect() {
		clearSounds();
		this.soundsToBlock.clear();
		this.soundCull.clear();
		this.replacements.clear();
		final Set<ResourceLocation> reg = Minecraft.getMinecraft().getSoundHandler().soundRegistry.getKeys();
		reg.forEach(resource -> {
			final String rs = resource.toString();
			if (ClientRegistry.SOUND.isSoundBlockedLogical(rs)) {
				this.soundsToBlock.add(rs);
			} else if (ClientRegistry.SOUND.isSoundCulled(rs)) {
				this.soundCull.put(rs, -ModOptions.sound.soundCullingThreshold);
			}
		});

		final ResourceLocation bowLooseResource = new ResourceLocation(DSurround.MOD_ID, "bow.loose");
		final SoundEvent bowLoose = Sounds.getSound(bowLooseResource);
		if (!this.soundsToBlock.contains(bowLooseResource.toString())) {
			this.replacements.put("minecraft:entity.arrow.shoot", bowLoose);
			this.replacements.put("minecraft:entity.skeleton.shoot", bowLoose);
		}
	}

	@Override
	public void onDisconnect() {
		clearSounds();
	}

	public void clearSounds() {
		this.emitters.values().forEach(Emitter::stop);
		this.emitters.clear();
		this.pending.clear();
		SoundEngine.instance().stopAllSounds();
	}

	public void queueAmbientSounds(@Nonnull final TObjectFloatHashMap<SoundEffect> sounds) {
		// Iterate through the existing emitters:
		// * If done, remove
		// * If not in the incoming list, fade
		// * If it does exist, update volume throttle and unfade if needed
		this.emitters.retainEntries((fx, emitter) -> {
			if (emitter.isDonePlaying()) {
				return false;
			}
			final float volume = sounds.get(fx);
			if (volume >= 0) {
				emitter.setVolumeThrottle(volume);
				if (emitter.isFading())
					emitter.unfade();
				sounds.put(fx, 0F);
			} else if (!emitter.isFading()) {
				emitter.fade();
			}
			return true;
		});

		// Any sounds left in the list are new and need
		// an emitter created.
		sounds.forEachEntry((fx, volume) -> {
			if (volume > 0)
				this.emitters.put(fx, new PlayerEmitter(fx));
			return true;
		});
	}

	public boolean isSoundPlaying(@Nonnull final BasicSound<?> sound) {
		return SoundEngine.instance().isSoundPlaying(sound);
	}

	public boolean isSoundPlaying(@Nonnull final String soundId) {
		return SoundEngine.instance().isSoundPlaying(soundId);
	}

	public void stopSound(@Nonnull final BasicSound<?> sound) {
		SoundEngine.instance().stopSound(sound);
	}
	
	@Nullable
	public String playSound(@Nonnull final BasicSound<?> sound) {
		if (sound == null || !sound.canSoundBeHeard(EnvironState.getPlayerPosition()))
			return null;

		// If it is a routable sound do so if possible
		if (sound.shouldRoute() && DSurround.routePacketToServer())
			this.sendToServer.add(sound);

		return SoundEngine.instance().playSound(sound);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void soundPlay(@Nonnull final PlaySoundEvent e) {
		// Don't mess with our ConfigSound instances from the config
		// menu
		final ISound theSound = e.getSound();
		if (theSound instanceof ConfigSound)
			return;

		final ResourceLocation soundResource = theSound != null ? theSound.getSoundLocation() : null;

		// Check to see if the sound is blocked
		if (theSound != null && soundResource != null) {
			final String resource = soundResource.toString();
			if (this.soundsToBlock.contains(resource)) {
				e.setResultSound(null);
				return;
			}
		}

		// Check to see if it needs to be culled
		if (ModOptions.sound.soundCullingThreshold > 0 && soundResource != null) {
			// Get the last time the sound was seen
			final int lastOccurance = this.soundCull.get(soundResource);
			if (lastOccurance != 0) {
				final int currentTick = EnvironState.getTickCounter();
				if ((currentTick - lastOccurance) < ModOptions.sound.soundCullingThreshold) {
					// It's been culled!
					e.setResultSound(null);
					return;
				} else {
					// Set when it happened and fall through for remapping and stuff
					this.soundCull.put(soundResource.toString(), currentTick);
				}
			}
		}

		// If it is Minecraft thunder handle the sound remapping to Dynamic Surroundings
		// thunder
		// and set the appropriate volume.
		if (e.getName().equals("entity.lightning.thunder")) {
			final String thunderSound = Sounds.THUNDER.getSoundName();
			if (!this.soundsToBlock.contains(thunderSound)) {
				final PositionedSound sound = (PositionedSound) theSound;
				if (sound != null && PositionedSoundUtil.getVolume(sound) > 16) {
					final BlockPos pos = new BlockPos(sound.getXPosF(), sound.getYPosF(), sound.getZPosF());
					final ISound newSound = Sounds.THUNDER.createSound(pos).setVolume(ModOptions.sound.thunderVolume);
					e.setResultSound(newSound);
				}
				return;
			}
		}

		// Check to see if the sound is going to be replaced with another sound
		if (theSound instanceof PositionedSound && soundResource != null) {
			final SoundEvent rep = this.replacements.get(soundResource.toString());
			if (rep != null) {
				e.setResultSound(new AdhocSound(rep, (PositionedSound) theSound));
			}
		}
	}

	@Nullable
	public String playSoundAtPlayer(@Nullable EntityPlayer player, @Nonnull final SoundEffect sound) {

		if (player == null)
			player = EnvironState.getPlayer();

		final BasicSound<?> s = sound.createSound(player);
		return playSound(s);
	}

	@Nullable
	public String playSoundAt(@Nonnull final BlockPos pos, @Nonnull final SoundEffect sound, final int tickDelay) {

		final BasicSound<?> s = sound.createSound(pos);
		if (tickDelay == 0)
			return playSound(s);

		s.setState(SoundState.DELAYED);
		this.pending.add(new PendingSound(s, tickDelay));
		return null;
	}

	@SubscribeEvent
	public void onDistributedSound(@Nonnull final PlayDistributedSoundEvent event) {
		try {
			// Need to only permit crafting. The other sounds are dynamically
			// produced by the client.
			final String soundResource = event.nbt.getString(BasicSound.NBT.SOUND_EVENT);
			if (!StringUtils.isEmpty(soundResource)) {
				if ("dsurround:crafting".equals(soundResource))
					this.playSound(new AdhocSound(event.nbt));
			}
		} catch (final Throwable t) {
			;
		}
	}

	@SubscribeEvent
	public void registryReloadEvent(@Nonnull final ReloadEvent.Registry event) {
		if (event.side == Side.CLIENT) {
			onConnect();
		}
	}

	@SubscribeEvent
	public void playerJoinWorldEvent(@Nonnull final EntityJoinWorldEvent event) {
		if (event.getEntity().getEntityWorld().isRemote && EnvironState.isPlayer(event.getEntity())
				&& !event.getEntity().isDead)
			clearSounds();
	}

	// Going to hijack the mood processing logic in MC since it is kinda busted.
	@SubscribeEvent
	public void doMoodProcessing(@Nonnull final TickEvent.ClientTickEvent event) {
		if (event.phase == Phase.END && EnvironState.getWorld() != null) {
			final WorldClient wc = (WorldClient) EnvironState.getWorld();
			// If we are at 1 it means we need to see if we can come up with a point
			// around the player that matches the ambient requirement (an air block
			// in the dark or something).
			if (wc.ambienceTicks == 1) {
				// Calculate a point around the player. +/- 15 blocks.
				final int deltaX = this.RANDOM.nextInt(30) - 15;
				final int deltaY = this.RANDOM.nextInt(30) - 15;
				final int deltaZ = this.RANDOM.nextInt(30) - 15;
				final int distance = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
				if (distance > 4 && distance <= 255) {
					final BlockPos blockpos = EnvironState.getPlayerPosition().add(deltaX, deltaY, deltaZ);
					final IBlockState iblockstate = wc.getBlockState(blockpos);
					if (iblockstate.getMaterial() == Material.AIR)
						if (wc.getLightFor(EnumSkyBlock.SKY, blockpos) <= 0)
							if (wc.getLight(blockpos) <= this.RANDOM.nextInt(8)) {
								final BasicSound<?> fx = Sounds.AMBIENT_CAVE.createSound(blockpos).setVolume(0.9F)
										.setPitch(0.8F + this.RANDOM.nextFloat() * 0.2F);
								this.playSound(fx);
								wc.ambienceTicks = this.RANDOM.nextInt(12000) + 6000;
								DSurround.log().debug("Next ambient event: %d ticks", wc.ambienceTicks);
							}
				}
			}

			// If it didn't process push it back a tick to avoid MC from
			// triggering.
			if (wc.ambienceTicks == 1)
				wc.ambienceTicks++;
		}
	}

	@SubscribeEvent
	public void diagnostics(@Nonnull final DiagnosticEvent.Gather event) {
		event.output.add(String.format("Ambiance Timer: %d", ((WorldClient) EnvironState.getWorld()).ambienceTicks));
		this.emitters.values().forEach(emitter -> event.output.add("EMITTER: " + emitter.toString()));
		this.pending.forEach(effect -> event.output
				.add((effect.getTickAge() < 0 ? "DELAYED: " : "PENDING: ") + effect.getSound().toString()));
	}

}
