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

package org.blockartistry.mod.DynSurround.registry;

import javax.annotation.Nonnull;

public enum TemperatureRating {
	
	ICY("icy", 0.0F),
	COOL("cool", 0.2F),
	MILD("mild", 1.0F),
	WARM("warm", 1.8F),
	HOT("hot", 100.F);

	private final String val;
	private final float tempRange;

	TemperatureRating(@Nonnull final String val, final float tempRange) {
		this.val = val;
		this.tempRange = tempRange;
	}

	@Nonnull
	public String getValue() {
		return this.val;
	}

	public float getTempRange() {
		return this.tempRange;
	}

	@Nonnull
	public static TemperatureRating fromTemp(final float temp) {
		for (final TemperatureRating rating : values())
			if (temp < rating.getTempRange())
				return rating;
		return TemperatureRating.MILD;
	}
}