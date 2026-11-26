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

import org.orecruncher.dsurround.ModBase;

import net.minecraft.launchwrapper.Launch;

/**
 * Starts the development bench (the {@code org.orecruncher.dsurround.bench} package: scenario runs and /dsbench) in a
 * development run. That package is left out of the published jar, so it is only named here, never referenced: in a
 * published jar, or outside development, this does nothing.
 */
public final class BenchHook {

	/**
	 * The scenario file to run, set by runClientBench
	 */
	public static final String SCENARIO_PROPERTY = "dsurround.scenario";

	private static final String BENCH = "org.orecruncher.dsurround.bench.Bench";

	private BenchHook() {
	}

	public static void start() {
		if (!Boolean.TRUE.equals(Launch.blackboard.get("fml.deobfuscatedEnvironment")))
			return;
		try {
			final Class<?> bench = Class.forName(BENCH);
			bench.getMethod("start", String.class).invoke(null, System.getProperty(SCENARIO_PROPERTY, ""));
		} catch (final ClassNotFoundException e) {
			// Not in this build
		} catch (final ReflectiveOperationException e) {
			ModBase.log().error("Unable to start the development bench", e);
		}
	}
}
