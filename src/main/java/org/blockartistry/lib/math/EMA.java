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

/**
 * Simple EMA calculator.
 */
public class EMA {

	private final String name;
	private double ema;
	private double factor;

	public EMA() {
		this("UNNAMED");
	}

	public EMA(@Nonnull final String name) {
		this(name, 100);
	}
	
	public EMA(@Nonnull final String name, final int periods) {
		this.name = name;
		this.factor = 2D / (periods + 1);
		this.ema = Double.NaN;
	}

	public double update(final double newValue) {
		if (Double.isNaN(this.ema)) {
			this.ema = newValue;
		} else {
			this.ema = (newValue - this.ema) * this.factor + this.ema;
		}
		return this.ema;
	}

	public String name() {
		return this.name;
	}
	
	public double get() {
		return this.ema;
	}

}
