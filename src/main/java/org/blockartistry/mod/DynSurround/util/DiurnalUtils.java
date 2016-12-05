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
		// Special case for The End
		if(world.provider.getDimension() == 1)
			return false;
		
		final float celestialAngle = getCelestialAngle(world, 0.0F);
		// 0.785 0.260
		return celestialAngle >= 0.785F || celestialAngle < 0.285F;
	}

	public static boolean isNighttime(final World world) {
		// Special case for The End
		if(world.provider.getDimension() == 1)
			return true;

		final float celestialAngle = getCelestialAngle(world, 0.0F);
		// 0.260 0.705
		return celestialAngle >= 0.285F && celestialAngle < 0.701F;
	}

	public static boolean isSunrise(final World world) {
		// Special case for The End
		if(world.provider.getDimension() == 1)
			return false;

		final float celestialAngle = getCelestialAngle(world, 0.0F);
		// 0.705
		return celestialAngle >= 0.701F && celestialAngle < 0.785F;
	}

	public static boolean isSunset(final World world) {
		// Special case for The End
		if(world.provider.getDimension() == 1)
			return false;

		final float celestialAngle = getCelestialAngle(world, 0.0F);
		return celestialAngle > 0.215 && celestialAngle <= 0.306F;
	}

	public static float getCelestialAngle(final World world, final float partialTickTime) {
		final float angle = world.getCelestialAngle(partialTickTime);
		return angle >= 1.0F ? angle - 1.0F : angle;
	}

	public static float getMoonPhaseFactor(final World world) {
		return world.getCurrentMoonPhaseFactor();
	}

	public static long getClockTime(final World world) {
		return world.getWorldTime() % 24000L;
	}
	
	
/*	
	public boolean isDaytime(final World world) {
		final long time = DiurnalUtils.getClockTime(world);
		return time < 13000;
	}

	public boolean isNighttime(final World world) {
		final long time = DiurnalUtils.getClockTime(world);
		return time >= 13000 && time < 22220L;
	}

	public boolean isSunrise(final World world) {
		final long time = DiurnalUtils.getClockTime(world);
		return time >= 22220L;
	}

	public boolean isSunset(final World world) {
		final long time = DiurnalUtils.getClockTime(world);
		return time >= 12000 && time < 14000;
	}
	*/
}
