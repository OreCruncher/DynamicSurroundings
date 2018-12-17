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

package org.orecruncher.dsurround.client.handlers;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.client.sound.Emitter;
import org.orecruncher.dsurround.client.sound.EntityEmitter;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.client.sound.SoundEngine;
import org.orecruncher.dsurround.client.sound.SoundInstance;
import org.orecruncher.dsurround.client.sound.SoundState;
import org.orecruncher.dsurround.client.sound.Sounds;
import org.orecruncher.dsurround.event.DiagnosticEvent;
import org.orecruncher.dsurround.registry.RegistryDataEvent;
import org.orecruncher.dsurround.registry.sound.SoundRegistry;
import org.orecruncher.lib.collections.ObjectArray;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SoundEffectHandler extends EffectHandlerBase {

	public static final SoundEffectHandler INSTANCE = new SoundEffectHandler();

	private final static class PendingSound {

		private final int timeMark;
		private final SoundInstance sound;

		public PendingSound(@Nonnull final SoundInstance sound, final int delay) {
			this.timeMark = EnvironState.getTickCounter() + delay;
			this.sound = sound;
		}

		public int getTickAge() {
			return EnvironState.getTickCounter() - this.timeMark;
		}

		public SoundInstance getSound() {
			return this.sound;
		}
	}

	private final Object2ObjectOpenHashMap<SoundEffect, Emitter> emitters = new Object2ObjectOpenHashMap<>();
	private final ObjectArray<PendingSound> pending = new ObjectArray<>();

	private SoundEffectHandler() {
		super("Sound Effects");
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {
		this.emitters.values().forEach(Emitter::update);

		//@formatter:off
		this.pending.stream()
			.filter(s -> s.getTickAge() >= 0)
			.forEach(s -> INSTANCE.playSound(s.getSound()));
		this.pending.removeIf(s -> s.getTickAge() >= 0);
		//@formatter:on

		doMoodProcessing();
	}

	@Override
	public void onConnect() {
		clearSounds();
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

	public void queueAmbientSounds(@Nonnull final Object2FloatOpenHashMap<SoundEffect> sounds) {
		// Iterate through the existing emitters:
		// * If done, remove
		// * If not in the incoming list, fade
		// * If it does exist, update volume throttle and unfade if needed
		this.emitters.object2ObjectEntrySet().removeIf(entry -> {
			final Emitter emitter = entry.getValue();
			if (emitter.isDonePlaying()) {
				return true;
			}
			final float volume = sounds.getFloat(entry.getKey());
			if (volume > 0) {
				emitter.setVolumeThrottle(volume);
				if (emitter.isFading())
					emitter.unfade();
				sounds.removeFloat(entry.getKey());
			} else if (!emitter.isFading()) {
				emitter.fade();
			}
			return false;
		});

		// Any sounds left in the list are new and need an emitter created.
		//@formatter:off
		sounds.forEach((fx, volume) -> {
			final Emitter e = new EntityEmitter(EnvironState.getPlayer(), fx);
			e.setVolumeThrottle(volume);
			this.emitters.put(fx, e);
		});
		//@formatter:on
	}

	public boolean isSoundPlaying(@Nonnull final SoundInstance sound) {
		return SoundEngine.instance().isSoundPlaying(sound);
	}

	public void stopSound(@Nonnull final SoundInstance sound) {
		SoundEngine.instance().stopSound(sound);
	}

	public boolean playSound(@Nonnull final SoundInstance sound) {
		return sound != null && sound.canSoundBeHeard() ? SoundEngine.instance().playSound(sound) : false;
	}

	public boolean playSoundAtPlayer(@Nonnull final EntityPlayer player, @Nonnull final SoundEffect sound) {
		return playSound(sound.createSoundNear(player));
	}

	public boolean playSoundAt(@Nonnull final BlockPos pos, @Nonnull final SoundEffect sound, final int tickDelay) {

		final SoundInstance s = sound.createSoundAt(pos);
		if (tickDelay == 0)
			return playSound(s);

		s.setState(SoundState.DELAYED);
		this.pending.add(new PendingSound(s, tickDelay));
		return false;
	}

	@SubscribeEvent
	public void registryReloadEvent(@Nonnull final RegistryDataEvent.Reload event) {
		if (event.reg instanceof SoundRegistry) {
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
	public void doMoodProcessing() {

		if (!(EnvironState.getWorld() instanceof WorldClient))
			return;

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
							final SoundInstance fx = Sounds.AMBIENT_CAVE.createSoundAt(blockpos).setVolume(0.9F)
									.setPitch(0.8F + this.RANDOM.nextFloat() * 0.2F);
							playSound(fx);
							wc.ambienceTicks = this.RANDOM.nextInt(12000) + 6000;
							ModBase.log().debug("Next ambient event: %d ticks", wc.ambienceTicks);
						}
			}
		}

		// If it didn't process push it back a tick to avoid MC from
		// triggering.
		if (wc.ambienceTicks == 1)
			wc.ambienceTicks = 2;
	}

	@SubscribeEvent
	public void diagnostics(@Nonnull final DiagnosticEvent.Gather event) {
		if (EnvironState.getWorld() instanceof WorldClient) {
			event.output
					.add(String.format("Ambiance Timer: %d", ((WorldClient) EnvironState.getWorld()).ambienceTicks));
		}
		this.emitters.values().forEach(emitter -> event.output.add("EMITTER: " + emitter.toString()));
		this.pending.forEach(effect -> event.output
				.add((effect.getTickAge() < 0 ? "DELAYED: " : "PENDING: ") + effect.getSound().toString()));
	}

}
