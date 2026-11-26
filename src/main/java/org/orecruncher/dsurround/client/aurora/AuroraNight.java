/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.orecruncher.dsurround.client.aurora;

import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.lib.random.XorShiftRandom;

/**
 * How active a night's aurora is through the night: the part of the 26.x line's realistic aurora the cinematic one
 * takes its liveliness from ({@code RealisticAurora.at}). A quiet night stays calm; the others break up partway
 * through, brightening, moving faster and showing more distinct rays, then fade back. Drawn from the night's seed, so
 * everyone in a world sees the same night.
 */
public final class AuroraNight {

	/**
	 * How active a night is: how much brighter it is at the height of its breakup (1 is twice as bright), and how much
	 * faster it moves (1 is twice as fast)
	 */
	public enum Activity {
		QUIET(0F, 0F), MODERATE(0.8F, 1.5F), ACTIVE(2.0F, 3F), STORM(2.0F, 5F);

		final float surgeGain;
		final float speedBoost;

		Activity(final float surgeGain, final float speedBoost) {
			this.surgeGain = surgeGain;
			this.speedBoost = speedBoost;
		}

		boolean breaksUp() {
			return this != QUIET;
		}
	}

	/**
	 * How the night is at a moment: how far into its breakup (0 to 1), how bright, how fast and how distinct its rays
	 */
	public static final class Moment {
		public final float surge;
		public final float gain;
		public final float speed;
		public final float rayContrast;

		Moment(final float surge, final float gain, final float speed, final float rayContrast) {
			this.surge = surge;
			this.gain = gain;
			this.speed = speed;
			this.rayContrast = rayContrast;
		}
	}

	// The breakup takes this share of the night to reach its height; afterwards the surge dies away over about this
	static final float BREAKUP_LENGTH = 0.03F;
	static final float RECOVERY_DECAY = 0.08F;
	private static final long ACTIVITY_SALT = 0x61637469L;
	private static final long TIMELINE_SALT = 0x74696D65L;

	public final Activity activity;
	// When the breakup begins, and how long its height lasts, as shares of the night
	final float onset;
	final float peakLength;

	private AuroraNight(@Nonnull final Activity activity, final float onset, final float peakLength) {
		this.activity = activity;
		this.onset = onset;
		this.peakLength = peakLength;
	}

	/**
	 * The night for the given seed: a storm {@code stormChance} percent of the time, and otherwise quiet or moderate as
	 * often as each other, and active half as often
	 */
	@Nonnull
	public static AuroraNight of(final long seed, final int stormChance) {
		final Random random = new XorShiftRandom(Aurora.mix(seed ^ ACTIVITY_SALT));
		final Activity activity;
		if (random.nextInt(100) < stormChance) {
			activity = Activity.STORM;
		} else {
			final int roll = random.nextInt(100);
			activity = roll < 40 ? Activity.QUIET : roll < 80 ? Activity.MODERATE : Activity.ACTIVE;
		}
		final Random timeline = new XorShiftRandom(Aurora.mix(seed ^ TIMELINE_SALT));
		final float onset = 0.3F + timeline.nextFloat() * 0.3F;
		final float peakLength = 0.1F + timeline.nextFloat() * 0.1F;
		return new AuroraNight(activity, onset, peakLength);
	}

	/**
	 * When its height ends, as a share of the night
	 */
	float peakEnd() {
		return this.onset + BREAKUP_LENGTH + this.peakLength;
	}

	/**
	 * How the night is at a point in it
	 *
	 * @param progress how far through the night, 0 (dusk) to 1 (dawn)
	 */
	@Nonnull
	public Moment at(final float progress) {
		final float p = Math.max(0F, Math.min(1F, progress));
		final boolean breaks = this.activity.breaksUp();
		// Gathering strength until the breakup (all night, if there is none)
		final float grown = breaks ? Math.min(p / this.onset, 1F) : p;
		final float surge;
		// How far it has faded since its height, 0 to 1
		float recovered = 0F;
		if (!breaks || p < this.onset) {
			surge = 0F;
		} else if (p < this.onset + BREAKUP_LENGTH) {
			final float t = (p - this.onset) / BREAKUP_LENGTH;
			surge = t * t * (3F - 2F * t);
		} else if (p < peakEnd()) {
			surge = 1F;
		} else {
			surge = (float) Math.exp(-(p - peakEnd()) / RECOVERY_DECAY);
			recovered = 1F - surge;
		}
		final float gain = (0.7F + 0.3F * grown) * (1F + this.activity.surgeGain * surge) * (1F - 0.4F * recovered);
		final float speed = 1F + this.activity.speedBoost * surge;
		final float rayContrast = (0.6F + 0.4F * surge) + (0.35F - (0.6F + 0.4F * surge)) * recovered;
		return new Moment(surge, gain, speed, rayContrast);
	}

	/**
	 * How far through the night a world time is: 0 at dusk (12500), 1 at dawn (23500)
	 */
	public static float progressOf(final long worldTime) {
		final long t = Math.floorMod(worldTime, 24000L);
		return Math.max(0F, Math.min(1F, (t - 12500L) / 11000F));
	}
}
