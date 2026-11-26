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

package org.orecruncher.dsurround.lib.profiling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.orecruncher.lib.logging.ModLog;

import jdk.jfr.consumer.RecordingFile;

/**
 * The recorder and its files: what a recording may keep, that it keeps nothing else, and how recordings are named,
 * zipped and pruned. Ported from the 26.x line's ProfilingTests.
 */
public class RecordingTests {

	@TempDir
	Path folder;

	@Test
	public void flightRecorderIsHere() {
		// The tests run on the Java 8 toolchain, which has it from 8u262
		assertTrue(JfrSupport.isAvailable());
	}

	@Test
	public void theAllowedEventsAreKnownAndNoneIsPrivate() {
		for (final String type : FlightRecording.ALLOWED) {
			assertTrue(type.startsWith("jdk."), type);
			assertFalse(FlightRecording.PRIVATE.contains(type), type);
		}
		assertTrue(FlightRecording.isAllowed("dsurround.", "dsurround.Frames"));
		assertFalse(FlightRecording.isAllowed("dsurround.", "jdk.InitialSystemProperty"));
		assertFalse(FlightRecording.isAllowed("dsurround.", "jdk.JVMInformation"));
		assertFalse(FlightRecording.isAllowed("dsurround.", "minecraft.NetworkSummary"));
	}

	@Test
	public void aRecordingIsWrittenWithNothingPrivate() throws Exception {
		final Path file = this.folder.resolve("dsurround-general-test.jfr");
		FlightRecording.record(file, Duration.ofSeconds(2), "dsurround.", Collections.emptyList(), () -> {
		});
		final Set<String> types = new HashSet<>();
		try (RecordingFile recording = new RecordingFile(file)) {
			while (recording.hasMoreEvents())
				types.add(recording.readEvent().getEventType().getName());
		}
		assertFalse(types.isEmpty());
		for (final String type : types)
			assertTrue(FlightRecording.isAllowed("dsurround.", type), type);
		// The home folder's name isn't in it anywhere
		final String home = System.getProperty("user.home");
		final String account = home.substring(Math.max(home.lastIndexOf('/'), home.lastIndexOf('\\')) + 1);
		if (account.length() >= 3) {
			final String text = new String(Files.readAllBytes(file), StandardCharsets.ISO_8859_1);
			assertFalse(text.contains(home), "the home folder is in the recording");
		}
		// Nothing unfinished left beside it
		assertFalse(Files.exists(file.resolveSibling(file.getFileName() + ProfileFiles.UNFINISHED)));
	}

	@Test
	public void filesAreNamedForTheirSceneAndTime() {
		assertEquals("dsurround-weather-20261126-073150.jfr",
				ProfileFiles.name("dsurround-", "WEATHER", LocalDateTime.of(2026, 11, 26, 7, 31, 50)));
	}

	@Test
	public void aRecordingIsZippedWithItsSummary() throws IOException {
		final Path recording = this.folder.resolve("dsurround-general-20261126-073150.jfr");
		Files.write(recording, new byte[] { 1, 2, 3 });
		final Path zip = ProfileFiles.zip(recording, "the summary");
		assertEquals("dsurround-general-20261126-073150.zip", zip.getFileName().toString());
		assertFalse(Files.exists(recording));
		try (ZipFile file = new ZipFile(zip.toFile())) {
			assertEquals(3, file.getEntry("dsurround-general-20261126-073150.jfr").getSize());
			assertEquals(11, file.getEntry("dsurround-general-20261126-073150.txt").getSize());
		}
	}

	@Test
	public void onlyTheNewestAreKept() throws IOException {
		for (int i = 0; i < 12; i++) {
			final Path file = this.folder.resolve("dsurround-general-" + i + ".zip");
			Files.write(file, new byte[] { 1 });
			Files.setLastModifiedTime(file, FileTime.fromMillis(1_000_000L * (i + 1)));
		}
		final Path unfinished = this.folder.resolve("dsurround-general-99.jfr.part");
		Files.write(unfinished, new byte[] { 1 });
		final Path other = this.folder.resolve("notes.txt");
		Files.write(other, new byte[] { 1 });

		assertEquals(3, ProfileFiles.prune(this.folder, "dsurround-", 10, ModLog.NULL_LOGGER));
		// The oldest two and the unfinished one went; the newest and anything else stayed
		assertFalse(Files.exists(this.folder.resolve("dsurround-general-0.zip")));
		assertFalse(Files.exists(this.folder.resolve("dsurround-general-1.zip")));
		assertTrue(Files.exists(this.folder.resolve("dsurround-general-11.zip")));
		assertFalse(Files.exists(unfinished));
		assertTrue(Files.exists(other));
	}

	@Test
	public void aMissingFolderIsNothingToPrune() {
		assertEquals(0, ProfileFiles.prune(this.folder.resolve("none"), "dsurround-", 10, ModLog.NULL_LOGGER));
	}

	@Test
	public void thePeakIsTheLargestOfTheLastSecondOrTwo() {
		final long[] now = { 0L };
		final PeakTimerEMA timer = new PeakTimerEMA("test", 100, () -> now[0]);
		timer.update(5_000_000);
		timer.update(1_000_000);
		assertEquals(5D, timer.getPeakMSecs(), 1e-9);
		// The next second still remembers it
		now[0] = 1_500_000_000L;
		timer.update(2_000_000);
		assertEquals(5D, timer.getPeakMSecs(), 1e-9);
		// Two seconds on, only the last second's
		now[0] = 2_600_000_000L;
		timer.update(3_000_000);
		assertEquals(3D, timer.getPeakMSecs(), 1e-9);
	}
}
