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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.lib.logging.ModLog;

/**
 * The recordings a profiler writes, in a folder of its own: {@code <prefix><scene>-<date-time>.zip}, holding the
 * recording ({@code .jfr}) of the same name and its summary. Zipped, a recording is a third the size, and can be
 * attached to an issue on GitHub, which takes zips but not recordings. Only the newest {@link #KEPT} are kept: older
 * ones are deleted as the client starts, with any left half written. Uses nothing from {@code jdk.jfr}, so it runs
 * whatever Java the game has. Ported from the 26.x line.
 */
public final class ProfileFiles {

	/**
	 * How many recordings are kept
	 */
	public static final int KEPT = 10;

	private static final String EXTENSION = ".jfr";
	private static final String ZIPPED = ".zip";
	// A recording's summary, in its zip
	private static final String SUMMARY = ".txt";
	// A recording as it is first written, before what is private is taken out of it (see FlightRecording), and a zip
	// as it is written
	static final String UNFINISHED = ".part";
	private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

	private ProfileFiles() {
	}

	/**
	 * The name of a recording of a scene made at a time, before it is zipped (see {@link #zip})
	 */
	@Nonnull
	public static String name(@Nonnull final String prefix, @Nonnull final String scene,
			@Nonnull final LocalDateTime when) {
		return prefix + scene.toLowerCase(Locale.ROOT) + "-" + when.format(STAMP) + EXTENSION;
	}

	/**
	 * Zips a finished recording into a zip of the same name beside it, with its summary if there is one, and deletes
	 * the recording. The zip is written under another name first, so a game stopped while writing it leaves no zip that
	 * looks finished.
	 *
	 * @param summary the recording's summary, written in the zip as a text file of the same name; null for none
	 * @return the zip
	 */
	@Nonnull
	public static Path zip(@Nonnull final Path recording, @Nullable final String summary) throws IOException {
		final String name = recording.getFileName().toString();
		final String stem = name.endsWith(EXTENSION) ? name.substring(0, name.length() - EXTENSION.length()) : name;
		final Path zip = recording.resolveSibling(stem + ZIPPED);
		final Path unfinished = recording.resolveSibling(stem + ZIPPED + UNFINISHED);
		try {
			try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(unfinished))) {
				final ZipEntry entry = new ZipEntry(name);
				entry.setLastModifiedTime(Files.getLastModifiedTime(recording));
				out.putNextEntry(entry);
				Files.copy(recording, out);
				out.closeEntry();
				if (summary != null) {
					final ZipEntry text = new ZipEntry(stem + SUMMARY);
					text.setLastModifiedTime(Files.getLastModifiedTime(recording));
					out.putNextEntry(text);
					out.write(summary.getBytes(StandardCharsets.UTF_8));
					out.closeEntry();
				}
			}
			Files.move(unfinished, zip, StandardCopyOption.REPLACE_EXISTING);
		} finally {
			Files.deleteIfExists(unfinished);
		}
		Files.delete(recording);
		return zip;
	}

	/**
	 * Deletes all but the newest {@code keep} recordings with the prefix in {@code folder}, and any left half written
	 * (by a game that stopped while writing one). Other files are left alone. Problems are logged, not thrown: it runs
	 * as the client starts.
	 *
	 * @return how many were deleted
	 */
	public static int prune(@Nonnull final Path folder, @Nonnull final String prefix, final int keep,
			@Nonnull final ModLog logger) {
		if (!Files.isDirectory(folder))
			return 0;
		int deleted = 0;
		try (Stream<Path> files = Files.list(folder)) {
			final List<Path> recordings = files.filter(f -> isOurs(f, prefix))
					.sorted(Comparator.comparingLong(ProfileFiles::modified).reversed()).collect(Collectors.toList());
			int kept = 0;
			for (final Path file : recordings) {
				final boolean unfinished = file.getFileName().toString().endsWith(UNFINISHED);
				if (!unfinished && kept < keep) {
					kept++;
					continue;
				}
				try {
					Files.deleteIfExists(file);
					deleted++;
				} catch (final IOException e) {
					logger.warn("Unable to delete the old profile %s: %s", file.getFileName(), e);
				}
			}
		} catch (final IOException e) {
			logger.warn("Unable to look through the profiles in %s: %s", folder, e);
		}
		if (deleted > 0)
			logger.info("Deleted %d old profile recording(s), keeping the newest %d", deleted, keep);
		return deleted;
	}

	private static boolean isOurs(@Nonnull final Path file, @Nonnull final String prefix) {
		String name = file.getFileName().toString();
		if (name.endsWith(UNFINISHED))
			name = name.substring(0, name.length() - UNFINISHED.length());
		// A recording left unzipped (by a game stopped before zipping it) counts too
		return name.startsWith(prefix) && (name.endsWith(ZIPPED) || name.endsWith(EXTENSION))
				&& Files.isRegularFile(file);
	}

	private static long modified(@Nonnull final Path file) {
		try {
			return Files.getLastModifiedTime(file).toMillis();
		} catch (final IOException e) {
			return 0L;
		}
	}
}
