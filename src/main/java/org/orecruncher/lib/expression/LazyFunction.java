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
package org.orecruncher.lib.expression;

import java.util.Locale;

public abstract class LazyFunction {
	/**
	 * Name of this function.
	 */
	private final String name;
	/**
	 * NumberValue of parameters expected for this function. <code>-1</code> denotes
	 * a variable number of parameters.
	 */
	private final int numParams;

	/**
	 * Creates a new function with given name and parameter count.
	 *
	 * @param name
	 *            The name of the function.
	 * @param numParams
	 *            The number of parameters for this function. <code>-1</code>
	 *            denotes a variable number of parameters.
	 */
	public LazyFunction(final String name, final int numParams) {
		this.name = name.toUpperCase(Locale.ROOT);
		this.numParams = numParams;
	}

	public String getName() {
		return this.name;
	}

	public int getNumParams() {
		return this.numParams;
	}

	public boolean numParamsVaries() {
		return this.numParams < 0;
	}

	public abstract LazyVariant lazyEval(final LazyVariant... lazyParams);
}
