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

package org.orecruncher.dsurround.client.fx.firefly;

import java.util.Random;

import javax.annotation.Nonnull;

/**
 * When a firefly flashes, from the 26.x line. Like real fireflies, it is dark most of the time and gives short
 * flashes, each rising quickly and fading away more slowly, in a rhythm that depends on its kind:
 * <ul>
 * <li>{@link Pattern#SINGLE}: one flash, fading over about a second, every five or six seconds, like the common
 * eastern firefly (Photinus pyralis)</li>
 * <li>{@link Pattern#DOUBLE}: two quick pulses close together, every three or four seconds</li>
 * <li>{@link Pattern#QUICK}: short, slightly irregular flashes every second and a half or so</li>
 * </ul>
 * A firefly lives for a few flashes, and until its last has faded. Times are in ticks.
 */
public final class FireflyFlash {

	/**
	 * A kind of flashing: pulses in each flash, ticks between the pulses of a flash, ticks for a pulse to reach full
	 * brightness, how quickly a pulse fades (ticks to fall to about a third), the shortest and longest time from one
	 * flash to the next, the fewest and most flashes in a firefly's life, how far each flash may come early or late (so
	 * the rhythm isn't exact), and how common the kind is, relative to the others.
	 */
	public enum Pattern {
		SINGLE(1, 0, 3, 8F, 100, 120, 2, 3, 4, 5),
		DOUBLE(2, 10, 2, 4F, 60, 80, 2, 3, 3, 3),
		QUICK(1, 0, 1, 3.5F, 25, 40, 3, 6, 6, 2);

		public final int pulses;
		public final int pulseGap;
		public final int rise;
		public final float decay;
		public final int minPeriod;
		public final int maxPeriod;
		public final int minFlashes;
		public final int maxFlashes;
		public final int jitter;
		public final int weight;

		Pattern(final int pulses, final int pulseGap, final int rise, final float decay, final int minPeriod,
				final int maxPeriod, final int minFlashes, final int maxFlashes, final int jitter, final int weight) {
			this.pulses = pulses;
			this.pulseGap = pulseGap;
			this.rise = rise;
			this.decay = decay;
			this.minPeriod = minPeriod;
			this.maxPeriod = maxPeriod;
			this.minFlashes = minFlashes;
			this.maxFlashes = maxFlashes;
			this.jitter = jitter;
			this.weight = weight;
		}

		static Pattern pick(@Nonnull final Random random) {
			int total = 0;
			for (final Pattern p : values())
				total += p.weight;
			int roll = random.nextInt(total);
			for (final Pattern p : values()) {
				roll -= p.weight;
				if (roll < 0)
					return p;
			}
			return SINGLE;
		}
	}

	// The first flash comes soon after a firefly appears, so it is seen
	private static final int MIN_FIRST = 5;
	private static final int MAX_FIRST = 25;
	// How long after its last pulse begins a firefly lasts: long enough for the pulse to fade to almost nothing
	private static final float FADE_OUT_DECAYS = 5F;
	// The "J" stroke: a firefly dips just before it flashes, then rises while lit. It looks this far ahead to dip.
	private static final float ANTICIPATION = 6F;

	private final Pattern pattern;
	private final float[] pulseStarts;
	private final int lifetime;

	public FireflyFlash(@Nonnull final Random random) {
		this(Pattern.pick(random), random);
	}

	public FireflyFlash(@Nonnull final Pattern pattern, @Nonnull final Random random) {
		this.pattern = pattern;

		final int flashes = pattern.minFlashes + random.nextInt(pattern.maxFlashes - pattern.minFlashes + 1);
		this.pulseStarts = new float[flashes * pattern.pulses];
		float flashStart = MIN_FIRST + random.nextInt(MAX_FIRST - MIN_FIRST + 1);
		int i = 0;
		for (int f = 0; f < flashes; f++) {
			for (int p = 0; p < pattern.pulses; p++)
				this.pulseStarts[i++] = flashStart + p * pattern.pulseGap;
			final int period = pattern.minPeriod + random.nextInt(pattern.maxPeriod - pattern.minPeriod + 1);
			final int jitter = pattern.jitter > 0 ? random.nextInt(pattern.jitter * 2 + 1) - pattern.jitter : 0;
			flashStart += period + jitter;
		}

		final float last = this.pulseStarts[this.pulseStarts.length - 1];
		this.lifetime = (int) Math.ceil(last + pattern.rise + pattern.decay * FADE_OUT_DECAYS);
	}

	@Nonnull
	public Pattern pattern() {
		return this.pattern;
	}

	/**
	 * How long the firefly lives, in ticks.
	 */
	public int lifetime() {
		return this.lifetime;
	}

	/**
	 * When each pulse begins, in ticks from the firefly appearing.
	 */
	@Nonnull
	public float[] pulseStarts() {
		return this.pulseStarts.clone();
	}

	/**
	 * How brightly the firefly is lit, 0 (dark) to 1.
	 *
	 * @param age ticks since it appeared, including the part of a tick for smooth drawing
	 */
	public float brightness(final float age) {
		// Only the two latest pulses begun can be the brightest: any before them has faded further than the one after
		// it, which is past its rise (pulses are further apart than a rise)
		int last = -1;
		while (last + 1 < this.pulseStarts.length && this.pulseStarts[last + 1] <= age)
			last++;
		if (last < 0)
			return 0F;
		final float brightness = pulse(age - this.pulseStarts[last]);
		return last == 0 ? brightness : Math.max(brightness, pulse(age - this.pulseStarts[last - 1]));
	}

	/**
	 * How much it climbs (positive) or sinks: a dip just before each flash, then a rise while it is lit.
	 */
	public float climb(final float age) {
		return 1.2F * brightness(age) - 0.4F * brightness(age + ANTICIPATION);
	}

	private float pulse(final float dt) {
		final int rise = this.pattern.rise;
		if (dt < rise) {
			final float t = dt / rise;
			return t * t * (3F - 2F * t);
		}
		return (float) Math.exp(-(dt - rise) / this.pattern.decay);
	}
}
