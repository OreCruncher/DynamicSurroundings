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

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

/**
 * Essentially swipe the ThreadLocalRandom code, but not make it thread
 * specific.  ThreadLocalRandom is fast, and this version avoids
 * synchronization and permits the use of seed values to maintain the
 * deterministic RANDOM behavior when using a specific world seed.
 * 
 * (By fast I mean like 4x faster using nextInt())
 */
public class XorShiftRandom extends Random {

	/**
	 * Shared RANDOM.  Not thread safe.
	 */
	public static final XorShiftRandom shared = new XorShiftRandom();

	private static final long serialVersionUID = 1422228009367463911L;
	private static Field getSeed = null;
	
	// Used to get hold of the seed value of a Random
	static {
		
		try {
			getSeed = Random.class.getDeclaredField("seed");
			getSeed.setAccessible(true);
		} catch(Throwable t) {
			;
		}
	}
	
	private static final long GAMMA = 0x9e3779b97f4a7c15L;
	private static final double DOUBLE_UNIT = 0x1.0p-53; // 1.0 / (1L << 53)
	private static final float FLOAT_UNIT = 0x1.0p-24f; // 1.0f / (1 << 24)

	private Double nextLocalGaussian = null;
	private long seed;

	private static long mix64(long z) {
		z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
		z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
		return z ^ (z >>> 33);
	}

	private static int mix32(long z) {
		z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
		return (int) (((z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L) >>> 32);
	}
	
	private static long initialSeed() {
		return mix64(System.currentTimeMillis()) ^ mix64(System.nanoTime());
	}

	public XorShiftRandom() {
		this(initialSeed());
	}

	public XorShiftRandom(@Nonnull final XorShiftRandom random) {
		this.seed = random.seed;
	}
	
	public XorShiftRandom(@Nonnull final Random random) {
		if(random instanceof XorShiftRandom) {
			this.seed = ((XorShiftRandom)random).seed;
		} else {
			try {
				if(getSeed != null)
					this.seed = ((AtomicLong)XorShiftRandom.getSeed.get(random)).get();
			} catch(Throwable t) {
				;
			} finally {
				if(seed == 0)
					this.seed = initialSeed();
			}
		}
	}
	
	public XorShiftRandom(final long seed) {
		this.seed = seed;
	}
	
	public XorShiftRandom(final long seed1, final long seed2) {
		this(mix64(seed1) ^ mix64(seed2));
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	private final long nextSeed() {
		return this.seed += GAMMA;
	}

	protected int next(int bits) {
		return (int) (mix64(nextSeed()) >>> (64 - bits));
	}

	private static final String BadBound = "bound must be positive";
	private static final String BadRange = "bound must be greater than origin";
	
	private long internalNextLong0(long origin, long bound) {
		long r = mix64(nextSeed());
		if (origin < bound) {
			long n = bound - origin, m = n - 1;
			if ((n & m) == 0L) // power of two
				r = (r & m) + origin;
			else if (n > 0L) { // reject over-represented candidates
				for (long u = r >>> 1; // ensure nonnegative
				u + m - (r = u % n) < 0L; // rejection check
				u = mix64(nextSeed()) >>> 1) // retry
					;
				r += origin;
			} else { // range not representable as long
				while (r < origin || r >= bound)
					r = mix64(nextSeed());
			}
		}
		return r;
	}

	private int internalNextInt0(int origin, int bound) {
		int r = mix32(nextSeed());
		if (origin < bound) {
			int n = bound - origin, m = n - 1;
			if ((n & m) == 0)
				r = (r & m) + origin;
			else if (n > 0) {
				for (int u = r >>> 1; u + m - (r = u % n) < 0; u = mix32(nextSeed()) >>> 1)
					;
				r += origin;
			} else {
				while (r < origin || r >= bound)
					r = mix32(nextSeed());
			}
		}
		return r;
	}

	private double internalNextDouble0(double origin, double bound) {
		double r = (nextLong() >>> 11) * DOUBLE_UNIT;
		if (origin < bound) {
			r = r * (bound - origin) + origin;
			if (r >= bound) // correct for rounding
				r = Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
		}
		return r;
	}

	public int nextInt() {
		return mix32(nextSeed());
	}

	public int nextInt(int bound) {
		if (bound <= 0)
			throw new IllegalArgumentException(BadBound);
		int r = mix32(nextSeed());
		int m = bound - 1;
		if ((bound & m) == 0) // power of two
			r &= m;
		else { // reject over-represented candidates
			for (int u = r >>> 1; u + m - (r = u % bound) < 0; u = mix32(nextSeed()) >>> 1)
				;
		}
		return r;
	}

	public int nextInt(int origin, int bound) {
		if (origin >= bound)
			throw new IllegalArgumentException(BadRange);
		return internalNextInt0(origin, bound);
	}

	public long nextLong() {
		return mix64(nextSeed());
	}

	public long nextLong(long bound) {
		if (bound <= 0)
			throw new IllegalArgumentException(BadBound);
		long r = mix64(nextSeed());
		long m = bound - 1;
		if ((bound & m) == 0L) // power of two
			r &= m;
		else { // reject over-represented candidates
			for (long u = r >>> 1; u + m - (r = u % bound) < 0L; u = mix64(nextSeed()) >>> 1)
				;
		}
		return r;
	}

	public long nextLong(long origin, long bound) {
		if (origin >= bound)
			throw new IllegalArgumentException(BadRange);
		return internalNextLong0(origin, bound);
	}

	public double nextDouble() {
		return (mix64(nextSeed()) >>> 11) * DOUBLE_UNIT;
	}

	public double nextDouble(double bound) {
		if (!(bound > 0.0))
			throw new IllegalArgumentException(BadBound);
		double result = (mix64(nextSeed()) >>> 11) * DOUBLE_UNIT * bound;
		return (result < bound) ? result
				: // correct for rounding
				Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
	}

	public double nextDouble(double origin, double bound) {
		if (!(origin < bound))
			throw new IllegalArgumentException(BadRange);
		return internalNextDouble0(origin, bound);
	}

	public boolean nextBoolean() {
		return mix32(nextSeed()) < 0;
	}

	public float nextFloat() {
		return (mix32(nextSeed()) >>> 8) * FLOAT_UNIT;
	}

	public double nextGaussian() {
		// Use nextLocalGaussian instead of nextGaussian field
		Double d = nextLocalGaussian;
		if (d != null) {
			nextLocalGaussian = null;
			return d.doubleValue();
		}
		double v1, v2, s;
		do {
			v1 = 2 * nextDouble() - 1; // between -1 and 1
			v2 = 2 * nextDouble() - 1; // between -1 and 1
			s = v1 * v1 + v2 * v2;
		} while (s >= 1 || s == 0);
		double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
		nextLocalGaussian = new Double(v2 * multiplier);
		return v1 * multiplier;
	}
}
