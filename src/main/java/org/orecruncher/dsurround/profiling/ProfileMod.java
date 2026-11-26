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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

/**
 * An installed mod's id and version, for a profile ({@link ModEvent}): nothing else of it, and cleaned of anything
 * path-like, as a version string is the mod's to write. Uses nothing from {@code jdk.jfr}.
 */
final class ProfileMod {

	// The longest an id or version is kept
	static final int MAX_LENGTH = 64;

	final String id;
	final String version;

	ProfileMod(@Nonnull final String id, @Nonnull final String version) {
		this.id = id;
		this.version = version;
	}

	@Nonnull
	static List<ProfileMod> capture() {
		final Map<String, String> versions = new TreeMap<>();
		for (final ModContainer mod : Loader.instance().getActiveModList())
			versions.putIfAbsent(mod.getModId(), mod.getVersion());
		return of(versions);
	}

	@Nonnull
	static List<ProfileMod> of(@Nonnull final Map<String, String> versions) {
		return versions.entrySet().stream().map(e -> new ProfileMod(clean(e.getKey()), clean(e.getValue())))
				.sorted(Comparator.comparing((ProfileMod m) -> m.id)).collect(Collectors.toList());
	}

	/**
	 * Printable ASCII only, no slashes or colons, at most {@link #MAX_LENGTH} characters; "?" for nothing
	 */
	@Nonnull
	static String clean(@Nullable final String text) {
		if (text == null || text.trim().isEmpty())
			return "?";
		final StringBuilder cleaned = new StringBuilder(Math.min(text.length(), MAX_LENGTH));
		for (int i = 0; i < text.length() && cleaned.length() < MAX_LENGTH; i++) {
			final char c = text.charAt(i);
			cleaned.append(c >= 0x20 && c < 0x7F && c != '/' && c != '\\' && c != ':' ? c : '?');
		}
		return cleaned.toString().trim();
	}
}
