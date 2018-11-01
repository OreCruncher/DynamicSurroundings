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

package org.orecruncher.dsurround.data.xface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;

public final class ModConfigurationFile {

	public static class ForgeEntry {
		@SerializedName("acousticProfile")
		public String acousticProfile = null;

		@SerializedName("dictionaryEntries")
		public List<String> dictionaryEntries = ImmutableList.of();
	}

	@SerializedName("biomes")
	public List<BiomeConfig> biomes = ImmutableList.of();

	@SerializedName("biomeAlias")
	public Map<String, String> biomeAlias = new HashMap<>();

	@SerializedName("blocks")
	public List<BlockConfig> blocks = ImmutableList.of();

	@SerializedName("dimensions")
	public List<DimensionConfig> dimensions = ImmutableList.of();

	@SerializedName("footsteps")
	public Map<String, String> footsteps = new HashMap<>();

	@SerializedName("footprints")
	public List<String> footprints = ImmutableList.of();

	@SerializedName("forgeMappings")
	public List<ForgeEntry> forgeMappings = ImmutableList.of();

	@SerializedName("itemConfig")
	public ItemConfig itemConfig = new ItemConfig();

	@SerializedName("variators")
	public Map<String, VariatorConfig> variators = new HashMap<>();

	@SerializedName("entities")
	public Map<String, EntityConfig> entities = new HashMap<>();

}
