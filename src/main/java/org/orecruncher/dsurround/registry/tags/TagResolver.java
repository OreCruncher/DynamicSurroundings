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

package org.orecruncher.dsurround.registry.tags;

import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Turns tag definitions into their members, with no game in it, as the 26.x line's client tag loader does for the
 * mod's own tags. Tags are defined by files laid out as 26.x's: {@code dsurround:effects/floor_squeaks} by every
 * {@code assets/<namespace>/dsconfigs/tags/block/effects/floor_squeaks.json} ({@link #filePath}), whatever the
 * namespace, each {@code {"values": [...]}} ({@link #parseFile}); a tag in several files has all their entries. An entry is a member's name (a block, an {@code ore:} name: what the lookup makes of it) or
 * {@code #tag}, every member of another tag. Names without a namespace are the mod's ({@code floor_squeaks} is
 * {@code dsurround:floor_squeaks}). A tag naming itself through others, or one that isn't defined, is reported and
 * adds nothing.
 */
public final class TagResolver {

	public static final String NAMESPACE = "dsurround";

	// The folder within each namespace's assets that holds the mod's configuration, as in the 26.x line
	public static final String CONFIG_FOLDER = "dsconfigs";

	private TagResolver() {
	}

	/**
	 * A tag's full name: lower case, with the mod's namespace if it has none
	 */
	@Nonnull
	public static String normalize(@Nonnull final String name) {
		final String n = name.trim().toLowerCase(Locale.ROOT);
		return n.indexOf(':') < 0 ? NAMESPACE + ":" + n : n;
	}

	/**
	 * Where a tag's files are within a namespace's assets: {@code dsurround:effects/floor_squeaks} is
	 * {@code dsconfigs/tags/block/effects/floor_squeaks.json}. As in the 26.x line, the tag's namespace doesn't
	 * choose the folder: every namespace's file at that path adds to the tag.
	 */
	@Nonnull
	public static String filePath(@Nonnull final String tag) {
		final String name = normalize(tag);
		return CONFIG_FOLDER + "/tags/block/" + name.substring(name.indexOf(':') + 1) + ".json";
	}

	/**
	 * A tag file's entries, as the game's tag files have them (26.x's are the same):
	 * {@code {"replace": false, "values": ["minecraft:planks", {"id": "ore:plankWood", "required": false}]}}.
	 * {@code replace} is ignored, as 26.x does: files only ever add to a tag.
	 *
	 * @throws IllegalArgumentException if the file isn't one
	 */
	@Nonnull
	public static List<String> parseFile(@Nonnull final Reader reader) {
		final JsonElement root;
		try {
			root = new JsonParser().parse(reader);
		} catch (final RuntimeException e) {
			throw new IllegalArgumentException("not valid JSON: " + e.getMessage(), e);
		}
		if (root == null || !root.isJsonObject())
			throw new IllegalArgumentException("expected an object with \"values\"");
		final JsonElement values = root.getAsJsonObject().get("values");
		if (values == null || !values.isJsonArray())
			throw new IllegalArgumentException("expected \"values\" to be a list");
		final List<String> entries = new ArrayList<>();
		for (final JsonElement value : (JsonArray) values) {
			if (value.isJsonPrimitive()) {
				entries.add(value.getAsString());
			} else if (value.isJsonObject() && ((JsonObject) value).has("id")) {
				entries.add(((JsonObject) value).get("id").getAsString());
			} else {
				throw new IllegalArgumentException("an entry is neither a name nor {\"id\": name}: " + value);
			}
		}
		return entries;
	}

	/**
	 * Reads the files of the tags asked for and of every tag they name, adding them to the definitions
	 *
	 * @param definitions each tag's entries so far, by full name (from the config files' {@code tags} sections)
	 * @param tags        the tags the mod uses
	 * @param files       the entries of a tag's files, by its full name (empty if it has none)
	 */
	public static void load(@Nonnull final Map<String, List<String>> definitions,
			@Nonnull final Collection<String> tags, @Nonnull final Function<String, List<String>> files) {
		final Deque<String> pending = new ArrayDeque<>();
		final Set<String> seen = new HashSet<>();
		for (final String tag : tags)
			pending.add(normalize(tag));
		pending.addAll(definitions.keySet());
		while (!pending.isEmpty()) {
			final String name = pending.poll();
			if (!seen.add(name))
				continue;
			final List<String> found = files.apply(name);
			if (!found.isEmpty())
				definitions.computeIfAbsent(name, k -> new ArrayList<>()).addAll(found);
			final List<String> entries = definitions.get(name);
			if (entries == null)
				continue;
			for (final String raw : entries) {
				final String entry = raw.trim();
				if (!entry.isEmpty() && entry.charAt(0) == '#')
					pending.add(normalize(entry.substring(1)));
			}
		}
	}

	/**
	 * Adds a config file's {@code tags} section to the definitions gathered so far: a tag defined in several files has all their entries
	 */
	public static void merge(@Nonnull final Map<String, List<String>> into,
			@Nonnull final Map<String, List<String>> definitions) {
		definitions.forEach((name, entries) -> {
			if (entries != null)
				into.computeIfAbsent(normalize(name), k -> new ArrayList<>()).addAll(entries);
		});
	}

	/**
	 * Every tag's members
	 *
	 * @param definitions each tag's entries, by full name
	 * @param lookup      the members an entry that isn't a tag names (empty for an unknown one, which it reports)
	 * @param newSet      makes a tag's set of members
	 * @param warn        told of tags that can't be resolved
	 */
	@Nonnull
	public static <T> Map<String, Set<T>> resolve(@Nonnull final Map<String, List<String>> definitions,
			@Nonnull final Function<String, Collection<T>> lookup, @Nonnull final Supplier<Set<T>> newSet,
			@Nonnull final Consumer<String> warn) {
		final Map<String, Set<T>> done = new LinkedHashMap<>();
		for (final String name : definitions.keySet())
			resolve(name, definitions, lookup, newSet, warn, done, new ArrayDeque<>());
		return done;
	}

	@Nonnull
	private static <T> Set<T> resolve(@Nonnull final String name, @Nonnull final Map<String, List<String>> definitions,
			@Nonnull final Function<String, Collection<T>> lookup, @Nonnull final Supplier<Set<T>> newSet,
			@Nonnull final Consumer<String> warn, @Nonnull final Map<String, Set<T>> done,
			@Nonnull final Deque<String> path) {
		final Set<T> known = done.get(name);
		if (known != null)
			return known;
		final Set<T> members = newSet.get();
		if (path.contains(name)) {
			warn.accept("Tag " + name + " includes itself: " + String.join(" -> ", path) + " -> " + name);
			return members;
		}
		final List<String> entries = definitions.get(name);
		if (entries == null) {
			warn.accept("Tag " + name + " is not defined" + (path.isEmpty() ? "" : " (named by " + path.peekLast() + ")"));
			done.put(name, members);
			return members;
		}
		path.addLast(name);
		final Set<String> seen = new HashSet<>();
		for (final String raw : entries) {
			final String entry = raw.trim();
			if (entry.isEmpty() || !seen.add(entry))
				continue;
			if (entry.charAt(0) == '#')
				members.addAll(resolve(normalize(entry.substring(1)), definitions, lookup, newSet, warn, done, path));
			else
				members.addAll(lookup.apply(entry));
		}
		path.removeLast();
		done.put(name, members);
		return members;
	}
}
