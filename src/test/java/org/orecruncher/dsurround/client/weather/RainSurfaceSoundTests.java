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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RainSurfaceSoundTests {

	private static final float EPSILON = 1e-5F;

	@Test
	public void withoutASurfaceASingleSoundIsAsItWas() {
		final RainMix mix = RainMix.single(null, 0.66F, 0.5F);
		assertEquals(0.66F, mix.volume, EPSILON);
		assertEquals(0.5F, mix.pitch, EPSILON);
		assertNull(mix.layer);
		assertEquals(0F, mix.layerVolume, EPSILON);
	}

	@Test
	public void aSurfaceSetsTheRainsVolumeAndPitch() {
		final RainMix mix = RainMix.single(RainSurface.SOFT, 0.6F, 1F);
		assertEquals(0.6F * RainSurface.SOFT.volume(), mix.volume, EPSILON);
		assertEquals(RainSurface.SOFT.pitch(), mix.pitch, EPSILON);
		assertNull(mix.layer);
	}

	@Test
	public void aSurfacesOwnRainTakesMostOfTheRainsPlace() {
		final RainMix mix = RainMix.single(RainSurface.METAL, 0.5F, 0.5F);
		final float volume = 0.5F * RainSurface.METAL.volume();
		assertNotNull(mix.layer);
		assertEquals(volume * RainMix.LAYER_SHARE, mix.layerVolume, EPSILON);
		assertEquals(volume * (1F - RainMix.LAYER_SHARE), mix.volume, EPSILON);
		// Between them as loud as the surface's rain alone
		assertEquals(volume, mix.volume + mix.layerVolume, EPSILON);
		// The rain keeps a roof's lower pitch, as the surface has it
		assertEquals(0.5F * RainSurface.METAL.pitch(), mix.pitch, EPSILON);
	}

	@Test
	public void theThreeRainsAreSharesOfVanillasVolume() {
		final RainMix mix = RainMix.of(null, 0.2F, 0.9F, 0.6F);
		assertEquals(0.2F * RainMix.calmShare(0.6F), mix.volume, EPSILON);
		assertEquals(0.2F * RainMix.mediumShare(0.6F), mix.mediumVolume, EPSILON);
		assertEquals(0.2F * RainMix.heavyShare(0.6F), mix.heavyVolume, EPSILON);
		assertEquals(0.9F, mix.pitch, EPSILON);
		assertNull(mix.layer);
	}

	@Test
	public void aSurfacesOwnRainTakesItsShareOfAllThree() {
		final RainMix mix = RainMix.of(RainSurface.METAL, 1F, 1F, 0.8F);
		final float onMetal = RainSurface.METAL.volume();
		assertNotNull(mix.layer);
		assertEquals(onMetal * RainMix.layerShare(0.8F), mix.layerVolume, EPSILON);
		final float kept = 1F - RainMix.LAYER_SHARE;
		assertEquals(onMetal * kept * RainMix.calmShare(0.8F), mix.volume, EPSILON);
		assertEquals(onMetal * kept * RainMix.mediumShare(0.8F), mix.mediumVolume, EPSILON);
		assertEquals(onMetal * kept * RainMix.heavyShare(0.8F), mix.heavyVolume, EPSILON);
		// Between them as loud as the three rains alone
		assertEquals(onMetal * (RainMix.calmShare(0.8F) + RainMix.mediumShare(0.8F) + RainMix.heavyShare(0.8F)),
				mix.volume + mix.mediumVolume + mix.heavyVolume + mix.layerVolume, EPSILON);
	}

	@Test
	public void theCalmRainGrowsLouderWithTheStorm() {
		assertEquals(RainMix.QUIETEST, RainMix.calmShare(0F), EPSILON);
		assertEquals(1F, RainMix.calmShare(1F), EPSILON);
		assertTrue(RainMix.calmShare(0.3F) < RainMix.calmShare(0.7F));
		assertEquals(1F, RainMix.calmShare(1.3F), EPSILON);
	}

	@Test
	public void vanillasHeavyRainOnlyComesInForStrongerStorms() {
		assertEquals(0F, RainMix.heavyShare(0.2F), EPSILON);
		assertEquals(0F, RainMix.heavyShare(RainMix.HEAVY_FROM), EPSILON);
		assertEquals(1F, RainMix.heavyShare(RainMix.HEAVY_FULL), EPSILON);
		assertEquals(1F, RainMix.heavyShare(1F), EPSILON);
		float last = 0F;
		for (float level = RainMix.HEAVY_FROM; level <= RainMix.HEAVY_FULL; level += 0.05F) {
			final float share = RainMix.heavyShare(level);
			assertTrue(share >= last, "rises smoothly, at " + level);
			last = share;
		}
	}

	@Test
	public void theMediumRainFillsTheMiddleOfTheStorm() {
		assertEquals(0F, RainMix.mediumShare(0.1F), EPSILON);
		assertEquals(0F, RainMix.mediumShare(RainMix.MEDIUM_FROM), EPSILON);
		assertEquals(1F, RainMix.mediumShare(RainMix.MEDIUM_FULL), EPSILON);
		assertEquals(1F, RainMix.mediumShare(RainMix.MEDIUM_FADE), EPSILON);
		assertEquals(0F, RainMix.mediumShare(1F), EPSILON);
		float last = 0F;
		for (float level = 0F; level <= 1F; level += 0.01F) {
			final float share = RainMix.mediumShare(level);
			assertTrue(share >= 0F && share <= 1F, "within 0 to 1, at " + level);
			assertTrue(Math.abs(share - last) < 0.1F, "no step, at " + level);
			if (level <= RainMix.MEDIUM_FULL)
				assertTrue(share >= last, "rises, at " + level);
			else if (level >= RainMix.MEDIUM_FADE)
				assertTrue(share <= last + 1e-6F, "falls, at " + level);
			last = share;
		}
	}

	@Test
	public void leavesMetalGlassAndWaterHaveRainOfTheirOwn() {
		for (final RainSurface surface : RainSurface.values()) {
			final boolean own = surface == RainSurface.LEAVES || surface == RainSurface.METAL
					|| surface == RainSurface.GLASS || surface == RainSurface.WATER;
			assertEquals(own, surface.layer() != null, surface.name());
		}
	}

	@Test
	public void theTallyGivesTheCommonestFirst() {
		final RainSurfaceTally tally = new RainSurfaceTally();
		assertEquals("", tally.summary());
		for (int i = 0; i < 3; i++)
			tally.add(RainSurface.WATER);
		tally.add(RainSurface.HARD);
		assertEquals("water 75%, hard 25%", tally.summary());
	}

	@Test
	public void theTallyKeepsOnlyTheLastSounds() {
		final RainSurfaceTally tally = new RainSurfaceTally();
		for (int i = 0; i < RainSurfaceTally.SIZE; i++)
			tally.add(RainSurface.HARD);
		for (int i = 0; i < RainSurfaceTally.SIZE; i++)
			tally.add(RainSurface.LEAVES);
		assertEquals("leaves 100%", tally.summary());
		tally.clear();
		assertTrue(tally.summary().isEmpty());
	}
}
