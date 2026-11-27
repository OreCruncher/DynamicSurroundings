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

import java.util.Locale;
import java.util.StringJoiner;

import javax.annotation.Nonnull;

/**
 * The surfaces the last rain sounds played on, for the diagnostics overlay, from the 26.x line: what the rain sounds
 * like it is falling on around the player. Client thread only.
 */
public final class RainSurfaceTally {

	public static final int SIZE = 64;

	private final byte[] recent = new byte[SIZE];
	private int count;

	public void add(@Nonnull final RainSurface surface) {
		this.recent[this.count % SIZE] = (byte) surface.ordinal();
		this.count++;
	}

	public void clear() {
		this.count = 0;
	}

	/**
	 * The share of each surface over the last sounds, the commonest first: "water 41%, hard 37%, leaves 22%", or empty
	 * before any
	 */
	@Nonnull
	public String summary() {
		final int n = Math.min(this.count, SIZE);
		if (n == 0)
			return "";
		final RainSurface[] values = RainSurface.values();
		final int[] counts = new int[values.length];
		for (int i = 0; i < n; i++)
			counts[this.recent[i]]++;
		final StringJoiner joiner = new StringJoiner(", ");
		for (int found = 0; found < values.length; found++) {
			int best = -1;
			for (int i = 0; i < counts.length; i++)
				if (counts[i] > 0 && (best < 0 || counts[i] > counts[best]))
					best = i;
			if (best < 0)
				break;
			joiner.add(String.format(Locale.ROOT, "%s %.0f%%", values[best].name().toLowerCase(Locale.ROOT),
					counts[best] * 100F / n));
			counts[best] = 0;
		}
		return joiner.toString();
	}
}
