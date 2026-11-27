/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.orecruncher.dsurround;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * The one "Special Effect Range" became a block effect range and an entity effect range: a config that had it keeps
 * its value for both
 */
public class EffectRangeMigrationTests {

	private static Configuration config(final String... keysAndValues) throws IOException {
		// No file: Forge's file constructor asks FML for the game folder
		final Configuration config = new Configuration();
		final ConfigCategory effects = config.getCategory(ModOptions.CATEGORY_EFFECTS);
		for (int i = 0; i < keysAndValues.length; i += 2)
			effects.put(keysAndValues[i], new Property(keysAndValues[i], keysAndValues[i + 1], Property.Type.INTEGER));
		return config;
	}

	@Test
	public void theOldRangeIsKeptForBoth() throws IOException {
		final Configuration config = config("Special Effect Range", "20");
		ModOptions.migrateEffectRange(config);
		final ConfigCategory effects = config.getCategory(ModOptions.CATEGORY_EFFECTS);
		assertEquals("20", effects.get("Block Effect Range").getString());
		assertEquals("20", effects.get("Entity Effect Range").getString());
		assertFalse(effects.containsKey("Special Effect Range"));
	}

	@Test
	public void aNewRangeAlreadySetStays() throws IOException {
		final Configuration config = config("Special Effect Range", "20", "Block Effect Range", "50");
		ModOptions.migrateEffectRange(config);
		final ConfigCategory effects = config.getCategory(ModOptions.CATEGORY_EFFECTS);
		assertEquals("50", effects.get("Block Effect Range").getString());
		assertEquals("20", effects.get("Entity Effect Range").getString());
	}

	@Test
	public void aNewConfigGetsTheDefaults() throws IOException {
		final Configuration config = config();
		ModOptions.migrateEffectRange(config);
		assertFalse(config.getCategory(ModOptions.CATEGORY_EFFECTS).containsKey("Block Effect Range"));
	}

	@Test
	public void theOptionsReadTheKeptValue() throws IOException {
		final int block = ModOptions.effects.blockEffectRange;
		final int entity = ModOptions.effects.entityEffectRange;
		try {
			ModOptions.load(config("Special Effect Range", "20"));
			assertEquals(20, ModOptions.effects.blockEffectRange);
			assertEquals(20, ModOptions.effects.entityEffectRange);
			ModOptions.load(config());
			assertEquals(40, ModOptions.effects.blockEffectRange);
			assertEquals(32, ModOptions.effects.entityEffectRange);
		} finally {
			ModOptions.effects.blockEffectRange = block;
			ModOptions.effects.entityEffectRange = entity;
		}
	}
}
