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

package org.blockartistry.mod.DynSurround.scripts.json;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.Module;
import org.blockartistry.mod.DynSurround.data.xface.BiomeConfig;
import org.blockartistry.mod.DynSurround.data.xface.Biomes;
import org.blockartistry.mod.DynSurround.data.xface.BlockClass;
import org.blockartistry.mod.DynSurround.data.xface.BlockConfig;
import org.blockartistry.mod.DynSurround.data.xface.Blocks;
import org.blockartistry.mod.DynSurround.data.xface.DimensionConfig;
import org.blockartistry.mod.DynSurround.data.xface.Dimensions;
import org.blockartistry.mod.DynSurround.data.xface.Footsteps;
import org.blockartistry.mod.DynSurround.util.JsonUtils;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;

public final class ConfigurationScript {

	public static class ForgeEntry {
		@SerializedName("blockClass")
		public String blockClass = null;

		@SerializedName("dictionaryEntries")
		public List<String> dictionaryEntries = ImmutableList.of();
	}

	@SerializedName("biomes")
	public List<BiomeConfig> biomes = ImmutableList.of();

	@SerializedName("blocks")
	public List<BlockConfig> blocks = ImmutableList.of();

	@SerializedName("dimensions")
	public List<DimensionConfig> dimensions = ImmutableList.of();

	@SerializedName("footsteps")
	public Map<String, String> footsteps = new HashMap<String, String>();

	@SerializedName("forgeMappings")
	public List<ForgeEntry> forgeMappings = ImmutableList.of();

	public static void process(final Reader reader) {
		final ConfigurationScript script = JsonUtils.load(reader, ConfigurationScript.class);

		if (script != null) {

			for (final DimensionConfig dimension : script.dimensions)
				Dimensions.register(dimension);

			// We don't want to process these items if the mod is running
			// on the server - they apply only to client side.
			if (!Module.proxy().isRunningAsServer()) {
				for (final BiomeConfig biome : script.biomes)
					Biomes.register(biome);

				for (final BlockConfig block : script.blocks)
					Blocks.register(block);

				for (final Entry<String, String> entry : script.footsteps.entrySet()) {
					final BlockClass blockClass = BlockClass.lookup(entry.getValue());
					if (blockClass == null) {
						ModLog.warn("Invalid blockClass for footsteps detected in script: %s", entry.getValue());
						continue;
					}
					Footsteps.registerFootsteps(blockClass, entry.getKey());
				}

				for (final ForgeEntry entry : script.forgeMappings) {
					final BlockClass blockClass = BlockClass.lookup(entry.blockClass);
					if (blockClass == null) {
						ModLog.warn("Invalid blockClass for forgeMappings detected in script: %s", entry.blockClass);
						continue;
					}

					for (final String name : entry.dictionaryEntries)
						Footsteps.registerForgeEntries(blockClass, name);
				}
			}
		}
	}
}
