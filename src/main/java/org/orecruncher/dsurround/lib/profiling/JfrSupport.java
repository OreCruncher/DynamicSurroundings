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

/**
 * Whether Java Flight Recorder can record here. OpenJDK 8 has its {@code jdk.jfr} API from 8u262; the Java 8u51 the
 * Minecraft launcher ships doesn't (Oracle's commercial recorder of that time had another API). Uses nothing from
 * {@code jdk.jfr} itself, and looks for it before anything that does is loaded.
 */
public final class JfrSupport {

	private static Boolean available;

	private JfrSupport() {
	}

	public static synchronized boolean isAvailable() {
		if (available == null) {
			boolean ok;
			try {
				Class.forName("jdk.jfr.FlightRecorder", false, ClassLoader.getSystemClassLoader());
				ok = FlightRecording.isRecorderAvailable();
			} catch (final Throwable t) {
				ok = false;
			}
			available = ok;
		}
		return available;
	}
}
