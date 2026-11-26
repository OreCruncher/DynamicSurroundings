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
import jdk.jfr.Timespan;

/**
 * A Java Flight Recorder event carrying how long one of the mod's client tick handlers took in one tick (see
 * {@link HandlerTicks}), written for every handler every tick while /dsprofile records, so a tick's handlers can be
 * added up and compared tick by tick. Only loaded while recording.
 */
@Name("dsurround.HandlerTick")
@Label("Handler Tick")
@Category("Dynamic Surroundings")
@Description("How long a Dynamic Surroundings client tick handler took in one client tick")
@StackTrace(false)
public final class HandlerTickEvent extends Event {

	@Label("Tick")
	@Description("The client tick the handler ran in; the same for every handler of that tick")
	long tick;

	@Label("Handler")
	String handler;

	@Label("Time")
	@Timespan(Timespan.NANOSECONDS)
	long time;

	@Label("Ran")
	@Description("Whether the handler had work this tick; one that didn't only asked")
	boolean ran;

	static void write(final long tick, final String handler, final long nanos, final boolean ran) {
		final HandlerTickEvent event = new HandlerTickEvent();
		event.tick = tick;
		event.handler = handler;
		event.time = nanos;
		event.ran = ran;
		event.commit();
	}
}
