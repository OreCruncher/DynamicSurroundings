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

package org.orecruncher.dsurround.client.weather.streaks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.orecruncher.lib.random.XorShiftRandom;

/**
 * Tests for the rain drops running down windows: how they cling and run, where they stop, and how their trails dry.
 * Ported from the 26.x line's tests.
 */
public class WindowStreaksTests {

	private static final WindowStreaks.Dripper NO_DRIPS = (x, y, z) -> fail("dripped off a window with something below it");

	private static WindowStreaks oneDrop(double y, double bottom, float size) {
		final WindowStreaks streaks = new WindowStreaks(10);
		streaks.add(10, y, 4.5, WindowStreaks.FACE_WEST, bottom, false, size, 0xF000F0, new XorShiftRandom(3));
		return streaks;
	}

	@Test
	public void clingsAtFirstThenRuns() {
		final WindowStreaks streaks = oneDrop(70.8, 68.06, 0.5F);
		final Random random = new XorShiftRandom(5);
		// However it was picked, it clings for at least the shortest first pause
		for (int t = 0; t < WindowStreaks.FIRST_PAUSE_MIN; t++) {
			streaks.tick(random, NO_DRIPS);
			assertEquals(70.8F, streaks.y[0], 1e-5F, "ran before it clung");
		}
		for (int t = 0; t < WindowStreaks.FIRST_PAUSE_MIN * 2 + WindowStreaks.FIRST_PAUSE_RANGE; t++)
			streaks.tick(random, NO_DRIPS);
		assertTrue(streaks.y[0] < 70.8F, "should have started running");
	}

	@Test
	public void runsInRushesWithPausesBetween() {
		final WindowStreaks streaks = oneDrop(75.5, 64.06, 1F);
		final Random random = new XorShiftRandom(7);
		int moving = 0, still = 0;
		float last = streaks.y[0];
		for (int t = 0; t < 200 && streaks.count() > 0; t++) {
			streaks.tick(random, NO_DRIPS);
			float step = last - streaks.y[0];
			assertTrue(step >= 0F, "drops never run up");
			assertTrue(step <= WindowStreaks.RUN_SPEED + 1e-5F, "too fast");
			if (step > 0F)
				moving++;
			else
				still++;
			last = streaks.y[0];
		}
		assertTrue(moving > 5, "should run some of the time");
		assertTrue(still > moving, "should cling more than it runs");
	}

	@Test
	public void ontoAFrameItRunsOffAndFades() {
		// Just above the bottom: it reaches it on its first run
		final WindowStreaks streaks = oneDrop(68.2, 68.06, 1F);
		final Random random = new XorShiftRandom(9);
		int reachedAt = -1, goneAt = -1;
		for (int t = 0; t < WindowStreaks.MAX_LIFETIME; t++) {
			streaks.tick(random, NO_DRIPS);
			if (streaks.count() == 0) {
				goneAt = t;
				break;
			}
			assertTrue(streaks.y[0] >= 68.06F - 1e-5F, "ran past the frame");
			if (reachedAt < 0 && streaks.y[0] <= 68.06F + 1e-5F)
				reachedAt = t;
		}
		assertTrue(reachedAt >= 0, "should reach the bottom");
		assertTrue(goneAt >= 0 && goneAt - reachedAt <= WindowStreaks.FADE_OUT + 1, "should fade straight away, not linger");
	}

	@Test
	public void overOpenAirItDripsOffTheEdgeAndIsGone() {
		final WindowStreaks streaks = new WindowStreaks(10);
		streaks.add(10, 68.2, 4.5, WindowStreaks.FACE_WEST, 68.01, true, 1F, 0xF000F0, new XorShiftRandom(3));
		final Random random = new XorShiftRandom(9);
		final List<double[]> drips = new ArrayList<>();
		for (int t = 0; t < WindowStreaks.MAX_LIFETIME && streaks.count() > 0; t++) {
			streaks.tick(random, (x, y, z) -> drips.add(new double[] { x, y, z }));
			if (streaks.count() > 0)
				assertTrue(streaks.y[0] >= 68.01F - 1e-5F, "ran past the edge");
		}
		assertEquals(0, streaks.count());
		assertEquals(1, drips.size(), "one drip where it ran off");
		final double[] drip = drips.get(0);
		assertTrue(drip[0] < 10F && drip[0] > 9.98F, "on the glass's side");
		assertEquals(68.01F, drip[1], 1e-5F, "at the edge");
		assertEquals(4.5F, drip[2], 1e-5F);
	}

	@Test
	public void aDropStartingBelowTheBottomStaysWhereItIs() {
		final WindowStreaks streaks = oneDrop(68.0, 68.06, 1F);
		assertEquals(68.0F, streaks.bottom[0], 1e-5F);
		streaks.tick(new XorShiftRandom(1), NO_DRIPS);
		assertEquals(68.0F, streaks.y[0], 1e-5F);
	}

	@Test
	public void theTrailDriesFromTheTopDownAndNeverBelowTheBead() {
		final WindowStreaks streaks = oneDrop(75.5, 64.06, 1F);
		final Random random = new XorShiftRandom(11);
		float longest = 0F;
		for (int t = 0; t < 150; t++) {
			float before = streaks.trailTop[0];
			streaks.tick(random, NO_DRIPS);
			assertTrue(streaks.trailTop[0] >= streaks.y[0] - 1e-5F, "trail below the bead");
			assertTrue(streaks.trailTop[0] <= before + 1e-5F, "trail grew upward");
			longest = Math.max(longest, streaks.trailTop[0] - streaks.y[0]);
		}
		assertTrue(longest > 0.05F, "running should leave a trail");
	}

	@Test
	public void liesJustOffTheGlass() {
		final WindowStreaks streaks = new WindowStreaks(10);
		final Random random = new XorShiftRandom(13);
		streaks.add(10, 70.5, 4.5, WindowStreaks.FACE_WEST, 68, false, 0.5F, 0, random);
		streaks.add(3.5, 70.5, 8, WindowStreaks.FACE_SOUTH, 68, false, 0.5F, 0, random);
		assertTrue(streaks.x[0] < 10F && streaks.x[0] > 10F - 0.02F);
		assertEquals(4.5F, streaks.z[0], 1e-5F);
		assertTrue(streaks.z[1] > 8F && streaks.z[1] < 8F + 0.02F);
		assertEquals(3.5F, streaks.x[1], 1e-5F);
	}

	@Test
	public void fadesInAndOut() {
		assertEquals(0F, WindowStreaks.fadeFor(0F, 100));
		assertEquals(1F, WindowStreaks.fadeFor(50F, 100));
		assertEquals(0F, WindowStreaks.fadeFor(100F, 100));
		assertTrue(WindowStreaks.fadeFor(95F, 100) < 1F);
	}

	@Test
	public void aFullSetLeavesOutTheRestAndClearingEmptiesIt() {
		final WindowStreaks streaks = new WindowStreaks(5);
		final Random random = new XorShiftRandom(15);
		for (int i = 0; i < 10; i++)
			streaks.add(i, 70, 0, WindowStreaks.FACE_NORTH, 68, false, 0.5F, 0, random);
		assertEquals(5, streaks.count());
		streaks.clear();
		assertEquals(0, streaks.count());
	}
}
