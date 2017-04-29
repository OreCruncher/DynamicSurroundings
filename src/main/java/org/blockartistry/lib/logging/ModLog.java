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

package org.blockartistry.lib.logging;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;

public class ModLog {

	private static final Map<String, ModLog> loggers = Maps.newHashMap();

	private final Logger logger;
	private boolean DEBUGGING;

	private ModLog(@Nonnull final Logger logger) {
		this.logger = logger;
		this.DEBUGGING = false;
	}

	public static ModLog getLogger(@Nonnull final String id) {
		final ModLog result = loggers.get(id);
		return result != null ? result : NULL_LOGGER;
	}

	public static ModLog setLogger(@Nonnull final String id, @Nonnull final Logger log) {
		final ModLog result = log == null ? NULL_LOGGER : new ModLog(log);
		loggers.put(id, result);
		return result;
	}

	public void setDebug(final boolean flag) {
		this.DEBUGGING = flag;
	}

	public boolean isDebugging() {
		return this.DEBUGGING;
	}

	public void info(@Nonnull final String msg, @Nullable final Object... parms) {
		this.logger.info(String.format(msg, parms));
	}

	public void warn(@Nonnull final String msg, @Nullable final Object... parms) {
		this.logger.warn(String.format(msg, parms));
	}

	public void debug(@Nonnull final String msg, @Nullable final Object... parms) {
		if (this.DEBUGGING)
			this.logger.info(String.format(msg, parms));
	}

	public void error(@Nonnull final String msg, @Nonnull final Throwable e) {
		this.logger.error(msg);
		e.printStackTrace();
	}

	public void catching(@Nonnull final Throwable t) {
		this.logger.catching(t);
		t.printStackTrace();
	}

	public static final ModLog NULL_LOGGER = new ModLog(null) {

		@Override
		public void setDebug(final boolean flag) {
		}

		@Override
		public boolean isDebugging() {
			return false;
		}

		@Override
		public void info(@Nonnull final String msg, @Nullable final Object... parms) {
		}

		@Override
		public void warn(@Nonnull final String msg, @Nullable final Object... parms) {
		}

		@Override
		public void debug(@Nonnull final String msg, @Nullable final Object... parms) {
		}

		@Override
		public void error(@Nonnull final String msg, @Nonnull final Throwable e) {
		}

		@Override
		public void catching(@Nonnull final Throwable t) {
		}
	};
}
