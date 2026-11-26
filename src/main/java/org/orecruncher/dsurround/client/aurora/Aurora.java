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

import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.lib.random.XorShiftRandom;

/**
 * One night's aurora: its colors and the shape of its curtains. Everything is drawn from the night's seed, so everyone
 * in a world sees the same aurora on the same night.
 * <p>
 * Curtains are laid out around the viewer, who is at the origin, in "sky units": a curtain's lower edge is about one
 * unit up and it stands one to a bit over one unit tall. Each one follows a path across the sky, square to a heading,
 * with a slow fold along it and a ripple that travels along it. The renderer scales this up to just inside where the
 * sky is drawn ({@link #SCALE}). Ported from the 26.x line.
 */
public final class Aurora {

	/**
	 * From sky units to blocks. The farthest a curtain can reach is a little under {@link #MAX_REACH} units.
	 */
	public static final float SCALE = 11F;
	public static final float MAX_REACH = 95F / SCALE;
	/**
	 * How bright a night's aurora is, at most: one of these. They are the original Dynamic Surroundings aurora's levels
	 * (its presets' alpha limits of 64, 80 and 96, doubled by its shader, over 255).
	 */
	static final float[] BRIGHTNESS_LEVELS = { 0.50F, 0.63F, 0.75F };
	// Curtain layout. Auroras lie toward the pole, so a curtain is always somewhere to the north
	public static final float MAX_HEADING = (float) Math.toRadians(30);
	private static final float BAND_SPACING = 0.6F;
	private static final float BAND_RISE = 0.1F;
	private static final float BAND_SHRINK = 0.15F;
	private static final float LEAN = 0.25F;
	// The ripple that travels along a curtain
	private static final float RIPPLE_AMOUNT = 0.1F;
	private static final float RIPPLE_WAVELENGTH = 2.5F;
	private static final float RIPPLE_SPEED = 0.5F;
	// How quickly the folds drift
	private static final float FOLD_DRIFT = 0.04F;
	private static final float PI = (float) Math.PI;
	private static final float TWO_PI = 2F * PI;

	public final long seed;
	public final AuroraPalette palette;
	// Which way it lies from the viewer, in radians: 0 is north (toward -Z), never more than MAX_HEADING either side
	public final float heading;
	// How far its path is from the viewer, how long it is, how high its lower edge is, and how tall the first curtain
	public final float distance;
	public final float length;
	public final float altitude;
	public final float height;
	// How many curtains, side by side (1 to 3)
	public final int bands;
	// How far the path folds in and out, and how many folds along it
	public final float foldAmount;
	public final float foldFrequency;
	// Where along their cycle the folds and ripples start
	public final float phase;
	// How bright it is, at most: one of BRIGHTNESS_LEVELS
	public final float brightness;
	// Where in the shader's pattern it starts, so each night's rays differ
	public final float textureOffset;

	private Aurora(final long seed, @Nonnull final Random random, final int maxBands) {
		this.seed = seed;
		this.palette = AuroraPalette.PALETTES.get(random.nextInt(AuroraPalette.PALETTES.size()));
		this.heading = range(random, -MAX_HEADING, MAX_HEADING);
		this.distance = range(random, 1.5F, 4F);
		this.length = range(random, 6F, 10F);
		this.altitude = range(random, 0.8F, 1.2F);
		this.height = range(random, 0.8F, 1.4F);
		this.bands = 1 + random.nextInt(Math.max(1, Math.min(3, maxBands)));
		this.foldAmount = range(random, 0.15F, 0.45F);
		this.foldFrequency = range(random, 0.75F, 1.75F);
		this.phase = random.nextFloat() * TWO_PI;
		this.brightness = BRIGHTNESS_LEVELS[random.nextInt(BRIGHTNESS_LEVELS.length)];
		this.textureOffset = random.nextFloat() * 1000F;
	}

	private static float range(@Nonnull final Random random, final float low, final float high) {
		return low + random.nextFloat() * (high - low);
	}

	/**
	 * The aurora for the given seed (the night's)
	 *
	 * @param maxBands the most curtains it may have, 1 to 3
	 */
	@Nonnull
	public static Aurora create(final long seed, final int maxBands) {
		return new Aurora(seed, new XorShiftRandom(mix(seed)), maxBands);
	}

	// SplitMix64's finalizer: nearby seeds (the next night's) give unrelated shapes
	static long mix(final long seed) {
		long z = seed + 0x9E3779B97F4A7C15L;
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}

	/**
	 * Where a curtain's lower edge is, in sky units, written into {@code out} (x, y, z)
	 *
	 * @param band which curtain, from 0 (nearest)
	 * @param s    how far along it, -1 (one end) to 1 (the other)
	 * @param time seconds, for the drifting folds and the traveling ripple
	 */
	public void bottom(final int band, final float s, final float time, @Nonnull final float[] out) {
		final float along = s * this.length * 0.5F;
		final float bandPhase = this.phase + band * 1.7F;
		final float fold = this.foldAmount
				* (float) Math.sin(s * PI * this.foldFrequency + bandPhase + time * FOLD_DRIFT);
		final float ripple = RIPPLE_AMOUNT
				* (float) Math.sin(along * TWO_PI / RIPPLE_WAVELENGTH - time * RIPPLE_SPEED + bandPhase);
		final float offset = this.distance + band * BAND_SPACING + fold + ripple;
		place(along, offset, this.altitude + band * BAND_RISE, out);
	}

	/**
	 * Where a curtain's top is, given where its lower edge is: straight up, leaning a little away from the viewer
	 */
	public void top(final int band, @Nonnull final float[] bottom, @Nonnull final float[] out) {
		final float h = bandHeight(band);
		final float lean = h * LEAN;
		out[0] = bottom[0] + outwardX() * lean;
		out[1] = bottom[1] + h;
		out[2] = bottom[2] + outwardZ() * lean;
	}

	public float bandHeight(final int band) {
		return this.height * (1F - band * BAND_SHRINK);
	}

	/**
	 * How bright each curtain is relative to the first
	 */
	public float bandBrightness(final int band) {
		return 1F - band * 0.2F;
	}

	/**
	 * How much a curtain fades toward its ends: 1 for most of it, falling to 0 at the very end
	 */
	public static float taper(final float s) {
		final float t = Math.max(0F, Math.min(1F, (Math.abs(s) - 0.5F) / 0.5F));
		return 1F - t * t * (3F - 2F * t);
	}

	/**
	 * Distance along a curtain, for the shader's pattern: in sky units, and different for each curtain and night
	 */
	public float textureU(final int band, final float s) {
		return this.textureOffset + band * 37F + s * this.length * 0.5F;
	}

	private void place(final float along, final float outward, final float y, @Nonnull final float[] out) {
		// Outward is the heading; along is square to it
		final float ox = outwardX();
		final float oz = outwardZ();
		out[0] = ox * outward - oz * along;
		out[1] = y;
		out[2] = oz * outward + ox * along;
	}

	private float outwardX() {
		return (float) Math.sin(this.heading);
	}

	private float outwardZ() {
		return (float) -Math.cos(this.heading);
	}
}
