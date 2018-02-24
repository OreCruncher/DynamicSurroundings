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

public class BooleanValue extends Variant {

	protected boolean value;

	public BooleanValue() {
		this.value = false;
	}

	public BooleanValue(final boolean b) {
		this.value = b;
	}

	public BooleanValue(final float f) {
		this.value = f != 0;
	}

	public BooleanValue(@Nonnull final String name) {
		super(name);
	}

	public BooleanValue(@Nonnull final String name, final boolean b) {
		super(name);
		this.value = b;
	}

	public BooleanValue(@Nonnull final String name, final float f) {
		super(name);
		this.value = f != 0;
	}

	@Override
	public float asNumber() {
		return this.value ? 1.0F : 0.0F;
	}

	@Override
	@Nonnull
	public String asString() {
		return this.value ? "TRUE" : "FALSE";
	}

	@Override
	public boolean asBoolean() {
		return this.value;
	}

	@Override
	public int compareTo(@Nonnull final Variant variant) {
		return Boolean.compare(this.value, variant.asBoolean());
	}

	@Override
	@Nonnull
	public Variant add(@Nonnull final Variant term) {
		return new BooleanValue(this.value || term.asBoolean());
	}

}
