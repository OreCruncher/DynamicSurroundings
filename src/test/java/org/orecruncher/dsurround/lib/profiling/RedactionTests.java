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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jdk.jfr.consumer.RecordingFile;

/**
 * Tests that the player's folders are overwritten in a recording, in every form and encoding they may be written in,
 * without the file changing its length or becoming unreadable. Ported from the 26.x line.
 */
public class RedactionTests {

	@TempDir
	Path folder;

	// A character array as JFR writes one: each character a variable-length number, seven bits a byte
	private static byte[] varints(final String text) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < text.length(); i++) {
			int c = text.charAt(i);
			while (c >= 0x80) {
				out.write((c & 0x7F) | 0x80);
				c >>>= 7;
			}
			out.write(c);
		}
		return out.toByteArray();
	}

	private static int count(final byte[] data, final byte[] find) {
		int n = 0;
		outer: for (int i = 0; i + find.length <= data.length; i++) {
			for (int j = 0; j < find.length; j++)
				if (data[i + j] != find[j])
					continue outer;
			n++;
		}
		return n;
	}

	private static String stars(final int n) {
		final char[] c = new char[n];
		Arrays.fill(c, '*');
		return new String(c);
	}

	private static void write(final ByteArrayOutputStream out, final byte[] bytes) {
		out.write(bytes, 0, bytes.length);
	}

	@Test
	public void theAccountsNameIsStarredInEachForm() {
		final Map<String, String> forms = Redaction.replacements("C:\\Users\\tedjk\\");
		assertEquals("C:\\Users\\*****", forms.get("C:\\Users\\tedjk"));
		assertEquals("C:/Users/*****", forms.get("C:/Users/tedjk"));
		final Map<String, String> spaced = Redaction.replacements("/home/Jo Smith");
		assertEquals("/home/** *****", spaced.get("/home/Jo Smith"));
		assertEquals("/home/**%20*****", spaced.get("/home/Jo%20Smith"));
	}

	@Test
	public void aHomeThatNamesNoOneIsLeft() {
		assertTrue(Redaction.replacements(null).isEmpty());
		assertTrue(Redaction.replacements("/").isEmpty());
		assertTrue(Redaction.replacements("/u").isEmpty());
	}

	@Test
	public void aStandInTakesAsManyBytesInEachEncoding() {
		// ASCII, Latin-1, Latin Extended, Greek, Devanagari, CJK
		for (final String name : new String[] { "tedjk", "Jos\u00e9", "\u0141ukasz", "\u1f08\u03bb\u03ad\u03be",
				"\u0935\u093f\u091c\u092f", "\u660e\u7f8e", "\ud55c\uad6d" }) {
			final String stars = Redaction.star(name);
			assertEquals(name.getBytes(StandardCharsets.UTF_8).length, stars.getBytes(StandardCharsets.UTF_8).length,
					name);
			assertEquals(varints(name).length, varints(stars).length, name);
		}
	}

	@Test
	public void everyEncodingIsOverwritten() {
		final String home = "C:/Users/Jos\u00e9";
		final String url = "file:///" + home + "/game/mods/dsurround.jar";
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		write(out, new byte[] { 1, 2, 3 });
		write(out, url.getBytes(StandardCharsets.ISO_8859_1));
		write(out, url.getBytes(StandardCharsets.UTF_8));
		write(out, varints(url));
		final byte[] data = out.toByteArray();
		final int length = data.length;

		assertEquals(3, Redaction.redact(data, Redaction.replacements(home)));
		assertEquals(length, data.length);
		assertEquals(0, count(data, home.getBytes(StandardCharsets.ISO_8859_1)));
		assertEquals(0, count(data, home.getBytes(StandardCharsets.UTF_8)));
		assertEquals(0, count(data, varints(home)));
		// The rest of the path is left
		assertEquals(3, count(data, "/game/mods/dsurround.jar".getBytes(StandardCharsets.US_ASCII)));
	}

	@Test
	public void aRedactedRecordingStillReads() throws Exception {
		// Overwrite something certain to be in any recording, a package name, and read every event back
		final Path file = this.folder.resolve("test.jfr");
		FlightRecording.record(file, Duration.ofSeconds(1), "dsurround.", Collections.emptyList(), () -> {
		});
		final long before = Files.size(file);
		final byte[] data = Files.readAllBytes(file);
		assertTrue(Redaction.redact(data, Redaction.replacements(null, Collections.singletonList("java/lang"))) > 0);
		Files.write(file, data);
		assertEquals(before, Files.size(file));
		int events = 0;
		try (RecordingFile recording = new RecordingFile(file)) {
			while (recording.hasMoreEvents()) {
				recording.readEvent();
				events++;
			}
		}
		assertTrue(events > 0);
	}

	@Test
	public void aCodeLocationsFolderIsFoundInEachScheme() {
		assertEquals("C:/Games/My%20Pack/mods",
				Redaction.folderOf(URI.create("file:///C:/Games/My%20Pack/mods/sodium.jar")));
		assertEquals("home/bob/.minecraft/mods",
				Redaction.folderOf(URI.create("jar:file:///home/bob/.minecraft/mods/b.jar!/")));
		assertNull(Redaction.folderOf(URI.create("jrt:/java.base")));
	}

	@Test
	public void theGamesFolderIsStarredLeavingTheJarsName() {
		// A game kept outside the home folder, the pack's name in its path
		final String url = "file:///D:/Games/Instances/Bob's SMP Pack/mods/sodium.jar";
		final byte[] data = url.getBytes(StandardCharsets.UTF_8);
		final Map<String, String> replacements = Redaction.replacements("C:\\Users\\bob",
				Collections.singletonList("D:\\Games\\Instances\\Bob's SMP Pack"));
		assertTrue(Redaction.redact(data, replacements) > 0);
		// Starred whole, its separators too: not even how deep it is or how long its names are is left
		assertEquals("file:///" + stars("D:/Games/Instances/Bob's SMP Pack".length()) + "/mods/sodium.jar",
				new String(data, StandardCharsets.UTF_8));
		// The percent-encoded form, as a URL writes it
		final byte[] encoded = "file:///D:/Games/Instances/Bob's%20SMP%20Pack/mods/sodium.jar"
				.getBytes(StandardCharsets.UTF_8);
		Redaction.redact(encoded, replacements);
		assertFalse(new String(encoded, StandardCharsets.UTF_8).contains("SMP"));
	}

	@Test
	public void aFolderInsideAnotherIsRedactedWhole() {
		final Map<String, String> replacements = Redaction.replacements("C:\\Users\\bob",
				Collections.singletonList("C:\\Users\\bob\\curseforge\\My Pack"));
		final byte[] data = "C:/Users/bob/curseforge/My Pack/mods/a.jar".getBytes(StandardCharsets.UTF_8);
		Redaction.redact(data, replacements);
		assertEquals(stars("C:/Users/bob/curseforge/My Pack".length()) + "/mods/a.jar",
				new String(data, StandardCharsets.UTF_8));
	}

	@Test
	public void aHomeFolderWrittenAnotherWayIsStarred() {
		for (final String path : new String[] { "C:/Users/JOHNSM~1/AppData/Local/Temp/1.jar",
				"c:\\users\\bob\\game\\a.jar", "file:///home/Jos%C3%A9/game/a.jar", "C:\\USERS\\Bob\\x" }) {
			final byte[] data = path.getBytes(StandardCharsets.UTF_8);
			assertEquals(1, Redaction.redactHomes(data), path);
			final String redacted = new String(data, StandardCharsets.UTF_8);
			assertFalse(redacted.contains("JOHNSM") || redacted.contains("bob") || redacted.contains("Bob")
					|| redacted.contains("Jos"), redacted);
			assertEquals(path.length(), redacted.length());
		}
		// Only the name: the folders past it are left, and so is a path with no home folder in it
		final byte[] data = "C:/Users/bob/game/a.jar".getBytes(StandardCharsets.UTF_8);
		Redaction.redactHomes(data);
		assertEquals("C:/Users/***/game/a.jar", new String(data, StandardCharsets.UTF_8));
		assertEquals(0, Redaction.redactHomes("D:/Games/users.txt".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	public void theGamesFolderAndTheClassPathArePrivate() {
		assertTrue(Redaction.privateFolders().contains(System.getProperty("user.dir")));
		// The folder of a jar on the class path (the tests' own dependencies)
		final String classPath = System.getProperty("java.class.path");
		final String first = classPath.split(java.io.File.pathSeparator)[0];
		assertTrue(Redaction.privateFolders()
				.contains(new java.io.File(first).getAbsoluteFile().getParent()));
	}
}
