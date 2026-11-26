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

package org.orecruncher.dsurround.profiling;

import org.orecruncher.dsurround.lib.profiling.RenderTimer;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

/**
 * A Java Flight Recorder event carrying one of the mod's render timers (see {@link RenderTimers}), written once a second
 * while /dsprofile records, so the GPU's time to draw each effect is in the recording beside the CPU's samples. Only
 * loaded while recording.
 */
@Name("dsurround.RenderTimer")
@Label("Render Timer")
@Category("Dynamic Surroundings")
@Description("How long a Dynamic Surroundings effect takes to draw")
@StackTrace(false)
public final class RenderTimerEvent extends Event {

	@Label("Effect")
	String effect;

	@Label("GPU average (ms)")
	double gpuAverage;

	@Label("GPU peak (ms)")
	double gpuPeak;

	@Label("CPU average (ms)")
	double cpuAverage;

	@Label("CPU peak (ms)")
	double cpuPeak;

	@Label("Per tick")
	@Description("Measured once a client tick rather than once a frame: its averages are a tick's")
	boolean perTick;

	@Label("Count")
	@Description("How many of what the effect draws there were a frame; NaN for an effect that doesn't count")
	double count;

	/**
	 * An event for each render timer that has measured in the last second. The timers are written on the render
	 * thread and read here; a value a frame out of date makes no difference
	 */
	static void writeAll() {
		for (final RenderTimer timer : RenderTimers.all()) {
			if (timer.isStale())
				continue;
			final RenderTimerEvent event = new RenderTimerEvent();
			event.effect = timer.name();
			event.gpuAverage = timer.getMSecs();
			event.gpuPeak = timer.getPeakMSecs();
			event.cpuAverage = timer.getCpuMSecs();
			event.cpuPeak = timer.getCpuPeakMSecs();
			event.count = Double.NaN;
			event.perTick = timer.isPerTick();
			event.commit();
		}
	}
}
