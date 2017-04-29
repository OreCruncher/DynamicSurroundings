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

package org.blockartistry.DynSurround.scripts.json;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModLog;
import org.blockartistry.DynSurround.data.xface.BiomeConfig;
import org.blockartistry.DynSurround.data.xface.BlockConfig;
import org.blockartistry.DynSurround.data.xface.DimensionConfig;
import org.blockartistry.DynSurround.data.xface.ItemConfig;
import org.blockartistry.DynSurround.registry.BiomeRegistry;
import org.blockartistry.DynSurround.registry.BlockRegistry;
import org.blockartistry.DynSurround.registry.DimensionRegistry;
import org.blockartistry.DynSurround.registry.FootstepsRegistry;
import org.blockartistry.DynSurround.registry.ItemRegistry;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.lib.JsonUtils;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;

import net.minecraftforge.fml.relauncher.Side;

public final class ConfigurationScript {

	public static class ForgeEntry {
		@SerializedName("acousticProfile")
		public String acousticProfile = null;

		@SerializedName("dictionaryEntries")
		public List<String> dictionaryEntries = ImmutableList.of();
	}

	@SerializedName("biomes")
	public List<BiomeConfig> biomes = ImmutableList.of();

	@SerializedName("biomeAlias")
	public Map<String, String> biomeAlias = new HashMap<String, String>();

	@SerializedName("blocks")
	public List<BlockConfig> blocks = ImmutableList.of();

	@SerializedName("dimensions")
	public List<DimensionConfig> dimensions = ImmutableList.of();

	@SerializedName("footsteps")
	public Map<String, String> footsteps = new HashMap<String, String>();

	@SerializedName("forgeMappings")
	public List<ForgeEntry> forgeMappings = ImmutableList.of();
	
	@SerializedName("itemConfig")
	public ItemConfig itemConfig = new ItemConfig();

	public static void process(@Nonnull Side side, @Nonnull final Reader reader) {

		try {
			
			final ConfigurationScript script = JsonUtils.load(reader, ConfigurationScript.class);
			final DimensionRegistry dimensions = RegistryManager.get(RegistryType.DIMENSION);

			for (final DimensionConfig dimension : script.dimensions)
				dimensions.register(dimension);

			// Do this first - config may want to alias biomes and that
			// needs to happen before processing actual biomes
			final BiomeRegistry biomes = RegistryManager.get(RegistryType.BIOME);
			for (final Entry<String, String> entry : script.biomeAlias.entrySet())
				biomes.registerBiomeAlias(entry.getKey(), entry.getValue());

			for (final BiomeConfig biome : script.biomes)
				biomes.register(biome);

			// We don't want to process these items if the mod is running
			// on the server - they apply only to client side.
			if(side == Side.SERVER)
				return;

			final BlockRegistry blocks = RegistryManager.get(RegistryType.BLOCK);
			for (final BlockConfig block : script.blocks)
				blocks.register(block);

			final FootstepsRegistry footsteps = RegistryManager.get(RegistryType.FOOTSTEPS);
			for (final ForgeEntry entry : script.forgeMappings) {
				for (final String name : entry.dictionaryEntries)
					footsteps.registerForgeEntries(entry.acousticProfile, name);
			}

			for (final Entry<String, String> entry : script.footsteps.entrySet()) {
				footsteps.registerBlocks(entry.getValue(), entry.getKey());
			}
			
			final ItemRegistry itemRegistry = RegistryManager.get(RegistryType.ITEMS);
			itemRegistry.register(script.itemConfig);
			
		} catch (final Throwable t) {
			ModLog.error("Unable to process configuration script", t);
		}
	}
}
