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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The scenario format: what a file says, the order runs are made in, and the shipped scenarios reading.
 */
public class ScenarioTests {

	private static Scenario parse(final String json) {
		return Scenario.parse("test", new JsonParser().parse(json).getAsJsonObject());
	}

	@Test
	public void theRunsAreRepeatedEveryOtherTimeReversed() {
		final Scenario scenario = parse("{\"repeats\": 3, \"scenes\": {\"s\": {}},"
				+ " \"runs\": [{\"label\": \"a\", \"scene\": \"s\"}, {\"label\": \"b\", \"scene\": \"s\"}]}");
		final List<String> order = scenario.order().stream().map(r -> r.label).collect(Collectors.toList());
		assertEquals("[a, b, b, a, a, b]", order.toString());
	}

	@Test
	public void aRunTakesItsSettingSetsThenItsOwn() {
		final Scenario scenario = parse("{\"scenes\": {\"s\": {}},"
				+ " \"settingSets\": {\"off\": {\"aurora.auroraEnable\": false, \"rain.allowBackgroundThunder\": false}},"
				+ " \"runs\": [{\"label\": \"a\", \"scene\": \"s\", \"use\": \"off\","
				+ " \"settings\": {\"rain.allowBackgroundThunder\": true}}]}");
		final Scenario.Run run = scenario.runs.get(0);
		assertEquals(2, run.settings.size());
		assertTrue(run.settings.get("rain.allowBackgroundThunder").getAsBoolean());
		assertEquals(false, run.settings.get("aurora.auroraEnable").getAsBoolean());
	}

	@Test
	public void theDefaultsAreTheTwentySixLinesOnes() {
		final Scenario scenario = parse("{\"scenes\": {\"s\": {\"offset\": [160, -20]}}, \"runs\": [{\"scene\": \"s\"}]}");
		assertEquals("bench", scenario.world);
		assertNull(scenario.seed);
		assertEquals(15, scenario.warmup);
		assertEquals(60, scenario.record);
		assertTrue(scenario.screenshot);
		final Scenario.Scene scene = scenario.scenes.get("s");
		assertEquals("general", scene.profile);
		assertEquals(160, scene.offsetX);
		assertEquals(-20, scene.offsetZ);
		assertEquals("s-0", scenario.runs.get(0).label);
	}

	@Test
	public void mistakesAreSaid() {
		assertThrows(IllegalArgumentException.class, () -> parse("{\"scenes\": {}, \"runs\": []}"));
		assertThrows(IllegalArgumentException.class,
				() -> parse("{\"scenes\": {}, \"runs\": [{\"scene\": \"missing\"}]}"));
		assertThrows(IllegalArgumentException.class,
				() -> parse("{\"scenes\": {\"s\": {}}, \"runs\": [{\"scene\": \"s\", \"use\": \"none\"}]}"));
	}

	@Test
	public void theShippedScenariosRead() throws Exception {
		final Path folder = Paths.get("scripts", "bench");
		try (Stream<Path> files = Files.list(folder)) {
			for (final Path file : files.filter(f -> f.toString().endsWith(".json")).collect(Collectors.toList())) {
				final Scenario scenario = Scenario.read(file);
				assertTrue(!scenario.runs.isEmpty(), file.toString());
				for (final Scenario.Scene scene : scenario.scenes.values())
					assertTrue(scene.profile.equals("general") || scene.profile.equals("aurora")
							|| scene.profile.equals("weather"), file + ": " + scene.profile);
			}
		}
	}
}
