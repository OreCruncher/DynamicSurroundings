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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.lib.profiling.RenderTimer;

/**
 * The mod's render timers (see {@link RenderTimer}): how long its effects take to draw. A profiling recording
 * (/dsprofile) writes them into the recording, and logs them when it is done. Each effect adds its own, in the order
 * they are first used.
 */
public final class RenderTimers {

	private static final List<RenderTimer> ALL = new CopyOnWriteArrayList<>();

	private RenderTimers() {
	}

	/**
	 * Adds a timer to those recorded
	 *
	 * @return the timer
	 */
	@Nonnull
	public static RenderTimer add(@Nonnull final RenderTimer timer) {
		ALL.add(timer);
		return timer;
	}

	@Nonnull
	public static List<RenderTimer> all() {
		return ALL;
	}
}
