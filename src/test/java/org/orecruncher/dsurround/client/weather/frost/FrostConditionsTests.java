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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.util.EnumFacing;

/**
 * Tests for how much frost windows get: with the cold, the time of day, the sun on the glass and shelter, and how it
 * grows and melts. Ported from the 26.x line's tests.
 */
public class FrostConditionsTests {

	// Times of day, as the game's celestial angle: 0 is noon, 0.75 sunrise
	private static final float BEFORE_SUNRISE = FrostConditions.COLDEST;
	private static final float MIDNIGHT = 0.5F;
	private static final float MORNING = 0.85F;
	private static final float AFTERNOON = 0.15F;
	private static final float SNOWY_PLAINS = 0F;
	private static final float SNOWY_TAIGA = -0.5F;
	private static final float PLAINS = 0.8F;

	@Test
	public void frostComesWithTheCold() {
		assertEquals(0F, FrostConditions.coldness(PLAINS));
		assertEquals(0F, FrostConditions.coldness(FrostConditions.NO_FROST_TEMPERATURE));
		assertEquals(1F, FrostConditions.coldness(FrostConditions.FULL_FROST_TEMPERATURE), 1.0E-5F);
		assertTrue(FrostConditions.coldness(SNOWY_TAIGA) > 1F);
		assertEquals(0F, FrostConditions.target(PLAINS, EnumFacing.NORTH, 1F, BEFORE_SUNRISE, 1F));
		assertEquals(1F, FrostConditions.target(SNOWY_PLAINS, EnumFacing.NORTH, 1F, BEFORE_SUNRISE, 1F), 1.0E-5F);
		// A cool biome: some frost before sunrise, less than a cold one
		float cool = FrostConditions.target(0.2F, EnumFacing.NORTH, 1F, BEFORE_SUNRISE, 1F);
		assertTrue(cool > 0F && cool < 1F);
	}

	@Test
	public void frostIsThickestBeforeSunriseAndThinnestInTheAfternoon() {
		assertEquals(1F, FrostConditions.night(BEFORE_SUNRISE), 1.0E-5F);
		assertTrue(FrostConditions.night(MIDNIGHT) > FrostConditions.night(0F));
		assertEquals(0F, FrostConditions.night(BEFORE_SUNRISE - 0.5F), 1.0E-5F);
		// In a cool biome it is gone by the afternoon; in the bitter cold some of it lasts
		assertEquals(0F, FrostConditions.target(0.2F, EnumFacing.NORTH, 1F, BEFORE_SUNRISE - 0.5F, 0F), 1.0E-5F);
		assertTrue(FrostConditions.target(SNOWY_TAIGA, EnumFacing.NORTH, 1F, BEFORE_SUNRISE - 0.5F, 0F) > 0.3F);
	}

	@Test
	public void theSunRisesInTheEastAndSetsInTheWest() {
		assertEquals(1F, FrostConditions.sunY(0F), 1.0E-5F);
		assertEquals(1F, FrostConditions.sunX(0.75F), 1.0E-5F);
		assertEquals(-1F, FrostConditions.sunX(0.25F), 1.0E-5F);
		assertTrue(FrostConditions.sunOn(EnumFacing.EAST, MORNING) > 0.5F);
		assertEquals(0F, FrostConditions.sunOn(EnumFacing.WEST, MORNING));
		assertTrue(FrostConditions.sunOn(EnumFacing.WEST, AFTERNOON) > 0.5F);
		// Never onto a north or south window, nor at night
		assertEquals(0F, FrostConditions.sunOn(EnumFacing.NORTH, MORNING));
		assertEquals(0F, FrostConditions.sunOn(EnumFacing.SOUTH, AFTERNOON));
		assertEquals(0F, FrostConditions.sunOn(EnumFacing.EAST, MIDNIGHT));
	}

	@Test
	public void theSunMeltsTheFrostOnTheWindowsItShinesOn() {
		float east = FrostConditions.target(SNOWY_TAIGA, EnumFacing.EAST, 1F, MORNING, 1F);
		float west = FrostConditions.target(SNOWY_TAIGA, EnumFacing.WEST, 1F, MORNING, 1F);
		float north = FrostConditions.target(SNOWY_TAIGA, EnumFacing.NORTH, 1F, MORNING, 1F);
		assertTrue(east < west);
		assertEquals(west, north, 1.0E-5F);
		// Less through cloud, and none under cover, where it doesn't reach
		assertTrue(FrostConditions.target(SNOWY_TAIGA, EnumFacing.EAST, 1F, MORNING, 0F) > east);
		float sheltered = FrostConditions.target(SNOWY_TAIGA, EnumFacing.EAST, 0F, MORNING, 1F);
		assertEquals(FrostConditions.target(SNOWY_TAIGA, EnumFacing.WEST, 0F, MORNING, 1F), sheltered, 1.0E-5F);
	}

	@Test
	public void glassUnderCoverFrostsLess() {
		float open = FrostConditions.target(SNOWY_PLAINS, EnumFacing.NORTH, 1F, BEFORE_SUNRISE, 1F);
		float covered = FrostConditions.target(SNOWY_PLAINS, EnumFacing.NORTH, 0F, BEFORE_SUNRISE, 1F);
		assertEquals(open * FrostConditions.SHELTERED_SHARE, covered, 1.0E-5F);
	}

	@Test
	public void frostCreepsInAndMeltsFaster() {
		float shown = 0F;
		for (int i = 0; i < 100; i++)
			shown = FrostConditions.ease(shown, 1F);
		assertEquals(100 * FrostConditions.GROW_PER_TICK, shown, 1.0E-5F);
		float melted = FrostConditions.ease(0.5F, 0F);
		assertEquals(0.5F - FrostConditions.MELT_PER_TICK, melted, 1.0E-6F);
		assertTrue(FrostConditions.MELT_PER_TICK > FrostConditions.GROW_PER_TICK);
		// Never past what it should be
		assertEquals(0.3F, FrostConditions.ease(0.3F - FrostConditions.GROW_PER_TICK / 2F, 0.3F));
		assertEquals(0.3F, FrostConditions.ease(0.3F + FrostConditions.MELT_PER_TICK / 2F, 0.3F));
	}
}
