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

package org.blockartistry.Presets.api;

import javax.annotation.Nonnull;

public final class Utils {

	private Utils() {

	}

	public static String[] toStringArray(@Nonnull final int[] values) {
		final String[] result = new String[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = Integer.toString(values[i]);
		return result;
	}

	public static String[] toStringArray(@Nonnull final boolean[] values) {
		final String[] result = new String[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = Boolean.toString(values[i]);
		return result;
	}

	public static String[] toStringArray(@Nonnull final double[] values) {
		final String[] result = new String[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = Double.toString(values[i]);
		return result;
	}

	public static int[] toIntArray(@Nonnull final String[] values) {
		final int[] result = new int[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = Integer.parseInt(values[i]);
		return result;
	}

	public static boolean[] toBooleanArray(@Nonnull final String[] values) {
		final boolean[] result = new boolean[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = Boolean.parseBoolean(values[i]);
		return result;
	}

	public static double[] toDoubleArray(@Nonnull final String[] values) {
		final double[] result = new double[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = Double.parseDouble(values[i]);
		return result;
	}

}
