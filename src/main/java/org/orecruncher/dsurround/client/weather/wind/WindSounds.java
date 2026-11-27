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

import net.minecraft.util.math.MathHelper;

/**
 * How loud the wind's loops play for the wind there is, from the 26.x line: biome sounds marked {@code followsWind}
 * (the wind loops) are scaled by this, on top of how much of the area their biome covers; and whether the wind is a
 * storm's, which brings a wind loop to biomes that have none of their own ({@code weather.isStormWind}).
 */
public final class WindSounds {

	/**
	 * In a calm, a share of the loop's volume: a windy biome still sounds windy between storms
	 */
	static final float CALM_FLOOR = 0.4F;
	/**
	 * Beyond a full storm's wind (thunderstorms, dust storms), how much louder it gets for each further full storm's
	 * worth, and the most it gets
	 */
	static final float BEYOND_RATE = 0.3F;
	static final float LOUDEST = 1.3F;
	/**
	 * Under cover, out of the wind, a share of the volume: still heard through the walls in a gale
	 */
	static final float SHELTERED = 0.5F;
	/**
	 * The steady wind, as a percent of a full storm's, from which it is a storm wind, and below which it no longer is
	 * (apart, so it doesn't flicker)
	 */
	static final float STORM_WIND_FROM = 70F;
	static final float STORM_WIND_UNTIL = 55F;
	/**
	 * The steady wind from which it is windy
	 */
	static final float WINDY_FROM = 50F;

	private WindSounds() {
	}

	/**
	 * The volume scale for wind loops
	 *
	 * @param speed    the wind's speed where the player is, gusts included, in blocks a tick
	 * @param exposure how open to the sky the player is, 0 to 1, as {@link Shelter#exposure} gives it
	 */
	public static float loopScale(final float speed, final float exposure) {
		final float share = Math.max(speed, 0F) / Wind.speedOfPercent(100);
		final float scale = share <= 1F ? CALM_FLOOR + (1F - CALM_FLOOR) * share
				: Math.min(1F + (share - 1F) * BEYOND_RATE, LOUDEST);
		return scale * (SHELTERED + (1F - SHELTERED) * MathHelper.clamp(exposure, 0F, 1F));
	}

	/**
	 * Whether it is a storm wind now
	 *
	 * @param percent the steady wind, as a percent of a full storm's
	 * @param was     whether it was a storm wind
	 */
	public static boolean isStormWind(final float percent, final boolean was) {
		return was ? percent >= STORM_WIND_UNTIL : percent >= STORM_WIND_FROM;
	}

	public static boolean isWindy(final float percent) {
		return percent >= WINDY_FROM;
	}
}
