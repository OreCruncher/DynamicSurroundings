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

package org.orecruncher.dsurround.client.footsteps.accents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.orecruncher.dsurround.client.footsteps.accents.WaterySurfaceAccent.Wading;
import org.orecruncher.dsurround.client.footsteps.accents.WaterySurfaceAccent.WetGround;
import org.orecruncher.dsurround.client.weather.GroundWetness;
import org.orecruncher.dsurround.client.weather.RainSurface;

public class WaterySurfaceAccentTests {

	private static final float EPSILON = 1e-5F;

	@Test
	public void waterOverTheFeetIsShallowThenWading() {
		assertEquals(Wading.DRY, WaterySurfaceAccent.wading(0F, false));
		assertEquals(Wading.DRY, WaterySurfaceAccent.wading(0.01F, false));
		assertEquals(Wading.DRY, WaterySurfaceAccent.wading(Float.NaN, false));
		// A spreading flow's thinnest layer is a ninth of a block
		assertEquals(Wading.SHALLOW, WaterySurfaceAccent.wading(1F / 9F, false));
		assertEquals(Wading.WADE, WaterySurfaceAccent.wading(0.89F, false));
		assertEquals(Wading.UNDER, WaterySurfaceAccent.wading(2F, true));
	}

	@Test
	public void wadingIsLouderDeeper() {
		assertEquals(WaterySurfaceAccent.WADE_QUIETEST, WaterySurfaceAccent.wadeVolume(WaterySurfaceAccent.WADING_FROM),
				EPSILON);
		assertEquals(1F, WaterySurfaceAccent.wadeVolume(WaterySurfaceAccent.WADE_LOUDEST_AT), EPSILON);
		assertEquals(1F, WaterySurfaceAccent.wadeVolume(3F), EPSILON);
	}

	@Test
	public void groundThatTurnsToMudSquelchesHardGroundSlapsTheRestIsQuiet() {
		assertEquals(WetGround.MUD, WaterySurfaceAccent.wetGround(RainSurface.SOFT, true));
		for (final RainSurface hard : new RainSurface[] { RainSurface.HARD, RainSurface.WOOD, RainSurface.METAL,
				RainSurface.GLASS })
			assertEquals(WetGround.SLAP, WaterySurfaceAccent.wetGround(hard, false), hard.name());
		for (final RainSurface soft : new RainSurface[] { RainSurface.SOFT, RainSurface.LEAVES, RainSurface.WATER,
				RainSurface.HOT })
			assertEquals(WetGround.NONE, WaterySurfaceAccent.wetGround(soft, false), soft.name());
	}

	@Test
	public void wetterIsLouder() {
		assertEquals(0F, WaterySurfaceAccent.slapVolume(WaterySurfaceAccent.DRY_BELOW - 0.01F), EPSILON);
		assertEquals(WaterySurfaceAccent.SLAP_QUIETEST, WaterySurfaceAccent.slapVolume(WaterySurfaceAccent.DRY_BELOW),
				EPSILON);
		assertEquals(WaterySurfaceAccent.SLAP_LOUDEST, WaterySurfaceAccent.slapVolume(1F), EPSILON);
		assertTrue(WaterySurfaceAccent.slapVolume(0.5F) > WaterySurfaceAccent.slapVolume(0.3F));

		assertEquals(0F, WaterySurfaceAccent.mudVolume(GroundWetness.MUDDY - 0.01F), EPSILON);
		assertEquals(WaterySurfaceAccent.MUD_QUIETEST, WaterySurfaceAccent.mudVolume(GroundWetness.MUDDY), EPSILON);
		assertEquals(1F, WaterySurfaceAccent.mudVolume(1F), EPSILON);
	}
}
