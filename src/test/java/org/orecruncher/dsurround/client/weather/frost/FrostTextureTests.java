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

package org.orecruncher.dsurround.client.weather.frost;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for the frost pattern: crystals that grow before the haze, the same each time, and halved for mipmaps.
 * Ported from the 26.x line's tests.
 */
public class FrostTextureTests {

	private static final FrostTexture.Pattern PATTERN = FrostTexture.generate(FrostTexture.SIZE, 42L);

	@Test
	public void theSameSeedGivesTheSamePattern() {
		final FrostTexture.Pattern again = FrostTexture.generate(FrostTexture.SIZE, 42L);
		assertArrayEquals(PATTERN.growth(), again.growth());
		assertArrayEquals(PATTERN.brightness(), again.brightness());
	}

	@Test
	public void everyTexelFrostsSometimeAndIsSomewhatBright() {
		for (int i = 0; i < PATTERN.growth().length; i++) {
			assertTrue(PATTERN.growth()[i] >= 0F && PATTERN.growth()[i] <= 1F);
			assertTrue(PATTERN.brightness()[i] >= FrostTexture.HAZE_BRIGHTNESS && PATTERN.brightness()[i] <= 1F);
		}
	}

	@Test
	public void crystalsGrowFirstAndTheHazeFillsInLast() {
		final float[] growth = PATTERN.growth();
		long crystal = 0, early = 0;
		for (final float g : growth) {
			if (g < FrostTexture.HAZE_GROWTH)
				crystal++;
			if (g < 0.3F)
				early++;
		}
		// Crystals cover some of it, not most; a few start right away
		assertTrue(crystal > growth.length / 20, "crystals: " + crystal);
		assertTrue(crystal < growth.length / 2, "crystals: " + crystal);
		assertTrue(early > 0);
		// The crystals are the brighter
		float crystalBright = 0F, hazeBright = 0F;
		int crystals = 0, haze = 0;
		for (int i = 0; i < growth.length; i++) {
			if (growth[i] < 0.4F) {
				crystalBright += PATTERN.brightness()[i];
				crystals++;
			} else if (growth[i] >= FrostTexture.HAZE_GROWTH) {
				hazeBright += PATTERN.brightness()[i];
				haze++;
			}
		}
		assertTrue(crystalBright / crystals > hazeBright / haze);
	}

	@Test
	public void aMipmapAveragesFourTexels() {
		final FrostTexture.Pattern half = FrostTexture.half(PATTERN);
		assertEquals(FrostTexture.SIZE / 2, half.size());
		float expected = (PATTERN.growth()[PATTERN.index(2, 4)] + PATTERN.growth()[PATTERN.index(3, 4)]
				+ PATTERN.growth()[PATTERN.index(2, 5)] + PATTERN.growth()[PATTERN.index(3, 5)]) / 4F;
		assertEquals(expected, half.growth()[half.index(1, 2)], 1.0E-6F);
		// Down to the smallest the texture keeps
		FrostTexture.Pattern level = PATTERN;
		for (int i = 1; i < FrostTexture.LEVELS; i++)
			level = FrostTexture.half(level);
		assertEquals(FrostTexture.SIZE >> (FrostTexture.LEVELS - 1), level.size());
	}

	@Test
	public void thePatternRepeats() {
		assertEquals(PATTERN.index(0, 0), PATTERN.index(FrostTexture.SIZE, -FrostTexture.SIZE));
		assertEquals(PATTERN.index(FrostTexture.SIZE - 1, 0), PATTERN.index(-1, 0));
	}
}
