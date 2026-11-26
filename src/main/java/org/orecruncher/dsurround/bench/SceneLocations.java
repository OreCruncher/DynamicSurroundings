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

package org.orecruncher.dsurround.bench;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

/**
 * The scenes a bench world remembers, in its folder ({@code dsurround-bench.json}): each found by its biome or marked
 * with /dsbench mark, with the world's seed and the mods they were found with (worldgen mods move biomes).
 */
final class SceneLocations {

	static final String FILE = "dsurround-bench.json";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * Where a scene is: its biome, the place and where to look, the share of the ground around that is its biome, and
	 * whether it was marked by hand (used as it is) rather than found
	 */
	static final class Location {
		String biome;
		double x;
		double y;
		double z;
		float yaw;
		float pitch;
		double biomeShare;
		boolean marked;

		Location(@Nonnull final String biome, final double x, final double y, final double z, final float yaw,
				final float pitch, final double biomeShare, final boolean marked) {
			this.biome = biome;
			this.x = x;
			this.y = y;
			this.z = z;
			this.yaw = yaw;
			this.pitch = pitch;
			this.biomeShare = biomeShare;
			this.marked = marked;
		}
	}

	private final Path file;
	private final Map<String, Location> scenes = new LinkedHashMap<>();
	private long seed;
	// The mods the scenes were found with, id@version; empty if not known
	private List<String> mods = Collections.emptyList();

	private SceneLocations(@Nonnull final Path file) {
		this.file = file;
	}

	@Nonnull
	static SceneLocations load(@Nonnull final Path worldFolder) throws IOException {
		final SceneLocations locations = new SceneLocations(worldFolder.resolve(FILE));
		if (Files.exists(locations.file)) {
			final JsonObject json = new JsonParser()
					.parse(new String(Files.readAllBytes(locations.file), StandardCharsets.UTF_8)).getAsJsonObject();
			locations.seed = json.has("seed") ? json.get("seed").getAsLong() : 0L;
			if (json.has("mods") && json.get("mods").isJsonArray()) {
				final List<String> mods = new ArrayList<>();
				json.getAsJsonArray("mods").forEach(m -> mods.add(m.getAsString()));
				locations.mods = mods;
			}
			if (json.has("scenes"))
				for (final Map.Entry<String, JsonElement> entry : json.getAsJsonObject("scenes").entrySet())
					locations.scenes.put(entry.getKey(), GSON.fromJson(entry.getValue(), Location.class));
		}
		return locations;
	}

	@Nullable
	Location get(@Nonnull final String scene) {
		return this.scenes.get(scene);
	}

	@Nonnull
	Map<String, Location> all() {
		return this.scenes;
	}

	/**
	 * The mods added (+) and gone (-) since the scenes were found
	 */
	@Nonnull
	List<String> modChanges() {
		if (this.mods.isEmpty())
			return Collections.emptyList();
		final List<String> now = mods();
		final List<String> changes = new ArrayList<>();
		now.stream().filter(m -> !this.mods.contains(m)).forEach(m -> changes.add("+" + m));
		this.mods.stream().filter(m -> !now.contains(m)).forEach(m -> changes.add("-" + m));
		return changes;
	}

	void put(@Nonnull final String scene, @Nonnull final Location location, final long seed) throws IOException {
		this.scenes.put(scene, location);
		this.seed = seed;
		this.mods = mods();
		save();
	}

	private void save() throws IOException {
		final JsonObject json = new JsonObject();
		json.addProperty("seed", this.seed);
		final JsonArray mods = new JsonArray();
		this.mods.forEach(mods::add);
		json.add("mods", mods);
		final JsonObject scenes = new JsonObject();
		this.scenes.forEach((name, location) -> scenes.add(name, GSON.toJsonTree(location)));
		json.add("scenes", scenes);
		Files.write(this.file, GSON.toJson(json).getBytes(StandardCharsets.UTF_8));
	}

	@Nonnull
	static List<String> mods() {
		final Set<String> mods = new TreeSet<>();
		for (final ModContainer mod : Loader.instance().getActiveModList())
			mods.add(mod.getModId() + "@" + mod.getVersion());
		return new ArrayList<>(mods);
	}
}
