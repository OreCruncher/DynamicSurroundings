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

package org.blockartistry.mod.DynSurround.client.handlers;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.event.DiagnosticEvent;
import org.blockartistry.mod.DynSurround.client.event.RegistryEvent;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.sound.Emitter;
import org.blockartistry.mod.DynSurround.client.sound.IMySound;
import org.blockartistry.mod.DynSurround.client.sound.PlayerEmitter;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.client.sound.SoundEngine;
import org.blockartistry.mod.DynSurround.client.sound.Sounds;
import org.blockartistry.mod.DynSurround.ModEnvironment;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.map.hash.TObjectFloatHashMap;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISoundEventListener;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SoundEffectHandler extends EffectHandlerBase implements ISoundEventListener {

	private static final int AGE_THRESHOLD_TICKS = 10;

	public static final SoundEffectHandler INSTANCE = new SoundEffectHandler();

	private final Map<SoundEffect, Emitter> emitters = new HashMap<SoundEffect, Emitter>();
	private final ArrayDeque<IMySound<?>> pending = new ArrayDeque<IMySound<?>>();

	private SoundEffectHandler() {

	}

	@Override
	@Nonnull
	public String getHandlerName() {
		return "SoundEffectHandler";
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {

		// Only execute every 4 ticks.
		if ((EnvironState.getTickCounter() % 4) != 0)
			return;

		for (final Emitter emitter : this.emitters.values())
			emitter.update();

		if (this.pending.size() > 0) {
			Iterables.removeIf(this.pending, new Predicate<IMySound<?>>() {
				@Override
				public boolean apply(final IMySound<?> input) {
					if (input.getTickAge() >= AGE_THRESHOLD_TICKS)
						return true;
					if (input.getTickAge() >= 0) {
						return playSound(input) != null;
					}
					return false;
				}
			});
		}
	}

	@Override
	public void onConnect() {
		clearSounds();
		SoundEngine.instance().addListender(this);
	}

	@Override
	public void onDisconnect() {
		SoundEngine.instance().removeListener(this);
		clearSounds();
	}

	public void clearSounds() {
		this.emitters.clear();
		this.pending.clear();
		SoundEngine.instance().stopAllSounds();
	}

	public void queueAmbientSounds(@Nonnull final TObjectFloatHashMap<SoundEffect> sounds) {

		// Quick optimization - if there are no sounds coming in we need
		// to clear out existing emitters and return. No reason to keep
		// going.
		if (sounds.size() == 0) {
			if (this.emitters.size() > 0) {
				for (final Emitter emit : this.emitters.values())
					emit.fade();
				this.emitters.clear();
			}
			return;
		}

		// Iterate through the existing emitters:
		// * If an emitter does not correspond to an incoming sound, remove.
		// * If an emitter does correspond, update the volume setting.
		final Iterator<Entry<SoundEffect, Emitter>> itr = this.emitters.entrySet().iterator();
		while (itr.hasNext()) {
			final Entry<SoundEffect, Emitter> e = itr.next();
			if (sounds.contains(e.getKey())) {
				e.getValue().setVolume(sounds.get(e.getKey()));
				// Set to 0 so that the "new sound" logic below
				// will ignore. Cheaper than removing the object
				// from the collection.
				sounds.put(e.getKey(), 0F);
			} else {
				e.getValue().fade();
				itr.remove();
			}
		}

		// Any sounds left in the list are new and need
		// an emitter created.
		final TObjectFloatIterator<SoundEffect> newSounds = sounds.iterator();
		while (newSounds.hasNext()) {
			newSounds.advance();
			if (newSounds.value() > 0)
				this.emitters.put(newSounds.key(), new PlayerEmitter(newSounds.key()));
		}
	}

	public boolean isSoundPlaying(@Nonnull final ISound sound) {
		return SoundEngine.instance().isSoundPlaying(sound);
	}

	public boolean isSoundPlaying(@Nonnull final String soundId) {
		return SoundEngine.instance().isSoundPlaying(soundId);
	}

	@Nullable
	public String playSound(@Nonnull final ISound sound) {
		return sound == null ? null : SoundEngine.instance().playSound(sound);
	}

	@SubscribeEvent
	public void soundPlay(@Nonnull final PlaySoundEvent e) {
		// Don't patch up - Weather2 has it's own sound
		if (ModEnvironment.Weather2.isLoaded())
			return;

		if (e.getName().equals("entity.lightning.thunder")) {
			final ISound sound = e.getSound();
			final BlockPos pos = new BlockPos(sound.getXPosF(), sound.getYPosF(), sound.getZPosF());
			final ISound newSound = Sounds.THUNDER.setVolume(ModOptions.thunderVolume).createSound(pos, this.RANDOM);
			e.setResultSound(newSound);
		}
	}

	@Nullable
	public String playSoundAtPlayer(@Nullable EntityPlayer player, @Nonnull final SoundEffect sound) {

		if (player == null)
			player = EnvironState.getPlayer();

		final IMySound<?> s = sound.createSound(player);
		return playSound(s);
	}

	/**
	 * Estimate whether a sound can be heard based on it's volume and distance.
	 */
	public static boolean canSoundBeHeard(@Nonnull final BlockPos soundPos, final float volume) {
		if (volume == 0.0F)
			return false;
		final BlockPos playerPos = EnvironState.getPlayerPosition();
		final double distanceSq = playerPos.distanceSq(soundPos);
		final double DROPOFF = 16 * 16;
		if (distanceSq <= DROPOFF)
			return true;
		final double power = volume * DROPOFF;
		return distanceSq <= power;
	}

	@Nullable
	public String playSoundAt(@Nonnull final BlockPos pos, @Nonnull final SoundEffect sound, final int tickDelay) {

		if (!canSoundBeHeard(pos, sound.getVolume()))
			return null;

		final IMySound<?> s = sound.createSound(pos, tickDelay);
		if(tickDelay == 0)
			return playSound(s);

		this.pending.add(s);
		return null;
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
		if (event.getEntity().world.isRemote && EnvironState.isPlayer(event.getEntity()) && !event.getEntity().isDead)
			clearSounds();
	}

	@SubscribeEvent
	public void diagnostics(@Nonnull final DiagnosticEvent.Gather event) {
		final int soundCount = SoundEngine.instance().currentSoundCount();
		final int maxCount = SoundEngine.instance().maxSoundCount();

		final StringBuilder builder = new StringBuilder();
		builder.append("SoundSystem: ").append(soundCount).append('/').append(maxCount);
		event.output.add(builder.toString());

		for (final SoundEffect effect : this.emitters.keySet())
			event.output.add("EMITTER: " + effect.toString() + "[vol:" + this.emitters.get(effect).getVolume() + "]");
		for (final IMySound<?> effect : pending)
			event.output.add((effect.getTickAge() < 0 ? "DELAYED: " : "PENDING: ") + effect.toString());
	}

	@Override
	public void soundPlay(@Nonnull final ISound soundIn, @Nonnull final SoundEventAccessor accessor) {
		// if (!ModOptions.enableDebugLogging)
		// return;
		//
		// if
		// (soundIn.getSoundLocation().getResourceDomain().equals(DSurround.RESOURCE_ID))
		// return;
		//
		// ModLog.debug("Sound callback: [%s]",
		// soundIn.getSoundLocation().toString());
	}

}
