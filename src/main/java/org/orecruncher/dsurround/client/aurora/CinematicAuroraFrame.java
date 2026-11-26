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

import java.util.List;

import javax.annotation.Nonnull;

/**
 * The cinematic aurora's shader values for a frame (aurora_cinematic.frag), worked out with no GPU in sight:
 * {@link CinematicAuroraRenderer} only uploads them. {@link #header} holds Frame, Look and Edge, a vec4 each; each of
 * the five ribbon arrays holds a vec4 for each of {@link CinematicAurora#MAX_RIBBONS} ribbons, with values that draw
 * nothing for ribbons there aren't. Ported from the 26.x line.
 */
public final class CinematicAuroraFrame {

	// How strong the violet toward the ribbons' tops is on a calm night, and how much more at the height of a breakup
	static final float BASE_VIOLET = 0.5F;
	static final float SURGE_VIOLET = 0.5F;
	// How far the night's activity is flattened (see activityGain): 1 not at all, 0 entirely
	static final float ACTIVITY_CURVE = 0.35F;

	/**
	 * Frame (seconds, strength 0 to 1, radians across a pixel up and down the screen at its middle, ribbon count),
	 * Look (ray contrast, violet, unused, unused) and Edge (the lower edge's height, unused...), four floats each
	 */
	public final float[] header = new float[12];
	// Each ribbon's line, folds, wave and sheet, light, and curl: four floats a ribbon
	public final float[] axis = new float[CinematicAurora.MAX_RIBBONS * 4];
	public final float[] fold = new float[CinematicAurora.MAX_RIBBONS * 4];
	public final float[] wave = new float[CinematicAurora.MAX_RIBBONS * 4];
	public final float[] light = new float[CinematicAurora.MAX_RIBBONS * 4];
	public final float[] curl = new float[CinematicAurora.MAX_RIBBONS * 4];

	/**
	 * How much brighter a ribbon is for the night's activity: the gain, flattened (to the power
	 * {@link #ACTIVITY_CURVE}), so a storm stays restrained and an ordinary night still shows
	 */
	static float activityGain(final float gain) {
		return (float) Math.pow(Math.max(gain, 0F), ACTIVITY_CURVE);
	}

	/**
	 * @param moment     how the night is (how active)
	 * @param motion     where the ribbons' folds, wave and rays have moved to
	 * @param alpha      how strongly, 0 to 1
	 * @param projection11 the projection matrix's (1,1): how much it scales up and down the screen
	 * @param time       seconds, for the animation
	 * @param height     the height in pixels of what it is drawn to
	 */
	@Nonnull
	public static CinematicAuroraFrame of(@Nonnull final CinematicAurora aurora, @Nonnull final AuroraNight.Moment moment,
			@Nonnull final CinematicAuroraMotion motion, final float alpha, final float projection11, final float time,
			final int height) {
		final CinematicAuroraFrame f = new CinematicAuroraFrame();
		final List<CinematicAurora.Ribbon> ribbons = aurora.ribbons;
		final int count = Math.min(ribbons.size(), CinematicAurora.MAX_RIBBONS);
		final float pixelAngle = 2F / (projection11 * height);
		final float surge = Math.max(0F, Math.min(1F, moment.surge));
		put(f.header, 0, time, Math.max(0F, Math.min(1F, alpha)), pixelAngle, count);
		put(f.header, 1, moment.rayContrast, BASE_VIOLET + SURGE_VIOLET * surge, 0F, 0F);
		put(f.header, 2, CinematicAurora.lowerEdgeAt(surge), 0F, 0F, 0F);
		for (int i = 0; i < CinematicAurora.MAX_RIBBONS; i++) {
			if (i < count) {
				final CinematicAurora.Ribbon ribbon = ribbons.get(i);
				final float cos = (float) Math.cos(ribbon.heading);
				final float sin = (float) Math.sin(ribbon.heading);
				final float distance = CinematicAurora.distanceAt(ribbon, surge);
				put(f.axis, i, cos, sin, distance, ribbon.bow);
				put(f.fold, i, ribbon.foldAmplitude, ribbon.foldWavelength, motion.fold(i), ribbon.waveAmplitude);
				put(f.wave, i, ribbon.waveWavelength, motion.wave(i), motion.ray(i), ribbon.thickness);
				put(f.light, i, ribbon.brightness * activityGain(moment.gain), ribbon.height, ribbon.raySharpness,
						ribbon.rayOffset);
				// The curl's middle on the ground, from where it is on the ribbon's line now (folds, wave and all)
				final CinematicAurora.Curl c = ribbon.curl;
				final float across = ribbon.centerAt(c.along, distance, motion.fold(i), motion.wave(i)) + c.offset;
				put(f.curl, i, c.along * cos - across * sin, c.along * sin + across * cos, c.radius,
						c.twistAt(motion.curl(i), surge));
			} else {
				put(f.axis, i, 1F, 0F, 0F, 0F);
				put(f.fold, i, 0F, 1F, 0F, 0F);
				put(f.wave, i, 1F, 0F, 0F, 1F);
				put(f.light, i, 0F, 1F, 0F, 0F);
				put(f.curl, i, 0F, 0F, 1F, 0F);
			}
		}
		return f;
	}

	private static void put(@Nonnull final float[] into, final int vector, final float x, final float y, final float z,
			final float w) {
		into[vector * 4] = x;
		into[vector * 4 + 1] = y;
		into[vector * 4 + 2] = z;
		into[vector * 4 + 3] = w;
	}
}
