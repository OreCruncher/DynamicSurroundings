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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * A biome's Forge biome dictionary types, the ones its mod gave and those worked out for it, and where each came
 * from: the 26.x line's biome traits brought to 1.12.2's types, so biome configs (biome.isSwamp) match biomes their
 * mod typed too loosely. AbyssalCraft types most of its biomes only DEAD, its swamp, lake, forest and ocean alike.
 * <p>
 * A biome whose mod gave it a kind (forest, plains, swamp...) or a dimension (nether, end) keeps the types it was
 * given: those are taken as meant, a type it lacks as one it hasn't (Forge has no temperate: a biome neither HOT nor
 * COLD is one), and guessing over them made vanilla's forest dense and its oceans wet. Only what follows from them is
 * added (snowy is cold). A biome with neither is worked out from its climate (unless left at the defaults), then its
 * name, then its top block, each filling only the groups ({@link TraitGroup}) nothing before it filled, then what
 * follows from those; last, conflicting guesses are cleaned up.
 */
public final class BiomeTraits {

	private static final Set<String> WET_KINDS = new HashSet<>(Arrays.asList("BEACH", "OCEAN", "RIVER", "SWAMP"));

	// In the order added
	private final Map<String, TraitSource> types = new LinkedHashMap<>();
	// Groups a guess settled with nothing in them (a temperate climate)
	private final Set<TraitGroup> settled = EnumSet.noneOf(TraitGroup.class);

	private BiomeTraits() {
	}

	@Nonnull
	public static BiomeTraits from(@Nonnull final BiomeEvidence biome) {
		final BiomeTraits result = new BiomeTraits();
		for (final String t : biome.types)
			result.types.put(t, TraitSource.DICTIONARY);
		result.add(derive(result.names()), TraitSource.DERIVED);

		if (result.hasAny(TraitGroup.KIND) || result.hasAny(TraitGroup.DIMENSION))
			return result;

		if (biome.hasClimate()) {
			result.add(climate(biome), TraitSource.CLIMATE);
			// The climate says how warm and wet it is, if only that it is neither
			result.settled.add(TraitGroup.TEMPERATURE);
			result.settled.add(TraitGroup.MOISTURE);
		}
		result.add(BiomeNameTraits.of(biome.path + " " + biome.name), TraitSource.NAME);
		result.add(topBlock(biome.topBlock, result.names()), TraitSource.TOP_BLOCK);
		result.add(derive(result.names()), TraitSource.DERIVED_FROM_GUESS);
		result.cleanup();
		return result;
	}

	/**
	 * The types, given and worked out
	 */
	@Nonnull
	public Set<String> names() {
		return Collections.unmodifiableSet(this.types.keySet());
	}

	/**
	 * The types worked out, not given
	 */
	@Nonnull
	public Set<String> added() {
		return this.types.entrySet().stream().filter(e -> e.getValue() != TraitSource.DICTIONARY).map(Map.Entry::getKey)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public TraitSource sourceOf(@Nonnull final String type) {
		return this.types.get(type);
	}

	/**
	 * The types by where they came from, as the biome dump and inspection show them: "DEAD (biome dictionary); SWAMP,
	 * WET (name)"
	 */
	@Nonnull
	public String describe() {
		if (this.types.isEmpty())
			return "none";
		final Map<TraitSource, List<String>> bySource = new EnumMap<>(TraitSource.class);
		this.types.forEach((t, s) -> bySource.computeIfAbsent(s, k -> new ArrayList<>()).add(t));
		return bySource.entrySet().stream()
				.map(e -> e.getValue().stream().sorted().collect(Collectors.joining(", ")) + " (" + e.getKey().label() + ")")
				.collect(Collectors.joining("; "));
	}

	@Override
	@Nonnull
	public String toString() {
		return describe();
	}

	private boolean hasAny(@Nonnull final TraitGroup group) {
		return !Collections.disjoint(this.types.keySet(), group.members());
	}

	/**
	 * Adds types from a source. A guess can't add to a group that already had a type (or was settled) before it;
	 * types in no group it always adds.
	 */
	private void add(@Nonnull final Set<String> found, @Nonnull final TraitSource source) {
		final Set<TraitGroup> filled = EnumSet.noneOf(TraitGroup.class);
		if (source.isGuess()) {
			filled.addAll(this.settled);
			for (final String t : this.types.keySet()) {
				final TraitGroup g = TraitGroup.of(t);
				if (g != null)
					filled.add(g);
			}
		}
		for (final String t : found.stream().sorted().collect(Collectors.toList())) {
			if (this.types.containsKey(t))
				continue;
			final TraitGroup g = TraitGroup.of(t);
			if (g == null || !filled.contains(g))
				this.types.put(t, source);
		}
	}

	/**
	 * What follows from types: snowy is cold; an ocean or river is water (Forge's own supertype)
	 */
	@Nonnull
	static Set<String> derive(@Nonnull final Set<String> types) {
		final Set<String> r = new HashSet<>();
		if (types.contains("SNOWY"))
			r.add("COLD");
		if (types.contains("OCEAN") || types.contains("RIVER"))
			r.add("WATER");
		return r;
	}

	/**
	 * Forge's own guess from a climate (BiomeDictionary.makeBestGuess): hot over 0.85, cold under 0.15; wet with
	 * rainfall over 0.85 or high humidity, dry under 0.15 or where it never rains or snows
	 */
	@Nonnull
	static Set<String> climate(@Nonnull final BiomeEvidence biome) {
		final Set<String> r = new HashSet<>();
		if (biome.temperature > 0.85F)
			r.add("HOT");
		else if (biome.temperature < 0.15F)
			r.add("COLD");
		if (biome.rainfall > 0.85F || biome.highHumidity)
			r.add("WET");
		else if (biome.rainfall < 0.15F || (!biome.canRain && !biome.snows))
			r.add("DRY");
		return r;
	}

	/**
	 * Sand on top is sandy (not on a beach, in water or a swamp, where sand says little), snow is snowy, ice snowy and
	 * cold, mycelium a mushroom biome
	 */
	@Nonnull
	static Set<String> topBlock(@Nonnull final String block, @Nonnull final Set<String> types) {
		final Set<String> r = new HashSet<>();
		final int colon = block.indexOf(':');
		final String path = colon < 0 ? block : block.substring(colon + 1);
		if (path.endsWith("sand") && Collections.disjoint(types, WET_KINDS))
			r.add("SANDY");
		if ("minecraft:snow".equals(block) || "minecraft:snow_layer".equals(block))
			r.add("SNOWY");
		if ("minecraft:ice".equals(block) || "minecraft:packed_ice".equals(block)) {
			r.add("SNOWY");
			r.add("COLD");
		}
		if ("minecraft:mycelium".equals(block))
			r.add("MUSHROOM");
		return r;
	}

	/**
	 * Conflicting guesses: wet over dry, hot over cold, dense over sparse, a river over an ocean. Given types are never
	 * taken away.
	 */
	private void cleanup() {
		drop("WET", "DRY");
		drop("HOT", "COLD");
		drop("DENSE", "SPARSE");
		drop("RIVER", "OCEAN");
	}

	private void drop(@Nonnull final String keep, @Nonnull final String loser) {
		final TraitSource s = this.types.get(loser);
		if (this.types.containsKey(keep) && s != null && s.isGuess())
			this.types.remove(loser);
	}
}
