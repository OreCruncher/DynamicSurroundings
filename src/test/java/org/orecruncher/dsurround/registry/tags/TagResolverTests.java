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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

public class TagResolverTests {

	// Entries resolve to themselves, but for "ore:" names, which give two members, and "unknown", which gives none
	private static Collection<String> lookup(final String entry) {
		if (entry.startsWith("ore:"))
			return Arrays.asList(entry + "/a", entry + "/b");
		if (entry.equals("unknown"))
			return Collections.emptyList();
		return Collections.singletonList(entry);
	}

	private static Map<String, Set<String>> resolve(final Map<String, List<String>> definitions,
			final List<String> warnings) {
		return TagResolver.resolve(definitions, TagResolverTests::lookup, TreeSet::new, warnings::add);
	}

	private static Map<String, List<String>> defs(final Object... pairs) {
		final Map<String, List<String>> raw = new LinkedHashMap<>();
		for (int i = 0; i < pairs.length; i += 2)
			raw.put((String) pairs[i], Arrays.asList((String[]) pairs[i + 1]));
		final Map<String, List<String>> merged = new LinkedHashMap<>();
		TagResolver.merge(merged, raw);
		return merged;
	}

	@Test
	public void namesWithoutANamespaceAreTheMods() {
		assertEquals("dsurround:floor_squeaks", TagResolver.normalize("floor_squeaks"));
		assertEquals("dsurround:floor_squeaks", TagResolver.normalize(" Floor_Squeaks "));
		assertEquals("othermod:floors", TagResolver.normalize("othermod:floors"));
	}

	@Test
	public void entriesAndOreNamesAreLookedUp() {
		final List<String> warnings = new ArrayList<>();
		final Map<String, Set<String>> tags = resolve(
				defs("floor_squeaks", new String[] { "minecraft:planks", "ore:plankWood", "unknown" }), warnings);
		assertEquals(new TreeSet<>(Arrays.asList("minecraft:planks", "ore:plankWood/a", "ore:plankWood/b")),
				tags.get("dsurround:floor_squeaks"));
		assertTrue(warnings.isEmpty());
	}

	@Test
	public void aTagTakesInAnotherTagsMembers() {
		final List<String> warnings = new ArrayList<>();
		final Map<String, Set<String>> tags = resolve(defs("floors", new String[] { "#planks", "minecraft:slab" },
				"planks", new String[] { "minecraft:planks" }), warnings);
		assertEquals(new TreeSet<>(Arrays.asList("minecraft:planks", "minecraft:slab")), tags.get("dsurround:floors"));
		assertEquals(Collections.singleton("minecraft:planks"), tags.get("dsurround:planks"));
		assertTrue(warnings.isEmpty());
	}

	@Test
	public void filesAddToTheSameTag() {
		final Map<String, List<String>> merged = new LinkedHashMap<>();
		TagResolver.merge(merged, Collections.singletonMap("floor_squeaks", Arrays.asList("minecraft:planks")));
		TagResolver.merge(merged, Collections.singletonMap("dsurround:floor_squeaks", Arrays.asList("mod:floor")));
		final Map<String, Set<String>> tags = resolve(merged, new ArrayList<>());
		assertEquals(new TreeSet<>(Arrays.asList("minecraft:planks", "mod:floor")),
				tags.get("dsurround:floor_squeaks"));
	}

	@Test
	public void anUndefinedTagIsReportedAndAddsNothing() {
		final List<String> warnings = new ArrayList<>();
		final Map<String, Set<String>> tags = resolve(defs("floors", new String[] { "#missing", "minecraft:planks" }),
				warnings);
		assertEquals(Collections.singleton("minecraft:planks"), tags.get("dsurround:floors"));
		assertEquals(1, warnings.size());
		assertTrue(warnings.get(0).contains("dsurround:missing"), warnings.get(0));
	}

	@Test
	public void aLoopIsReportedAndEndsThere() {
		final List<String> warnings = new ArrayList<>();
		final Map<String, Set<String>> tags = resolve(
				defs("a", new String[] { "#b", "x" }, "b", new String[] { "#a", "y" }), warnings);
		assertEquals(new HashSet<>(Arrays.asList("x", "y")), tags.get("dsurround:a"));
		assertTrue(tags.get("dsurround:b").containsAll(Arrays.asList("y")));
		assertEquals(1, warnings.size());
		assertTrue(warnings.get(0).contains("includes itself"), warnings.get(0));
	}

	@Test
	public void filesAreLaidOutAs26xs() {
		assertEquals("dsconfigs/tags/block/effects/floor_squeaks.json", TagResolver.filePath("effects/floor_squeaks"));
		assertEquals("dsconfigs/tags/block/effects/floor_squeaks.json",
				TagResolver.filePath(BlockTags.FLOOR_SQUEAKS));
		assertEquals("dsurround:effects/floor_squeaks", BlockTags.FLOOR_SQUEAKS);
		assertTrue(BlockTags.ALL.contains(BlockTags.FLOOR_SQUEAKS));
	}

	@Test
	public void aFileHoldsNamesOrIds() {
		final List<String> entries = TagResolver.parseFile(new StringReader(
				"{\"replace\": false, \"values\": [\"minecraft:planks\", {\"id\": \"ore:plankWood\", \"required\": false}, \"#floors\"]}"));
		assertEquals(Arrays.asList("minecraft:planks", "ore:plankWood", "#floors"), entries);
	}

	@Test
	public void aBrokenFileIsRefused() {
		assertThrows(IllegalArgumentException.class, () -> TagResolver.parseFile(new StringReader("[]")));
		assertThrows(IllegalArgumentException.class,
				() -> TagResolver.parseFile(new StringReader("{\"values\": \"minecraft:planks\"}")));
		assertThrows(IllegalArgumentException.class,
				() -> TagResolver.parseFile(new StringReader("{\"values\": [[1]]}")));
		assertThrows(IllegalArgumentException.class, () -> TagResolver.parseFile(new StringReader("{\"values\": [")));
	}

	@Test
	public void theFilesOfTagsNamedAreReadToo() {
		final Map<String, List<String>> files = new TreeMap<>();
		files.put("dsurround:effects/floor_squeaks", Arrays.asList("#planks", "minecraft:slab"));
		files.put("dsurround:planks", Arrays.asList("minecraft:planks"));
		files.put("dsurround:never_named", Arrays.asList("minecraft:stone"));
		final List<String> asked = new ArrayList<>();
		final Map<String, List<String>> definitions = new TreeMap<>();
		// A config file's tags section adds to the files
		TagResolver.merge(definitions,
				Collections.singletonMap("effects/floor_squeaks", Arrays.asList("mod:floor")));
		TagResolver.load(definitions, Collections.singletonList(BlockTags.FLOOR_SQUEAKS), name -> {
			asked.add(name);
			return files.getOrDefault(name, Collections.emptyList());
		});
		assertEquals(Arrays.asList("mod:floor", "#planks", "minecraft:slab"),
				definitions.get("dsurround:effects/floor_squeaks"));
		assertEquals(Arrays.asList("minecraft:planks"), definitions.get("dsurround:planks"));
		assertEquals(Arrays.asList("dsurround:effects/floor_squeaks", "dsurround:planks"), asked);
		final Map<String, Set<String>> tags = resolve(definitions, new ArrayList<>());
		assertEquals(new TreeSet<>(Arrays.asList("mod:floor", "minecraft:planks", "minecraft:slab")),
				tags.get("dsurround:effects/floor_squeaks"));
	}
}
