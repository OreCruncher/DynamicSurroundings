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
package org.orecruncher.lib;

import javax.annotation.Nonnull;

import org.orecruncher.lib.logging.ModLog;

import net.minecraftforge.fml.relauncher.Side;

/**
 * Diagnostic class used to ensure access by a specific named thread. Useful for
 * catching errors such as submitting sounds/particles to the client side from a
 * server side thread.
 */
public final class ThreadGuard {

	public static final String CLIENT_THREAD = "Client thread";
	public static final String SERVER_THREAD = "Server thread";

	public static enum Action {
		// Take no action
		NONE,
		// Log the violation and move on
		LOG,
		// Throw a runtime exception to crash and burn
		EXCEPTION;
	};

	// Name of the thread that is allowed access
	private final String threadName;
	// Context of the guard
	private final String context;
	// Logger to send information to on violation
	private final ModLog log;
	// Action to take on violation
	private Action action = Action.LOG;

	public ThreadGuard(@Nonnull final ModLog log, @Nonnull final Side side, @Nonnull final String context) {
		this(log, side == Side.CLIENT ? CLIENT_THREAD : SERVER_THREAD, context);
	}

	public ThreadGuard(@Nonnull final ModLog log, @Nonnull final String threadName, @Nonnull final String context) {
		this.log = log;
		this.threadName = threadName;
		this.context = context;
	}

	public ThreadGuard setAction(@Nonnull final Action action) {
		this.action = action;
		return this;
	}

	public void check(@Nonnull final String locus) {
		if (this.action == Action.NONE)
			return;

		final String name = Thread.currentThread().getName();
		if (!name.equals(this.threadName)) {
			final String txt = String.format("[%s::%s] illegal access by thread [%s]!", this.context, locus, name);
			if (this.action == Action.LOG)
				this.log.error(txt, new Throwable());
			else
				throw new RuntimeException(txt);
		}
	}
}
