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
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

/**
 * A bench scenario: the world to run in, the scenes, and the runs, each a scene recorded with some settings. Read from
 * a JSON file (comments allowed); see {@code scripts/bench/README.md} for the format. The 26.x line's format, less
 * what 1.12.2 hasn't (sounds, boats, Improved Transparency).
 */
final class Scenario {

	/**
	 * Where a run is recorded: the biome to find it in if the world doesn't remember it, where to look, the commands
	 * run once the player is there, as the warm-up starts and before the screenshot (and the ticks the shot waits after
	 * those), the /dsprofile scene, how fast to fly east, and how far east and south of its place to arrive
	 */
	static final class Scene {
		@Nullable
		final String biome;
		@Nullable
		final Float yaw;
		@Nullable
		final Float pitch;
		final List<String> commands;
		final List<String> settledCommands;
		final String profile;
		final List<String> shotCommands;
		final int shotDelay;
		final double flySpeed;
		final int offsetX;
		final int offsetZ;

		Scene(@Nonnull final JsonObject s) {
			this.biome = s.has("biome") ? s.get("biome").getAsString() : null;
			this.yaw = s.has("yaw") ? s.get("yaw").getAsFloat() : null;
			this.pitch = s.has("pitch") ? s.get("pitch").getAsFloat() : null;
			this.commands = strings(s, "commands");
			this.settledCommands = strings(s, "settledCommands");
			this.profile = s.has("profile") ? s.get("profile").getAsString() : "general";
			this.shotCommands = strings(s, "shotCommands");
			this.shotDelay = s.has("shotDelay") ? Math.max(0, s.get("shotDelay").getAsInt()) : 0;
			this.flySpeed = s.has("flySpeed") ? s.get("flySpeed").getAsDouble() : 0D;
			this.offsetX = s.has("offset") ? s.getAsJsonArray("offset").get(0).getAsInt() : 0;
			this.offsetZ = s.has("offset") ? s.getAsJsonArray("offset").get(1).getAsInt() : 0;
		}
	}

	/**
	 * One recording: its name in the results, its scene, its settings (the setting sets it uses, in order, then its
	 * own), and what it records in a few words
	 */
	static final class Run {
		final String label;
		final String scene;
		final Map<String, JsonElement> settings;
		final String caption;

		Run(@Nonnull final String label, @Nonnull final String scene, @Nonnull final Map<String, JsonElement> settings,
				@Nonnull final String caption) {
			this.label = label;
			this.scene = scene;
			this.settings = settings;
			this.caption = caption;
		}
	}

	final String name;
	final String world;
	@Nullable
	final Long seed;
	final List<String> setup;
	final int warmup;
	final int record;
	final int repeats;
	@Nullable
	final Boolean vsync;
	@Nullable
	final Integer maxFps;
	// The window's size for the session, if both are given: what a GPU draws scales with the pixels, so runs to
	// compare need the same size
	@Nullable
	final Integer width;
	@Nullable
	final Integer height;
	final boolean screenshot;
	@Nullable
	final Float volume;
	@Nullable
	final Float musicVolume;
	final Map<String, Scene> scenes = new LinkedHashMap<>();
	final List<Run> runs = new ArrayList<>();

	private Scenario(@Nonnull final String name, @Nonnull final JsonObject json) {
		this.name = name;
		final JsonObject sceneJson = json.has("scenes") ? json.getAsJsonObject("scenes") : new JsonObject();
		for (final Map.Entry<String, JsonElement> entry : sceneJson.entrySet())
			this.scenes.put(entry.getKey(), new Scene(entry.getValue().getAsJsonObject()));

		// Named groups of settings, for runs to share ("use"): such as every effect turned off
		final Map<String, JsonObject> sets = new LinkedHashMap<>();
		if (json.has("settingSets"))
			for (final Map.Entry<String, JsonElement> entry : json.getAsJsonObject("settingSets").entrySet())
				sets.put(entry.getKey(), entry.getValue().getAsJsonObject());

		if (json.has("runs")) {
			for (final JsonElement element : json.getAsJsonArray("runs")) {
				final JsonObject r = element.getAsJsonObject();
				final String scene = r.get("scene").getAsString();
				if (!this.scenes.containsKey(scene))
					throw new IllegalArgumentException("run names scene '" + scene + "', which isn't in scenes");
				final Map<String, JsonElement> settings = new LinkedHashMap<>();
				if (r.has("use")) {
					final JsonElement use = r.get("use");
					final List<String> names = new ArrayList<>();
					if (use.isJsonArray())
						use.getAsJsonArray().forEach(n -> names.add(n.getAsString()));
					else
						names.add(use.getAsString());
					for (final String setName : names) {
						final JsonObject set = sets.get(setName);
						if (set == null)
							throw new IllegalArgumentException(
									"run uses setting set '" + setName + "', which isn't in settingSets");
						for (final Map.Entry<String, JsonElement> setting : set.entrySet())
							settings.put(setting.getKey(), setting.getValue());
					}
				}
				if (r.has("settings"))
					for (final Map.Entry<String, JsonElement> setting : r.getAsJsonObject("settings").entrySet())
						settings.put(setting.getKey(), setting.getValue());
				final String label = r.has("label") ? r.get("label").getAsString() : scene + "-" + this.runs.size();
				this.runs.add(new Run(label, scene, settings,
						r.has("caption") ? r.get("caption").getAsString() : label));
			}
		}
		if (this.runs.isEmpty())
			throw new IllegalArgumentException("a scenario needs runs");

		final JsonObject video = json.has("video") ? json.getAsJsonObject("video") : new JsonObject();
		this.world = json.has("world") ? json.get("world").getAsString() : "bench";
		this.seed = json.has("seed") ? json.get("seed").getAsLong() : null;
		this.setup = strings(json, "setup");
		this.warmup = json.has("warmup") ? json.get("warmup").getAsInt() : 15;
		this.record = json.has("record") ? json.get("record").getAsInt() : 60;
		this.repeats = json.has("repeats") ? Math.max(1, json.get("repeats").getAsInt()) : 1;
		this.vsync = video.has("vsync") ? video.get("vsync").getAsBoolean() : null;
		this.maxFps = video.has("maxFps") ? video.get("maxFps").getAsInt() : null;
		this.width = video.has("width") ? video.get("width").getAsInt() : null;
		this.height = video.has("height") ? video.get("height").getAsInt() : null;
		this.screenshot = !json.has("screenshots") || json.get("screenshots").getAsBoolean();
		this.volume = json.has("volume") ? json.get("volume").getAsFloat() : null;
		this.musicVolume = json.has("musicVolume") ? json.get("musicVolume").getAsFloat() : null;
	}

	@Nonnull
	static Scenario read(@Nonnull final Path file) throws IOException {
		try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			final JsonReader json = new JsonReader(reader);
			// Comments allowed
			json.setLenient(true);
			final String name = file.getFileName().toString().replaceFirst("\\.json$", "");
			return parse(name, new JsonParser().parse(json).getAsJsonObject());
		}
	}

	@Nonnull
	static Scenario parse(@Nonnull final String name, @Nonnull final JsonObject json) {
		return new Scenario(name, json);
	}

	/**
	 * Every run in the order they are made: the runs, repeated, every other time reversed (A B, B A), so a drift over
	 * the session doesn't look like a setting's cost
	 */
	@Nonnull
	List<Run> order() {
		final List<Run> order = new ArrayList<>();
		for (int i = 0; i < this.repeats; i++) {
			final List<Run> pass = new ArrayList<>(this.runs);
			if (i % 2 == 1)
				Collections.reverse(pass);
			order.addAll(pass);
		}
		return order;
	}

	@Nonnull
	private static List<String> strings(@Nonnull final JsonObject json, @Nonnull final String member) {
		final List<String> list = new ArrayList<>();
		if (json.has(member)) {
			final JsonArray array = json.getAsJsonArray(member);
			for (final JsonElement element : array)
				list.add(element.getAsString());
		}
		return list;
	}
}
