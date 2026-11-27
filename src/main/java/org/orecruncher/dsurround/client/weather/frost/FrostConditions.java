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

package org.orecruncher.dsurround.client.weather.frost;

import javax.annotation.Nonnull;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

/**
 * How much frost a window has, 0 to 1. Ported from the 26.x line. Frost follows the cold: none at a temperature of
 * {@link #NO_FROST_TEMPERATURE} and above, as much as the cold allows at {@link #FULL_FROST_TEMPERATURE} and below (a
 * snowy plain). It is thickest just before sunrise and thinnest in the afternoon; in the bitter cold it lasts all day,
 * in a cool biome only the night. The sun melts it where it shines on the glass: in Minecraft the sun crosses the sky
 * from east to west, so windows facing east clear in the morning and those facing west in the evening, while north and
 * south windows keep theirs. Glass under cover, out of the open sky, frosts less. What is shown eases toward that over
 * minutes, as frost creeps in and melts away.
 */
public final class FrostConditions {

	private static final float TWO_PI = (float) (Math.PI * 2D);

	// The temperatures, as the game's (a snowy plain is 0, a plain 0.8), between which frost comes on
	static final float NO_FROST_TEMPERATURE = 0.3F;
	static final float FULL_FROST_TEMPERATURE = 0.0F;
	// The share of the night's frost that is left in the afternoon, at its warmest, in the bitter cold
	static final float AFTERNOON_SHARE = 0.6F;
	// From this coldness some of the night's frost lasts through the day, the share growing up to AFTERNOON_SHARE at
	// BITTER_COLD (a cold taiga, or high in the mountains)
	static final float DAY_COLDNESS = 0.5F;
	static final float BITTER_COLD = 1.6F;
	// The coldest time of day, as the game's celestial angle (0 is noon, 0.75 sunrise): just before sunrise
	static final float COLDEST = 0.72F;
	// How much the sun shining straight onto the glass takes off, in clear weather
	static final float SUN_MELT = 0.9F;
	// Glass with no sky light in front of it frosts this share as much as glass in the open
	static final float SHELTERED_SHARE = 0.5F;
	// How fast what is shown moves toward what it should be, per tick: frost creeps in over a couple of minutes, and
	// melts in about one
	static final float GROW_PER_TICK = 1F / 2400F;
	static final float MELT_PER_TICK = 1F / 1200F;

	private FrostConditions() {
	}

	/**
	 * How cold it is for frost, 0 to 1 and beyond: 0 at {@link #NO_FROST_TEMPERATURE} and above, 1 at
	 * {@link #FULL_FROST_TEMPERATURE}, more for colder
	 */
	public static float coldness(final float temperature) {
		return Math.max(0F, (NO_FROST_TEMPERATURE - temperature) / (NO_FROST_TEMPERATURE - FULL_FROST_TEMPERATURE));
	}

	/**
	 * How much the time of day favors frost, 0 to 1: 1 just before sunrise, 0 in the afternoon
	 *
	 * @param timeOfDay the game's celestial angle, 0 to 1, with 0 at noon
	 */
	static float night(final float timeOfDay) {
		return 0.5F + 0.5F * MathHelper.cos(TWO_PI * (timeOfDay - COLDEST));
	}

	/**
	 * The sun's direction, as a unit vector's x and y: it rises in the east (positive x), is overhead at noon, and sets
	 * in the west; under the horizon at night. There is no z: it never leans north or south.
	 *
	 * @param timeOfDay the game's celestial angle, 0 to 1, with 0 at noon
	 */
	static float sunX(final float timeOfDay) {
		return -MathHelper.sin(TWO_PI * timeOfDay);
	}

	static float sunY(final float timeOfDay) {
		return MathHelper.cos(TWO_PI * timeOfDay);
	}

	/**
	 * How much the sun shines onto a window's side, 0 to 1: straight onto it is 1; on a side looking north or south, or
	 * at night, none
	 *
	 * @param face the side of the glass, which way it looks
	 */
	static float sunOn(@Nonnull final EnumFacing face, final float timeOfDay) {
		if (sunY(timeOfDay) <= 0F)
			return 0F;
		return Math.max(0F, sunX(timeOfDay) * face.getFrontOffsetX());
	}

	/**
	 * How much frost a side of a window should have, 0 to 1
	 *
	 * @param timeOfDay the game's celestial angle, 0 to 1, with 0 at noon
	 * @param clearSky  how clear the sky is, 0 to 1
	 */
	public static float target(@Nonnull final FrostFace face, final float timeOfDay, final float clearSky) {
		return target(face.temperature, face.side, face.skyShare, timeOfDay, clearSky);
	}

	/**
	 * How much frost a window should have, 0 to 1
	 *
	 * @param temperature the temperature in front of it
	 * @param face        the side of the glass, which way it looks
	 * @param skyShare    the sky light in front of it, 0 to 1: glass under cover frosts less, and the sun doesn't reach
	 *                    it
	 * @param timeOfDay   the game's celestial angle, 0 to 1, with 0 at noon
	 * @param clearSky    how clear the sky is, 0 to 1: the sun melts less through cloud
	 */
	static float target(final float temperature, @Nonnull final EnumFacing face, final float skyShare,
			final float timeOfDay, final float clearSky) {
		final float coldness = coldness(temperature);
		if (coldness <= 0F)
			return 0F;
		// The colder, the more of the night's frost lasts through the day
		final float dayShare = MathHelper.clamp((coldness - DAY_COLDNESS) / (BITTER_COLD - DAY_COLDNESS), 0F, 1F)
				* AFTERNOON_SHARE;
		final float time = lerp(night(timeOfDay), dayShare, 1F);
		float frost = Math.min(coldness, 1F) * time * lerp(skyShare, SHELTERED_SHARE, 1F);
		frost *= 1F - SUN_MELT * sunOn(face, timeOfDay) * clearSky * skyShare;
		return MathHelper.clamp(frost, 0F, 1F);
	}

	/**
	 * What is shown a tick later: a step toward the target, smaller to grow than to melt
	 */
	static float ease(final float shown, final float target) {
		if (shown < target)
			return Math.min(target, shown + GROW_PER_TICK);
		return Math.max(target, shown - MELT_PER_TICK);
	}

	static float lerp(final float t, final float a, final float b) {
		return a + t * (b - a);
	}
}
