/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.orecruncher.dsurround.client.fx.firefly;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Tests for the fireflies' wandering flight, from the 26.x line's
 */
public class WanderingFlightTests {

	private static final WanderingFlight.Settings SETTINGS = new WanderingFlight.Settings(
			0.012F, 0.03F, 0.025F, 0.9F, 0.12F, 0.018F, 0.004F, 0.2F, 6);
	private static final WanderingFlight.Obstacles OPEN_AIR = (x, y, z) -> false;

	private static double horizontalSpeed(WanderingFlight flight) {
		return Math.hypot(flight.xd(), flight.zd());
	}

	@Test
	public void startsUnderWayAtCruisingSpeed() {
		for (long seed = 0; seed < 100; seed++) {
			WanderingFlight flight = new WanderingFlight(SETTINGS, new Random(seed));
			double speed = horizontalSpeed(flight);
			assertTrue(speed >= SETTINGS.minSpeed - 1e-6 && speed <= SETTINGS.maxSpeed + 1e-6, "speed " + speed);
			assertEquals(0D, flight.yd());
		}
	}

	@Test
	public void neverFasterThanItsTopSpeed() {
		for (long seed = 0; seed < 50; seed++) {
			WanderingFlight flight = new WanderingFlight(SETTINGS, new Random(seed));
			for (int age = 0; age < 200; age++) {
				flight.tick(0, 0, 0, age, 0F, OPEN_AIR);
				assertTrue(horizontalSpeed(flight) <= SETTINGS.maxSpeed + 1e-6);
			}
		}
	}

	@Test
	public void turnsSmoothly() {
		// The velocity only eases toward its target, so it never changes by much in a tick
		double limit = SETTINGS.steer * 2 * (SETTINGS.maxSpeed + SETTINGS.lift + SETTINGS.bob);
		for (long seed = 0; seed < 50; seed++) {
			WanderingFlight flight = new WanderingFlight(SETTINGS, new Random(seed));
			for (int age = 0; age < 200; age++) {
				double xd = flight.xd(), yd = flight.yd(), zd = flight.zd();
				flight.tick(0, 0, 0, age, 1F, OPEN_AIR);
				double change = Math.sqrt(Math.pow(flight.xd() - xd, 2) + Math.pow(flight.yd() - yd, 2) + Math.pow(flight.zd() - zd, 2));
				assertTrue(change <= limit, "changed by " + change);
			}
		}
	}

	@Test
	public void wandersRatherThanFlyingStraight() {
		// Over a long flight it ends up well short of how far it flew, since its path bends
		int bent = 0;
		for (long seed = 0; seed < 50; seed++) {
			WanderingFlight flight = new WanderingFlight(SETTINGS, new Random(seed));
			double x = 0, z = 0, travelled = 0;
			for (int age = 0; age < 200; age++) {
				flight.tick(x, 0, z, age, 0F, OPEN_AIR);
				x += flight.xd();
				z += flight.zd();
				travelled += horizontalSpeed(flight);
			}
			if (Math.hypot(x, z) < travelled * 0.9)
				bent++;
		}
		assertTrue(bent >= 40, "only " + bent + " of 50 flights bent");
	}

	@Test
	public void climbsAndSinksAsAsked() {
		for (float climb : new float[]{1F, -1F}) {
			WanderingFlight flight = new WanderingFlight(SETTINGS, new Random(7));
			double y = 0;
			for (int age = 0; age < 100; age++) {
				flight.tick(0, y, 0, age, climb, OPEN_AIR);
				y += flight.yd();
			}
			assertEquals(Math.signum(climb), Math.signum(y), "climb " + climb + " ended at " + y);
		}
	}

	@Test
	public void turnsAsideAndLiftsWhenBlocked() {
		for (long seed = 0; seed < 50; seed++) {
			WanderingFlight flight = new WanderingFlight(SETTINGS, new Random(seed));
			double xd = flight.xd(), zd = flight.zd();
			flight.tick(0, 0, 0, 0, -1F, (x, y, z) -> true);
			// Steering away: the change in velocity points back against the way it was going, and up
			double dx = flight.xd() - xd;
			double dz = flight.zd() - zd;
			assertTrue(dx * xd + dz * zd < 0, "kept heading into the obstacle");
			assertTrue(flight.yd() > 0, "didn't lift");
		}
	}

	@Test
	public void looksAheadAlongItsPath() {
		// Only the point it is heading for is blocked: a wall ahead, nothing to the sides or behind
		WanderingFlight flight = new WanderingFlight(SETTINGS, new Random(3));
		double[] asked = new double[3];
		flight.tick(10, 20, 30, 0, 0F, (x, y, z) -> {
			asked[0] = x;
			asked[1] = y;
			asked[2] = z;
			return false;
		});
		double reach = Math.hypot(asked[0] - 10, asked[2] - 30);
		assertTrue(reach >= SETTINGS.minSpeed * 0.75 * SETTINGS.lookAhead - 1e-6);
		assertTrue(reach <= SETTINGS.maxSpeed * SETTINGS.lookAhead + 1e-6);
	}

	@Test
	public void sameSeedSameFlight() {
		WanderingFlight a = new WanderingFlight(SETTINGS, new Random(42));
		WanderingFlight b = new WanderingFlight(SETTINGS, new Random(42));
		for (int age = 0; age < 100; age++) {
			a.tick(0, 0, 0, age, 0.5F, OPEN_AIR);
			b.tick(0, 0, 0, age, 0.5F, OPEN_AIR);
			assertEquals(a.xd(), b.xd());
			assertEquals(a.yd(), b.yd());
			assertEquals(a.zd(), b.zd());
		}
	}
}
