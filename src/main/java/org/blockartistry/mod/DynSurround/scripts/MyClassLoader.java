/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.blockartistry.mod.DynSurround.scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.blockartistry.mod.DynSurround.ModLog;

final class MyClassLoader extends ClassLoader {

	// regex expressions used to determine if a class is permitted
	// for load.
	private static String[] classWhiteList = { "^java\\.lang\\..*", "^java\\.util\\..*", "^java\\.math\\..*",
			"^javax\\.script\\.ScriptEngine", "^<root>\\..*", "^scala[\\.]?[a-zA-Z0-9_\\$]*$",
			"^scala\\.reflect\\.ScalaSignature", "^\\$line[0-9]*.*", "^scala\\.runtime\\..*",
			"^scala\\.collection\\..*", "^scala\\.math", "^scala\\.ref", "^scala\\.runtime",
			"^org\\.blockartistry\\.mod\\.DynSurround\\.data\\.xface\\..*" };

	private final List<Pattern> tests = new ArrayList<Pattern>();

	public MyClassLoader() {
		super(Thread.currentThread().getContextClassLoader());

		for (int i = 0; i < classWhiteList.length; i++) {
			tests.add(Pattern.compile(classWhiteList[i]));
		}

	}

	private boolean isAllowed(final String name) {
		for (final Pattern p : tests) {
			if (p.matcher(name).matches())
				return true;
		}
		return false;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		synchronized (getClassLoadingLock(name)) {
			// First, check if the class has already been loaded
			Class<?> c = findLoadedClass(name);
			if (c != null)
				return c;
			if (!isAllowed(name)) {
				ModLog.debug("Requested class load '%s'; blocked!", name);
				throw new ClassNotFoundException();
			}
			return super.loadClass(name, resolve);
		}
	}
}
