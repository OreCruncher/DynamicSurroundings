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

import javax.annotation.Nonnull;

import org.lwjgl.opengl.ARBTimerQuery;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GLContext;

/**
 * How long drawing something takes: on the CPU, building and handing over the drawing, and on the GPU, doing it. The
 * two don't add up to a frame's time: the GPU works on a frame while the CPU builds the next. A GPU timestamp is taken
 * before and after (ARB_timer_query), and the difference averaged.
 * <p>
 * Timestamps rather than an elapsed time query: only one of those can run at a time. The results come a frame or more
 * later; waiting for them would hold the CPU up until the GPU caught up, so a few frames' are kept, and read once ready.
 * Render thread only. Times only the CPU where timer queries aren't supported, or for a timer made CPU only (what it
 * times only fills a buffer drawn elsewhere, as particles do). Ported from the 26.x line.
 */
public final class RenderTimer {

	// Frames whose results may still be on the way
	private static final int FRAMES = 4;
	// How long since it last measured before it counts as stale
	private static final long STALE_AFTER_NANOS = 1_000_000_000L;

	private final String name;
	private final boolean perTick;
	private final boolean gpuTimed;
	private final PeakTimerEMA cpu;
	private final PeakTimerEMA gpu;

	// A start and an end query for each frame, in that order; null until made
	private int[] queries;
	private final boolean[] pending = new boolean[FRAMES];
	private int frame;
	private boolean measuring;
	// When the CPU started, by System.nanoTime; 0 if not measuring
	private long cpuStart;
	// When it last measured, by System.nanoTime; 0 if it never has
	private long lastMeasured;
	// Not known until there is a context: null, then whether queries could be made
	private Boolean supported;

	public RenderTimer(@Nonnull final String name) {
		this(name, false, true);
	}

	/**
	 * @param perTick  whether what it times runs once a client tick rather than once a frame: its averages are then a
	 *                 tick's, which recordings note so as not to set them against a frame's
	 * @param gpuTimed whether to time the GPU as well; false for something that only fills a buffer drawn elsewhere
	 */
	public RenderTimer(@Nonnull final String name, final boolean perTick, final boolean gpuTimed) {
		this.name = name;
		this.perTick = perTick;
		this.gpuTimed = gpuTimed;
		this.cpu = new PeakTimerEMA(name);
		this.gpu = new PeakTimerEMA(name);
	}

	/**
	 * Marks the start of what is measured
	 */
	public void begin() {
		this.cpuStart = System.nanoTime();
		if (!isSupported())
			return;
		collect();
		// Still waiting on this frame's from a few frames ago: the GPU is behind, so this one isn't measured
		if (this.pending[this.frame])
			return;
		ARBTimerQuery.glQueryCounter(this.queries[this.frame * 2], ARBTimerQuery.GL_TIMESTAMP);
		this.measuring = true;
	}

	/**
	 * Marks the end of what is measured, if {@link #begin} started measuring
	 */
	public void end() {
		if (this.cpuStart != 0) {
			final long now = System.nanoTime();
			this.cpu.update(now - this.cpuStart);
			this.cpuStart = 0;
			this.lastMeasured = now;
		}
		if (!this.measuring)
			return;
		ARBTimerQuery.glQueryCounter(this.queries[this.frame * 2 + 1], ARBTimerQuery.GL_TIMESTAMP);
		this.measuring = false;
		this.pending[this.frame] = true;
		this.frame = (this.frame + 1) % FRAMES;
	}

	/**
	 * Whether it hasn't measured in the last second: what it measures isn't being drawn, and its average is left over
	 * from when it was
	 */
	public boolean isStale() {
		return this.lastMeasured == 0 || System.nanoTime() - this.lastMeasured >= STALE_AFTER_NANOS;
	}

	/**
	 * Takes in the results that have come, without waiting for any
	 */
	private void collect() {
		for (int i = 0; i < FRAMES; i++) {
			if (!this.pending[i])
				continue;
			final int start = this.queries[i * 2];
			final int end = this.queries[i * 2 + 1];
			if (GL15.glGetQueryObjecti(end, GL15.GL_QUERY_RESULT_AVAILABLE) == 0
					|| GL15.glGetQueryObjecti(start, GL15.GL_QUERY_RESULT_AVAILABLE) == 0)
				continue;
			this.pending[i] = false;
			final long from = ARBTimerQuery.glGetQueryObjectui64(start, GL15.GL_QUERY_RESULT);
			final long to = ARBTimerQuery.glGetQueryObjectui64(end, GL15.GL_QUERY_RESULT);
			if (to >= from)
				this.gpu.update(to - from);
		}
	}

	private boolean isSupported() {
		if (this.supported == null) {
			boolean ok = false;
			if (this.gpuTimed) {
				try {
					ok = GLContext.getCapabilities().GL_ARB_timer_query;
					if (ok) {
						this.queries = new int[FRAMES * 2];
						for (int i = 0; i < this.queries.length; i++)
							this.queries[i] = GL15.glGenQueries();
					}
				} catch (final RuntimeException e) {
					ok = false;
				}
			}
			this.supported = ok;
		}
		return this.supported;
	}

	/**
	 * Whether it times something run once a client tick rather than once a frame
	 */
	public boolean isPerTick() {
		return this.perTick;
	}

	/**
	 * Whether it times the GPU (and the GPU can be timed here)
	 */
	public boolean isGpuTimed() {
		return Boolean.TRUE.equals(this.supported);
	}

	@Nonnull
	public String name() {
		return this.name;
	}

	/**
	 * The GPU's time, in milliseconds; 0 where it isn't timed
	 */
	public double getMSecs() {
		return this.gpu.getMSecs();
	}

	/**
	 * The GPU's longest time lately, in milliseconds
	 */
	public double getPeakMSecs() {
		return this.gpu.getPeakMSecs();
	}

	/**
	 * The CPU's time, in milliseconds
	 */
	public double getCpuMSecs() {
		return this.cpu.getMSecs();
	}

	/**
	 * The CPU's longest time lately, in milliseconds
	 */
	public double getCpuPeakMSecs() {
		return this.cpu.getPeakMSecs();
	}

	@Override
	@Nonnull
	public String toString() {
		final String cpu = String.format("%s:%7.3f,%7.3f", this.name, this.cpu.getMSecs(), this.cpu.getPeakMSecs());
		if (!isGpuTimed())
			return cpu + " /    n/a";
		return String.format("%s /%7.3f,%7.3f", cpu, this.gpu.getMSecs(), this.gpu.getPeakMSecs());
	}
}
