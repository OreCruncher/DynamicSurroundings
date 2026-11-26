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

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

/**
 * A Java Flight Recorder event carrying an installed mod's id and version (see {@link ProfileMod}), written for each
 * mod once as /dsprofile starts recording. Only loaded while recording.
 */
@Name("dsurround.Mod")
@Label("Mod")
@Category("Dynamic Surroundings")
@Description("An installed mod's id and version, as a Dynamic Surroundings profile was recorded with")
@StackTrace(false)
public final class ModEvent extends Event {

	@Label("Id")
	String id;

	@Label("Version")
	String version;

	static void write(final List<ProfileMod> mods) {
		for (final ProfileMod mod : mods) {
			final ModEvent event = new ModEvent();
			event.id = mod.id;
			event.version = mod.version;
			event.commit();
		}
	}
}
