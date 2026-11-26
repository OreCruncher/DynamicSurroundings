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

import java.util.Arrays;

import javax.annotation.Nullable;

import org.orecruncher.dsurround.lib.profiling.RenderTimer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The game's frames, for the recording /dsprofile is making (see {@link FramesEvent}): how long each frame took, from
 * the start of one frame's drawing to the next's, and how long drawing the frame took, on the GPU and the CPU, by a
 * {@link RenderTimer} around the game's whole drawing of it (Forge's render tick: the world and the screen over it).
 * The effects' timers are measured the same way, so each is a share of the frame's. While nothing records, it does
 * nothing. Uses nothing from {@code jdk.jfr}.
 */
@SideOnly(Side.CLIENT)
public final class FrameTimes {

	/**
	 * Drawing a frame, the whole of it
	 */
	static final RenderTimer RENDER_TIMER = new RenderTimer("Frame");

	private static volatile boolean recording;
	// Render thread only: when the last frame's drawing began, by System.nanoTime; 0 for none yet
	private static long lastBegin;
	// Render thread only: whether the timer was begun this frame, so it is ended even if recording stopped meanwhile
	private static boolean timing;

	// Frame lengths in ms since the last take, guarded by LOCK
	private static final Object LOCK = new Object();
	private static float[] lengths = new float[512];
	private static int count;

	private FrameTimes() {
	}

	/**
	 * Hooks Forge's render tick, which runs around the game's drawing of each frame
	 */
	public static void register() {
		MinecraftForge.EVENT_BUS.register(FrameTimes.class);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onRenderTickStart(final TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START)
			begin();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onRenderTickEnd(final TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.END)
			end();
	}

	static void begin() {
		if (!recording)
			return;
		final long now = System.nanoTime();
		if (lastBegin != 0)
			add((now - lastBegin) / 1_000_000F);
		lastBegin = now;
		RENDER_TIMER.begin();
		timing = true;
	}

	static void end() {
		if (!timing)
			return;
		timing = false;
		RENDER_TIMER.end();
	}

	/**
	 * A frame's length, ms
	 */
	static void add(final float ms) {
		synchronized (LOCK) {
			if (count == lengths.length)
				lengths = Arrays.copyOf(lengths, count * 2);
			lengths[count++] = ms;
		}
	}

	static void start() {
		synchronized (LOCK) {
			count = 0;
		}
		// Read on the render thread only once recording is seen, so the first frame after starts afresh
		lastBegin = 0;
		recording = true;
	}

	static void stop() {
		recording = false;
	}

	/**
	 * The frames since the last take, and the drawing's averages now; null if there were none
	 */
	@Nullable
	static Second take() {
		final float[] taken;
		synchronized (LOCK) {
			if (count == 0)
				return null;
			taken = Arrays.copyOf(lengths, count);
			count = 0;
		}
		Arrays.sort(taken);
		double total = 0;
		for (final float length : taken)
			total += length;
		return new Second(taken.length, total / taken.length, percentile(taken, 0.5), percentile(taken, 0.95),
				taken[taken.length - 1], RENDER_TIMER.getMSecs(), RENDER_TIMER.getCpuMSecs());
	}

	private static float percentile(final float[] sorted, final double share) {
		return sorted[Math.min(sorted.length - 1, (int) (share * sorted.length))];
	}

	/**
	 * A second's frames: how many, their mean, median and 95th percentile lengths and the longest (ms), and drawing a
	 * frame on the GPU and the CPU (ms, the running averages)
	 */
	static final class Second {
		final int frames;
		final double mean;
		final double p50;
		final double p95;
		final double max;
		final double gpu;
		final double cpu;

		Second(final int frames, final double mean, final double p50, final double p95, final double max,
				final double gpu, final double cpu) {
			this.frames = frames;
			this.mean = mean;
			this.p50 = p50;
			this.p95 = p95;
			this.max = max;
			this.gpu = gpu;
			this.cpu = cpu;
		}
	}
}
