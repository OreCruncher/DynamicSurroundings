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
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Forge biome dictionary types a guess may only add when the biome has none of the group yet: its dimension, how warm
 * it is, how wet, how much grows, and what kind of place it is. A biome typed forest by its mod doesn't become a
 * swamp because of its name. Other types (snowy, sandy, coniferous, spooky, rare...) describe a biome of any kind, and
 * guesses add to them. As in the 26.x line's biome traits, with Forge's types for traits.
 * <p>
 * Forge has no temperate or "neither wet nor dry": a biome with none of those types is one. So a climate that says
 * so settles the group with nothing in it ({@link BiomeTraits}).
 */
public enum TraitGroup {

	DIMENSION("NETHER", "END", "VOID"),
	TEMPERATURE("HOT", "COLD"),
	MOISTURE("WET", "DRY"),
	VEGETATION("DENSE", "SPARSE"),
	KIND("FOREST", "PLAINS", "MOUNTAIN", "HILLS", "SWAMP", "OCEAN", "RIVER", "BEACH", "JUNGLE", "SAVANNA", "MESA",
			"WASTELAND", "MUSHROOM");

	private final Set<String> members;

	TraitGroup(@Nonnull final String... members) {
		this.members = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(members)));
	}

	@Nonnull
	public Set<String> members() {
		return this.members;
	}

	/**
	 * The group a type is in, or null if it isn't in one
	 */
	@Nullable
	public static TraitGroup of(@Nonnull final String type) {
		for (final TraitGroup group : values())
			if (group.members.contains(type))
				return group;
		return null;
	}
}
