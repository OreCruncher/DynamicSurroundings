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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

// The cases are biomes from a survey (/dsdump biomes) of vanilla, Biomes O' Plenty, Biomes You Go, Traverse and
// AbyssalCraft
public class BiomeTraitsTests {

	private static Set<String> set(final String types) {
		return types.isEmpty() ? Collections.emptySet() : new HashSet<>(Arrays.asList(types.split(" ")));
	}

	private static BiomeTraits traits(final String path, final String name, final String types, final float temp,
			final float rain, final String top) {
		return BiomeTraits.from(new BiomeEvidence(path, name, set(types), temp, rain, true, false, false, top));
	}

	@Test
	public void aBiomeWithAKindKeepsItsTypes() {
		// Guessing over these made vanilla's forest dense and its ocean wet
		assertEquals(set(""), traits("forest", "Forest", "FOREST", 0.7F, 0.8F, "minecraft:grass").added());
		assertEquals(set("").size(), traits("ocean", "Ocean", "OCEAN WATER", 0.5F, 0.5F, "minecraft:grass").added().size());
		assertEquals(set(""), traits("rocky_plains", "Rocky Plains", "HILLS PLAINS SPARSE", 0.9F, 0.7F, "minecraft:grass").added());
		// ...or a dimension
		assertEquals(set(""), traits("undergarden", "Undergarden", "HOT LUSH NETHER", 2F, 0F, "minecraft:netherrack").added());
	}

	@Test
	public void whatFollowsIsAddedToAnyBiome() {
		final BiomeTraits alps = traits("balps", "Alps", "HILLS MOUNTAIN SNOWY", -0.5F, 0.5F, "minecraft:snow");
		assertEquals(set("COLD"), alps.added());
		assertEquals(TraitSource.DERIVED, alps.sourceOf("COLD"));
	}

	@Test
	public void aBiomeWithoutAKindIsWorkedOutFromItsName() {
		final BiomeTraits swamp = traits("abyssal_swamp", "Abyssal Swamp", "DEAD", 0.5F, 0.5F, "abyssalcraft:fusedabyssalsand");
		assertEquals(set("SWAMP WET"), swamp.added());
		assertEquals(TraitSource.NAME, swamp.sourceOf("SWAMP"));
		assertEquals(TraitSource.DICTIONARY, swamp.sourceOf("DEAD"));
		assertEquals("DEAD (biome dictionary); SWAMP, WET (name)", swamp.describe());

		final BiomeTraits ocean = traits("dreadlands_ocean", "Dreadlands Ocean", "DEAD", 0.5F, 0.5F, "minecraft:air");
		assertEquals(set("OCEAN WET SPOOKY WATER"), ocean.added());
		assertEquals(TraitSource.DERIVED_FROM_GUESS, ocean.sourceOf("WATER"));

		// The id's words run together; the name's don't
		assertEquals(set("FOREST DENSE"), traits("bzelkovaforest", "Zelkova Forest", "COLD CONIFEROUS", 0.25F, 0.5F, "minecraft:grass").added());
		assertEquals(set("SWAMP"), traits("marsh", "Marsh", "LUSH WET", 0.6F, 0.8F, "minecraft:grass").added());
	}

	@Test
	public void aClimateSettlesTemperatureAndMoisture() {
		// Temperate and neither wet nor dry: the name's HOT and DRY don't get in
		final BiomeTraits t = traits("ash_desert", "Ash Desert", "", 0.6F, 0.5F, "minecraft:gravel");
		assertEquals(set("SANDY"), t.added());
		// A hot climate
		final BiomeTraits hot = traits("ash_desert", "Ash Desert", "", 2F, 0F, "minecraft:gravel");
		assertEquals(set("HOT DRY SANDY"), hot.added());
		assertEquals(TraitSource.CLIMATE, hot.sourceOf("HOT"));
		// The defaults say nothing: the name fills them
		final BiomeTraits d = traits("abyssal_desert", "Abyssal Desert", "DEAD", 0.5F, 0.5F, "abyssalcraft:abyssalsand");
		assertEquals(set("HOT DRY SANDY"), d.added());
		assertEquals(TraitSource.NAME, d.sourceOf("HOT"));
	}

	@Test
	public void theTopBlockComesLast() {
		final BiomeTraits w = traits("abyssal_wastelands", "Abyssal Wasteland", "DEAD", 0.5F, 0.5F, "abyssalcraft:abyssalsand");
		assertEquals(TraitSource.TOP_BLOCK, w.sourceOf("SANDY"));
		// Sand in a swamp says little
		assertFalse(traits("abyssal_swamp", "Abyssal Swamp", "DEAD", 0.5F, 0.5F, "minecraft:sand").names().contains("SANDY"));
		assertTrue(traits("x", "Frozen Thing", "", 0.5F, 0.5F, "minecraft:packed_ice").names().contains("SNOWY"));
	}

	@Test
	public void wordsAreWholeWords() {
		assertTrue(BiomeNameTraits.of("snowy_peaks").contains("SNOWY"));
		assertTrue(BiomeNameTraits.of("Shrublands").contains("PLAINS"));
		assertTrue(BiomeNameTraits.of("autumnal_woodlands").contains("FOREST"));
		assertFalse(BiomeNameTraits.of("alpine_dice").contains("CONIFEROUS"));
		assertFalse(BiomeNameTraits.of("alpine_dice").contains("SNOWY"));
		assertFalse(BiomeNameTraits.of("seasonal_forest").contains("OCEAN"));
	}

	@Test
	public void guessesDontConflict() {
		// A nether name's DRY, a swamp's WET: wet wins
		final BiomeTraits t = traits("nether_swamp", "Nether Swamp", "", 0.5F, 0.5F, "minecraft:netherrack");
		assertTrue(t.names().contains("WET"));
		assertFalse(t.names().contains("DRY"));
	}
}
