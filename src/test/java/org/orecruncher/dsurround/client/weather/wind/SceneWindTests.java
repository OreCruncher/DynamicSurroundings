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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the wind other effects lean with. Ported from the 26.x line's tests.
 */
public class SceneWindTests {

	@AfterEach
	public void stillAir() {
		SceneWind.clear();
	}

	@Test
	public void beforeTheFirstTickTheAirIsStill() {
		SceneWind.clear();
		assertEquals(0F, SceneWind.x(10, 10));
		assertEquals(0F, SceneWind.z(10, 10));
	}

	@Test
	public void theWindIsTheWindServicesWind() {
		// Due east, toward positive x
		final Wind wind = new Wind();
		WindAccess.set(wind, 0F, 0.2F);
		SceneWind.update(wind, 0, 0);
		assertEquals(0.2F, SceneWind.x(5, 5), 1e-5F);
		assertEquals(0F, SceneWind.z(5, 5), 1e-5F);
	}

	@Test
	public void theWindHasItsOwnDiagnosticsLine() {
		final Wind wind = new Wind();
		WindAccess.set(wind, 0F, 0.2F);
		assertEquals("Wind: 0.20 toward E (90)", WindService.diagnosticText(wind));
	}

	@Test
	public void somethingLightIsCarriedAtAboutHalfTheWind() {
		// As a particle moves: pushed, then slowed by its drag, tick after tick
		for (final float friction : new float[] { 0.92F, 0.96F }) {
			float speed = 0F;
			for (int t = 0; t < 500; t++)
				speed = (speed + SceneWind.push(0.2F, 1F, friction)) * friction;
			assertEquals(0.2F * SceneWind.CARRIED_SHARE, speed, 0.2F * 0.06F, "drag " + friction);
		}
		assertEquals(0F, SceneWind.push(0.2F, 0F, 0.92F), "sheltered: no push");
	}

	@Test
	public void aDriftCatchesTheWindAndGoesNoFurther() {
		// Whatever the particle does with its own speed, the drift alone never outruns the wind's carrying speed
		final float carried = 0.2F * SceneWind.CARRIED_SHARE;
		float drift = 0F;
		float last = 0F;
		for (int t = 0; t < 2000; t++) {
			drift = SceneWind.drift(drift, 0.2F, 1F);
			assertTrue(drift >= last && drift <= carried, "tick " + t + ": " + drift);
			last = drift;
		}
		assertEquals(carried, drift, 1e-5F);
	}

	@Test
	public void aNewParticleCatchesTheWindAsTheModsSmokeDoes() {
		// The smoke's speed under push and drag, beside a drift, from still air: close all the way
		final float friction = 0.93F;
		final float carried = 0.2F * SceneWind.CARRIED_SHARE;
		float speed = 0F;
		float drift = 0F;
		for (int t = 1; t <= 60; t++) {
			speed = (speed + SceneWind.push(0.2F, 1F, friction)) * friction;
			drift = SceneWind.drift(drift, 0.2F, 1F);
			assertEquals(speed, drift, carried * 0.1F, "tick " + t);
		}
		assertTrue(SceneWind.drift(0F, 0.2F, 1F) < carried * 0.1F, "not at once");
	}

	@Test
	public void aGustEasesInAndOut() {
		final float drift = 0.1F * SceneWind.CARRIED_SHARE;
		final float gusting = SceneWind.drift(drift, 0.2F, 1F);
		assertTrue(gusting > drift && gusting < 0.2F * SceneWind.CARRIED_SHARE * 0.6F, "a step, not a jump");
		final float easing = SceneWind.drift(gusting, 0.1F, 1F);
		assertTrue(easing < gusting && easing > drift, "dies away");
	}

	@Test
	public void outOfTheWindADriftDiesAway() {
		float drift = 0.1F;
		for (int t = 0; t < 200; t++)
			drift = SceneWind.drift(drift, 0.2F, 0F);
		assertEquals(0F, drift, 1e-5F);
		assertEquals(0F, SceneWind.drift(0F, 0F, 1F), "still air: none");
	}
}
