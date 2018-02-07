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

package org.blockartistry.lib.math;

import javax.annotation.Nonnull;

import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

/**
 * Replacement algos for SIN_TABLE/cos in Minecraft's MathStuff routines. Use
 * the Riven method:
 * 
 * http://riven8192.blogspot.com/2009/08/fastmath-sincos-lookup-tables.html
 * http://riven8192.blogspot.com/2009/08/fastmath-atan2-lookup-table.html
 */
public class MathStuff {

	private static final int SIN_BITS = 12;
	private static final int SIN_MASK = ~(-1 << SIN_BITS);
	private static final int SIN_COUNT = SIN_MASK + 1;
	private static final float RAD_FULL = (float) (Math.PI * 2.0);
	private static final float RAD_TO_INDEX = SIN_COUNT / RAD_FULL;
	private static final float DEG_FULL = 360.0F;
	private static final float DEG_TO_INDEX = SIN_COUNT / DEG_FULL;
	private final static float COS_TO_SIN = (float) (Math.PI / 2.0);
	private static final float[] SIN_TABLE = new float[SIN_COUNT];

	private static final int ATAN2_BITS = 7;
	private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
	private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
	private static final int ATAN2_COUNT = ATAN2_MASK + 1;
	private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);
	private static final float ATAN2_DIM_MINUS_1 = (ATAN2_DIM - 1);
	private static final float[] ATAN2_TABLE = new float[ATAN2_COUNT];

	private static final float RAD_TO_DEG = (float) (180.D / Math.PI);
	private static final float DEG_TO_RAD = (float) (Math.PI / 180.0D);

	public static final float PI_F = (float) Math.PI;
	public static final float E_F = (float) Math.E;

	static {

		for (int i = 0; i < SIN_COUNT; i++) {
			SIN_TABLE[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * RAD_FULL);
		}

		// Fix-up cardinals
		for (int i = 0; i < 360; i += 90) {
			SIN_TABLE[(int) (i * DEG_TO_INDEX) & SIN_MASK] = (float) Math.sin(i * Math.PI / 180.0);
		}

		// atan2
		for (int i = 0; i < ATAN2_DIM; i++) {
			for (int j = 0; j < ATAN2_DIM; j++) {
				final float x0 = (float) i / ATAN2_DIM;
				final float y0 = (float) j / ATAN2_DIM;
				ATAN2_TABLE[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
			}
		}
	}

	public static final float sin(final float rad) {
		return SIN_TABLE[(int) (rad * RAD_TO_INDEX) & SIN_MASK];
	}

	public static final float cos(final float rad) {
		return SIN_TABLE[(int) ((rad + COS_TO_SIN) * RAD_TO_INDEX) & SIN_MASK];
	}

	public static final float tan(final float rad) {
		return sin(rad) / cos(rad);
	}

	public static final double sin(final double rad) {
		final float tmp = (float) rad;
		return SIN_TABLE[(int) (tmp * RAD_TO_INDEX) & SIN_MASK];
	}

	public static final double cos(final double rad) {
		final float tmp = (float) rad;
		return SIN_TABLE[(int) ((tmp + COS_TO_SIN) * RAD_TO_INDEX) & SIN_MASK];
	}

	public static final double tan(final double rad) {
		return tan((float) rad);
	}

	public static final float atan2(float y, float x) {
		final float add, mul;

		if (x < 0.0f) {
			if (y < 0.0f) {
				x = -x;
				y = -y;

				mul = 1.0f;
			} else {
				x = -x;
				mul = -1.0f;
			}

			add = -PI_F;
		} else {
			if (y < 0.0f) {
				y = -y;
				mul = -1.0f;
			} else {
				mul = 1.0f;
			}

			add = 0.0f;
		}

		final float invDiv = ATAN2_DIM_MINUS_1 / ((x < y) ? y : x);
		final int xi = (int) (x * invDiv);
		final int yi = (int) (y * invDiv);

		return (ATAN2_TABLE[yi * ATAN2_DIM + xi] + add) * mul;
	}

	public static final double atan2(final double y, final double x) {
		final float tmp1 = (float) y;
		final float tmp2 = (float) x;
		return (double) atan2(tmp1, tmp2);
	}

	// Misc functions

	public static final float toRadians(final float degrees) {
		return degrees * DEG_TO_RAD;
	}

	public static final float toDegrees(final float radians) {
		return radians * RAD_TO_DEG;
	}

	public static float wrapDegrees(float value) {
		value = value % 360.0F;

		if (value >= 180.0F) {
			value -= 360.0F;
		}

		if (value < -180.0F) {
			value += 360.0F;
		}

		return value;
	}

	public static double wrapDegrees(double value) {
		value = value % 360.0D;

		if (value >= 180.0D) {
			value -= 360.0D;
		}

		if (value < -180.0D) {
			value += 360.0D;
		}

		return value;
	}

	public static final float abs(final float val) {
		return val < 0.0F ? -val : val;
	}

	public static final double abs(final double val) {
		return val < 0.0F ? -val : val;
	}
	
	public static final long abs(final long val) {
		return val < 1L ? -val : val;
	}

	public static final int abs(final int val) {
		return val < 1 ? -val : val;
	}

	public static float sqrt(final float value) {
		return (float) Math.sqrt((double) value);
	}

	public static double sqrt(final double value) {
		return (float) Math.sqrt(value);
	}

	public static int floor(final double value) {
		final int i = (int) value;
		return value < (double) i ? i - 1 : i;
	}

	public static int floor(final float value) {
		final int i = (int) value;
		return value < (float) i ? i - 1 : i;
	}

	public static float clamp(final float num, final float min, final float max) {
		return num < min ? min : (num > max ? max : num);
	}

	public static double clamp(final double num, final double min, final double max) {
		return num < min ? min : (num > max ? max : num);
	}

	public static int clamp(final int num, final int min, final int max) {
		return num < min ? min : (num > max ? max : num);
	}

	// Assumes center at origin.
	public static Vec2f rotateScale(@Nonnull final Vec2f coord, final float radians, final float scale) {
		final float f = cos(radians);
		final float f1 = sin(radians);
		final float d0 = coord.x * f + coord.y * f1;
		final float d1 = coord.y * f - coord.x * f1;
		return new Vec2f(d0 * scale, d1 * scale);
	}
	
	public static Vec2f rotate(@Nonnull final Vec2f coord, final float radians) {
		return rotateScale(coord, radians, 1F);
	}

	public static final Vec3d getVectorForRotation(final float pitch, final float yaw) {
		float f = cos(-yaw * 0.017453292F - PI_F);
		float f1 = sin(-yaw * 0.017453292F - PI_F);
		float f2 = -cos(-pitch * 0.017453292F);
		float f3 = sin(-pitch * 0.017453292F);
		return new Vec3d((double) (f1 * f2), (double) f3, (double) (f * f2));
	}
}
