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

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

/**
 * A Java Flight Recorder event carrying a second's frames (see {@link FrameTimes}), written once a second while
 * /dsprofile records, so each effect's render timer can be set against the frame it is part of. Only loaded while
 * recording.
 */
@Name("dsurround.Frames")
@Label("Frames")
@Category("Dynamic Surroundings")
@Description("How long the game's frames took in a second, and how long drawing one took on the GPU and the CPU")
@StackTrace(false)
public final class FramesEvent extends Event {

	@Label("Frames")
	int frames;

	@Label("Mean (ms)")
	@Description("The frames' mean length, from the start of one's drawing to the next's")
	double mean;

	@Label("Median (ms)")
	double p50;

	@Label("95th percentile (ms)")
	double p95;

	@Label("Longest (ms)")
	double max;

	@Label("GPU drawing (ms)")
	@Description("Drawing a frame on the GPU, the world and the screen over it; the running average, as the render timers'")
	double gpu;

	@Label("CPU drawing (ms)")
	@Description("Drawing a frame on the CPU, the world and the screen over it; the running average, as the render timers'")
	double cpu;

	static void write(final FrameTimes.Second second) {
		final FramesEvent event = new FramesEvent();
		event.frames = second.frames;
		event.mean = second.mean;
		event.p50 = second.p50;
		event.p95 = second.p95;
		event.max = second.max;
		event.gpu = second.gpu;
		event.cpu = second.cpu;
		event.commit();
	}
}
