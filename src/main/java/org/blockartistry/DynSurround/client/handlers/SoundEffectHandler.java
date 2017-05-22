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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.event.DiagnosticEvent;
import org.blockartistry.DynSurround.client.event.PlayDistributedSoundEvent;
import org.blockartistry.DynSurround.client.event.RegistryEvent;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.sound.BasicSound;
import org.blockartistry.DynSurround.client.sound.Emitter;
import org.blockartistry.DynSurround.client.sound.PlayerEmitter;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.client.sound.SoundEngine;
import org.blockartistry.DynSurround.client.sound.SoundState;
import org.blockartistry.DynSurround.client.sound.Sounds;
import org.blockartistry.DynSurround.network.Network;
import org.blockartistry.DynSurround.network.PacketPlaySound;
import org.blockartistry.lib.collections.ObjectArray;

import com.google.common.base.Predicate;
import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.map.hash.TObjectFloatHashMap;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SoundEffectHandler extends EffectHandlerBase {

	private static final int AGE_THRESHOLD_TICKS = 10;
	public static final SoundEffectHandler INSTANCE = new SoundEffectHandler();

	private static final Predicate<PendingSound> PENDING_SOUNDS = new Predicate<PendingSound>() {
		@Override
		public boolean apply(final PendingSound input) {
			if (input.getTickAge() >= AGE_THRESHOLD_TICKS) {
				input.getSound().setState(SoundState.ERROR);
				return true;
			}
			if (input.getTickAge() >= 0) {
				return INSTANCE.playSound(input.getSound()) != null;
			}
			return false;
		}
	};

	/*
	 * Used to track sound in the PENDING list.
	 */
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

	private final Map<SoundEffect, Emitter> emitters = new HashMap<SoundEffect, Emitter>();
	private final ObjectArray<PendingSound> pending = new ObjectArray<PendingSound>();
	private final ObjectArray<BasicSound<?>> sendToServer = new ObjectArray<BasicSound<?>>();

	private SoundEffectHandler() {
		super("SoundEffectHandler");
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {

		for (final Emitter emitter : this.emitters.values())
			emitter.update();

		if (this.pending.size() > 0)
			this.pending.removeIf(PENDING_SOUNDS);

		// Flush out cached sounds
		if (this.sendToServer.size() > 0) {
			for (int i = 0; i < this.sendToServer.size(); i++) {
				Network.sendToServer(new PacketPlaySound(player, this.sendToServer.get(i)));
			}
			this.sendToServer.clear();
		}
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
		for (final Emitter e : this.emitters.values()) {
			e.stop();
		}
		this.emitters.clear();
		this.pending.clear();
		SoundEngine.instance().stopAllSounds();
	}

	public void queueAmbientSounds(@Nonnull final TObjectFloatHashMap<SoundEffect> sounds) {
		// Iterate through the existing emitters:
		// * If done, remove
		// * If not in the incoming list, fade
		// * If it does exist, update volume throttle and unfade if needed
		final Iterator<Entry<SoundEffect, Emitter>> itr = this.emitters.entrySet().iterator();
		while (itr.hasNext()) {
			final Entry<SoundEffect, Emitter> e = itr.next();
			final Emitter emitter = e.getValue();
			if (emitter.isDonePlaying()) {
				DSurround.log().debug("Removing emitter: %s", emitter.toString());
				itr.remove();
			} else if (sounds.contains(e.getKey())) {
				emitter.setVolumeThrottle(sounds.get(e.getKey()));
				if (emitter.isFading())
					emitter.unfade();
				// Set to 0 so that the "new sound" logic below
				// will ignore. Cheaper than removing the object
				// from the collection.
				sounds.put(e.getKey(), 0F);
			} else {
				if (!emitter.isFading())
					emitter.fade();
			}
		}

		// Any sounds left in the list are new and need
		// an emitter created.
		final TObjectFloatIterator<SoundEffect> newSounds = sounds.iterator();
		while (newSounds.hasNext()) {
			newSounds.advance();
			if (newSounds.value() > 0) {
				final SoundEffect effect = newSounds.key();
				this.emitters.put(effect, new PlayerEmitter(effect));
			}
		}
	}

	public boolean isSoundPlaying(@Nonnull final BasicSound<?> sound) {
		return SoundEngine.instance().isSoundPlaying(sound);
	}

	public boolean isSoundPlaying(@Nonnull final String soundId) {
		return SoundEngine.instance().isSoundPlaying(soundId);
	}

	@Nullable
	public String playSound(@Nonnull final BasicSound<?> sound) {
		if (sound == null || !sound.canSoundBeHeard(EnvironState.getPlayerPosition()))
			return null;

		// If it is a routable sound do so if possible
		if (sound.shouldRoute() && DSurround.isInstalledOnServer())
			this.sendToServer.add(sound);

		return SoundEngine.instance().playSound(sound);
	}

	@SubscribeEvent
	public void soundPlay(@Nonnull final PlaySoundEvent e) {
		if (e.getName().equals("entity.lightning.thunder")) {
			final ISound sound = e.getSound();
			final BlockPos pos = new BlockPos(sound.getXPosF(), sound.getYPosF(), sound.getZPosF());
			final ISound newSound = Sounds.THUNDER.createSound(pos).setVolume(ModOptions.thunderVolume);
			e.setResultSound(newSound);
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
			final BasicSound<?> sound = (BasicSound<?>) Class.forName(event.soundClass).newInstance();
			sound.deserializeNBT(event.nbt);
			sound.setRoutable(false);
			this.playSound((BasicSound<?>) sound);
		} catch (final Throwable t) {
			;
		}
	}

	/*
	 * Fired when the underlying biome config is reloaded.
	 */
	@SubscribeEvent
	public void registryReloadEvent(@Nonnull final RegistryEvent.Reload event) {
		if (event.getSide() == Side.CLIENT)
			clearSounds();
	}

	/*
	 * Fired when the player joins a world, such as when the dimension changes.
	 */
	@SubscribeEvent
	public void playerJoinWorldEvent(@Nonnull final EntityJoinWorldEvent event) {
		if (event.getEntity().worldObj.isRemote && EnvironState.isPlayer(event.getEntity())
				&& !event.getEntity().isDead)
			clearSounds();
	}

	@SubscribeEvent
	public void diagnostics(@Nonnull final DiagnosticEvent.Gather event) {
		final int soundCount = SoundEngine.instance().currentSoundCount();
		final int maxCount = SoundEngine.instance().maxSoundCount();

		final StringBuilder builder = new StringBuilder();
		builder.append("SoundSystem: ").append(soundCount).append('/').append(maxCount);
		event.output.add(builder.toString());

		for (final Emitter effect : this.emitters.values())
			event.output.add("EMITTER: " + effect.toString());
		for (final PendingSound effect : this.pending)
			event.output.add((effect.getTickAge() < 0 ? "DELAYED: " : "PENDING: ") + effect.getSound().toString());
	}

}
