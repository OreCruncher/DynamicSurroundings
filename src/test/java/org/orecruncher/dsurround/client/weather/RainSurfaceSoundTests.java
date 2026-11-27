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
	public void withoutASurfaceTheRainIsAsItWas() {
		final RainMix mix = RainMix.of(null, 0.66F, 0.5F);
		assertEquals(0.66F, mix.volume, EPSILON);
		assertEquals(0.5F, mix.pitch, EPSILON);
		assertNull(mix.layer);
		assertEquals(0F, mix.layerVolume, EPSILON);
	}

	@Test
	public void aSurfaceSetsTheRainsVolumeAndPitch() {
		final RainMix mix = RainMix.of(RainSurface.SOFT, 0.6F, 1F);
		assertEquals(0.6F * RainSurface.SOFT.volume(), mix.volume, EPSILON);
		assertEquals(RainSurface.SOFT.pitch(), mix.pitch, EPSILON);
		assertNull(mix.layer);
	}

	@Test
	public void aSurfacesOwnRainTakesMostOfTheRainsPlace() {
		final RainMix mix = RainMix.of(RainSurface.METAL, 0.5F, 0.5F);
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
