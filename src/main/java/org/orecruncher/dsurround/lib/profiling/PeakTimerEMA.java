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

package org.orecruncher.dsurround.lib.profiling;

import java.util.function.LongSupplier;

import javax.annotation.Nonnull;

/**
 * An exponential moving average of times in nanoseconds, over roughly the last {@code periods} samples, that also
 * keeps the peak: the largest sample of the last second or two, which the average hides (a hitch once a second barely
 * moves it). Samples are gathered over a second, and the peak is the largest of that second's and the one before, so it
 * doesn't drop to nothing as a second begins. Not thread-safe. Ported from the 26.x line's TimerEMA.
 */
public class PeakTimerEMA {

	static final long PEAK_WINDOW_NANOS = 1_000_000_000L;

	private final String name;
	private final double factor;
	private final LongSupplier clock;
	private double ema = Double.NaN;
	private long windowStart = Long.MIN_VALUE;
	private double windowPeak;
	private double previousPeak;

	public PeakTimerEMA(@Nonnull final String name) {
		this(name, 100, System::nanoTime);
	}

	/**
	 * @param clock the time in nanoseconds, for the peak's windows
	 */
	PeakTimerEMA(@Nonnull final String name, final int periods, @Nonnull final LongSupplier clock) {
		this.name = name;
		this.factor = 2D / (periods + 1);
		this.clock = clock;
	}

	/**
	 * Adds a sample, in nanoseconds
	 */
	public void update(final double nanos) {
		roll();
		this.windowPeak = Math.max(this.windowPeak, nanos);
		this.ema = Double.isNaN(this.ema) ? nanos : nanos * this.factor + this.ema * (1 - this.factor);
	}

	@Nonnull
	public String name() {
		return this.name;
	}

	/**
	 * The average, in milliseconds; 0 before any sample
	 */
	public double getMSecs() {
		return Double.isNaN(this.ema) ? 0D : this.ema / 1_000_000D;
	}

	/**
	 * The largest sample of the last second or two, in milliseconds; 0 if there were none
	 */
	public double getPeakMSecs() {
		roll();
		return Math.max(this.windowPeak, this.previousPeak) / 1_000_000D;
	}

	/**
	 * Moves on to a new window once the current one is a second old
	 */
	private void roll() {
		final long now = this.clock.getAsLong();
		if (this.windowStart == Long.MIN_VALUE) {
			this.windowStart = now;
			return;
		}
		final long elapsed = now - this.windowStart;
		if (elapsed < PEAK_WINDOW_NANOS)
			return;
		// A whole window or more went by without samples: nothing from before is recent
		this.previousPeak = elapsed < 2 * PEAK_WINDOW_NANOS ? this.windowPeak : 0D;
		this.windowPeak = 0D;
		this.windowStart = now;
	}

	@Override
	@Nonnull
	public String toString() {
		return String.format("%s:%7.3fms, peak%7.3f", this.name, getMSecs(), getPeakMSecs());
	}
}
