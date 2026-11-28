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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Tests for when fireflies flash, from the 26.x line's
 */
public class FireflyFlashTests {

	private static final int SEEDS = 200;

	private static float peakNear(FireflyFlash flash, float start) {
		float peak = 0F;
		for (float t = start; t < start + 10F; t += 0.25F)
			peak = Math.max(peak, flash.brightness(t));
		return peak;
	}

	@Test
	public void darkBeforeTheFirstFlash() {
		for (long seed = 0; seed < SEEDS; seed++) {
			FireflyFlash flash = new FireflyFlash(new Random(seed));
			float first = flash.pulseStarts()[0];
			assertTrue(first >= 5F && first <= 25F, "first flash at " + first);
			for (float t = 0F; t <= first; t += 0.5F)
				assertEquals(0F, flash.brightness(t));
		}
	}

	@Test
	public void eachPulseReachesFullBrightness() {
		for (long seed = 0; seed < SEEDS; seed++) {
			FireflyFlash flash = new FireflyFlash(new Random(seed));
			for (float start : flash.pulseStarts())
				assertEquals(1F, peakNear(flash, start), 0.01F);
		}
	}

	@Test
	public void mostlyDark() {
		// A real firefly is dark most of the time: count how much of its life it is more than faintly lit (the
		// fading tail of each flash counts as lit, so a short-lived double flasher is lit the most)
		for (long seed = 0; seed < SEEDS; seed++) {
			FireflyFlash flash = new FireflyFlash(new Random(seed));
			int lit = 0;
			for (int t = 0; t < flash.lifetime(); t++)
				if (flash.brightness(t) > 0.1F)
					lit++;
			float share = lit / (float) flash.lifetime();
			assertTrue(share < 0.45F, flash.pattern() + " lit " + share + " of the time");
		}
	}

	@Test
	public void darkBetweenFlashes() {
		for (long seed = 0; seed < SEEDS; seed++) {
			FireflyFlash flash = new FireflyFlash(FireflyFlash.Pattern.SINGLE, new Random(seed));
			float[] starts = flash.pulseStarts();
			for (int i = 1; i < starts.length; i++) {
				float midway = (starts[i - 1] + starts[i]) / 2F;
				assertTrue(flash.brightness(midway) < 0.01F);
			}
		}
	}

	@Test
	public void flashesQuickerThanTheyFade() {
		FireflyFlash flash = new FireflyFlash(FireflyFlash.Pattern.SINGLE, new Random(1));
		float start = flash.pulseStarts()[0];
		// Up to full in 3 ticks; still lit a few ticks after
		assertEquals(1F, flash.brightness(start + 3F), 1e-4);
		assertTrue(flash.brightness(start + 6F) > 0.3F);
		assertTrue(flash.brightness(start + 1.5F) < flash.brightness(start + 4.5F));
	}

	@Test
	public void doubleFlashesComeInPairs() {
		for (long seed = 0; seed < SEEDS; seed++) {
			FireflyFlash flash = new FireflyFlash(FireflyFlash.Pattern.DOUBLE, new Random(seed));
			float[] starts = flash.pulseStarts();
			assertEquals(0, starts.length % 2);
			for (int i = 0; i < starts.length; i += 2) {
				assertEquals(FireflyFlash.Pattern.DOUBLE.pulseGap, starts[i + 1] - starts[i], 1e-6);
				// Dimmed between the two pulses, then lit again
				assertTrue(flash.brightness(starts[i + 1]) < 0.2F);
			}
		}
	}

	@Test
	public void periodsMatchTheirKind() {
		for (FireflyFlash.Pattern pattern : FireflyFlash.Pattern.values()) {
			for (long seed = 0; seed < SEEDS; seed++) {
				float[] starts = new FireflyFlash(pattern, new Random(seed)).pulseStarts();
				for (int i = pattern.pulses; i < starts.length; i += pattern.pulses) {
					float period = starts[i] - starts[i - pattern.pulses];
					assertTrue(period >= pattern.minPeriod - pattern.jitter && period <= pattern.maxPeriod + pattern.jitter,
							pattern + " period " + period);
				}
				int flashes = starts.length / pattern.pulses;
				assertTrue(flashes >= pattern.minFlashes && flashes <= pattern.maxFlashes);
			}
		}
	}

	@Test
	public void livesUntilItsLastFlashHasFaded() {
		for (long seed = 0; seed < SEEDS; seed++) {
			FireflyFlash flash = new FireflyFlash(new Random(seed));
			float[] starts = flash.pulseStarts();
			assertTrue(flash.lifetime() > starts[starts.length - 1]);
			assertTrue(flash.brightness(flash.lifetime()) < 0.01F, "still lit when it goes");
		}
	}

	@Test
	public void dipsBeforeAFlashAndRisesWhileLit() {
		FireflyFlash flash = new FireflyFlash(FireflyFlash.Pattern.SINGLE, new Random(5));
		float start = flash.pulseStarts()[0];
		assertTrue(flash.climb(start - 3F) < 0F, "no dip before the flash");
		assertTrue(flash.climb(start + 3F) > 0F, "no rise while lit");
		assertEquals(0F, flash.climb(0F));
	}

	@Test
	public void brightnessIsThatOfItsBrightestPulse() {
		// Only the latest two pulses are looked at: the same as looking at every one
		for (long seed = 0; seed < SEEDS; seed++) {
			FireflyFlash flash = new FireflyFlash(new Random(seed));
			float[] starts = flash.pulseStarts();
			FireflyFlash.Pattern pattern = flash.pattern();
			for (float t = 0F; t <= flash.lifetime(); t += 0.25F) {
				float expected = 0F;
				for (float start : starts) {
					float dt = t - start;
					if (dt < 0F)
						break;
					float pulse = dt < pattern.rise ? (dt / pattern.rise) * (dt / pattern.rise) * (3F - 2F * dt / pattern.rise)
							: (float) Math.exp(-(dt - pattern.rise) / pattern.decay);
					expected = Math.max(expected, pulse);
				}
				assertEquals(expected, flash.brightness(t), 1e-6F, pattern + " at " + t);
			}
		}
	}

	@Test
	public void allKindsAppear() {
		EnumMap<FireflyFlash.Pattern, Integer> counts = new EnumMap<>(FireflyFlash.Pattern.class);
		for (long seed = 0; seed < 1000; seed++)
			counts.merge(new FireflyFlash(new Random(seed)).pattern(), 1, Integer::sum);
		assertEquals(FireflyFlash.Pattern.values().length, counts.size());
		// Single flashers are the most common
		assertTrue(counts.get(FireflyFlash.Pattern.SINGLE) > counts.get(FireflyFlash.Pattern.QUICK));
	}
}
