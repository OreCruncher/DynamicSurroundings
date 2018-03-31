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
package org.blockartistry.lib.task;

import javax.annotation.Nonnull;

import com.google.common.util.concurrent.ListenableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Helper methods for queuing of tasks for execution with the client and/or
 * server threads of Minecraft.
 */
public final class Scheduler {

	private Scheduler() {

	}

	/**
	 * Schedules the specified task for execution. It is possible that the task will
	 * executed immediately because the current thread of execution is appropriate
	 * for the request.
	 *
	 * It is possible that the specified side does not exist, such as a client side
	 * request on a dedicated server. In that case the task will not be executed and
	 * the return value of the method will indicate this condition.
	 *
	 * @param side
	 *            The side on which the task is to execute
	 * @param task
	 *            The task to execute
	 * @return ListenableFuture<> if submitted, null if not
	 */
	public static ListenableFuture<Object> schedule(@Nonnull final Side side, @Nonnull final Runnable task) {
		final IThreadListener tl = side == Side.SERVER ? FMLCommonHandler.instance().getMinecraftServerInstance()
				: Minecraft.getMinecraft();
		return tl == null ? null : tl.addScheduledTask(task);
	}

	/**
	 * Schedules the specified task for execution on the appropriate side. The task
	 * will not execute immediately. It is deferred until the next time the side
	 * thread checks the scheduled task queue.
	 * 
	 * @param side
	 *            The side on which the task is to execute
	 * @param task
	 *            The task to execute
	 */
	public static void scheduleDeferred(@Nonnull final Side side, @Nonnull final Runnable task) {
		try {
			final Thread t = new Thread(() -> schedule(side, task));
			t.start();
			t.join();
		} catch (@Nonnull final Throwable t) {

		}
	}
}
