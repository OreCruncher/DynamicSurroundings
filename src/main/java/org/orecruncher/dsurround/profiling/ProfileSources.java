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

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.lib.profiling.FlightRecording;
import org.orecruncher.dsurround.lib.profiling.FlightRecording.Source;

import com.google.common.collect.ImmutableList;

/**
 * The mod's event types and what feeds them, and the recording made with them: what it was made on, written once
 * ({@link EnvironmentEvent}, a {@link ModEvent} for each installed mod), each client tick handler's time every tick
 * ({@link HandlerTickEvent}), the render timers ({@link RenderTimerEvent}) and the frames ({@link FramesEvent}) once a
 * second. Uses {@code jdk.jfr}: {@link Profiler} only calls it once it knows the recorder is there.
 */
final class ProfileSources {

	/**
	 * The prefix of the mod's event names, which a recording keeps
	 */
	static final String NAMESPACE = "dsurround.";

	private static final Runnable NOTHING = () -> {
	};

	private ProfileSources() {
	}

	@Nonnull
	static List<Source> sources() {
		return ImmutableList.of(Source.once(EnvironmentEvent.class), Source.once(ModEvent.class),
				Source.live(HandlerTickEvent.class, () -> HandlerTicks.start(HandlerTickEvent::write),
						HandlerTicks::stop),
				// The render timers' running averages, read: nothing is counted to flush
				Source.periodic(RenderTimerEvent.class, RenderTimerEvent::writeAll, NOTHING, NOTHING),
				// An event holds a second's frames, and the summaries count them as a second's
				Source.periodic(FramesEvent.class, ProfileSources::writeFrames, FrameTimes::start, FrameTimes::stop));
	}

	static boolean isWarm() {
		return FlightRecording.isWarm();
	}

	static void warmUp(@Nonnull final Duration length) throws Exception {
		FlightRecording.warmUp(length, NAMESPACE, sources());
	}

	static void record(@Nonnull final Path file, @Nonnull final Duration length,
			@Nonnull final ProfileEnvironment environment) throws Exception {
		FlightRecording.record(file, length, NAMESPACE, sources(), () -> {
			EnvironmentEvent.write(environment);
			ModEvent.write(environment.modList);
		});
	}

	private static void writeFrames() {
		final FrameTimes.Second second = FrameTimes.take();
		if (second != null)
			FramesEvent.write(second);
	}
}
