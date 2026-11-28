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

package org.orecruncher.dsurround.client.handlers.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for when a swing plays its sound: once as it starts, not again when a slow weapon's swing stalls a tick.
 */
public class EntitySwingEffectTests {

	// Plays the sound for each tick's (in progress, progress), as the effect would
	private static int sounds(final boolean[] inProgress, final int[] progress) {
		int count = 0;
		boolean wasSwinging = false;
		int last = 0;
		for (int t = 0; t < progress.length; t++) {
			if (EntitySwingEffect.swingStarts(inProgress[t], progress[t], wasSwinging, last))
				count++;
			wasSwinging = inProgress[t];
			last = progress[t];
		}
		return count;
	}

	@Test
	public void aSwingSoundsOnce() {
		assertEquals(1, sounds(new boolean[] { false, true, true, true, true, true, false },
				new int[] { 0, 0, 1, 2, 3, 4, 0 }));
	}

	@Test
	public void aSlowSwingThatStallsSoundsOnce() {
		// The Betweenlands' heavy weapons: the progress stands still a tick at a time
		assertEquals(1, sounds(new boolean[] { false, true, true, true, true, true, true, true, false },
				new int[] { 0, 0, 1, 1, 2, 2, 3, 3, 0 }));
	}

	@Test
	public void aSwingStartedAgainBeforeItEndsSoundsAgain() {
		assertEquals(2, sounds(new boolean[] { false, true, true, true, true, true, true },
				new int[] { 0, 0, 1, 2, 3, 0, 1 }));
	}
}
