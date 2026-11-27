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

package org.orecruncher.dsurround.client.weather.wind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class WindSoundsTests {

	private static final float EPSILON = 1e-5F;
	private static final float FULL = Wind.speedOfPercent(100);

	@Test
	public void aCalmStillSoundsWindyAndAFullStormIsFullVolume() {
		assertEquals(WindSounds.CALM_FLOOR, WindSounds.loopScale(0F, 1F), EPSILON);
		assertEquals(1F, WindSounds.loopScale(FULL, 1F), EPSILON);
		assertTrue(WindSounds.loopScale(FULL * 0.3F, 1F) < WindSounds.loopScale(FULL * 0.7F, 1F));
	}

	@Test
	public void beyondAFullStormItGrowsSlowlyToALimit() {
		assertEquals(1F + WindSounds.BEYOND_RATE, WindSounds.loopScale(FULL * 2F, 1F), EPSILON);
		assertEquals(WindSounds.LOUDEST, WindSounds.loopScale(FULL * 10F, 1F), EPSILON);
	}

	@Test
	public void underCoverItIsQuieter() {
		assertEquals(WindSounds.SHELTERED, WindSounds.loopScale(FULL, 0F), EPSILON);
		assertEquals((1F + WindSounds.SHELTERED) / 2F, WindSounds.loopScale(FULL, 0.5F), EPSILON);
	}

	@Test
	public void aStormWindComesAndGoesAtDifferentSpeeds() {
		assertFalse(WindSounds.isStormWind(65F, false));
		assertTrue(WindSounds.isStormWind(70F, false));
		// Once on, it stays on until the wind drops further
		assertTrue(WindSounds.isStormWind(60F, true));
		assertFalse(WindSounds.isStormWind(54F, true));
	}
}
