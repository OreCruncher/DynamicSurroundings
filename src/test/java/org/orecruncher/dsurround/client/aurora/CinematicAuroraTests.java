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

package org.orecruncher.dsurround.client.aurora;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * The cinematic aurora's model: the same for a night everywhere, its curls kept within the reach the shader looks in,
 * its motion wrapped and steady, and its night's moment through its phases. Ported from the 26.x line's tests.
 */
public class CinematicAuroraTests {

	@Test
	public void aNightsRibbonsAreTheSameEveryTime() {
		final CinematicAurora a = CinematicAurora.create(42L);
		final CinematicAurora b = CinematicAurora.create(42L);
		assertEquals(a.layout, b.layout);
		assertEquals(a.ribbons.size(), b.ribbons.size());
		for (int i = 0; i < a.ribbons.size(); i++)
			assertEquals(a.ribbons.get(i).heading, b.ribbons.get(i).heading);
	}

	@Test
	public void layoutsHaveTheirRibbonCounts() {
		final Set<CinematicAurora.Layout> seen = EnumSet.noneOf(CinematicAurora.Layout.class);
		for (long night = 0; night < 300; night++) {
			final CinematicAurora aurora = CinematicAurora.create(night);
			seen.add(aurora.layout);
			final int n = aurora.ribbons.size();
			if (aurora.layout == CinematicAurora.Layout.HORSESHOE)
				assertTrue(n >= 1 && n <= 2, "horseshoe with " + n);
			else
				assertTrue(n >= 2 && n <= 4, "waves with " + n);
			assertTrue(n <= CinematicAurora.MAX_RIBBONS);
			for (final CinematicAurora.Ribbon ribbon : aurora.ribbons)
				// The shader's offset from a ribbon, worked out square to it, holds up to this slope
				assertTrue(ribbon.steepest() <= 1.1F + 1e-4F, "slope " + ribbon.steepest());
		}
		assertEquals(EnumSet.allOf(CinematicAurora.Layout.class), seen);
	}

	@Test
	public void aCurlNeverMovesAPointPastItsReach() {
		// The shader only looks within the reach for a ribbon a curl has moved: a point moved farther would be cut off
		for (long night = 0; night < 200; night++) {
			for (final CinematicAurora.Ribbon ribbon : CinematicAurora.create(night).ribbons) {
				final CinematicAurora.Curl curl = ribbon.curl;
				if (curl.twist == 0F)
					continue;
				final float twist = curl.twistAt((float) (Math.PI / 2), 1F);
				final float reach = CinematicAurora.Curl.reachOf(curl.radius, twist);
				final float ax = (float) Math.cos(ribbon.heading);
				final float az = (float) Math.sin(ribbon.heading);
				for (float x = -4F; x <= 4F; x += 0.1F)
					for (float z = -4F; z <= 4F; z += 0.1F) {
						final float px = x * curl.radius * CinematicAurora.CURL_STRETCH;
						final float pz = z * curl.radius;
						final float[] moved = CinematicAurora.twist(px, pz, 0F, 0F, curl.radius, twist, ax, az);
						final double distance = Math.hypot(moved[0] - px, moved[1] - pz);
						assertTrue(distance <= reach + 1e-3, distance + " past " + reach);
					}
			}
		}
	}

	@Test
	public void motionWrapsAndDoesNotLeapAfterAPause() {
		final CinematicAurora aurora = CinematicAurora.create(7L);
		final CinematicAuroraMotion motion = new CinematicAuroraMotion();
		motion.advance(aurora, 1F, 100F);
		final float before = motion.wave(0);
		// A minute's pause moves it on by no more than MAX_STEP's worth
		motion.advance(aurora, 1F, 160F);
		final CinematicAurora.Ribbon ribbon = aurora.ribbons.get(0);
		final double moved = Math.abs(motion.wave(0) - before);
		final double most = Math.abs(ribbon.waveSpeed) * CinematicAuroraMotion.MAX_STEP + 1e-4;
		assertTrue(moved <= most || moved >= ribbon.waveWavelength - most, "leapt " + moved);
		for (float t = 160F; t < 2000F; t += 0.2F)
			motion.advance(aurora, 5F, t);
		assertTrue(motion.fold(0) >= 0F && motion.fold(0) < 2 * Math.PI + 1e-4);
		assertTrue(motion.wave(0) >= 0F && motion.wave(0) <= ribbon.waveWavelength);
		assertTrue(motion.ray(0) >= 0F && motion.ray(0) <= CinematicAuroraMotion.RAY_WRAP);
	}

	@Test
	public void theNightBreaksUpAndRecovers() {
		AuroraNight busy = null;
		AuroraNight quiet = null;
		for (long night = 0; night < 500 && (busy == null || quiet == null); night++) {
			final AuroraNight n = AuroraNight.of(night, 10);
			if (n.activity == AuroraNight.Activity.QUIET)
				quiet = n;
			else if (n.activity == AuroraNight.Activity.STORM)
				busy = n;
		}
		assertTrue(busy != null && quiet != null);
		// Quiet: no breakup all night, only gathering strength
		assertEquals(0F, quiet.at(0.5F).surge);
		assertTrue(quiet.at(0.9F).gain > quiet.at(0.1F).gain);
		// A storm: calm before, at its height in the middle of its peak, fading after
		assertEquals(0F, busy.at(busy.onset - 0.01F).surge);
		final AuroraNight.Moment height = busy.at(busy.onset + AuroraNight.BREAKUP_LENGTH + busy.peakLength / 2);
		assertEquals(1F, height.surge);
		assertTrue(height.speed > 5F && height.gain > 2F);
		assertTrue(busy.at(1F).surge < 0.5F);
	}

	@Test
	public void theNightRunsFromDuskToDawn() {
		assertEquals(0F, AuroraNight.progressOf(6000L));
		assertEquals(0F, AuroraNight.progressOf(12500L));
		assertEquals(0.5F, AuroraNight.progressOf(18000L), 1e-6);
		assertEquals(1F, AuroraNight.progressOf(23500L));
		assertEquals(0.5F, AuroraNight.progressOf(18000L + 24000L * 5), 1e-6);
	}

	@Test
	public void ribbonsThatArentThereDrawNothing() {
		CinematicAurora aurora = null;
		for (long night = 0; aurora == null || aurora.ribbons.size() > 2; night++)
			aurora = CinematicAurora.create(night);
		final CinematicAuroraMotion motion = new CinematicAuroraMotion();
		motion.advance(aurora, 1F, 0F);
		final CinematicAuroraFrame frame = CinematicAuroraFrame.of(aurora, AuroraNight.of(1L, 10).at(0.5F), motion, 1F,
				1.7F, 0F, 1080);
		assertEquals(aurora.ribbons.size(), (int) frame.header[3]);
		for (int i = aurora.ribbons.size(); i < CinematicAurora.MAX_RIBBONS; i++)
			assertEquals(0F, frame.light[i * 4], "ribbon " + i + " has brightness");
	}
}
