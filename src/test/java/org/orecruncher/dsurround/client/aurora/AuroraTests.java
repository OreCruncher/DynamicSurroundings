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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * The classic aurora's shape: the same for a night everywhere, inside the sky, and as varied as the original's.
 */
public class AuroraTests {

	@Test
	public void aNightsAuroraIsTheSameEveryTime() {
		final Aurora a = Aurora.create(20261126L, 3);
		final Aurora b = Aurora.create(20261126L, 3);
		assertEquals(a.heading, b.heading);
		assertEquals(a.bands, b.bands);
		assertEquals(a.palette, b.palette);
		assertEquals(a.textureOffset, b.textureOffset);
	}

	@Test
	public void nightsDifferAndBandsKeepToTheSetting() {
		final Set<Float> headings = new HashSet<>();
		final Set<AuroraPalette> palettes = new HashSet<>();
		for (long night = 0; night < 200; night++) {
			final Aurora aurora = Aurora.create(night, 2);
			headings.add(aurora.heading);
			palettes.add(aurora.palette);
			assertTrue(aurora.bands >= 1 && aurora.bands <= 2, "bands " + aurora.bands);
			assertTrue(Math.abs(aurora.heading) <= Aurora.MAX_HEADING);
		}
		assertTrue(headings.size() > 190);
		// Nearly every palette shows up in 200 nights
		assertTrue(palettes.size() >= AuroraPalette.PALETTES.size() - 2, palettes.size() + " palettes");
	}

	@Test
	public void curtainsStayInsideTheSky() {
		final float[] bottom = new float[3];
		final float[] top = new float[3];
		for (long night = 0; night < 200; night++) {
			final Aurora aurora = Aurora.create(night, 3);
			for (int band = 0; band < aurora.bands; band++)
				for (float s = -1F; s <= 1F; s += 0.05F)
					for (float time = 0F; time < 600F; time += 37F) {
						aurora.bottom(band, s, time, bottom);
						aurora.top(band, bottom, top);
						final double reach = Math.sqrt(top[0] * top[0] + top[1] * top[1] + top[2] * top[2]);
						assertTrue(reach < Aurora.MAX_REACH, "reach " + reach + " on night " + night);
						// Always above the viewer, toward the north (-Z), as auroras lie toward the pole
						assertTrue(bottom[1] > 0.5F && top[1] > bottom[1]);
					}
		}
	}

	@Test
	public void curtainsTaperToNothingAtTheirEnds() {
		assertEquals(1F, Aurora.taper(0F));
		assertEquals(1F, Aurora.taper(0.5F));
		assertEquals(0F, Aurora.taper(1F), 1e-6);
		assertEquals(0F, Aurora.taper(-1F), 1e-6);
		assertTrue(Aurora.taper(0.75F) > 0F && Aurora.taper(0.75F) < 1F);
	}

	@Test
	public void thePalettesAreTheOriginalsDistinct() {
		assertEquals(AuroraPalette.PALETTES.size(), new HashSet<>(AuroraPalette.PALETTES).size());
		assertTrue(AuroraPalette.PALETTES.size() >= 18, AuroraPalette.PALETTES.size() + " palettes");
	}
}
