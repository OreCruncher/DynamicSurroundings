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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.orecruncher.dsurround.lib.profiling.FlightRecording;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;

/**
 * The mod's side of /dsprofile: its events reach a recording, and a recording is summed up. Made with the recorder as
 * /dsprofile makes one, less what needs the game running (the environment, which asks OpenGL). Ported from the 26.x
 * line.
 */
public class ProfilingTests {

	@TempDir
	Path folder;

	@Test
	public void modsAreSortedByIdAndCleaned() {
		final Map<String, String> versions = new HashMap<>();
		versions.put("zeta", "1.0");
		versions.put("alpha", "C:\\Users\\bob\\1.2\u00e9");
		final List<ProfileMod> mods = ProfileMod.of(versions);
		assertEquals("alpha", mods.get(0).id);
		assertEquals("C??Users?bob?1.2?", mods.get(0).version);
		assertEquals("?", ProfileMod.clean(" "));
	}

	@Test
	public void everySceneSaysWhatItIs() throws Exception {
		final String lang = new String(java.nio.file.Files.readAllBytes(
				java.nio.file.Paths.get(getClass().getResource("/assets/dsurround/lang/en_us.lang").toURI())),
				java.nio.charset.StandardCharsets.UTF_8);
		for (final ProfileScene scene : ProfileScene.values()) {
			assertTrue(lang.contains(scene.translationKey() + ".what="), scene.name());
			assertTrue(lang.contains(scene.translationKey() + ".tip="), scene.name());
		}
	}

	@Test
	public void aRecordingHasTheHandlersTicksAndTheFrames() throws Exception {
		final Path file = this.folder.resolve("dsurround-general-test.jfr");
		final AtomicReference<Throwable> failure = new AtomicReference<>();
		final Thread recorder = new Thread(() -> {
			try {
				FlightRecording.record(file, Duration.ofSeconds(3), ProfileSources.NAMESPACE, ProfileSources.sources(),
						() -> {
						});
			} catch (final Throwable t) {
				failure.set(t);
			}
		});
		recorder.start();
		// Once it has started listening, a few ticks and frames, as the game would give them
		final long deadline = System.nanoTime() + 2_000_000_000L;
		while (!HandlerTicks.isRecording() && System.nanoTime() < deadline)
			Thread.sleep(10);
		assertTrue(HandlerTicks.isRecording());
		for (int tick = 1; tick <= 5; tick++) {
			HandlerTicks.ticked(tick, "Weather", 200_000L, true);
			HandlerTicks.ticked(tick, "Aurora", 50_000L, false);
			FrameTimes.add(16.7F);
			Thread.sleep(50);
		}
		recorder.join();
		if (failure.get() != null)
			throw new AssertionError(failure.get());

		final Set<String> handlers = new HashSet<>();
		int frames = 0;
		try (RecordingFile recording = new RecordingFile(file)) {
			while (recording.hasMoreEvents()) {
				final RecordedEvent event = recording.readEvent();
				if (event.getEventType().getName().equals("dsurround.HandlerTick"))
					handlers.add(event.getString("handler"));
				else if (event.getEventType().getName().equals("dsurround.Frames"))
					frames += event.getInt("frames");
			}
		}
		assertEquals(2, handlers.size());
		assertEquals(5, frames);

		// And summed up: the ticks' line names the costliest handler first
		final String text = ProfileSummary.of(file).text("general");
		assertTrue(text.startsWith("Dynamic Surroundings profile: general"), text);
		assertTrue(text.contains("most: Weather 0.20, Aurora 0.05"), text);
		assertTrue(text.contains("Frames: 60 fps"), text);
	}

	@Test
	public void aSummaryFitsInADiscordMessage() {
		final ProfileSummary summary = new ProfileSummary();
		summary.environment("mod", "Dynamic Surroundings 4.0.0 (Forge 14.23.5.2847)");
		for (int s = 0; s < 60; s++)
			summary.frame(60, 16.7, 20, 40, 8, 6);
		for (int i = 0; i < 300; i++)
			summary.timer("A long effect name, number " + i, 0.5, 0.25, false);
		final String text = summary.text("general");
		assertTrue(text.length() <= ProfileSummary.LIMIT, Integer.toString(text.length()));
		assertTrue(text.contains("more"), text);
	}

	@Test
	public void theHeadlineIsTheEffectWithTheLargestShare() {
		final ProfileSummary summary = new ProfileSummary();
		summary.frame(60, 16.7, 20, 40, 10, 5);
		summary.timer("Weather", 1, 0.5, false);
		summary.timer("Aurora", 4, 0.25, false);
		final ProfileHeadline headline = summary.headline();
		assertNotNull(headline);
		assertEquals("Aurora", headline.effect);
		assertEquals(0.4, headline.gpuShare, 1e-9);
		assertEquals(60, headline.fps);
	}

	@Test
	public void anEffectTimedOnceATickIsGivenAsItsTimeAFrame() {
		final ProfileSummary summary = new ProfileSummary();
		summary.frame(40, 25, 30, 40, 10, 10);
		// 1 ms a tick, 20 ticks a second over 40 frames: half a millisecond a frame
		summary.timer("Rain Splashes", 0, 1, true);
		assertTrue(summary.text("general").contains("Rain Splashes 0.00/0.50"), summary.text("general"));
	}
}
