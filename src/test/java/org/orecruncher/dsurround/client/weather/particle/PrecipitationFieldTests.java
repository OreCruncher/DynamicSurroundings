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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.util.math.MathHelper;

/**
 * Tests for the drops of particle rain and snow: how many there are, where they fall, and when they go. Ported from
 * the 26.x line's tests.
 */
public class PrecipitationFieldTests {

	private static final int GROUND = 64;
	private static final double CAMERA_Y = 70.5;

	/**
	 * Flat ground everywhere. Rain falls on x below {@code snowFrom}, snow from there on; nothing falls on z below
	 * {@code dryBelowZ}.
	 */
	private static final class Columns implements IColumnSource {
		final int ground;
		final int snowFrom;
		final int dryBelowZ;

		Columns(final int ground, final int snowFrom, final int dryBelowZ) {
			this.ground = ground;
			this.snowFrom = snowFrom;
			this.dryBelowZ = dryBelowZ;
		}

		@Override
		public int groundHeight(final int x, final int z) {
			return this.ground;
		}

		@Override
		public PrecipitationKind precipitation(final int x, final int y, final int z) {
			if (z < this.dryBelowZ)
				return PrecipitationKind.NONE;
			return x < this.snowFrom ? PrecipitationKind.RAIN : PrecipitationKind.SNOW;
		}

		@Override
		public int light(final int x, final int y, final int z) {
			return 0xF000F0;
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

	private static final int SAND = 0xDBD3A0;

	/**
	 * Flat ground everywhere: a desert, with dust, for x below {@code dustBelowX}, and rain from there on; or, if
	 * {@code east}, the other way round
	 */
	private static final class Desert implements IColumnSource {
		final int ground;
		final int dustBelowX;
		final boolean east;

		Desert(final int ground, final int dustBelowX) {
			this(ground, dustBelowX, false);
		}

		Desert(final int ground, final int dustBelowX, final boolean east) {
			this.ground = ground;
			this.dustBelowX = dustBelowX;
			this.east = east;
		}

		@Override
		public int groundHeight(final int x, final int z) {
			return this.ground;
		}

		@Override
		public PrecipitationKind precipitation(final int x, final int y, final int z) {
			return (x < this.dustBelowX) != this.east ? PrecipitationKind.DUST : PrecipitationKind.RAIN;
		}

		@Override
		public int light(final int x, final int y, final int z) {
			return 0xF000F0;
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

	private static final IColumnSource ALL_DESERT = new Desert(GROUND, Integer.MAX_VALUE);

	private static final IColumnSource RAIN_EVERYWHERE = new Columns(GROUND, Integer.MAX_VALUE, Integer.MIN_VALUE);

	private static void tick(final PrecipitationField field, final IColumnSource columns, final double x, final double z,
			final float rainLevel, final int ticks) {
		tick(field, columns, x, z, rainLevel, new Wind(), ticks);
	}

	private static void tick(final PrecipitationField field, final IColumnSource columns, final double x, final double z,
			final float rainLevel, final Wind wind, final int ticks) {
		final Random random = new XorShiftRandom(42);
		for (int i = 0; i < ticks; i++)
			field.tick(columns, x, CAMERA_Y, z, rainLevel, 1F, wind, random);
	}

	// How faded in a drop is, as the packed record gives the renderers
	private static float fade(final PrecipitationField field, final int i, final float partialTick) {
		final PrecipitationField.Drops d = field.drops();
		return PrecipitationField.fade(d.kind[i], d.age[i], d.lifetime[i], d.covered[i], partialTick);
	}

	/**
	 * A wind at full storm strength, blowing steadily, without gusts
	 */
	private static Wind steadyWind() {
		final Wind wind = new Wind();
		final Random random = new XorShiftRandom(9);
		// Long enough to build up most of the way; then no gust is under way
		for (int i = 0; i < 300; i++) {
			wind.tick(1F, false, false, 4, random);
		}
		WindAccess.gust(wind, 0, 0F);
		return wind;
	}

	@Test
	public void farFromTheWorldsMiddleTheWindStillCarriesTheDrops() {
		// At 8,000,000 a float moves in half blocks: a snowflake's drift each tick, a fraction of that, was lost, so
		// the snow fell straight down and now and then jumped half a block
		final double far = 8_000_000D;
		final PrecipitationField field = new PrecipitationField(500);
		final Columns snowEverywhere = new Columns(GROUND, Integer.MIN_VALUE, Integer.MIN_VALUE);
		tick(field, snowEverywhere, far, far, 1F, steadyWind(), 40);
		assertTrue(field.count() > 100, "enough snow to look at");

		int drifting = 0;
		for (int i = 0; i < field.count(); i++) {
			final double moved = Math.abs(field.drops().x[i] - field.drops().prevX[i]) + Math.abs(field.drops().z[i] - field.drops().prevZ[i]);
			assertTrue(moved < 0.5D, "no jumps of half a block");
			if (moved > 0.001D)
				drifting++;
		}
		assertTrue(drifting > field.count() * 9 / 10, "most drift a little every tick: " + drifting + " of " + field.count());
	}

	@Test
	public void theNumberOfDropsFollowsTheRainLevel() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, RAIN_EVERYWHERE, 0, 0, 0.5F, 20);
		assertTrue(field.count() >= 450 && field.count() <= 500, () -> "half strength gave " + field.count());

		tick(field, RAIN_EVERYWHERE, 0, 0, 1F, 20);
		assertTrue(field.count() >= 950, () -> "full strength gave " + field.count());
		assertTrue(field.count() <= field.capacity());
	}

	@Test
	public void whenTheRainStopsTheDropsFallOut() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, RAIN_EVERYWHERE, 0, 0, 1F, 10);
		assertTrue(field.count() > 0);

		// Rain falls at least RAIN_SPEED_MIN a tick through the cylinder's height
		final int ticks = (int) Math.ceil((PrecipitationField.ABOVE + PrecipitationField.BELOW) / PrecipitationField.RAIN_SPEED_MIN) + 1;
		tick(field, RAIN_EVERYWHERE, 0, 0, 0F, ticks);
		assertEquals(0, field.count());
	}

	@Test
	public void dropsStayAboveTheGroundAndNearTheCamera() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, RAIN_EVERYWHERE, 0, 0, 1F, 30);
		final float radius = PrecipitationField.DEFAULT_RADIUS;
		for (int i = 0; i < field.count(); i++) {
			assertTrue(field.drops().y[i] > GROUND, "below the ground");
			assertTrue(field.drops().y[i] <= CAMERA_Y + PrecipitationField.ABOVE, "above the cylinder");
			assertTrue(field.drops().x[i] * field.drops().x[i] + field.drops().z[i] * field.drops().z[i] <= radius * radius, "outside the cylinder");
		}
	}

	@Test
	public void nothingFallsWhereTheBiomeIsDry() {
		final PrecipitationField field = new PrecipitationField(1000);
		// Dry for z below 0: half the disc
		tick(field, new Columns(GROUND, Integer.MAX_VALUE, 0), 0, 0, 1F, 20);
		assertTrue(field.count() > 0);
		for (int i = 0; i < field.count(); i++)
			assertTrue(field.drops().z[i] >= 0, "a drop in a dry column");
	}

	@Test
	public void groundAboveTheCylinderHasNoDrops() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, new Columns((int) CAMERA_Y + PrecipitationField.ABOVE + 1, Integer.MAX_VALUE, Integer.MIN_VALUE), 0, 0, 1F, 10);
		assertEquals(0, field.count());
	}

	@Test
	public void eachFlakeAndPuffKeepsTheTurnOfItsPhase() {
		// The turn is worked out as a drop ticks, not as it is drawn; it must follow the phase through spawning,
		// ticking, and drops being moved about in the arrays as others go. Snow, rain and dust, enough ticks for
		// plenty to land or settle and be replaced
		final PrecipitationField snow = new PrecipitationField(1000);
		tick(snow, new Columns(GROUND, 0, Integer.MIN_VALUE), 0, 0, 1F, 120);
		final PrecipitationField dust = new PrecipitationField(1000);
		tick(dust, new Desert(GROUND, 0), 0, 0, 1F, 120);
		int checked = 0;
		for (final PrecipitationField field : new PrecipitationField[] { snow, dust }) {
			for (int i = 0; i < field.count(); i++) {
				if (field.drops().kind[i] == PrecipitationField.RAIN)
					continue;
				final float spin = field.drops().phase[i] * (field.drops().kind[i] == PrecipitationField.DUST
						? DropRenderer.DUST_SPIN : DropRenderer.SNOW_SPIN);
				assertEquals(MathHelper.cos(spin), field.drops().spinCos[i], 1e-6F, "drop " + i);
				assertEquals(MathHelper.sin(spin), field.drops().spinSin[i], 1e-6F, "drop " + i);
				checked++;
			}
		}
		assertTrue(snow.dustCount() == 0 && dust.dustCount() > 0);
		assertTrue(checked > 100, checked + " checked");
	}

	@Test
	public void coldColumnsGetSlowSnow() {
		final PrecipitationField field = new PrecipitationField(1000);
		// Snow for x from 0 on
		tick(field, new Columns(GROUND, 0, Integer.MIN_VALUE), 0, 0, 1F, 10);
		boolean sawRain = false, sawSnow = false;
		for (int i = 0; i < field.count(); i++) {
			if (field.drops().kind[i] == PrecipitationField.SNOW) {
				sawSnow = true;
				assertTrue(field.drops().speed[i] < PrecipitationField.RAIN_SPEED_MIN);
			} else {
				sawRain = true;
			}
		}
		assertTrue(sawRain && sawSnow);
	}

	@Test
	public void dropsLeftBehindByTheCameraGo() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, RAIN_EVERYWHERE, 0, 0, 1F, 10);

		// Far enough that none of the old drops are near
		tick(field, RAIN_EVERYWHERE, 100, 100, 1F, 1);
		final float radius = PrecipitationField.DEFAULT_RADIUS;
		for (int i = 0; i < field.count(); i++) {
			final double dx = field.drops().x[i] - 100, dz = field.drops().z[i] - 100;
			assertTrue(dx * dx + dz * dz <= radius * radius, "a drop left behind");
		}
	}

	@Test
	public void aWiderFieldKeepsLandingsForItsArea() {
		// Regression: at a wide range only the first 128 landings a tick splashed, so splashes thinned out
		assertEquals(PrecipitationField.MAX_LANDINGS, new PrecipitationField(1000).maxLandings(), "the default range");
		assertEquals(PrecipitationField.MAX_LANDINGS * 4, new PrecipitationField(1000, PrecipitationField.DEFAULT_RADIUS * 2).maxLandings());
		assertTrue(new PrecipitationField(1000, 8).maxLandings() < PrecipitationField.MAX_LANDINGS, "narrower, fewer");
	}

	@Test
	public void numbersScaleWithTheArea() {
		assertEquals(16, PrecipitationField.forArea(16, PrecipitationField.DEFAULT_RADIUS));
		assertEquals(64, PrecipitationField.forArea(16, PrecipitationField.DEFAULT_RADIUS * 2));
		assertEquals(Math.round(16 * (32F / 12F) * (32F / 12F)), PrecipitationField.forArea(16, 32));
		assertEquals(1, PrecipitationField.forArea(1, 1), "never none");
	}

	@Test
	public void rainDropsLandOnTheGround() {
		final PrecipitationField field = new PrecipitationField(1000);
		// Long enough for drops to fall from the top of the cylinder to the ground
		tick(field, RAIN_EVERYWHERE, 0, 0, 1F, 30);
		assertTrue(field.landings() > 0);
		assertTrue(field.landings() <= field.maxLandings());
		final float radius = PrecipitationField.DEFAULT_RADIUS;
		for (int i = 0; i < field.landings(); i++) {
			assertEquals(GROUND, field.landingGround(i));
			final double x = field.landingX(i), z = field.landingZ(i);
			assertTrue(x * x + z * z <= radius * radius, "landed outside the cylinder");
		}
	}

	@Test
	public void snowLandsAsSnowForTheFlecks() {
		final PrecipitationField field = new PrecipitationField(1000);
		// Snow everywhere, on ground just below the camera so flakes reach it quickly
		tick(field, new Columns((int) CAMERA_Y - 1, Integer.MIN_VALUE, Integer.MIN_VALUE), 0, 0, 1F, 200);
		assertTrue(field.count() > 0);
		assertTrue(field.landings() > 0);
		for (int i = 0; i < field.landings(); i++) {
			assertTrue(field.landingIsSnow(i));
			assertTrue(field.landingSize(i) >= 0F && field.landingSize(i) <= 1F);
			assertTrue(field.landingVariant(i) >= 0F && field.landingVariant(i) <= 1F);
		}
	}

	@Test
	public void snowCarriedIntoTheSideOfSomethingTallerHitsTheSideNotTheTop() {
		// Every other column a pillar topping out above the cylinder, so no drop starts over one: any landing on a
		// pillar's top would be a flake that swayed into its side
		final int low = (int) CAMERA_Y - 1, tall = (int) CAMERA_Y + PrecipitationField.ABOVE + 10;
		final IColumnSource pillars = new IColumnSource() {
			@Override
			public int groundHeight(final int x, final int z) {
				return (x & 1) == 0 ? low : tall;
			}

			@Override
			public PrecipitationKind precipitation(final int x, final int y, final int z) {
				return PrecipitationKind.SNOW;
			}

			@Override
			public int light(final int x, final int y, final int z) {
				return 0xF000F0;
			}

			@Override
			public int dustColor(final int x, final int y, final int z) {
				return SAND;
			}

			@Override
			public int fallbackDustColor(final int x, final int y, final int z) {
				return SAND;
			}
		};
		final PrecipitationField field = new PrecipitationField(1000);
		final Random random = new XorShiftRandom(42);
		int landedOnLow = 0, hitSides = 0;
		for (int t = 0; t < 200; t++) {
			field.tick(pillars, 0, CAMERA_Y, 0, 1F, 1F, new Wind(), random);
			for (int i = 0; i < field.landings(); i++) {
				if (!field.landingBlownIn(i)) {
					assertEquals(low, field.landingGround(i), "landed on top of a pillar it hit the side of");
					landedOnLow++;
					continue;
				}
				hitSides++;
				// Followed on from the gap it was in, moving into a pillar's column, below the pillar's top
				final double x = field.landingX(i);
				assertEquals(low, pillars.groundHeight(MathHelper.floor(x), 0), "should start out in the open");
				assertEquals(tall, pillars.groundHeight(MathHelper.floor(x + field.landingMotionX(i)), 0), "should be moving into a pillar");
				assertTrue(field.landingY(i) <= tall);
			}
		}
		assertTrue(landedOnLow > 0, "flakes should still land between the pillars");
		assertTrue(hitSides > 0, "swaying flakes should hit the pillars' sides");
	}

	@Test
	public void rainBlownIntoTheSideOfSomethingIsFollowedOnForTheWindows() {
		// A checkerboard of pillars topping out above the cylinder: in any wind, rain crosses into them from the side
		final int low = (int) CAMERA_Y - 1, tall = (int) CAMERA_Y + PrecipitationField.ABOVE + 10;
		final IColumnSource pillars = new IColumnSource() {
			@Override
			public int groundHeight(final int x, final int z) {
				return ((x ^ z) & 1) == 0 ? low : tall;
			}

			@Override
			public PrecipitationKind precipitation(final int x, final int y, final int z) {
				return PrecipitationKind.RAIN;
			}

			@Override
			public int light(final int x, final int y, final int z) {
				return 0xF000F0;
			}

			@Override
			public int dustColor(final int x, final int y, final int z) {
				return SAND;
			}

			@Override
			public int fallbackDustColor(final int x, final int y, final int z) {
				return SAND;
			}
		};
		final PrecipitationField field = new PrecipitationField(1000);
		final Wind wind = steadyWind();
		final Random random = new XorShiftRandom(42);
		int blownIn = 0;
		for (int t = 0; t < 40; t++) {
			field.tick(pillars, 0, CAMERA_Y, 0, 1F, 1F, wind, random);
			for (int i = 0; i < field.landings(); i++) {
				if (!field.landingBlownIn(i))
					continue;
				blownIn++;
				assertFalse(field.landingIsSnow(i));
				final double x = field.landingX(i), z = field.landingZ(i);
				assertEquals(low, pillars.groundHeight(MathHelper.floor(x), MathHelper.floor(z)), "should start out in the open");
				assertEquals(tall, pillars.groundHeight(MathHelper.floor(x + field.landingMotionX(i)), MathHelper.floor(z + field.landingMotionZ(i))),
						"should be moving into a pillar");
				assertTrue(field.landingMotionY(i) < 0F, "falling");
			}
		}
		assertTrue(blownIn > 0, "wind-blown rain should hit the pillars' sides");
	}

	@Test
	public void rainLandsAsRain() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, RAIN_EVERYWHERE, 0, 0, 1F, 30);
		assertTrue(field.landings() > 0);
		for (int i = 0; i < field.landings(); i++)
			assertFalse(field.landingIsSnow(i));
	}

	@Test
	public void dropsFallingOutOfTheBottomDontLand() {
		final PrecipitationField field = new PrecipitationField(1000);
		// Ground far below the cylinder: drops leave through its bottom without reaching it
		tick(field, new Columns(0, Integer.MAX_VALUE, Integer.MIN_VALUE), 0, 0, 1F, 40);
		assertEquals(0, field.landings());
	}

	@Test
	public void clearingForgetsTheLandings() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, RAIN_EVERYWHERE, 0, 0, 1F, 30);
		field.clear();
		assertEquals(0, field.landings());
	}

	@Test
	public void withoutWindRainFallsStraightDown() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, RAIN_EVERYWHERE, 0, 0, 1F, 10);
		for (int i = 0; i < field.count(); i++) {
			assertEquals(0F, field.drops().windX[i]);
			assertEquals(0F, field.drops().windZ[i]);
		}
	}

	@Test
	public void theWindCarriesDropsDownwind() {
		final Wind wind = steadyWind();
		assertTrue(wind.speed() > 0.1F, () -> "only " + wind.speed());
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, RAIN_EVERYWHERE, 0, 0, 1F, wind, 10);

		assertTrue(field.count() > 0);
		for (int i = 0; i < field.count(); i++) {
			// Each drop moves the way the wind blows, by about as much, give or take its own drift
			final float along = field.drops().windX[i] * wind.x() + field.drops().windZ[i] * wind.z();
			final float speedSq = wind.speed() * wind.speed();
			assertTrue(along > 0.8F * speedSq && along < 1.2F * speedSq, "a drop not carried with the wind");
		}
	}

	@Test
	public void snowCatchesTheWindLaterThanRain() {
		final Wind wind = steadyWind();
		final PrecipitationField rain = new PrecipitationField(1000);
		final PrecipitationField snow = new PrecipitationField(1000);
		tick(rain, RAIN_EVERYWHERE, 0, 0, 1F, wind, 5);
		tick(snow, new Columns(GROUND, Integer.MIN_VALUE, Integer.MIN_VALUE), 0, 0, 1F, wind, 5);

		// A gust doubles the wind; one tick later, rain has gone further toward it than snow has
		WindAccess.gust(wind, 40, 1F);
		for (int t = 0; t < 30; t++) {
			wind.tick(1F, false, false, 4, new XorShiftRandom(t));
		}
		final Random random = new XorShiftRandom(1);
		rain.tick(RAIN_EVERYWHERE, 0, CAMERA_Y, 0, 0F, 1F, wind, random);
		snow.tick(new Columns(GROUND, Integer.MIN_VALUE, Integer.MIN_VALUE), 0, CAMERA_Y, 0, 0F, 1F, wind, random);
		assertTrue(meanSpeed(rain) / averageDrift(rain) > meanSpeed(snow) / averageDrift(snow));
	}

	private static float meanSpeed(final PrecipitationField field) {
		float total = 0F;
		for (int i = 0; i < field.count(); i++)
			total += Math.hypot(field.drops().windX[i], field.drops().windZ[i]);
		return total / field.count();
	}

	private static float averageDrift(final PrecipitationField field) {
		float total = 0F;
		for (int i = 0; i < field.count(); i++)
			total += field.drops().drift[i];
		return total / field.count();
	}

	@Test
	public void snowflakesAreSpreadOverTheirLooks() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, new Columns(GROUND, Integer.MIN_VALUE, Integer.MIN_VALUE), 0, 0, 1F, 5);
		final int[] seen = new int[4];
		for (int i = 0; i < field.count(); i++)
			seen[PackedDrops.variant(PrecipitationField.pick(field.drops(), i), seen.length)]++;
		for (int n = 0; n < seen.length; n++) {
			final int look = n;
			assertTrue(seen[n] > field.count() / 8, () -> "look " + look + " only " + seen[look] + " of " + field.count());
		}
	}

	@Test
	public void snowflakesVaryInSizeMostlySmall() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, new Columns(GROUND, Integer.MIN_VALUE, Integer.MIN_VALUE), 0, 0, 1F, 5);
		int small = 0, large = 0;
		for (int i = 0; i < field.count(); i++) {
			final float size = PrecipitationField.snowSize(field.drops(), i);
			assertTrue(size >= 0F && size <= 1F);
			if (size < 0.25F)
				small++;
			else if (size > 0.75F)
				large++;
		}
		assertTrue(large > 0, "no large flakes");
		assertTrue(small > 2 * large, () -> "small flakes should be the most common");
	}

	@Test
	public void largerSnowflakesFallFaster() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, new Columns(GROUND, Integer.MIN_VALUE, Integer.MIN_VALUE), 0, 0, 1F, 5);
		for (int i = 0; i < field.count(); i++)
			for (int j = 0; j < field.count(); j++)
				if (field.drops().speed[i] > field.drops().speed[j])
					assertTrue(PrecipitationField.snowSize(field.drops(), i) >= PrecipitationField.snowSize(field.drops(), j));
	}

	@Test
	public void lightRainHasSmallDropsAndADownpourLargeOnes() {
		final Random random = new XorShiftRandom(21);
		float lightTotal = 0F, heavyTotal = 0F;
		for (int i = 0; i < 1000; i++) {
			lightTotal += PrecipitationField.rainDropSize(0.1F, random);
			heavyTotal += PrecipitationField.rainDropSize(1F, random);
		}
		final float light = lightTotal / 1000;
		final float heavy = heavyTotal / 1000;
		assertTrue(light < 0.3F, () -> "light rain averaged " + light);
		assertTrue(heavy > 0.75F, () -> "a downpour averaged " + heavy);
	}

	@Test
	public void smallDropsFallSlower() {
		final PrecipitationField light = new PrecipitationField(1000);
		final PrecipitationField heavy = new PrecipitationField(1000);
		tick(light, RAIN_EVERYWHERE, 0, 0, 0.1F, 3);
		tick(heavy, RAIN_EVERYWHERE, 0, 0, 1F, 3);
		assertTrue(meanFallSpeed(light) < meanFallSpeed(heavy));
		for (int i = 0; i < heavy.count(); i++)
			assertEquals((heavy.drops().speed[i] - PrecipitationField.RAIN_SPEED_MIN) / PrecipitationField.RAIN_SPEED_RANGE, PrecipitationField.rainSize(heavy.drops(), i), 1e-5F);
	}

	private static float meanFallSpeed(final PrecipitationField field) {
		float total = 0F;
		for (int i = 0; i < field.count(); i++)
			total += field.drops().speed[i];
		return total / field.count();
	}

	@Test
	public void landingsCarryTheDropsSize() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, RAIN_EVERYWHERE, 0, 0, 1F, 30);
		assertTrue(field.landings() > 0);
		for (int i = 0; i < field.landings(); i++)
			assertTrue(field.landingSize(i) >= 0F && field.landingSize(i) <= 1F);
	}

	@Test
	public void aDesertFillsWithDustNotRain() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, ALL_DESERT, 0, 0, 1F, 10);
		assertTrue(field.count() > 0);
		assertEquals(field.count(), field.dustCount(), "everything in a desert is dust");
		for (int i = 0; i < field.count(); i++) {
			assertEquals(PrecipitationField.DUST, field.drops().kind[i]);
			assertEquals(SAND, field.drops().color[i]);
		}
		assertTrue(field.isDustAt(0, 0));
	}

	@Test
	public void dustTakesOnlyItsShareOfTheField() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, ALL_DESERT, 0, 0, 1F, 20);
		assertTrue(field.dustCount() <= (int) (field.capacity() * PrecipitationField.DUST_SHARE));
		assertTrue(field.dustCount() > (int) (field.capacity() * PrecipitationField.DUST_SHARE) / 2);

		final PrecipitationField half = new PrecipitationField(1000);
		tick(half, ALL_DESERT, 0, 0, 0.5F, 20);
		assertTrue(half.dustCount() <= (int) (half.capacity() * PrecipitationField.DUST_SHARE * 0.5F));
	}

	@Test
	public void dustBlowsLowAboveTheGround() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, ALL_DESERT, 0, 0, 1F, 20);
		int low = 0;
		for (int i = 0; i < field.count(); i++) {
			// The turbulence can dip a puff a little below its usual lift, from which it eases back up; never into
			// the ground
			assertTrue(field.drops().y[i] > GROUND, "dust below the ground");
			if (field.drops().y[i] < GROUND + PrecipitationField.DUST_MIN_LIFT + PrecipitationField.DUST_HEIGHT / 2F)
				low++;
		}
		// Squaring the height puts about seven in ten in the lower half
		assertTrue(low > field.count() * 0.6F, () -> "too few puffs low down");
	}

	@Test
	public void dustSettlesAndNeverLands() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, ALL_DESERT, 0, 0, 1F, 10);
		assertTrue(field.count() > 0);
		assertEquals(0, field.landings());

		// With the storm over, every puff settles within its lifetime
		tick(field, ALL_DESERT, 0, 0, 0F, PrecipitationField.DUST_MIN_LIFETIME + PrecipitationField.DUST_LIFETIME_RANGE);
		assertEquals(0, field.count());
		assertEquals(0, field.dustCount());
	}

	@Test
	public void dustFadesInAndOut() {
		final PrecipitationField field = new PrecipitationField(100);
		tick(field, ALL_DESERT, 0, 0, 1F, 1);
		assertTrue(field.count() > 0);
		assertEquals(0F, fade(field, 0, 0F));
		// Half way through its life it is at full strength; at the end, gone
		field.drops().age[0] = field.drops().lifetime[0] / 2;
		assertEquals(1F, fade(field, 0, 0F));
		field.drops().age[0] = field.drops().lifetime[0];
		assertEquals(0F, fade(field, 0, 0F));
	}

	@Test
	public void dustBlownOutOfTheDesertFadesOut() {
		// Desert for x from 0, rain to the west, where the wind blows: dust blown out of the desert drifts on a little
		// way, fading, then goes
		final Wind wind = steadyWind();
		final PrecipitationField field = new PrecipitationField(1000);
		assertTrue(wind.x() < 0F, "the wind blows out of the desert");
		final Desert desert = new Desert(GROUND, 0, true);
		final Random random = new XorShiftRandom(42);
		boolean sawFading = false;
		for (int t = 0; t < 60; t++) {
			field.tick(desert, 0, CAMERA_Y, 0, 1F, 1F, wind, random);
			for (int i = 0; i < field.count(); i++) {
				if (field.drops().kind[i] != PrecipitationField.DUST || field.drops().x[i] > -1F)
					continue;
				assertTrue(field.drops().lifetime[i] - field.drops().age[i] <= PrecipitationField.DUST_FADE, "dust outside the desert not fading");
				sawFading |= fade(field, i, 0F) > 0F;
			}
		}
		assertTrue(sawFading, "no dust drifted out of the desert");
	}

	/**
	 * Desert everywhere, rising in one-block steps from {@code firstStep} eastward, a block every {@code run} blocks
	 */
	private static final class Slope implements IColumnSource {
		final int firstStep;
		final int run;

		Slope(final int firstStep, final int run) {
			this.firstStep = firstStep;
			this.run = run;
		}

		@Override
		public int groundHeight(final int x, final int z) {
			return GROUND + (x < this.firstStep ? 0 : 1 + (x - this.firstStep) / this.run);
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
			return SAND;
		}

		@Override
		public int fallbackDustColor(final int x, final int y, final int z) {
			return SAND;
		}
	}

	/**
	 * A strong wind blowing due east, toward positive x
	 */
	private static Wind eastWind(final float speed) {
		final Wind wind = new Wind();
		WindAccess.setSteady(wind, 0F, speed);
		return wind;
	}

	@Test
	public void dustGlidesUpASlopeRatherThanJumping() {
		// Regression: a puff reaching a higher column was put on top of it in a single tick, a jump of a block
		final Slope columns = new Slope(-4, 2);
		final Wind wind = eastWind(0.5F);
		final PrecipitationField field = new PrecipitationField(1000);
		final Random random = new XorShiftRandom(31);
		// How much more a puff rose in a tick than its climb allows, from how far it was blown; the turbulence adds
		// a little either way
		float worst = Float.NEGATIVE_INFINITY;
		for (int t = 0; t < 60; t++) {
			field.tick(columns, 0, CAMERA_Y, 0, 1F, 1F, wind, random);
			for (int i = 0; i < field.count(); i++) {
				if (field.drops().age[i] == 0)
					continue;
				final float along = (float) Math.hypot(field.drops().windX[i], field.drops().windZ[i]);
				final float rise = field.drops().y[i] - field.drops().prevY[i];
				worst = Math.max(worst, rise - Math.max(along * PrecipitationField.DUST_MAX_CLIMB, PrecipitationField.DUST_MIN_RISE));
			}
		}
		final float finalWorst = worst;
		final float turbulence = PrecipitationField.DUST_TURBULENCE_HEIGHT * PrecipitationField.DUST_TURBULENCE_RATE;
		assertTrue(finalWorst <= turbulence + 1e-4F, () -> "a puff rose " + finalWorst + " more than its climb allows");
	}

	@Test
	public void dustRidesUpASteadySlopeInAStrongWind() {
		// A one in one slope, in the strongest dust storm wind: the puffs keep up with the ground rather than being
		// left behind in it and taken for blown into a wall
		final Slope columns = new Slope(-12, 1);
		final Wind wind = eastWind(WindAccess.strongest());
		// Over many storms: about one in twelve is in the slope at any one time, so a single storm can have more than one
		// in ten by chance
		int climbing = 0, buried = 0;
		for (int seed = 1; seed <= 20; seed++) {
			final PrecipitationField field = new PrecipitationField(1000);
			final Random random = new XorShiftRandom(seed);
			// Long enough that every puff now has climbed from where it spawned
			for (int t = 0; t < 30; t++)
				field.tick(columns, 0, CAMERA_Y, 0, 1F, 1F, wind, random);
			for (int i = 0; i < field.count(); i++) {
				if (field.drops().age[i] < 10)
					continue;
				climbing++;
				final float ground = columns.groundHeight((int) Math.floor(field.drops().x[i]), 0);
				if (field.drops().y[i] < ground)
					buried++;
			}
		}
		assertTrue(climbing > 0);
		final int finalBuried = buried, finalClimbing = climbing;
		assertTrue(buried < climbing / 10, () -> finalBuried + " of " + finalClimbing + " puffs left in the slope");
	}

	@Test
	public void theEdgeOfTheRainIsAsDenseAsItsMiddle() {
		// Regression: with half the area dry, every drop the storm called for went into the other half, twice as
		// densely, and the field spent its tries every tick on the dry half
		final PrecipitationField whole = new PrecipitationField(1000);
		final PrecipitationField half = new PrecipitationField(1000);
		tick(whole, RAIN_EVERYWHERE, 0, 0, 1F, 20);
		// Dry for z below 0: 12 of the 25 rows
		tick(half, new Columns(GROUND, Integer.MAX_VALUE, 0), 0, 0, 1F, 20);
		final float expected = whole.count() * 13F / 25F;
		assertEquals(expected, half.count(), expected * 0.1F);
	}

	/**
	 * An awning over x 2 to 5: the ground is its top there, with open air beneath it down to the real ground. Says which
	 * threads asked it anything.
	 */
	private static final class Awning implements IColumnSource {
		final Set<Thread> askedFrom = Collections.newSetFromMap(new ConcurrentHashMap<>());
		final AtomicInteger openAsked = new AtomicInteger();

		private static boolean under(final int x) {
			return x >= 2 && x < 6;
		}

		@Override
		public int groundHeight(final int x, final int z) {
			this.askedFrom.add(Thread.currentThread());
			return under(x) ? GROUND + 6 : GROUND;
		}

		@Override
		public PrecipitationKind precipitation(final int x, final int y, final int z) {
			this.askedFrom.add(Thread.currentThread());
			return PrecipitationKind.RAIN;
		}

		@Override
		public boolean isOpen(final int x, final int y, final int z) {
			this.askedFrom.add(Thread.currentThread());
			this.openAsked.incrementAndGet();
			return y >= GROUND && (!under(x) || y < GROUND + 5);
		}

		@Override
		public int light(final int x, final int y, final int z) {
			this.askedFrom.add(Thread.currentThread());
			return 0xF000F0;
		}

		@Override
		public int dustColor(final int x, final int y, final int z) {
			this.askedFrom.add(Thread.currentThread());
			return SAND;
		}

		@Override
		public int fallbackDustColor(final int x, final int y, final int z) {
			this.askedFrom.add(Thread.currentThread());
			return SAND;
		}
	}

	@Test
	public void aStepOnAnotherThreadAsksTheWorldNothing() throws Exception {
		// Only the thread that starts and finishes a step may read the world: the step leaves it the drops carried in
		// under the awning to answer
		final Awning awning = new Awning();
		final PrecipitationField field = new PrecipitationField(1000);
		final Wind wind = eastWind(WindAccess.strongest());
		final Random random = new XorShiftRandom(5);
		final ExecutorService stepper = Executors.newSingleThreadExecutor();
		try {
			for (int t = 0; t < 40; t++) {
				field.finish(awning);
				field.start(awning, 0, CAMERA_Y, 0, 1F, 1F, wind, random, stepper);
			}
			field.finish(awning);
		} finally {
			stepper.shutdown();
		}
		assertEquals(Collections.singleton(Thread.currentThread()), awning.askedFrom);
		assertTrue(awning.openAsked.get() > 0, "some drops were carried in under the awning");
		assertTrue(IntStream.range(0, field.count()).anyMatch(i -> field.drops().covered[i] > 0), "and drift on under it");
	}

	@Test
	public void theDropsDrawnStayAsTheyAreUntilTheStepFinishes() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, RAIN_EVERYWHERE, 0, 0, 1F, 10);
		final int count = field.count();
		final float y = field.drops().y[0];

		// A step held back until let go: until it is finished, it is the other set of drops it moves
		final List<Runnable> held = new ArrayList<>();
		field.start(RAIN_EVERYWHERE, 0, CAMERA_Y, 0, 1F, 1F, new Wind(), new XorShiftRandom(1), held::add);
		assertTrue(field.isStepping());
		assertThrows(IllegalStateException.class,
				() -> field.start(RAIN_EVERYWHERE, 0, CAMERA_Y, 0, 1F, 1F, new Wind(), new XorShiftRandom(1), Runnable::run),
				"one step at a time");
		held.forEach(Runnable::run);
		assertEquals(count, field.count());
		assertEquals(y, field.drops().y[0]);

		assertTrue(field.finish(RAIN_EVERYWHERE));
		assertFalse(field.isStepping());
		assertNotEquals(y, field.drops().y[0], "moved once finished");
		assertFalse(field.finish(RAIN_EVERYWHERE), "nothing left to finish");
	}

	@Test
	public void aFailedStepLeavesTheDropsAsTheyWere() {
		final PrecipitationField field = new PrecipitationField(1000);
		tick(field, RAIN_EVERYWHERE, 0, 0, 1F, 10);
		final int count = field.count();
		final float y = field.drops().y[0];

		final Random broken = new Random() {
			private static final long serialVersionUID = 1L;

			@Override
			public float nextFloat() {
				throw new IllegalStateException("broken");
			}
		};
		field.start(RAIN_EVERYWHERE, 0, CAMERA_Y, 0, 1F, 1F, new Wind(), broken, Runnable::run);
		final IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> field.finish(RAIN_EVERYWHERE));
		assertEquals("broken", thrown.getMessage());
		assertEquals(count, field.count());
		assertEquals(y, field.drops().y[0]);
		assertEquals(0, field.landings());

		// And the next step goes ahead
		tick(field, RAIN_EVERYWHERE, 0, 0, 1F, 1);
		assertNotEquals(y, field.drops().y[0]);
	}

	@Test
	public void aFieldWithNothingLeftIsIdleBetweenStorms() {
		final PrecipitationField field = new PrecipitationField(1000);
		assertTrue(field.isIdle(0F));
		assertFalse(field.isIdle(0.5F), "a storm on");

		tick(field, RAIN_EVERYWHERE, 0, 0, 1F, 5);
		assertFalse(field.isIdle(0F), "drops still falling");
	}

	@Test
	public void dropsWellBehindTheCameraAreNotDrawn() {
		// Looking north, toward negative z: left is west, up is up
		final DropRenderer.View view = new DropRenderer.View(0, 0, 0, -1F, 0F, 0F, 0F, 1F, 0F, 0F);
		// The margin the renderer keeps: the longest streak or the largest flake, and room to spare
		final float margin = Math.max(DropRenderer.rainLength(1F),
				DropRenderer.SNOW_MIN_HALF_SIZE + DropRenderer.SNOW_HALF_SIZE_RANGE) + DropRenderer.BEHIND_SPARE;
		assertTrue(view.sees(0F, 0F, -5F, margin), "ahead");
		assertTrue(view.sees(4F, 0F, 0F, margin), "beside");
		assertTrue(view.sees(0F, 0F, margin - 0.1F, margin), "just behind: a streak could reach into view");
		assertFalse(view.sees(0F, 0F, 5F, margin), "well behind");
	}

	@Test
	public void distantRainAndSnowAreThinnedOut() {
		assertEquals(1F, DropRenderer.shareDrawn(0F, PrecipitationField.DEFAULT_RADIUS));
		assertEquals(1F, DropRenderer.shareDrawn(PrecipitationField.DEFAULT_RADIUS * DropRenderer.THIN_FROM_SHARE, PrecipitationField.DEFAULT_RADIUS));
		assertEquals(DropRenderer.THIN_KEEP_AT_EDGE, DropRenderer.shareDrawn(PrecipitationField.DEFAULT_RADIUS, PrecipitationField.DEFAULT_RADIUS), 1e-6F);
		float previous = 1F;
		for (float d = 0F; d <= PrecipitationField.DEFAULT_RADIUS; d += 0.25F) {
			final float share = DropRenderer.shareDrawn(d, PrecipitationField.DEFAULT_RADIUS);
			assertTrue(share <= previous, "the share drawn grows with distance");
			previous = share;
		}
	}

	@Test
	public void aThinnedDropFadesRatherThanPopping() {
		// Near, every drop is drawn whole
		assertEquals(1F, DropRenderer.thinned(0.99F, 1F, PrecipitationField.DEFAULT_RADIUS));
		// At the edge, drops under the share are drawn whole, those well over it not at all, and those just over it
		// part way, so a drop slips out as the camera moves away
		final float edge = PrecipitationField.DEFAULT_RADIUS;
		final float share = DropRenderer.THIN_KEEP_AT_EDGE;
		assertEquals(1F, DropRenderer.thinned(share - 0.01F, edge, PrecipitationField.DEFAULT_RADIUS));
		assertEquals(0F, DropRenderer.thinned(share + DropRenderer.THIN_FADE + 0.01F, edge, PrecipitationField.DEFAULT_RADIUS));
		final float part = DropRenderer.thinned(share + DropRenderer.THIN_FADE / 2F, edge, PrecipitationField.DEFAULT_RADIUS);
		assertTrue(part > 0F && part < 1F, () -> "part way: " + part);
	}

	@Test
	public void thinningSparesAboutTwoFifthsOfTheDrawing() {
		// Drops spread evenly over the disc, each with its own random number: how many are drawn at all
		final Random random = new XorShiftRandom(41);
		int drawn = 0;
		final int n = 20000;
		for (int i = 0; i < n; i++) {
			final float distance = PrecipitationField.DEFAULT_RADIUS * (float) Math.sqrt(random.nextFloat());
			if (DropRenderer.thinned(random.nextFloat(), distance, PrecipitationField.DEFAULT_RADIUS) > 0F)
				drawn++;
		}
		final float share = drawn / (float) n;
		assertTrue(share > 0.55F && share < 0.68F, () -> "drew " + share + " of the drops");
	}

	/**
	 * Rain everywhere, with the given share of it falling as snow
	 */
	private static final class Sleet implements IColumnSource {
		final float share;

		Sleet(final float share) {
			this.share = share;
		}

		@Override
		public int groundHeight(final int x, final int z) {
			return GROUND;
		}

		@Override
		public PrecipitationKind precipitation(final int x, final int y, final int z) {
			return PrecipitationKind.RAIN;
		}

		@Override
		public float snowShare(final int x, final int y, final int z) {
			return this.share;
		}

		@Override
		public int light(final int x, final int y, final int z) {
			return 0xF000F0;
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

	private static float snowShareOf(final PrecipitationField field) {
		int snow = 0;
		for (int i = 0; i < field.count(); i++)
			if (field.drops().kind[i] == PrecipitationField.SNOW)
				snow++;
		return snow / (float) field.count();
	}

	@Test
	public void nearTheSnowLineTheRainIsPartSnow() {
		final PrecipitationField none = new PrecipitationField(1000);
		tick(none, new Sleet(0F), 0, 0, 1F, 3);
		assertEquals(0F, snowShareOf(none));

		final PrecipitationField half = new PrecipitationField(1000);
		tick(half, new Sleet(0.5F), 0, 0, 1F, 3);
		assertEquals(0.5F, snowShareOf(half), 0.08F);
	}

	@Test
	public void densityThinsTheField() {
		final PrecipitationField full = new PrecipitationField(1000);
		final PrecipitationField half = new PrecipitationField(1000);
		final Random random = new XorShiftRandom(5);
		for (int t = 0; t < 20; t++) {
			full.tick(RAIN_EVERYWHERE, 0, CAMERA_Y, 0, 1F, 1F, new Wind(), random);
			half.tick(RAIN_EVERYWHERE, 0, CAMERA_Y, 0, 1F, 0.5F, new Wind(), random);
		}
		assertEquals(full.count() / 2F, half.count(), full.count() * 0.06F);
	}

	@Test
	public void aWiderRangeHasDropsFurtherOut() {
		final PrecipitationField wide = new PrecipitationField(4000, 20);
		tick(wide, RAIN_EVERYWHERE, 0, 0, 1F, 10);
		float furthest = 0F;
		for (int i = 0; i < wide.count(); i++) {
			final float distance = (float) Math.hypot(wide.drops().x[i], wide.drops().z[i]);
			assertTrue(distance <= 20F + 1e-4F, "beyond the range");
			furthest = Math.max(furthest, distance);
		}
		assertTrue(furthest > PrecipitationField.DEFAULT_RADIUS, "none past the default range");
		assertEquals(20, wide.radius());
	}

	@Test
	public void thinningFollowsTheRange() {
		// The same share is drawn at the same share of the way out, whatever the range
		for (float share = 0F; share <= 1F; share += 0.1F) {
			final float atDefault = DropRenderer.shareDrawn(share * PrecipitationField.DEFAULT_RADIUS, PrecipitationField.DEFAULT_RADIUS);
			final float atWide = DropRenderer.shareDrawn(share * 24F, 24F);
			assertEquals(atDefault, atWide, 1e-5F);
		}
		assertEquals(DropRenderer.THIN_KEEP_AT_EDGE, DropRenderer.shareDrawn(24F, 24F), 1e-6F);
	}

	@Test
	public void newDropsFadeIn() {
		final PrecipitationField field = new PrecipitationField(10);
		tick(field, RAIN_EVERYWHERE, 0, 0, 1F, 1);
		assertTrue(field.count() > 0);
		assertEquals(0F, fade(field, 0, 0F));
		assertTrue(fade(field, 0, 0.5F) > 0F && fade(field, 0, 0.5F) < 1F);
	}
}
