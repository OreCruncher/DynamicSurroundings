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
 * A Java Flight Recorder event carrying what a profile was recorded on (see {@link ProfileEnvironment}), written once
 * as /dsprofile starts recording. The same name and fields as the 26.x line's, so its scripts read it. Only loaded while
 * recording: a player's Java may not have {@code jdk.jfr}.
 */
@Name("dsurround.Environment")
@Label("Environment")
@Category("Dynamic Surroundings")
@Description("The graphics card, settings and versions a Dynamic Surroundings profile was recorded with")
@StackTrace(false)
public final class EnvironmentEvent extends Event {

	@Label("Mod")
	String mod;

	@Label("Mods installed")
	int mods;

	@Label("GPU")
	String gpu;

	@Label("GPU vendor")
	String gpuVendor;

	@Label("GPU type")
	String gpuType;

	@Label("Driver")
	String driver;

	@Label("Backend")
	String backend;

	@Label("Graphics")
	String graphics;

	@Label("Shader pack")
	boolean shaderPack;

	@Label("Render distance")
	int renderDistance;

	@Label("Width")
	@Description("The frame's width in pixels, as drawn")
	int width;

	@Label("Height")
	@Description("The frame's height in pixels, as drawn")
	int height;

	@Label("Frame rate limit")
	@Description("The frame rate limit setting; 260 is unlimited")
	int frameRateLimit;

	@Label("VSync")
	boolean vsync;

	@Label("Development run")
	@Description("The development environment's run the game was started with; empty for a player's game")
	String devRun;

	@Label("Java")
	String java;

	@Label("Operating system")
	String os;

	static void write(final ProfileEnvironment e) {
		final EnvironmentEvent event = new EnvironmentEvent();
		event.mod = e.mod;
		event.mods = e.mods;
		event.gpu = e.gpu;
		event.gpuVendor = e.gpuVendor;
		event.gpuType = e.gpuType;
		event.driver = e.driver;
		event.backend = e.backend;
		event.graphics = e.graphics;
		event.shaderPack = e.shaderPack;
		event.renderDistance = e.renderDistance;
		event.width = e.width;
		event.height = e.height;
		event.frameRateLimit = e.frameRateLimit;
		event.vsync = e.vsync;
		event.devRun = e.devRun;
		event.java = e.java;
		event.os = e.os;
		event.commit();
	}
}
