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

package org.orecruncher.dsurround.client.weather;

/**
 * Where around the player a rain sound plays. Vanilla, and this mod until now, played it on one of the rain splashes
 * scattered around the player that tick, each at {@code nextInt(r) - nextInt(r)} blocks off on each axis: weighted to
 * the middle on each axis, but as the ring of ground grows with the distance, few sounds came close (8% within 2
 * blocks, 26% within 4, half past 6). A surface the player stands on, under only part of the rain about, got few of
 * them, and its own rain, a clip with each, came and went. Here a sound's spot is picked on its own: any way round, out
 * to the range, but mostly near ({@link #distance}).
 */
public final class RainSoundSpots {

	/**
	 * How many columns are tried for a sound's spot before it falls back to a splash's: a column whose rain lands out
	 * of the range up or down (a cliff, a cave's roof) is passed over
	 */
	public static final int TRIES = 4;

	private RainSoundSpots() {
	}

	/**
	 * How far from the player a sound plays, horizontally, from a roll of 0 to 1: {@code range * roll^2}, so with a
	 * range of 12, 41% within 2 blocks, 58% within 4, 71% within 6 and 82% within 8, half within 3
	 */
	public static double distance(final double roll, final int range) {
		return range * roll * roll;
	}
}
