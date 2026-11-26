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

package org.orecruncher.dsurround.client.weather.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.util.math.MathHelper;

/**
 * Tests for drops carried in under an overhang: they drift on in a little way, out of the wind, fading, and land
 * where they reach something, rather than vanishing at the roof's edge. Ported from the 26.x line's tests.
 */
public class PrecipitationCoverTests {

	private static final int GROUND = 64;
	private static final double CAMERA_Y = 70.5;
	// A roof a block thick over the downwind half of the world, with open air under it down to the ground
	private static final int ROOF_BOTTOM = 72;
	private static final int ROOF_TOP = 73;
	private static final int SAND = 0xDBD3A0;

	/**
	 * Flat ground, with a roof over every column downwind of the camera
	 */
	private static final class Overhang implements IColumnSource {
		final PrecipitationKind kind;
		final float windX;
		final float windZ;

		Overhang(final PrecipitationKind kind, final float windX, final float windZ) {
			this.kind = kind;
			this.windX = windX;
			this.windZ = windZ;
		}

		boolean roofed(final int x, final int z) {
			return (x + 0.5F) * this.windX + (z + 0.5F) * this.windZ > 0F;
		}

		@Override
		public int groundHeight(final int x, final int z) {
			return this.roofed(x, z) ? ROOF_TOP : GROUND;
		}

		@Override
		public boolean isOpen(final int x, final int y, final int z) {
			if (y < GROUND)
				return false;
			return !this.roofed(x, z) || y < ROOF_BOTTOM || y >= ROOF_TOP;
		}

		@Override
		public PrecipitationKind precipitation(final int x, final int y, final int z) {
			return this.kind;
		}

		@Override
		public int light(final int x, final int y, final int z) {
			return this.roofed(x, z) && y < ROOF_BOTTOM ? 0x700070 : 0xF000F0;
		}

		@Override
		public int dustColor(final int x, final int y, final int z) {
			return SAND;
		}

		@Override
		public int fallbackDustColor(final int x, final int y, final int z) {
			return SAND;
		}
	}

	private static Wind steadyWind() {
		final Wind wind = new Wind();
		final Random random = new XorShiftRandom(9);
		for (int i = 0; i < 300; i++)
			wind.tick(1F, false, false, 4, random);
		WindAccess.gust(wind, 0, 0F);
		return wind;
	}

	private static Overhang overhang(final PrecipitationKind kind, final Wind wind) {
		return new Overhang(kind, wind.directionX(), wind.directionZ());
	}

	/**
	 * Ticks the field, checking every drop under cover each tick
	 *
	 * @param lasts how long it may stay under cover
	 * @return how many drop-ticks were spent under cover
	 */
	private static int run(final PrecipitationField field, final Overhang roof, final Wind wind, final int ticks,
			final int lasts) {
		final Random random = new XorShiftRandom(42);
		int coveredTicks = 0;
		for (int t = 0; t < ticks; t++) {
			field.tick(roof, 0, CAMERA_Y, 0, 1F, 1F, wind, random);
			for (int i = 0; i < field.count(); i++) {
				if (field.drops().covered[i] == 0)
					continue;
				coveredTicks++;
				final int x = MathHelper.floor(field.drops().x[i]), z = MathHelper.floor(field.drops().z[i]);
				assertTrue(roof.roofed(x, z), "under cover but not under the roof");
				assertTrue(field.drops().y[i] < ROOF_BOTTOM, "under cover but above the roof's underside");
				assertTrue(field.drops().covered[i] <= lasts, "lasted too long under cover");
				// Out of the wind it drifts only a little way in from the edge
				final double in = field.drops().x[i] * roof.windX + field.drops().z[i] * roof.windZ;
				assertTrue(in < 6D, "drifted too far in under the roof: " + in);
				assertEquals(0x700070, field.drops().light[i], "lit as the roof's top, not where it is");
			}
		}
		return coveredTicks;
	}

	@Test
	public void snowDriftsInUnderTheEavesAndFades() {
		final Wind wind = steadyWind();
		final PrecipitationField field = new PrecipitationField(2000);
		final int covered = run(field, overhang(PrecipitationKind.SNOW, wind), wind, 200, PrecipitationField.SNOW_COVER_TICKS);
		assertTrue(covered > 0, "snow should drift in under the roof");
	}

	@Test
	public void rainBlowsInOnlyBriefly() {
		final Wind wind = steadyWind();
		final PrecipitationField field = new PrecipitationField(2000);
		final int covered = run(field, overhang(PrecipitationKind.RAIN, wind), wind, 100, PrecipitationField.RAIN_COVER_TICKS);
		assertTrue(covered > 0, "rain should blow in under the roof");
	}

	@Test
	public void dustDriftsInUnderTheEavesToo() {
		final Wind wind = steadyWind();
		final PrecipitationField field = new PrecipitationField(2000);
		final int covered = run(field, overhang(PrecipitationKind.DUST, wind), wind, 200, PrecipitationField.DUST_COVER_TICKS);
		assertTrue(covered > 0, "dust should drift in under the roof");
	}

	@Test
	public void withoutWindRainDoesNotGetUnderTheRoof() {
		final Wind wind = steadyWind();
		final Overhang roof = overhang(PrecipitationKind.RAIN, wind);
		final PrecipitationField field = new PrecipitationField(2000);
		final Random random = new XorShiftRandom(42);
		for (int t = 0; t < 100; t++) {
			field.tick(roof, 0, CAMERA_Y, 0, 1F, 1F, new Wind(), random);
			for (int i = 0; i < field.count(); i++)
				assertEquals(0, field.drops().covered[i]);
		}
	}

	@Test
	public void snowThatReachesTheGroundUnderCoverLandsThere() {
		final Wind wind = steadyWind();
		final Overhang roof = overhang(PrecipitationKind.SNOW, wind);
		final PrecipitationField field = new PrecipitationField(2000);
		final Random random = new XorShiftRandom(42);
		int underCover = 0;
		for (int t = 0; t < 300; t++) {
			field.tick(roof, 0, CAMERA_Y, 0, 1F, 1F, wind, random);
			for (int i = 0; i < field.landings(); i++) {
				if (!field.landingBlownIn(i))
					continue;
				final int x = MathHelper.floor(field.landingX(i)), z = MathHelper.floor(field.landingZ(i));
				// Followed on from where it was, under the roof, near the ground and coming down onto it
				if (roof.roofed(x, z) && field.landingY(i) < GROUND + 1) {
					assertTrue(field.landingMotionY(i) < 0F);
					underCover++;
				}
			}
		}
		assertTrue(underCover > 0, "snow drifting in under the roof should land on the ground beneath it");
	}

	@Test
	public void underCoverADropFadesOut() {
		assertEquals(1F, PrecipitationField.coverFade(0F, 40));
		assertEquals(0.5F, PrecipitationField.coverFade(20F, 40), 1.0E-6F);
		assertEquals(0F, PrecipitationField.coverFade(40F, 40));
		assertEquals(0F, PrecipitationField.coverFade(60F, 40));
	}

	@Test
	public void snowAndDustLingerLongerThanRain() {
		assertTrue(PrecipitationField.coverTicks(PrecipitationField.SNOW) > PrecipitationField.coverTicks(PrecipitationField.RAIN));
		assertTrue(PrecipitationField.coverTicks(PrecipitationField.DUST) > PrecipitationField.coverTicks(PrecipitationField.RAIN));
	}
}
