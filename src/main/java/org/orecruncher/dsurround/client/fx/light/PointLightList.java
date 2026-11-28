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

package org.orecruncher.dsurround.client.fx.light;

import java.util.Arrays;

import javax.annotation.Nonnull;

/**
 * The point lights recorded for a frame, and which of them are drawn: the nearest, the furthest of those fading out
 * (from the 26.x line's PointLights). No game in it; {@link PointLights} draws them.
 */
public final class PointLightList {

	/**
	 * The most lights drawn: the nearest, which are the brightest on screen and cost the most to draw (each is a square
	 * over the pixels it may light, the whole screen when the camera is inside its reach). A field full of fireflies
	 * could otherwise draw hundreds.
	 */
	static final int MAX_DRAWN = 64;
	/**
	 * From this far down the nearest, the lights fade, to nothing at {@link #MAX_DRAWN}: one moving across that limit
	 * fades in or out rather than switching.
	 */
	static final int FADE_FROM = 48;
	// A light this faint changes no pixel: under half a step of an 8 bit color, at its brightest
	static final float MIN_STRENGTH = 0.5F / 255F;
	// x, y, z (relative to the camera: small enough for a float wherever the world is), red, green, blue, strength,
	// radius
	static final int STRIDE = 8;

	float[] lights = new float[STRIDE * 64];
	int count;
	// For sorting the lights nearest first: each one's distance and index, and the lights in that order
	private long[] sortKeys = new long[64];
	private float[] sorted = new float[STRIDE * 64];

	/**
	 * Records a light, its position relative to the camera.
	 *
	 * @param strength 0 to 1
	 * @param radius   how far it reaches, in blocks
	 */
	public void add(final float x, final float y, final float z, final float red, final float green, final float blue,
			final float strength, final float radius) {
		if (strength < MIN_STRENGTH || radius <= 0F)
			return;
		if ((this.count + 1) * STRIDE > this.lights.length)
			this.lights = Arrays.copyOf(this.lights, this.lights.length * 2);
		final int i = this.count * STRIDE;
		this.lights[i] = x;
		this.lights[i + 1] = y;
		this.lights[i + 2] = z;
		this.lights[i + 3] = red;
		this.lights[i + 4] = green;
		this.lights[i + 5] = blue;
		this.lights[i + 6] = strength;
		this.lights[i + 7] = radius;
		this.count++;
	}

	public int size() {
		return this.count;
	}

	public void clear() {
		this.count = 0;
	}

	/**
	 * Puts the lights in order of nearness to the camera when there are enough for some to fade or be left out, and
	 * says how many are drawn
	 */
	int prepare() {
		if (this.count > FADE_FROM)
			sortNearest(this.lights, this.count, 0D, 0D, 0D);
		return Math.min(this.count, MAX_DRAWN);
	}

	/**
	 * How much the light {@code rank} lights down the nearest is drawn at: in full up to {@link #FADE_FROM}, fading
	 * to nothing at {@link #MAX_DRAWN}.
	 */
	static float rankFade(final int rank) {
		if (rank < FADE_FROM)
			return 1F;
		return Math.max(0F, (float) (MAX_DRAWN - rank) / (MAX_DRAWN - FADE_FROM));
	}

	/**
	 * Puts the first {@code n} lights in {@code lights} in order of how near each is to the camera, nearest first.
	 */
	void sortNearest(@Nonnull final float[] lights, final int n, final double cameraX, final double cameraY,
			final double cameraZ) {
		if (this.sortKeys.length < n)
			this.sortKeys = new long[lights.length / STRIDE];
		if (this.sorted.length < n * STRIDE)
			this.sorted = new float[lights.length];
		// Each light's distance squared, then its index: a float's bits order as it does when it isn't negative
		for (int l = 0; l < n; l++) {
			final int i = l * STRIDE;
			final double x = lights[i] - cameraX, y = lights[i + 1] - cameraY, z = lights[i + 2] - cameraZ;
			this.sortKeys[l] = ((long) Float.floatToIntBits((float) (x * x + y * y + z * z)) << 32) | l;
		}
		Arrays.sort(this.sortKeys, 0, n);
		for (int r = 0; r < n; r++)
			System.arraycopy(lights, (int) this.sortKeys[r] * STRIDE, this.sorted, r * STRIDE, STRIDE);
		System.arraycopy(this.sorted, 0, lights, 0, n * STRIDE);
	}
}
