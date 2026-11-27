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

package org.orecruncher.dsurround.client.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

public class RainSoundSpotsTests {

	private static double shareWithin(final double blocks, final int range) {
		final Random random = new Random(3);
		final int n = 200000;
		int within = 0;
		for (int i = 0; i < n; i++)
			if (RainSoundSpots.distance(random.nextDouble(), range) < blocks)
				within++;
		return (double) within / n;
	}

	@Test
	public void mostSoundsPlayNearThePlayer() {
		assertEquals(0.41, shareWithin(2, 12), 0.01);
		assertEquals(0.58, shareWithin(4, 12), 0.01);
		assertEquals(0.50, shareWithin(3, 12), 0.01);
		assertEquals(0.82, shareWithin(8, 12), 0.01);
	}

	@Test
	public void soundsReachOutToTheRange() {
		assertEquals(0D, RainSoundSpots.distance(0D, 12));
		assertEquals(12D, RainSoundSpots.distance(1D, 12));
		assertEquals(1D, shareWithin(12.0001, 12));
	}
}
