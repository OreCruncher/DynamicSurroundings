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

package org.blockartistry.mod.DynSurround.util;

import net.minecraft.world.World;

public final class DiurnalUtils {

	private DiurnalUtils() {
	}

	public static boolean isDaytime(final World world) {
		return !world.provider.getHasNoSky() && world.provider.getSunBrightnessFactor(1.0f) > 0.6f;
	}

	public static boolean isNighttime(final World world) {
		return !world.provider.getHasNoSky() && world.provider.getSunBrightnessFactor(1.0f) < 0.1f;
	}

	public static boolean isSunrise(final World world) {
		if (world.provider.getHasNoSky())
			return false;

		float brFactor = world.provider.getSunBrightnessFactor(1.0f);
		return brFactor > 0.1f && brFactor < 0.6f && Math.sin(world.getCelestialAngleRadians(1.0f)) < 0.0;
	}

	public static boolean isSunset(final World world) {
		if (world.provider.getHasNoSky())
			return false;

		float brFactor = world.provider.getSunBrightnessFactor(1.0f);
		return brFactor > 0.1f && brFactor < 0.6f && Math.sin(world.getCelestialAngleRadians(1.0f)) > 0.0;
	}

	public static float getMoonPhaseFactor(final World world) {
		return world.getCurrentMoonPhaseFactor();
	}
}
