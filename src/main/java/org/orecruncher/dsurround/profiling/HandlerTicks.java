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

import javax.annotation.Nonnull;

/**
 * Where the client tick's handlers say how long each took, for the recording /dsprofile is making (see
 * {@link HandlerTickEvent}); while nothing records, what they say goes nowhere. Uses nothing from {@code jdk.jfr}, so
 * the handlers can call it in a Java without it.
 */
public final class HandlerTicks {

	@FunctionalInterface
	interface Sink {
		void ticked(long tick, String handler, long nanos, boolean ran);
	}

	private static volatile Sink sink;

	private HandlerTicks() {
	}

	/**
	 * Whether a recording is listening, so a handler loop can skip work it only does for one
	 */
	public static boolean isRecording() {
		return sink != null;
	}

	/**
	 * One handler's time in one tick, from the client thread
	 *
	 * @param ran whether the handler had work this tick
	 */
	public static void ticked(final long tick, @Nonnull final String handler, final long nanos, final boolean ran) {
		final Sink current = sink;
		if (current != null)
			current.ticked(tick, handler, nanos, ran);
	}

	static void start(@Nonnull final Sink recording) {
		sink = recording;
	}

	static void stop() {
		sink = null;
	}
}
