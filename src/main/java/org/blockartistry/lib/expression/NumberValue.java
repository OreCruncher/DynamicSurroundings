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

package org.blockartistry.lib.expression;

import javax.annotation.Nonnull;

public class NumberValue extends Variant {

	protected float value;

	public NumberValue() {
		this.value = 0.0F;
	}

	public NumberValue(final float v) {
		this.value = v;
	}

	public NumberValue(@Nonnull final Float v) {
		this.value = v.floatValue();
	}

	public NumberValue(final double d) {
		this.value = (float) d;
	}

	public NumberValue(@Nonnull final String name) {
		super(name);
		this.value = 0.0F;
	}

	public NumberValue(@Nonnull final String name, final float v) {
		super(name);
		this.value = v;
	}

	public NumberValue(@Nonnull final String name, @Nonnull final Float v) {
		super(name);
		this.value = v.floatValue();
	}

	public NumberValue(@Nonnull final String name, final double d) {
		super(name);
		this.value = (float) d;
	}

	@Override
	public float asNumber() {
		return this.value;
	}

	@Override
	@Nonnull
	public String asString() {
		final int i = (int) this.value;
		if (i == this.value)
			return Integer.toString(i);
		return Float.toString(this.value);
	}
	
	@Override
	public boolean asBoolean() {
		return this.value != 0;
	}

	@Override
	public int compareTo(@Nonnull final Variant variant) {
		return Float.compare(this.value, variant.asNumber());
	}

	@Override
	@Nonnull
	public Variant add(@Nonnull final Variant term) {
		return new NumberValue(this.value + term.asNumber());
	}

}