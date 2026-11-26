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
 * Where each cinematic ribbon's folds, wave, rays and curl have moved to, frame by frame. Their speeds change through
 * the night, so each frame moves them on by how far they go in the time since the last rather than working it out from
 * the time and a speed, which would leap when the speed changed. Each is kept within the length its pattern repeats
 * at, so it stays precise as a float. Ported from the 26.x line.
 */
public final class CinematicAuroraMotion {

	/**
	 * The length the rays' pattern repeats at along a ribbon, in kilometers: both of the shader's ray noises repeat
	 * within it (the noise texture's size of cells of FINE_RAY and COARSE_RAY kilometers)
	 */
	static final double RAY_WRAP = AuroraRenderer.NOISE_SIZE * 12D;
	/**
	 * The most time moved on in one frame, in seconds: after a pause or a stall they carry on from where they were
	 */
	static final float MAX_STEP = 0.25F;
	private static final double TWO_PI = 2 * Math.PI;

	private final float[] fold = new float[CinematicAurora.MAX_RIBBONS];
	private final float[] wave = new float[CinematicAurora.MAX_RIBBONS];
	private final float[] ray = new float[CinematicAurora.MAX_RIBBONS];
	private final float[] curl = new float[CinematicAurora.MAX_RIBBONS];
	private float lastTime = Float.NaN;
	// The ribbons they are moving for: others start over
	private List<CinematicAurora.Ribbon> ribbons;

	/**
	 * Moves them on to {@code time}
	 *
	 * @param speed how much faster than when calm they move now
	 * @param time  seconds
	 */
	public void advance(@Nonnull final CinematicAurora aurora, final float speed, final float time) {
		final List<CinematicAurora.Ribbon> list = aurora.ribbons;
		final int n = Math.min(list.size(), CinematicAurora.MAX_RIBBONS);
		if (list != this.ribbons) {
			this.ribbons = list;
			for (int i = 0; i < n; i++) {
				final CinematicAurora.Ribbon ribbon = list.get(i);
				this.fold[i] = wrap(ribbon.foldPhase, TWO_PI);
				this.wave[i] = 0F;
				this.ray[i] = wrap(-ribbon.rayOffset, RAY_WRAP);
				this.curl[i] = wrap(ribbon.curl.phase, TWO_PI);
			}
			this.lastTime = time;
		}
		final float step = Math.max(0F, Math.min(MAX_STEP, time - this.lastTime));
		this.lastTime = time;
		for (int i = 0; i < n; i++) {
			final CinematicAurora.Ribbon ribbon = list.get(i);
			this.fold[i] = wrap(this.fold[i] + (double) ribbon.foldSpeed * speed * step, TWO_PI);
			this.wave[i] = wrap(this.wave[i] + (double) ribbon.waveSpeed * speed * step, ribbon.waveWavelength);
			this.ray[i] = wrap(this.ray[i] + (double) ribbon.raySpeed * speed * step, RAY_WRAP);
			this.curl[i] = wrap(this.curl[i] + (double) ribbon.curl.speed * speed * step, TWO_PI);
		}
	}

	/**
	 * Where along their cycle ribbon i's folds are, in radians, within a turn
	 */
	public float fold(final int i) {
		return this.fold[i];
	}

	/**
	 * How far ribbon i's wave has traveled along it, in kilometers, within its wavelength
	 */
	public float wave(final int i) {
		return this.wave[i];
	}

	/**
	 * How far ribbon i's rays have moved along it, in kilometers, within {@link #RAY_WRAP}
	 */
	public float ray(final int i) {
		return this.ray[i];
	}

	/**
	 * Where ribbon i's curl's breathing is, in radians, within a turn
	 */
	public float curl(final int i) {
		return this.curl[i];
	}

	private static float wrap(final double value, final double length) {
		final double wrapped = value % length;
		return (float) (wrapped < 0D ? wrapped + length : wrapped);
	}
}
