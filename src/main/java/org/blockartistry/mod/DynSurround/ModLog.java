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

package org.blockartistry.mod.DynSurround;

import org.apache.logging.log4j.Logger;

public final class ModLog {
	
	public static boolean DEBUGGING = false;

	private ModLog() {
	}

	private static Logger logger;

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger log) {
		logger = log;
	}

	public static void info(String msg, Object... parms) {
		if (logger != null)
			logger.info(String.format(msg, parms));
	}

	public static void warn(String msg, Object... parms) {
		if (logger != null)
			logger.warn(String.format(msg, parms));
	}

	public static void debug(String msg, Object... parms) {
		if (logger != null && DEBUGGING) {
			logger.info(String.format(msg, parms));
		}
	}

	public static void error(String msg, Throwable e) {
		if (logger != null)
			logger.error(msg);
		e.printStackTrace();
	}

	public static void catching(Throwable t) {
		if (logger != null) {
			logger.catching(t);
			t.printStackTrace();
		}
	}
}
