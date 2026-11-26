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

/**
 * Lets tests set a wind. Ported from the 26.x line's tests.
 */
public final class WindAccess {

	private WindAccess() {
	}

	/**
	 * A steady wind blowing toward {@code angle}, in radians (0 is east, toward positive x), at {@code speed}, with no
	 * gust
	 */
	public static void set(final Wind wind, final float angle, final float speed) {
		wind.set(angle, speed, 1);
		wind.gust(0, 0F);
	}

	/**
	 * A steady wind as {@link #set}, keeping any gust under way
	 */
	public static void setSteady(final Wind wind, final float angle, final float speed) {
		wind.set(angle, speed, 1);
	}

	/**
	 * Starts a gust lasting {@code ticks}, {@code power} stronger than the steady wind at its height; 0 ticks ends any
	 */
	public static void gust(final Wind wind, final int ticks, final float power) {
		wind.gust(ticks, power);
	}

	/**
	 * The strongest steady wind there is: a thunderstorm's, at full strength, raising dust
	 */
	public static float strongest() {
		return Wind.MAX_SPEED * Wind.DUST_FACTOR * Wind.THUNDER_FACTOR;
	}
}
