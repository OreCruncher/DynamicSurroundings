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
package org.orecruncher.dsurround.client.footsteps;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions.Trace;
import org.orecruncher.dsurround.client.handlers.SoundEffectHandler;
import org.orecruncher.dsurround.client.sound.SoundBuilder;
import org.orecruncher.dsurround.client.sound.SoundInstance;
import org.orecruncher.dsurround.registry.acoustics.EventType;
import org.orecruncher.dsurround.registry.acoustics.IAcoustic;
import org.orecruncher.dsurround.registry.acoustics.IOptions;
import org.orecruncher.dsurround.registry.acoustics.ISoundPlayer;
import org.orecruncher.dsurround.registry.sound.SoundRegistry;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Plays a footstep's acoustics. A delayed part (the second half of a step, say) goes to the sound handler's queue,
 * counted in ticks: it was timed in milliseconds and dropped if more than 1.2 times its delay late, which, checked once
 * a tick, any late tick did (a garbage collection, chunks loading, a pause), and stalled while the entity rode or slept.
 */
@SideOnly(Side.CLIENT)
public class SoundPlayer implements ISoundPlayer {

	// A tick, in milliseconds: what a delay given in milliseconds is counted in
	static final int TICK_MILLIS = 50;

	protected final Random random = XorShiftRandom.current();
	protected final float scale;

	public SoundPlayer(final float volumeScale) {
		this.scale = volumeScale;
	}

	public void playAcoustic(@Nonnull final Association assoc, @Nonnull final EventType event) {
		playAcoustic(assoc.getStrikeLocation().getStrikePosition(), assoc.getData(), event, null);
	}

	private void logAcousticPlay(@Nonnull final IAcoustic[] acoustics, @Nonnull final EventType event) {
		if (ModBase.log().isDebugging()) {
			final String txt = Arrays.stream(acoustics).map(IAcoustic::getName).collect(Collectors.joining(","));
			ModBase.log().debug(Trace.FOOTSTEP_ACOUSTIC, "Playing acoustic %s for event %s", txt,
					event.toString().toUpperCase());
		}
	}

	public void playAcoustic(@Nonnull final Vec3d location, @Nullable final IAcoustic[] acoustics,
			@Nonnull final EventType event, @Nullable final IOptions inputOptions) {
		if (acoustics != null && acoustics.length > 0) {
			logAcousticPlay(acoustics, event);
			for (IAcoustic acoustic : acoustics) acoustic.playSound(this, location, event, inputOptions);
		}
	}

	@Override
	public void playSound(@Nonnull final Vec3d location, @Nonnull final SoundEvent sound, final float volume,
			final float pitch, @Nullable final IOptions options) {
		// A delayed sound is queued, in ticks; otherwise it plays now
		final int ticks = options != null && options.isDelayedSound()
				? delayTicks(randAB(this.random, options.getDelayMin(), options.getDelayMax()))
				: 0;
		try {
			final SoundInstance s = SoundBuilder.builder(sound, SoundRegistry.FOOTSTEPS).setPosition(location)
					.setVolume(volume * this.scale).setPitch(pitch).build();
			SoundEffectHandler.INSTANCE.playSound(s, ticks);
		} catch (final Throwable t) {
			ModBase.log().error("Unable to play sound", t);
		}
	}

	/**
	 * A delay in milliseconds as ticks: rounded up, and at least one (the next tick)
	 */
	static int delayTicks(final long millis) {
		return (int) Math.max(1L, (millis + TICK_MILLIS - 1) / TICK_MILLIS);
	}

	@Override
	public Random getRNG() {
		return this.random;
	}

	private long randAB(@Nonnull final Random rng, final long a, final long b) {
		// From a to b: a + nextInt(b + 1) went as far as a + b
		return a >= b ? a : a + rng.nextInt((int) (b - a + 1));
	}

}
