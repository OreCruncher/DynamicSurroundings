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
package org.orecruncher.lib.random;

/**
 * Simple Linear congruential generator for integer psuedo random numbers.
 * Intended to be fast. Limit is that it can only generate random numbers 0 -
 * 32K.
 */
public final class LCGRandom {

	private long v;

	/**
	 * Creates and seeds an LCG using an integer from XorShiftRandom.
	 */
	public LCGRandom() {
		this(XorShiftRandom.current().nextLong());
	}

	/**
	 * Creates and initializes an LCG generator using a seed value.
	 * 
	 * @param seed
	 *            Seed to initialize the LCG generator with
	 */
	public LCGRandom(final long seed) {
		this.v = seed;
	}

	/**
	 * Generates a random number between 0 and the bound specified.
	 * 
	 * @param bound
	 *            upper bound of the random integer generated
	 * @return Pseudo random integer between 0 and bound
	 */
	public int nextInt(final int bound) {
		this.v = (2862933555777941757L * this.v + 3037000493L);
		return ((int) ((this.v >> 32) & 0x7FFFFFFF)) % bound;
	}
}
