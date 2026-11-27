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

package org.orecruncher.dsurround.registry.biome.traits;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Types suggested by the words of a biome's id and name, from the 26.x line's name fallback with Forge's types: the
 * last thing looked at, for what nothing else said. Both the id and the name are read, as some mods run an id's
 * words together (Biomes You Go's "bzelkovaforest" is "Zelkova Forest").
 * <p>
 * Words are split at underscores, slashes and spaces, and a word must be one looked for, or that with a common ending
 * ({@link #ENDINGS}): "snowy" and "woodlands" count, but "ice" isn't found in "dice" nor "pine" in "alpine".
 */
public final class BiomeNameTraits {

	// Endings a word may have past the one looked for: plurals, -y (snowy), -ed (forested), -lands (shrublands)...
	static final List<String> ENDINGS = Arrays.asList("", "s", "es", "y", "ies", "ed", "ing", "ous", "land", "lands",
			"wood", "woods");

	private BiomeNameTraits() {
	}

	@Nonnull
	public static Set<String> of(@Nonnull final String text) {
		final Set<String> words = words(text);
		final Set<String> r = new HashSet<>();
		if (has(words, "nether"))
			add(r, "NETHER", "HOT", "DRY");
		if (has(words, "ocean", "sea"))
			add(r, "OCEAN", "WET");
		if (has(words, "river", "stream", "lake"))
			add(r, "RIVER", "WET");
		if (has(words, "forest", "wood", "grove", "thicket"))
			add(r, "FOREST", "DENSE");
		if (has(words, "taiga", "pine", "spruce", "conifer"))
			add(r, "FOREST", "CONIFEROUS");
		if (has(words, "jungle", "tropic", "tropics"))
			add(r, "JUNGLE", "HOT", "WET");
		if (has(words, "swamp", "bog", "marsh", "bayou", "quagmire", "fen", "mire"))
			add(r, "SWAMP", "WET");
		if (has(words, "field", "plain", "prairie", "grassland", "meadow", "steppe", "clearing", "shrub", "pasture"))
			add(r, "PLAINS");
		if (has(words, "savanna", "savannah"))
			add(r, "SAVANNA");
		if (has(words, "plateau", "highland", "hill"))
			add(r, "HILLS");
		if (has(words, "tundra"))
			add(r, "PLAINS", "SNOWY");
		if (has(words, "glacier"))
			add(r, "SNOWY", "COLD");
		if (has(words, "desert", "dune", "outback"))
			add(r, "SANDY", "HOT", "DRY");
		if (has(words, "badland", "wasteland", "waste"))
			add(r, "WASTELAND", "DRY");
		if (has(words, "mesa", "canyon"))
			add(r, "MESA");
		if (has(words, "beach", "shore"))
			add(r, "BEACH");
		if (has(words, "snow", "frozen", "ice", "icy", "frost"))
			add(r, "SNOWY", "COLD");
		if (has(words, "peak", "slope", "mountain", "cliff", "alps"))
			add(r, "MOUNTAIN");
		if (has(words, "mushroom", "fungal", "fungi", "shroom"))
			add(r, "MUSHROOM");
		if (has(words, "dark", "dead", "dread", "spooky", "haunted"))
			add(r, "SPOOKY");
		if (has(words, "magic", "magical", "enchanted"))
			add(r, "MAGICAL");
		return r;
	}

	@Nonnull
	static Set<String> words(@Nonnull final String text) {
		final Set<String> result = new HashSet<>(Arrays.asList(text.toLowerCase(Locale.ROOT).split("[_/\\s]+")));
		result.remove("");
		return result;
	}

	/**
	 * Whether one of the words is one of {@code wanted}, possibly with one of the {@link #ENDINGS}
	 */
	static boolean has(@Nonnull final Set<String> words, @Nonnull final String... wanted) {
		for (final String part : words)
			for (final String word : wanted)
				if (part.startsWith(word) && ENDINGS.contains(part.substring(word.length())))
					return true;
		return false;
	}

	private static void add(@Nonnull final Set<String> to, @Nonnull final String... types) {
		Collections.addAll(to, types);
	}
}
