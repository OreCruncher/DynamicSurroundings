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

package org.orecruncher.dsurround.client.fx.particle.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests for which waterfalls are heard and how many splashes one throws, from the 26.x line's
 */
public class WaterfallSoundsTests {

	// A waterfall for the choosing: its strength and squared distance
	private static final class Fall {
		final int strength;
		final double distanceSq;

		Fall(final int strength, final double distance) {
			this.strength = strength;
			this.distanceSq = distance * distance;
		}
	}

	private static List<Fall> heard(final List<Fall> falls, final int cap, final int cutoff) {
		return WaterfallSounds.chooseHeard(falls, f -> f.strength, f -> f.distanceSq, cap, f -> 20D * 20D, cutoff);
	}

	@Test
	public void atMostTheCapAreHeardTheMostImpactfulFirst() {
		final Fall nearBig = new Fall(8, 3);
		final Fall nearSmall = new Fall(1, 4);
		final Fall farBig = new Fall(8, 15);
		final Fall farSmall = new Fall(1, 15);
		final List<Fall> chosen = heard(Arrays.asList(farSmall, farBig, nearSmall, nearBig), 2, 0);
		assertEquals(2, chosen.size());
		assertTrue(chosen.contains(nearBig));
		// A small waterfall close by beats a big one far off
		assertTrue(chosen.contains(nearSmall));
	}

	@Test
	public void underTheCapEveryOneInRangeIsHeard() {
		final List<Fall> falls = new ArrayList<>();
		for (int i = 0; i < 5; i++)
			falls.add(new Fall(3, 5 + i));
		falls.add(new Fall(9, 30));
		// The one 30 blocks out is too far to hear
		assertEquals(5, heard(falls, 32, 0).size());
	}

	@Test
	public void theCutoffKeepsItsOldScale() {
		// The option counted 2 more than the drop: a cutoff of 5 hears a drop of 3, not one of 2
		assertEquals(5, WaterfallSounds.cutoffScale(3));
		assertEquals(1, heard(Arrays.asList(new Fall(3, 5), new Fall(2, 5)), 32, 5).size());
		// More than 10 turns them off
		assertEquals(0, heard(Arrays.asList(new Fall(10, 5)), 32, 11).size());
	}

	@Test
	public void aLouderSoundIsHeardFurther() {
		// 16 blocks at a volume of 1 or less; times the volume above it
		assertEquals(16D, WaterfallSounds.heardRange(0.8F), 1e-9);
		assertEquals(16D, WaterfallSounds.heardRange(1F), 1e-9);
		assertEquals(24D, WaterfallSounds.heardRange(1.5F), 1e-9);
		assertEquals(32D, WaterfallSounds.heardRange(2F), 1e-9);
		// Each waterfall by its own range: a big one 28 blocks off is heard, a small one isn't
		final Fall big = new Fall(8, 28);
		final Fall small = new Fall(1, 28);
		final List<Fall> chosen = WaterfallSounds.chooseHeard(Arrays.asList(big, small), f -> f.strength,
				f -> f.distanceSq, 32, f -> f.strength >= 7 ? 36D * 36D : 20D * 20D, 0);
		assertEquals(1, chosen.size());
		assertTrue(chosen.contains(big));
	}

	@Test
	public void splashesFollowTheParticlesOptionAndThinWithDistance() {
		// All: between half and all of the limit; Decreased: half that; Minimal: none
		for (int roll = 0; roll < 100; roll++) {
			final int all = WaterfallSplashes.count(20, 0, roll);
			assertTrue(all >= 10 && all <= 19, "all " + all);
			final int decreased = WaterfallSplashes.count(20, 1, roll);
			assertTrue(decreased >= 5 && decreased <= 9, "decreased " + decreased);
			assertEquals(0, WaterfallSplashes.count(20, 2, roll));
		}
		assertEquals(3, WaterfallSplashes.count(3, 0, 7));
		assertEquals(12, WaterfallSplashes.byDistance(12, 10D * 10D));
		assertEquals(6, WaterfallSplashes.byDistance(12, 20D * 20D));
		assertEquals(3, WaterfallSplashes.byDistance(12, 40D * 40D));
		assertEquals(0, WaterfallSplashes.byDistance(12, 70D * 70D));
	}
}
