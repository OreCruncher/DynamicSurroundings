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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Redacts the paths of the player's folders in a recording file. Most of what is private is kept out of a recording by
 * keeping only the events known to hold none of it (see {@link FlightRecording#ALLOWED}), but some is shared between
 * events, where leaving events out can't reach it: where a class's code was loaded from, say, which is a path in the
 * player's folders, and names the folder a game or modpack is in.
 * <ul>
 * <li>The home folder: its last part (the account's name) starred, as the system writes it, with forward slashes, and
 * as in a file URL.</li>
 * <li>Every other folder code was loaded from (the class path, and every jar Forge loaded mods from), the game's folder
 * and the temporary folder: starred whole, separators too, so only the jar's own name is left.</li>
 * <li>Then whatever follows {@code Users} or {@code home} in a path, in any case or encoding: a home folder written
 * another way (a short name, {@code C:\Users\JOHNSM~1}, another case, percent-encoded, or one a launcher's
 * {@code user.home} doesn't name).</li>
 * </ul>
 * Done on the file's bytes, in place, without changing its length: Java Flight Recorder writes text as UTF-8, Latin-1,
 * or each character as a variable-length number, and each character is swapped for one that takes as many bytes in
 * each, so the file reads as it did. A character outside the Basic Multilingual Plane is left (it can't be swapped so),
 * and so is a character past ASCII in the last pass, which can't tell how a string is encoded. Ported from the 26.x line.
 */
public final class Redaction {

	// Stand-ins for a character of the name, taking as many bytes as it does in each of JFR's encodings (UTF-8, and a
	// variable-length number of seven bits a byte): ASCII (one and one); up to U+07FF (two and two); up to U+3FFF
	// (three and two); the rest of the Basic Multilingual Plane (three and three)
	private static final char ONE_BYTE = '*';
	private static final char TWO_BYTES = (char) 0x00D7;
	private static final char THREE_TWO_BYTES = (char) 0x2217;
	private static final char THREE_BYTES = (char) 0xFF0A;
	// The folders a home folder is in, as the last pass looks for them, lower case
	private static final byte[][] HOMES = { ascii("users"), ascii("home") };
	// The longest name the last pass stars: past it, what follows is taken for something other than a path
	private static final int LONGEST_NAME = 64;
	// A location's schemes, each two letters or more (a drive's is one), and the slashes after them
	private static final Pattern SCHEMES = Pattern.compile("(?:[A-Za-z][A-Za-z0-9+.-]+:)+/*");

	private Redaction() {
	}

	/**
	 * Redacts the player's folders (see {@link #privateFolders}) wherever they are in {@code file}
	 */
	public static void redact(@Nonnull final Path file) throws IOException {
		final byte[] data = Files.readAllBytes(file);
		if (redact(data, replacements(System.getProperty("user.home"), privateFolders())) + redactHomes(data) > 0)
			Files.write(file, data);
	}

	/**
	 * The folders whose paths are private: the game's (the working folder), the temporary folder, and every folder on
	 * the class path or that Forge's class loader loads from (the mods' jars)
	 */
	@Nonnull
	static List<String> privateFolders() {
		final Set<String> folders = new LinkedHashSet<>();
		folders.add(System.getProperty("user.dir"));
		folders.add(System.getProperty("java.io.tmpdir"));
		final String classPath = System.getProperty("java.class.path");
		if (classPath != null)
			for (final String entry : classPath.split(Pattern.quote(File.pathSeparator)))
				folders.add(new File(entry).getAbsoluteFile().getParent());
		for (final URL url : launchSources()) {
			try {
				folders.add(folderOf(url.toURI()));
			} catch (final Exception ignored) {
				// Not a URI as written: its folder is still on the class path, or not a folder at all
			}
		}
		folders.remove(null);
		return new ArrayList<>(folders);
	}

	// The jars and folders LaunchWrapper's class loader loads from (every mod's jar), if it is there
	@Nonnull
	private static List<URL> launchSources() {
		try {
			final Class<?> launch = Class.forName("net.minecraft.launchwrapper.Launch");
			final Object loader = launch.getField("classLoader").get(null);
			if (loader != null) {
				@SuppressWarnings("unchecked")
				final List<URL> sources = (List<URL>) loader.getClass().getMethod("getSources").invoke(loader);
				return sources;
			}
		} catch (final Throwable ignored) {
			// Not under LaunchWrapper (a unit test)
		}
		return new ArrayList<>();
	}

	/**
	 * The folder a code location is in, as written in it, without its scheme: {@code jar:file:/C:/game/mods/a.jar!/}
	 * is {@code C:/game/mods}; null for one in no folder
	 */
	@Nullable
	static String folderOf(@Nonnull final URI location) {
		final String text = location.toString();
		int end = text.length();
		for (final String stop : new String[] { "!", "#", "%23" }) {
			final int at = text.indexOf(stop);
			if (at >= 0)
				end = Math.min(end, at);
		}
		final int slash = text.lastIndexOf('/', end - 1);
		// Past the schemes (jar:file:) and their slashes, though not a drive's letter
		final java.util.regex.Matcher scheme = SCHEMES.matcher(text);
		final int start = scheme.lookingAt() ? scheme.end() : 0;
		if (slash <= start)
			return null;
		return text.substring(start, slash);
	}

	/**
	 * The home folder as it may be written, each with what it is redacted to: as the system writes it, with forward
	 * slashes, and with spaces as in a URL; the last part of it (the account's name) starred out
	 */
	@Nonnull
	static Map<String, String> replacements(@Nullable final String home) {
		final Map<String, String> forms = new LinkedHashMap<>();
		if (home == null)
			return forms;
		final String trimmed = trim(home);
		final int cut = Math.max(trimmed.lastIndexOf('/'), trimmed.lastIndexOf('\\')) + 1;
		final String name = trimmed.substring(cut);
		// Too short to stand out: a home folder this short (a root, "/u") names no one
		if (name.length() < 2)
			return forms;
		final String starred = trimmed.substring(0, cut) + star(name);
		forms.put(trimmed, starred);
		forms.put(trimmed.replace('\\', '/'), starred.replace('\\', '/'));
		if (trimmed.contains(" "))
			forms.put(trimmed.replace('\\', '/').replace(" ", "%20"), starred.replace('\\', '/').replace(" ", "%20"));
		return forms;
	}

	/**
	 * The home folder's forms and every other folder's, longest first, so a folder inside another is redacted whole
	 * before the one it is in is. Each other folder is starred whole (see {@link #starPath}): as written, with forward
	 * slashes, and percent-encoded and decoded as in a URL.
	 */
	@Nonnull
	static Map<String, String> replacements(@Nullable final String home, @Nonnull final Collection<String> folders) {
		final Map<String, String> forms = new LinkedHashMap<>(replacements(home));
		for (final String folder : folders) {
			if (folder == null)
				continue;
			final String trimmed = trim(folder);
			// A root ("C:", "/tmp") names nothing
			if (trimmed.replaceAll("[/\\\\:]", "").length() < 4)
				continue;
			final Set<String> written = new LinkedHashSet<>();
			written.add(trimmed);
			written.add(trimmed.replace('\\', '/'));
			try {
				final URI uri = URI.create("file:///" + trimmed.replace('\\', '/').replace(" ", "%20"));
				written.add(trim(uri.getPath()).replaceFirst("^/", ""));
				written.add(trim(uri.getRawPath()).replaceFirst("^/", ""));
			} catch (final IllegalArgumentException ignored) {
				// Not a URL as written: its plain forms are enough
			}
			for (final String form : written)
				forms.putIfAbsent(form, starPath(form));
		}
		final List<Map.Entry<String, String>> sorted = new ArrayList<>(forms.entrySet());
		sorted.sort(Comparator.comparingInt((Map.Entry<String, String> e) -> e.getKey().length()).reversed());
		final Map<String, String> ordered = new LinkedHashMap<>();
		for (final Map.Entry<String, String> entry : sorted)
			ordered.put(entry.getKey(), entry.getValue());
		return ordered;
	}

	@Nonnull
	private static String trim(@Nonnull final String path) {
		return path.replaceAll("[\\\\/]+$", "");
	}

	/**
	 * Each character of {@code name} as one that takes as many bytes in each of JFR's encodings
	 */
	@Nonnull
	static String star(@Nonnull final String name) {
		final StringBuilder out = new StringBuilder(name.length());
		for (int i = 0; i < name.length(); i++) {
			final char c = name.charAt(i);
			if (c < 0x80)
				out.append(c == ' ' ? ' ' : ONE_BYTE);
			else if (c < 0x800)
				out.append(TWO_BYTES);
			else if (c < 0x4000)
				out.append(THREE_TWO_BYTES);
			else if (!Character.isSurrogate(c))
				out.append(THREE_BYTES);
			else
				out.append(c);
		}
		return out.toString();
	}

	/**
	 * A path starred whole, separators and spaces too, so not even how many folders it has or how long their names are
	 * is left (the path keeps its length: the file can't change size)
	 */
	@Nonnull
	static String starPath(@Nonnull final String path) {
		return star(path).replace(' ', ONE_BYTE);
	}

	/**
	 * Redacts every form in {@code data}, in each of JFR's encodings, in the order given
	 *
	 * @return how many places were redacted
	 */
	static int redact(@Nonnull final byte[] data, @Nonnull final Map<String, String> replacements) {
		int count = 0;
		for (final Map.Entry<String, String> entry : replacements.entrySet()) {
			count += replace(data, utf8(entry.getKey()), utf8(entry.getValue()));
			count += replace(data, varints(entry.getKey()), varints(entry.getValue()));
			final byte[] latin = latin1(entry.getKey());
			final byte[] latinStars = latin1(entry.getValue());
			if (latin != null && latinStars != null)
				count += replace(data, latin, latinStars);
		}
		return count;
	}

	/**
	 * Stars the name after {@code Users} or {@code home} in a path, in any case: between a separator before and after
	 * it, as long as the name is at most {@link #LONGEST_NAME} characters with nothing but printable ASCII and
	 * characters past it between (these, which take more than a byte, left as they are: the encoding isn't known here)
	 *
	 * @return how many names were starred
	 */
	static int redactHomes(@Nonnull final byte[] data) {
		int count = 0;
		for (final byte[] home : HOMES) {
			for (int i = 1; i + home.length + 1 < data.length; i++) {
				if (!isSeparator(data[i - 1]) || !matchesIgnoringCase(data, i, home)
						|| !isSeparator(data[i + home.length]))
					continue;
				final int start = i + home.length + 1;
				int end = start;
				while (end < data.length && end - start <= LONGEST_NAME && !isSeparator(data[end])
						&& (data[end] >= 0x20 || data[end] < 0))
					end++;
				if (end == start || end >= data.length || !isSeparator(data[end]))
					continue;
				boolean starred = false;
				for (int j = start; j < end; j++) {
					final byte b = data[j];
					if (b > 0x20 && b < 0x7F && b != ONE_BYTE) {
						data[j] = ONE_BYTE;
						starred = true;
					}
				}
				if (starred)
					count++;
				i = end;
			}
		}
		return count;
	}

	private static boolean isSeparator(final byte b) {
		return b == '/' || b == '\\';
	}

	private static boolean matchesIgnoringCase(final byte[] data, final int at, final byte[] lower) {
		for (int j = 0; j < lower.length; j++) {
			int b = data[at + j];
			if (b >= 'A' && b <= 'Z')
				b += 'a' - 'A';
			if (b != lower[j])
				return false;
		}
		return true;
	}

	private static int replace(final byte[] data, final byte[] find, final byte[] with) {
		if (find.length == 0 || find.length != with.length)
			return 0;
		int count = 0;
		outer: for (int i = 0; i + find.length <= data.length; i++) {
			for (int j = 0; j < find.length; j++)
				if (data[i + j] != find[j])
					continue outer;
			System.arraycopy(with, 0, data, i, with.length);
			count++;
			i += find.length - 1;
		}
		return count;
	}

	private static byte[] ascii(final String text) {
		return text.getBytes(StandardCharsets.US_ASCII);
	}

	private static byte[] utf8(final String text) {
		return text.getBytes(StandardCharsets.UTF_8);
	}

	// Latin-1, if every character is in it
	@Nullable
	private static byte[] latin1(final String text) {
		for (int i = 0; i < text.length(); i++)
			if (text.charAt(i) > 0xFF)
				return null;
		return text.getBytes(StandardCharsets.ISO_8859_1);
	}

	// Each character as JFR writes it in a character array: a variable-length number, seven bits a byte
	private static byte[] varints(final String text) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream(text.length());
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
}
