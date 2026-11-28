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

package org.orecruncher.dsurround.client.fx.light;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for which point lights are drawn when there are many: the nearest, the furthest of those fading out. From the
 * 26.x line's
 */
public class PointLightListTests {

	// Lights at the given x, along a line, each with its index as its strength to tell them apart
	private static float[] lightsAt(float... xs) {
		float[] lights = new float[(xs.length + 1) * PointLightList.STRIDE];
		for (int l = 0; l < xs.length; l++) {
			int i = l * PointLightList.STRIDE;
			lights[i] = xs[l];
			lights[i + 1] = 64F;
			lights[i + 2] = -20F;
			lights[i + 3] = 0.2F;
			lights[i + 4] = 0.9F;
			lights[i + 5] = 0.4F;
			lights[i + 6] = l;
			lights[i + 7] = 1.75F;
		}
		return lights;
	}

	@Test
	public void sortsNearestFirstKeepingEachLightWhole() {
		// The camera at x 10: distances 30, 2, 15, 0.5, 8
		float[] lights = lightsAt(40F, 8F, -5F, 10.5F, 18F);
		new PointLightList().sortNearest(lights, 5, 10.0, 64.0, -20.0);

		float[] expectedX = {10.5F, 8F, 18F, -5F, 40F};
		float[] expectedIndex = {3F, 1F, 4F, 2F, 0F};
		for (int r = 0; r < 5; r++) {
			int i = r * PointLightList.STRIDE;
			assertEquals(expectedX[r], lights[i], 1e-6F, "position of the light ranked " + r);
			assertEquals(expectedIndex[r], lights[i + 6], 1e-6F, "the light ranked " + r);
			assertEquals(0.9F, lights[i + 4], 1e-6F);
			assertEquals(1.75F, lights[i + 7], 1e-6F);
		}
		// Past the lights sorted, untouched
		assertEquals(0F, lights[5 * PointLightList.STRIDE + 7]);
	}

	@Test
	public void sortsMoreLightsThanItHasRoomForYet() {
		int n = 300;
		float[] xs = new float[n];
		for (int l = 0; l < n; l++)
			xs[l] = (l * 7919) % n;
		float[] lights = lightsAt(xs);
		new PointLightList().sortNearest(lights, n, 0.0, 64.0, -20.0);
		for (int r = 0; r < n; r++)
			assertEquals(r, lights[r * PointLightList.STRIDE], 1e-6F);
	}

	@Test
	public void theNearestAreDrawnInFullAndTheRestFadeOut() {
		assertEquals(1F, PointLightList.rankFade(0));
		assertEquals(1F, PointLightList.rankFade(PointLightList.FADE_FROM - 1));
		assertEquals(0F, PointLightList.rankFade(PointLightList.MAX_DRAWN));
		assertEquals(0F, PointLightList.rankFade(PointLightList.MAX_DRAWN + 100));
		float previous = 1F;
		for (int rank = PointLightList.FADE_FROM; rank <= PointLightList.MAX_DRAWN; rank++) {
			float fade = PointLightList.rankFade(rank);
			assertTrue(fade <= previous, "brighter further down at " + rank);
			assertTrue(previous - fade <= 1F / (PointLightList.MAX_DRAWN - PointLightList.FADE_FROM) + 1e-6F,
					"jumps at " + rank);
			previous = fade;
		}
	}
}
