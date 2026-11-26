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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.orecruncher.lib.random.XorShiftRandom;

/**
 * Tests for the wind that slants rain and snow: its strength, its direction, and its gusts. Ported from the 26.x
 * line's tests.
 */
public class WindTests {

	private static Wind blow(final float rainLevel, final boolean thundering, final long day, final int ticks) {
		final Wind wind = new Wind();
		final Random random = new XorShiftRandom(11);
		for (int i = 0; i < ticks; i++)
			wind.tick(rainLevel, thundering, false, day, random);
		return wind;
	}

	@Test
	public void betweenStormsALightBreeze() {
		assertEquals(Wind.BREEZE, blow(0F, false, 1, 500).speed(), 1e-4F);
		assertTrue(Wind.BREEZE < Wind.MAX_SPEED * Wind.CALM_FRACTION, "lighter than the lightest storm");
	}

	@Test
	public void heavierStormsBlowHarder() {
		final float light = blow(0.2F, false, 1, 500).speed();
		final float heavy = blow(1F, false, 1, 500).speed();
		final float thunder = blow(1F, true, 1, 500).speed();
		assertTrue(light > 0F);
		assertTrue(heavy > light);
		assertTrue(thunder > heavy);
		assertEquals(Wind.MAX_SPEED * Wind.THUNDER_FACTOR, thunder, 0.01F);
	}

	@Test
	public void dustStormsBlowHardest() {
		final Wind wind = new Wind();
		final Random random = new XorShiftRandom(11);
		for (int i = 0; i < 500; i++)
			wind.tick(1F, false, true, 1, random);
		assertEquals(Wind.MAX_SPEED * Wind.DUST_FACTOR, wind.speed(), 0.01F);
		assertTrue(wind.speed() > blow(1F, true, 1, 500).speed(), "harder than a thunderstorm");
	}

	@Test
	public void theWindsVectorFollowsItsSpeedAndDirection() {
		final Wind wind = blow(1F, false, 7, 300);
		assertEquals(wind.speed(), (float) Math.hypot(wind.x(), wind.z()), 1e-5F);
		assertEquals(wind.directionX() * wind.speed(), wind.x(), 1e-6F);
		assertEquals(wind.directionZ() * wind.speed(), wind.z(), 1e-6F);
		assertEquals(1F, (float) Math.hypot(wind.directionX(), wind.directionZ()), 1e-5F);
	}

	@Test
	public void theWindBuildsUpOverSeconds() {
		// After a second it is well short of a full storm's wind
		assertTrue(blow(1F, false, 1, 20).speed() < 0.5F * Wind.MAX_SPEED);
	}

	@Test
	public void everyPlayerStartsFromTheDaysDirection() {
		// The wandering is each player's own, but only a little in the first ticks
		final Wind a = new Wind();
		final Wind b = new Wind();
		a.tick(1F, false, false, 12, new XorShiftRandom(1));
		b.tick(1F, false, false, 12, new XorShiftRandom(2));
		assertEquals(a.directionX(), b.directionX(), 0.01F);
		assertEquals(a.directionZ(), b.directionZ(), 0.01F);

		final Wind other = new Wind();
		other.tick(1F, false, false, 13, new XorShiftRandom(1));
		assertNotEquals(a.directionX(), other.directionX(), 0.01F);
	}

	@Test
	public void theDirectionWandersSlowly() {
		final Wind wind = new Wind();
		final Random random = new XorShiftRandom(5);
		wind.tick(1F, false, false, 3, random);
		final float x = wind.directionX(), z = wind.directionZ();
		for (int i = 0; i < 20; i++)
			wind.tick(1F, false, false, 3, random);
		// At most MAX_WANDER a tick
		final double turned = Math.acos(Math.max(-1F, Math.min(1F, x * wind.directionX() + z * wind.directionZ())));
		assertTrue(turned <= 20 * Wind.MAX_WANDER + 1e-4, () -> "turned " + turned);
	}

	@Test
	public void gustsComeAndGo() {
		final Wind wind = new Wind();
		final Random random = new XorShiftRandom(8);
		boolean sawGust = false, sawCalm = false;
		for (int i = 0; i < 2000; i++) {
			wind.tick(1F, false, false, 1, random);
			if (wind.isGusting())
				sawGust = true;
			else
				sawCalm = true;
		}
		assertTrue(sawGust && sawCalm);
	}

	@Test
	public void aGustSweepsAcrossFromUpwind() {
		final Wind wind = new Wind();
		wind.gust(20, 1F);
		final float upwind = -Wind.DEFAULT_REACH, downwind = Wind.DEFAULT_REACH;

		// Just set out: only the upwind edge feels it, a little
		assertEquals(1F, wind.gustFactor(downwind, 0F));
		assertEquals(1F, wind.gustFactor(0F, 0F));

		// Half way through passing the upwind edge it is at its height there, and hasn't reached downwind
		final Random random = new XorShiftRandom(3);
		for (int i = 0; i < 10; i++)
			wind.tick(1F, false, false, 1, random);
		assertEquals(2F, wind.gustFactor(upwind, 0F), 1e-4F);
		assertEquals(1F, wind.gustFactor(downwind, 0F));

		// Later it has passed upwind and reached downwind
		final int toCross = (int) Math.ceil(2F * Wind.DEFAULT_REACH / Wind.GUST_TRAVEL);
		for (int i = 0; i < toCross; i++)
			wind.tick(1F, false, false, 1, random);
		assertEquals(1F, wind.gustFactor(upwind, 0F));
		assertTrue(wind.gustFactor(downwind, 0F) > 1.5F);
	}

	@Test
	public void aSnapshotIsTheWindOfItsTick() {
		final Wind wind = new Wind();
		wind.set(0.6F, 0.2F, 1);
		wind.gust(20, 1F);
		final Random random = new XorShiftRandom(3);
		// Through a whole gust, and the calm after it
		final int ticks = (int) Math.ceil(20 + 2F * Wind.DEFAULT_REACH / Wind.GUST_TRAVEL) + 5;
		for (int t = 0; t < ticks; t++) {
			final Wind.Snapshot snapshot = wind.snapshot();
			assertEquals(wind.x(), snapshot.x());
			assertEquals(wind.z(), snapshot.z());
			assertEquals(wind.directionX(), snapshot.directionX());
			assertEquals(wind.directionZ(), snapshot.directionZ());
			for (float along = -Wind.DEFAULT_REACH; along <= Wind.DEFAULT_REACH; along += 0.25F)
				assertEquals(wind.gustFactor(along, 0F), snapshot.gustFactor(along), 1e-3F, "tick " + t + ", " + along + " along");
			wind.tick(1F, false, false, 1, random);
		}
		assertFalse(wind.isGusting());
	}

	@Test
	public void bearingsAreClockwiseFromNorth() {
		// North is toward negative z, east toward positive x
		assertEquals(0F, Wind.bearing(0F, -1F), 1e-3F);
		assertEquals(90F, Wind.bearing(1F, 0F), 1e-3F);
		assertEquals(180F, Wind.bearing(0F, 1F), 1e-3F);
		assertEquals(270F, Wind.bearing(-1F, 0F), 1e-3F);
		assertEquals(45F, Wind.bearing(1F, -1F), 1e-3F);
	}

	@Test
	public void bearingsRoundToTheNearestCompassPoint() {
		assertEquals("N", Wind.compassPoint(0F));
		assertEquals("N", Wind.compassPoint(22F));
		assertEquals("NE", Wind.compassPoint(23F));
		assertEquals("E", Wind.compassPoint(90F));
		assertEquals("SW", Wind.compassPoint(225F));
		assertEquals("NW", Wind.compassPoint(330F));
		assertEquals("N", Wind.compassPoint(359F));
	}

	@Test
	public void aGustSweepsTheWholeOfAWiderRange() {
		final Wind wind = new Wind();
		wind.setReach(24F);
		wind.gust(20, 1F);
		// Its front sets out from the edge of the wider range, and is still under way when it reaches the far side
		final Random random = new XorShiftRandom(3);
		for (int i = 0; i < 10; i++)
			wind.tick(1F, false, false, 1, random);
		assertEquals(2F, wind.gustFactor(-24F, 0F), 1e-4F);
		final int toCross = (int) Math.ceil(48F / Wind.GUST_TRAVEL);
		for (int i = 0; i < toCross; i++)
			wind.tick(1F, false, false, 1, random);
		assertTrue(wind.isGusting());
		assertTrue(wind.gustFactor(24F, 0F) > 1.5F, "reached the far side");
	}

	@Test
	public void gustsStartAtTheEdgeOfTheDefaultRange() {
		assertEquals(PrecipitationField.DEFAULT_RADIUS, Wind.DEFAULT_REACH, 1e-6F);
	}

	@Test
	public void aBearingTurnsIntoTheSameDirection() {
		for (final float bearing : new float[] { 0F, 45F, 90F, 135F, 180F, 225F, 270F, 315F, 10F, 359F }) {
			final Wind wind = new Wind();
			wind.setBearing(bearing);
			assertEquals(bearing, wind.bearing(), 0.01F, "bearing " + bearing);
		}
	}

	@Test
	public void aSetSpeedAndDirectionHold() {
		final Wind wind = new Wind();
		wind.setSpeed(0.4F);
		wind.setBearing(90F);
		final Random random = new XorShiftRandom(17);
		// A light storm would ease the speed down, the direction would wander, and a new day turn it: none of them do
		for (int i = 0; i < 400; i++)
			wind.tick(0.2F, false, false, i < 200 ? 1 : 2, random);
		assertEquals(0.4F, wind.speed(), 1e-6F);
		assertEquals(90F, wind.bearing(), 0.01F);
		assertTrue(wind.isSet() && wind.isSpeedSet() && wind.isBearingSet());
	}

	@Test
	public void speedAndDirectionAreSetApart() {
		final Wind wind = new Wind();
		wind.setBearing(180F);
		final Random random = new XorShiftRandom(19);
		for (int i = 0; i < 300; i++)
			wind.tick(1F, false, false, 1, random);
		assertTrue(wind.isBearingSet() && !wind.isSpeedSet());
		assertEquals(Wind.MAX_SPEED, wind.speed(), 0.01F, "the speed still follows the storm");
	}

	@Test
	public void clearingLetsTheWindGoItsOwnWay() {
		final Wind wind = new Wind();
		wind.setSpeed(0.6F);
		wind.clearSet();
		assertFalse(wind.isSet());
		final Random random = new XorShiftRandom(23);
		for (int i = 0; i < 500; i++)
			wind.tick(1F, false, false, 1, random);
		assertEquals(Wind.MAX_SPEED, wind.speed(), 0.01F, "eased back to the storm's");
	}

	@Test
	public void percentsAreOfAFullStormsWind() {
		assertEquals(Wind.MAX_SPEED, Wind.speedOfPercent(100), 1e-6F);
		assertEquals(100, Wind.percentOfFullStorm(Wind.MAX_SPEED));
		assertEquals(250, Wind.percentOfFullStorm(Wind.speedOfPercent(250)));
	}

	@Test
	public void compassPointsHaveTheirBearings() {
		assertEquals(8, Wind.compassPoints().size());
		assertEquals(0F, Wind.bearingOf("N"));
		assertEquals(90F, Wind.bearingOf("E"));
		assertEquals(225F, Wind.bearingOf("SW"));
		for (final String point : Wind.compassPoints())
			assertEquals(point, Wind.compassPoint(Wind.bearingOf(point)));
	}

	@Test
	public void outsideAGustTheWindIsSteady() {
		final Wind wind = new Wind();
		for (float along = -12F; along <= 12F; along += 1F)
			assertEquals(1F, wind.gustFactor(along, 0.5F));
	}
}
