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

import java.lang.reflect.Array;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Predicate;

public final class MyUtils {

	private static final int[] EMPTY = {};

	private MyUtils() {
	}

	@Nonnull
	public static int[] splitToInts(@Nonnull final String str, final char splitChar) {

		final String[] tokens = StringUtils.split(str, splitChar);
		if (tokens == null || tokens.length == 0)
			return EMPTY;

		final int[] result = new int[tokens.length];
		for (int i = 0; i < tokens.length; i++) {
			result[i] = Integer.parseInt(tokens[i]);
		}

		return result;
	}

	@Nonnull
	public static <T> T[] concatenate(@Nonnull final T[] a, @Nullable final T[] b) {
		if (b == null)
			return a;

		final int aLen = a.length;
		final int bLen = b.length;

		if (bLen == 0)
			return a;

		@SuppressWarnings("unchecked")
		final T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);

		return c;
	}

	@Nonnull
	public static <T> T[] append(@Nonnull final T[] a, @Nullable final T b) {

		if (b == null)
			return a;

		final int aLen = a.length;

		@SuppressWarnings("unchecked")
		final T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + 1);
		System.arraycopy(a, 0, c, 0, aLen);
		c[aLen] = b;

		return c;
	}

	@Nullable
	public static <T> T find(@Nullable final List<T> list, @Nonnull final Predicate<T> pred) {
		if (list == null || list.size() == 0)
			return null;

		for (final T e : list)
			if (pred.apply(e))
				return e;
		return null;
	}

	@SafeVarargs
	public static <T> List<T> addAll(@Nonnull final List<T> list, final T... objs) {
		if (objs != null)
			for (int i = 0; i < objs.length; i++)
				if (objs[i] != null)
					list.add(objs[i]);
		return list;
	}
}
