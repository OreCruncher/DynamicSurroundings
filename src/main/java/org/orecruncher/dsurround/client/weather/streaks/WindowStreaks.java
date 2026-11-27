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

package org.orecruncher.dsurround.client.weather.streaks;

import java.util.Random;

import net.minecraft.util.math.MathHelper;

/**
 * Rain drops running down windows (ported from the 26.x line): each a bead on the outside of the glass that clings for
 * a moment, then runs down in a short rush, then clings again, as real drops do, leaving a wet trail that dries from
 * the top down. Where the glass ends it goes: over open air it drips off the edge, and onto a frame, a sill or the wall
 * below it runs off and fades. Kept in flat arrays; drops added when it is full are left out. Client thread only.
 */
public final class WindowStreaks {

	/**
	 * Told where a drop runs off the bottom edge of a window over open air, to hang a drip there
	 */
	@FunctionalInterface
	public interface Dripper {
		void drip(double x, double y, double z);
	}

	// The side of the glass a drop is on: which way it looks
	public static final byte FACE_WEST = 1;
	public static final byte FACE_EAST = 2;
	public static final byte FACE_NORTH = 3;
	public static final byte FACE_SOUTH = 4;

	// Ticks a drop lasts at most; how long it takes to fade in, and to fade out once it has run off onto what is below
	static final int MAX_LIFETIME = 300;
	static final int FADE_IN = 3;
	static final int FADE_OUT = 10;
	// Ticks a new drop clings before it first runs: the largest drop the least, the smallest about twice that
	static final int FIRST_PAUSE_MIN = 10;
	static final int FIRST_PAUSE_RANGE = 30;
	// Ticks it clings between runs, and how far each run goes, in blocks
	static final int PAUSE_MIN = 4;
	static final int PAUSE_RANGE = 26;
	static final float RUN_MIN = 0.08F;
	static final float RUN_RANGE = 0.4F;
	// How fast it runs, in blocks a tick
	static final float RUN_SPEED = 0.08F;
	// How fast its trail dries, from the top down, in blocks a tick
	static final float TRAIL_DRY = 0.01F;
	// Just off the glass, and a little apart from each other, so neither the glass nor other drops show through
	static final float LIFT = 0.008F;
	static final float LIFT_RANGE = 0.004F;

	private final int capacity;
	// Where the bead is: x and z on the glass (lifted off it), y now and last tick; and the lowest it can go
	final double[] x, z;
	final float[] y, prevY, bottom;
	// The top of its wet trail, now and last tick
	final float[] trailTop, prevTrailTop;
	// How far it has left to go in this run, and the ticks it has left to cling before the next
	final float[] runLeft;
	final int[] pause;
	// How big the drop was, 0 to 1
	final float[] size;
	final int[] age, lifetime, light;
	// The side of the glass it is on (see FACE_WEST)
	final byte[] face;
	// Whether there is open air under the glass's bottom edge, so it drips off there
	final boolean[] dripsOff;
	int count;

	public WindowStreaks(final int capacity) {
		this.capacity = capacity;
		this.x = new double[capacity];
		this.z = new double[capacity];
		this.y = new float[capacity];
		this.prevY = new float[capacity];
		this.bottom = new float[capacity];
		this.trailTop = new float[capacity];
		this.prevTrailTop = new float[capacity];
		this.runLeft = new float[capacity];
		this.pause = new int[capacity];
		this.size = new float[capacity];
		this.age = new int[capacity];
		this.lifetime = new int[capacity];
		this.light = new int[capacity];
		this.face = new byte[capacity];
		this.dripsOff = new boolean[capacity];
	}

	public int count() {
		return this.count;
	}

	public void clear() {
		this.count = 0;
	}

	/**
	 * Which way a face looks along x: -1 for west, 1 for east, otherwise 0
	 */
	static int normalX(final byte face) {
		return face == FACE_WEST ? -1 : face == FACE_EAST ? 1 : 0;
	}

	/**
	 * Which way a face looks along z: -1 for north, 1 for south, otherwise 0
	 */
	static int normalZ(final byte face) {
		return face == FACE_NORTH ? -1 : face == FACE_SOUTH ? 1 : 0;
	}

	/**
	 * Adds a drop on a window
	 *
	 * @param x        where on the glass: the side is at {@code x} (west or east) or {@code z} (north or south)
	 * @param face     the side of the glass it is on (see {@link #FACE_WEST})
	 * @param bottom   the lowest it can run to: where the glass ends
	 * @param dripsOff whether there is open air under that edge, so it drips off there rather than running onto
	 *                 whatever is below
	 * @param size     how big the drop was, 0 to 1
	 * @param light    its packed light
	 */
	public void add(final double x, final double y, final double z, final byte face, final double bottom,
			final boolean dripsOff, final float size, final int light, final Random random) {
		if (this.count == this.capacity)
			return;
		final int i = this.count++;
		final float lift = LIFT + random.nextFloat() * LIFT_RANGE;
		this.x[i] = x + normalX(face) * lift;
		this.z[i] = z + normalZ(face) * lift;
		this.y[i] = this.prevY[i] = (float) y;
		this.trailTop[i] = this.prevTrailTop[i] = (float) y;
		this.bottom[i] = (float) Math.min(bottom, y);
		this.face[i] = face;
		this.dripsOff[i] = dripsOff;
		this.size[i] = MathHelper.clamp(size, 0F, 1F);
		this.pause[i] = FIRST_PAUSE_MIN + (int) ((1F - this.size[i]) * FIRST_PAUSE_MIN)
				+ random.nextInt(FIRST_PAUSE_RANGE);
		this.runLeft[i] = RUN_MIN + random.nextFloat() * RUN_RANGE;
		this.age[i] = 0;
		this.lifetime[i] = MAX_LIFETIME;
		this.light[i] = light;
	}

	/**
	 * Moves the drops a tick: those clinging wait, and those running run. One reaching the bottom edge of its window
	 * drips off it, if there is open air below, and is gone; otherwise it runs off onto what is below, and fades.
	 * Their trails dry.
	 *
	 * @param dripper told where drops drip off
	 */
	public void tick(final Random random, final Dripper dripper) {
		int i = 0;
		while (i < this.count) {
			if (++this.age[i] >= this.lifetime[i]) {
				remove(i);
				continue;
			}
			this.prevY[i] = this.y[i];
			this.prevTrailTop[i] = this.trailTop[i];

			if (this.y[i] <= this.bottom[i]) {
				if (this.dripsOff[i]) {
					// Off the edge: a drip hangs there, and falls
					dripper.drip(this.x[i], this.bottom[i], this.z[i]);
					remove(i);
					continue;
				}
				// Onto the frame, the sill or the wall: it runs off, and fades
				this.lifetime[i] = Math.min(this.lifetime[i], this.age[i] + FADE_OUT);
			} else if (this.pause[i] > 0) {
				this.pause[i]--;
			} else {
				final float step = Math.min(RUN_SPEED, this.runLeft[i]);
				this.y[i] = Math.max(this.y[i] - step, this.bottom[i]);
				this.runLeft[i] -= step;
				if (this.runLeft[i] <= 0F) {
					this.pause[i] = PAUSE_MIN + random.nextInt(PAUSE_RANGE);
					this.runLeft[i] = RUN_MIN + random.nextFloat() * RUN_RANGE;
				}
			}
			this.trailTop[i] = Math.max(this.y[i], this.trailTop[i] - TRAIL_DRY);
			i++;
		}
	}

	// Moves the last drop into the gap, so the drops stay packed at the front of the arrays
	private void remove(final int i) {
		final int last = --this.count;
		this.x[i] = this.x[last];
		this.z[i] = this.z[last];
		this.y[i] = this.y[last];
		this.prevY[i] = this.prevY[last];
		this.bottom[i] = this.bottom[last];
		this.trailTop[i] = this.trailTop[last];
		this.prevTrailTop[i] = this.prevTrailTop[last];
		this.runLeft[i] = this.runLeft[last];
		this.pause[i] = this.pause[last];
		this.size[i] = this.size[last];
		this.age[i] = this.age[last];
		this.lifetime[i] = this.lifetime[last];
		this.light[i] = this.light[last];
		this.face[i] = this.face[last];
		this.dripsOff[i] = this.dripsOff[last];
	}

	/**
	 * How much of a drop is shown, 0 to 1: it fades in when new, and out at the end of its life
	 */
	float fade(final int i, final float partialTick) {
		return fadeFor(this.age[i] + partialTick, this.lifetime[i]);
	}

	static float fadeFor(final float age, final int lifetime) {
		return MathHelper.clamp(Math.min(age / FADE_IN, (lifetime - age) / FADE_OUT), 0F, 1F);
	}
}
