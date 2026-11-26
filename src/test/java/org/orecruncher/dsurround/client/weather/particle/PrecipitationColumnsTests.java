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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.IntBinaryOperator;

import org.junit.jupiter.api.Test;
import org.orecruncher.lib.random.XorShiftRandom;

/**
 * Tests for the color dust takes: the region's loose ground, whatever it blows over. Ported from the 26.x line's
 * tests.
 */
public class PrecipitationColumnsTests {

	private static final int RADIUS = 12;
	private static final int SAND = 0xF7E9A3;
	private static final int RED_SAND = 0xD87F33;
	private static final int FALLBACK = 0x123456;

	/**
	 * A desert on flat ground, dust everywhere, its ground's color by {@code colorAt(x, z)}
	 */
	private static final class Desert implements IColumnSource {
		final IntBinaryOperator colorAt;

		Desert(final IntBinaryOperator colorAt) {
			this.colorAt = colorAt;
		}

		@Override
		public int groundHeight(final int x, final int z) {
			return 64;
		}

		@Override
		public PrecipitationKind precipitation(final int x, final int y, final int z) {
			return PrecipitationKind.DUST;
		}

		@Override
		public int light(final int x, final int y, final int z) {
			return 0xF000F0;
		}

		@Override
		public int dustColor(final int x, final int y, final int z) {
			return this.colorAt.applyAsInt(x, z);
		}

		@Override
		public int fallbackDustColor(final int x, final int y, final int z) {
			return FALLBACK;
		}
	}

	private static PrecipitationColumns look(final IntBinaryOperator colorAt) {
		final PrecipitationColumns columns = new PrecipitationColumns(RADIUS);
		columns.update(new Desert(colorAt), 0, 70, 0);
		return columns;
	}

	@Test
	public void dustOverGrassOrWaterIsTheRegionsSand() {
		// Sand to the north, grass (or water) to the south: no color of its own
		final PrecipitationColumns columns = look((x, z) -> z < 0 ? SAND : IColumnSource.NOT_A_DUST_SOURCE);
		assertEquals(SAND, columns.regionalDustColor());
		assertEquals(SAND, columns.dustColorAt(columns.index(0, 5)), "over the grass");
		assertEquals(SAND, columns.dustColorAt(columns.index(0, -5)), "over the sand");
	}

	@Test
	public void withNoLooseGroundInSightTheBiomeDecides() {
		final PrecipitationColumns columns = look((x, z) -> IColumnSource.NOT_A_DUST_SOURCE);
		assertEquals(FALLBACK, columns.regionalDustColor());
		assertEquals(FALLBACK, columns.dustColorAt(columns.index(3, 3)));
	}

	@Test
	public void mixedGroundAveragesOut() {
		// Red sand to the west, sand to the east: the region's color is between the two
		final PrecipitationColumns columns = look((x, z) -> x < 0 ? RED_SAND : SAND);
		final int regional = columns.regionalDustColor();
		assertTrue(between((regional >> 16) & 0xFF, (RED_SAND >> 16) & 0xFF, (SAND >> 16) & 0xFF));
		assertTrue(between((regional >> 8) & 0xFF, (RED_SAND >> 8) & 0xFF, (SAND >> 8) & 0xFF));
		assertTrue(between(regional & 0xFF, RED_SAND & 0xFF, SAND & 0xFF));
	}

	@Test
	public void overLooseGroundItIsHalfItsOwnColor() {
		// One patch of red sand in a sand desert: dust rising from it is half red sand, half the region's sand
		final PrecipitationColumns columns = look((x, z) -> x == 2 && z == 2 ? RED_SAND : SAND);
		final int regional = columns.regionalDustColor();
		final int patch = columns.dustColorAt(columns.index(2, 2));
		assertEquals((((RED_SAND >> 16) & 0xFF) + ((regional >> 16) & 0xFF)) / 2, (patch >> 16) & 0xFF);
		assertEquals((((RED_SAND >> 8) & 0xFF) + ((regional >> 8) & 0xFF)) / 2, (patch >> 8) & 0xFF);
		assertEquals(((RED_SAND & 0xFF) + (regional & 0xFF)) / 2, patch & 0xFF);
		assertNotEquals(regional, patch);
	}

	/**
	 * Flat ground at a height that can be changed, rain where x is below {@code rainBelowX} and nothing beyond; counts
	 * how often its heights are read
	 */
	private static final class Changing implements IColumnSource {
		int ground = 64;
		int rainBelowX = Integer.MAX_VALUE;
		int heightReads;
		int weatherReads;

		@Override
		public int groundHeight(final int x, final int z) {
			this.heightReads++;
			return this.ground;
		}

		@Override
		public PrecipitationKind precipitation(final int x, final int y, final int z) {
			this.weatherReads++;
			return x < this.rainBelowX ? PrecipitationKind.RAIN : PrecipitationKind.NONE;
		}

		@Override
		public int light(final int x, final int y, final int z) {
			return 0xF000F0;
		}

		@Override
		public int dustColor(final int x, final int y, final int z) {
			return IColumnSource.NOT_A_DUST_SOURCE;
		}

		@Override
		public int fallbackDustColor(final int x, final int y, final int z) {
			return FALLBACK;
		}
	}

	@Test
	public void standingStillAFewRowsAreLookedAtAgainEachTick() {
		final Changing source = new Changing();
		final PrecipitationColumns columns = new PrecipitationColumns(RADIUS);
		assertTrue(columns.refresh(source, 0, 70, 0), "the first look");
		final int size = 2 * RADIUS + 1;
		assertEquals(columns.columnCount(), source.heightReads, "every column, the first time");

		// The ground changes; a few rows see it each tick, and all of them within the refresh interval
		source.ground = 66;
		final int rows = PrecipitationColumns.rowsPerTick(size);
		for (int t = 0; t < PrecipitationColumns.REFRESH_TICKS; t++) {
			final int before = source.heightReads;
			columns.refresh(source, 0, 70, 0);
			final int read = source.heightReads - before;
			assertTrue(read > 0 && read <= rows * size, "a few rows a tick, not " + read);
		}
		int inArea = 0;
		for (int z = -RADIUS; z <= RADIUS; z++)
			for (int x = -RADIUS; x <= RADIUS; x++) {
				final int index = columns.index(x, z);
				if (index < 0)
					continue;
				inArea++;
				assertEquals(66, columns.ground(index), "column " + x + ", " + z);
			}
		assertEquals(columns.columnCount(), inArea);
	}

	@Test
	public void whatFallsIsAskedAgainOnlyEveryFewLooks() {
		final Changing source = new Changing();
		final PrecipitationColumns columns = new PrecipitationColumns(RADIUS);
		columns.refresh(source, 0, 70, 0);
		final int count = columns.columnCount();
		assertEquals(count, source.weatherReads, "every column, the first time");

		// Standing still, a few rows are looked at again each tick, their heights and light read each time, and what
		// falls asked again on a column's WEATHER_LOOKS-th look. Ticks enough for every row's next WEATHER_LOOKS - 1
		// looks, the rows taken in turn
		final int size = 2 * RADIUS + 1;
		final int rows = PrecipitationColumns.rowsPerTick(size);
		for (int t = 0; t < (PrecipitationColumns.WEATHER_LOOKS - 1) * size / rows; t++)
			columns.refresh(source, 0, 70, 0);
		assertEquals(count, source.weatherReads, "kept for the looks in between");
		assertEquals(count * PrecipitationColumns.WEATHER_LOOKS, source.heightReads, "the heights read every look");
		// One more look each: asked again
		for (int t = 0; t < (size + rows - 1) / rows; t++)
			columns.refresh(source, 0, 70, 0);
		assertEquals(2 * count, source.weatherReads, "every column asked again");
	}

	@Test
	public void aChangeInWhatFallsIsSeenWithinTheWeatherLooks() {
		// A season turning: rain stops east of x = 0, which no block shows
		final Changing source = new Changing();
		final PrecipitationColumns columns = new PrecipitationColumns(RADIUS);
		columns.refresh(source, 0, 70, 0);
		assertEquals(PrecipitationKind.RAIN, columns.kind(columns.index(5, 0)));
		source.rainBelowX = 0;
		for (int t = 0; t < PrecipitationColumns.REFRESH_TICKS * PrecipitationColumns.WEATHER_LOOKS; t++)
			columns.refresh(source, 0, 70, 0);
		assertEquals(PrecipitationKind.NONE, columns.kind(columns.index(5, 0)));
		assertEquals(PrecipitationKind.RAIN, columns.kind(columns.index(-5, 0)));
		// The share of falling columns agrees with a fresh look
		final PrecipitationColumns fresh = new PrecipitationColumns(RADIUS);
		fresh.update(source, 0, 70, 0);
		assertEquals(fresh.fallingShare(), columns.fallingShare(), 1e-6F);
	}

	@Test
	public void aNewHeightAsksWhatFallsAgain() {
		// The camera climbs above the ground: what falls is taken at its height (snow higher up), so each column is
		// asked again on its next look
		final Changing source = new Changing();
		final PrecipitationColumns columns = new PrecipitationColumns(RADIUS);
		columns.refresh(source, 0, 70, 0);
		final int count = columns.columnCount();
		for (int t = 0; t < PrecipitationColumns.REFRESH_TICKS; t++)
			columns.refresh(source, 0, 90, 0);
		assertEquals(2 * count, source.weatherReads);

		// Below the ground's top, the height is the ground's: the camera's own changes nothing
		final Changing low = new Changing();
		low.ground = 100;
		final PrecipitationColumns under = new PrecipitationColumns(RADIUS);
		under.refresh(low, 0, 70, 0);
		for (int t = 0; t < PrecipitationColumns.REFRESH_TICKS; t++)
			under.refresh(low, 0, 75, 0);
		assertEquals(count, low.weatherReads);
	}

	@Test
	public void aNewGroundAsksWhatFallsAgain() {
		// A column looked up before its chunk arrived has the lowest ground, and the placeholder biome's weather: the
		// chunk arriving changes the ground, though the camera above it keeps the height what falls is taken at
		final Changing source = new Changing();
		source.ground = -64;
		final PrecipitationColumns columns = new PrecipitationColumns(RADIUS);
		columns.refresh(source, 0, 150, 0);
		final int count = columns.columnCount();
		source.ground = 64;
		for (int t = 0; t < PrecipitationColumns.REFRESH_TICKS; t++)
			columns.refresh(source, 0, 150, 0);
		assertEquals(2 * count, source.weatherReads);
	}

	/**
	 * The columns of the disc around a camera column, as x and z packed in a long
	 */
	private static Set<Long> disc(final int cameraX, final int cameraZ) {
		final Set<Long> columns = new HashSet<>();
		for (int dz = -RADIUS; dz <= RADIUS; dz++)
			for (int dx = -RADIUS; dx <= RADIUS; dx++)
				if (PrecipitationColumns.inDisc(dx, dz, RADIUS))
					columns.add(pack(cameraX + dx, cameraZ + dz));
		return columns;
	}

	private static long pack(final int x, final int z) {
		return (long) x << 32 | (z & 0xFFFFFFFFL);
	}

	@Test
	public void movingLooksUpOnlyTheColumnsThatCameIntoTheArea() {
		final Changing source = new Changing();
		final PrecipitationColumns columns = new PrecipitationColumns(RADIUS);
		columns.refresh(source, 0, 70, 0);
		final int size = 2 * RADIUS + 1;

		// A column east: the columns that came into the disc, besides the rows due a fresh look (the area's first
		// rows, the first tick after a full look)
		final int before = source.heightReads;
		assertTrue(columns.refresh(source, 1, 70, 0));
		final int rows = PrecipitationColumns.rowsPerTick(size);
		final Set<Long> expected = disc(1, 0);
		final Set<Long> entered = new HashSet<>(expected);
		entered.removeAll(disc(0, 0));
		expected.removeIf(c -> {
			final int z = (int) c.longValue();
			return z - (0 - RADIUS) >= rows && !entered.contains(c);
		});
		assertEquals(expected.size(), source.heightReads - before);
		assertTrue(columns.index(1 + RADIUS, 0) >= 0, "the new edge is in the area");
		assertEquals(-1, columns.index(-RADIUS, 0), "the old edge has left it");
	}

	@Test
	public void theRunningTotalsMatchAFreshLookAfterMoving() {
		// Rain to the west, and a desert of red sand and sand to the east: walking east changes every share
		final IColumnSource mixed = new IColumnSource() {
			@Override
			public int groundHeight(final int x, final int z) {
				return 64;
			}

			@Override
			public PrecipitationKind precipitation(final int x, final int y, final int z) {
				return x < 0 ? PrecipitationKind.RAIN : PrecipitationKind.DUST;
			}

			@Override
			public int light(final int x, final int y, final int z) {
				return 0xF000F0;
			}

			@Override
			public int dustColor(final int x, final int y, final int z) {
				return z < 3 ? RED_SAND : (x % 3 == 0 ? IColumnSource.NOT_A_DUST_SOURCE : SAND);
			}

			@Override
			public int fallbackDustColor(final int x, final int y, final int z) {
				return FALLBACK;
			}
		};
		final PrecipitationColumns walked = new PrecipitationColumns(RADIUS);
		walked.refresh(mixed, -20, 70, 0);
		for (int x = -19; x <= 15; x++)
			walked.refresh(mixed, x, 70, x / 3);

		final PrecipitationColumns fresh = new PrecipitationColumns(RADIUS);
		fresh.update(mixed, 15, 70, 5);
		assertEquals(fresh.fallingShare(), walked.fallingShare(), 1e-6F);
		assertEquals(fresh.dustShare(), walked.dustShare(), 1e-6F);
		assertEquals(fresh.regionalDustColor(), walked.regionalDustColor());
		for (int z = 5 - RADIUS; z <= 5 + RADIUS; z++)
			for (int x = 15 - RADIUS; x <= 15 + RADIUS; x++) {
				final int index = fresh.index(x, z);
				assertEquals(index, walked.index(x, z), "the same disc, at " + x + ", " + z);
				if (index >= 0)
					assertEquals(fresh.kind(index), walked.kind(index), "column " + x + ", " + z);
			}
	}

	@Test
	public void anInvalidatedLookIsRedone() {
		final Changing source = new Changing();
		final PrecipitationColumns columns = new PrecipitationColumns(RADIUS);
		columns.refresh(source, 0, 70, 0);

		// The ground changes; without looking again the columns still have the old height
		source.ground = 66;
		columns.refresh(source, 0, 70, 0);
		assertEquals(64, columns.ground(columns.index(0, 0)));

		columns.invalidate();
		assertTrue(columns.refresh(source, 0, 70, 0));
		assertEquals(66, columns.ground(columns.index(0, 0)));
	}

	@Test
	public void theShareOfColumnsWithRainIsCounted() {
		final Changing source = new Changing();
		// Rain west of x = 0: the disc's columns but the middle row north to south, halved
		source.rainBelowX = 0;
		final PrecipitationColumns columns = new PrecipitationColumns(RADIUS);
		columns.update(source, 0, 70, 0);
		final int west = (columns.columnCount() - (RADIUS * 2 + 1)) / 2;
		assertEquals(west / (float) columns.columnCount(), columns.fallingShare(), 1e-5F);
		assertEquals(0F, columns.dustShare());
	}

	@Test
	public void onlyTheDiscIsLookedUp() {
		for (final int radius : new int[] { 8, 12, 16, 32 }) {
			final Changing source = new Changing();
			final PrecipitationColumns columns = new PrecipitationColumns(radius);
			columns.update(source, 0, 70, 0);
			final int square = (radius * 2 + 1) * (radius * 2 + 1);
			assertEquals(columns.columnCount(), source.heightReads, "each column read once");
			assertTrue(columns.columnCount() < square * 0.9F, "the corners are left out at " + radius);
			assertTrue(columns.columnCount() > Math.PI * radius * radius, "the whole disc at " + radius);
			assertEquals(-1, columns.index(radius, radius), "a corner");
			assertTrue(columns.index(radius, 0) >= 0, "the edge, east");
			assertTrue(columns.index(0, -radius) >= 0, "the edge, north");
		}
	}

	@Test
	public void everyPlaceADropCanBeHasItsColumn() {
		// The camera anywhere in its column, drops anywhere within the radius of it, as the field keeps them
		final Random random = new XorShiftRandom(7);
		for (final int radius : new int[] { 8, 12, 16 }) {
			final PrecipitationColumns columns = new PrecipitationColumns(radius);
			columns.update(new Changing(), 0, 70, 0);
			for (int i = 0; i < 20000; i++) {
				final double cameraX = random.nextDouble();
				final double cameraZ = random.nextDouble();
				final double angle = random.nextDouble() * Math.PI * 2;
				// Mostly at the very edge, where a column could be missed
				final double r = radius * (i % 2 == 0 ? 1 - random.nextDouble() * 1e-3 : Math.sqrt(random.nextDouble()));
				final double x = cameraX + Math.cos(angle) * r;
				final double z = cameraZ + Math.sin(angle) * r;
				assertTrue(columns.index(x, z) >= 0, "no column at " + x + ", " + z + " for radius " + radius);
			}
		}
	}

	private static boolean between(final int value, final int a, final int b) {
		return value >= Math.min(a, b) && value <= Math.max(a, b) && value != a && value != b;
	}
}
