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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.orecruncher.dsurround.lib.profiling.FlightRecording;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;

/**
 * Recordings made one after another in the same game keep every name: Java 8's recorder caches strings of 16 to 128
 * characters by a one-bit epoch, and the second of two recordings lost the names the first had written (a bench run's
 * second recording, its "Biome Sound Effects" handler and "Particles: rainsplash" timer).
 */
public class StringPoolTests {

	@TempDir
	Path folder;

	// A handler named past the pool's 16 characters, ticked while a recording runs
	private int record(final Path file) throws Exception {
		final AtomicReference<Throwable> failure = new AtomicReference<>();
		final Thread recorder = new Thread(() -> {
			try {
				FlightRecording.record(file, Duration.ofSeconds(2), ProfileSources.NAMESPACE, ProfileSources.sources(),
						() -> {
						});
			} catch (final Throwable t) {
				failure.set(t);
			}
		});
		recorder.start();
		final long deadline = System.nanoTime() + 2_000_000_000L;
		while (!HandlerTicks.isRecording() && System.nanoTime() < deadline)
			Thread.sleep(10);
		for (int tick = 1; tick <= 5; tick++) {
			HandlerTicks.ticked(tick, "Biome Sound Effects", 1000L, true);
			Thread.sleep(20);
		}
		recorder.join();
		if (failure.get() != null)
			throw new AssertionError(failure.get());
		int named = 0;
		try (RecordingFile recording = new RecordingFile(file)) {
			while (recording.hasMoreEvents()) {
				final RecordedEvent event = recording.readEvent();
				if (event.getEventType().getName().equals("dsurround.HandlerTick")
						&& "Biome Sound Effects".equals(event.getString("handler")))
					named++;
			}
		}
		return named;
	}

	@Test
	public void namesSurviveRecordingAgain() throws Exception {
		// As /dsprofile does, the first time in a game
		ProfileSources.warmUp(Duration.ofSeconds(1));
		assertEquals(5, record(this.folder.resolve("first.jfr")));
		assertEquals(5, record(this.folder.resolve("second.jfr")));
		assertEquals(5, record(this.folder.resolve("third.jfr")));
	}
}
