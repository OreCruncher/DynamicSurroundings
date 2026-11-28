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

/**
 * Tests for the flecks snowflakes leave where they land: how they melt, and how many are kept. Ported from the 26.x
 * line's tests.
 */
public class SnowFlecksTests {

	@Test
	public void liesWholeThenMelts() {
		final int lifetime = 50;
		assertEquals(1F, SnowFlecks.remainingForAge(0F, lifetime));
		assertEquals(1F, SnowFlecks.remainingForAge(lifetime * SnowFlecks.WHOLE_SHARE, lifetime), 1e-5F,
				"whole for a while");

		float previous = 1F;
		for (int age = (int) (lifetime * SnowFlecks.WHOLE_SHARE) + 1; age < lifetime; age++) {
			final float remaining = SnowFlecks.remainingForAge(age, lifetime);
			assertTrue(remaining < previous, "should keep melting");
			assertTrue(remaining > 0F, "gone too soon");
			previous = remaining;
		}
		assertEquals(0F, SnowFlecks.remainingForAge(lifetime, lifetime));
	}

	@Test
	public void shrinksAsItMelts() {
		assertEquals(1F, SnowFlecks.sizeFor(1F), 1e-5F);
		assertEquals(SnowFlecks.MELTED_SIZE, SnowFlecks.sizeFor(0F), 1e-5F);
		assertTrue(SnowFlecks.sizeFor(0.5F) < 1F && SnowFlecks.sizeFor(0.5F) > SnowFlecks.MELTED_SIZE);
	}

	@Test
	public void flecksMeltAndGo() {
		final SnowFlecks flecks = new SnowFlecks(100);
		final Random random = new Random(3);
		for (int i = 0; i < 50; i++)
			flecks.add(i, 64, 0, 0.5F, 0.5F, 0xF000F0, random);
		assertEquals(50, flecks.count());

		for (int t = 0; t < SnowFlecks.MIN_LIFETIME - 1; t++)
			flecks.tick();
		assertEquals(50, flecks.count(), "none should have melted yet");

		for (int t = 0; t < SnowFlecks.LIFETIME_RANGE + 1; t++)
			flecks.tick();
		assertEquals(0, flecks.count());
	}

	@Test
	public void flecksLieJustAboveTheSurfaceAndKeepTheirFlake() {
		final SnowFlecks flecks = new SnowFlecks(100);
		final Random random = new Random(5);
		for (int i = 0; i < 100; i++)
			flecks.add(0, 64.5, 0, i / 99F, 0.25F, 0xF000F0, random);
		for (int i = 0; i < flecks.count(); i++) {
			assertTrue(flecks.y[i] > 64.5F && flecks.y[i] < 64.5F + 0.02F, "not just above the surface");
			assertEquals(1F, flecks.turnCos[i] * flecks.turnCos[i] + flecks.turnSin[i] * flecks.turnSin[i], 1e-4F);
			assertEquals(0.25F, flecks.variant[i]);
			assertEquals(0xF000F0, flecks.light[i]);
		}
	}

	@Test
	public void aFleckOnASideStandsJustOffIt() {
		final SnowFlecks flecks = new SnowFlecks(10);
		final Random random = new Random(9);
		flecks.add(10, 70.3, 4.6, SnowFlecks.FACE_WEST, 0.5F, 0.5F, 0, random);
		flecks.add(3.2, 70.3, 8, SnowFlecks.FACE_SOUTH, 0.5F, 0.5F, 0, random);
		// Off the face, the way it looks, by the lift; the height where it hit
		assertTrue(flecks.x[0] < 10D && flecks.x[0] > 10D - 0.02D);
		assertEquals(70.3F, flecks.y[0], 1e-5F);
		assertEquals(4.6D, flecks.z[0], 1e-5D);
		assertEquals(SnowFlecks.FACE_WEST, flecks.face[0]);
		assertTrue(flecks.z[1] > 8D && flecks.z[1] < 8D + 0.02D);
		assertEquals(3.2D, flecks.x[1], 1e-5D);
		assertEquals(SnowFlecks.FACE_SOUTH, flecks.face[1]);
	}

	@Test
	public void facesLookTheWayTheyAreNamed() {
		assertEquals(-1, SnowFlecks.normalX(SnowFlecks.FACE_WEST));
		assertEquals(1, SnowFlecks.normalX(SnowFlecks.FACE_EAST));
		assertEquals(-1, SnowFlecks.normalZ(SnowFlecks.FACE_NORTH));
		assertEquals(1, SnowFlecks.normalZ(SnowFlecks.FACE_SOUTH));
		assertEquals(0, SnowFlecks.normalX(SnowFlecks.FACE_TOP));
		assertEquals(0, SnowFlecks.normalZ(SnowFlecks.FACE_TOP));
		assertEquals(0, SnowFlecks.normalZ(SnowFlecks.FACE_EAST));
		assertEquals(0, SnowFlecks.normalX(SnowFlecks.FACE_NORTH));
	}

	@Test
	public void aFullSetLeavesOutTheRest() {
		final SnowFlecks flecks = new SnowFlecks(10);
		final Random random = new Random(7);
		for (int i = 0; i < 20; i++)
			flecks.add(i, 64, 0, 0F, 0F, 0, random);
		assertEquals(10, flecks.count());
		assertEquals(9D, flecks.x[9], "the first ten are the ones kept");
	}

	@Test
	public void clearingRemovesThemAll() {
		final SnowFlecks flecks = new SnowFlecks(10);
		flecks.add(0, 64, 0, 0F, 0F, 0, new Random(1));
		flecks.clear();
		assertEquals(0, flecks.count());
	}
}
