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
package org.orecruncher.dsurround.expression;

import org.orecruncher.dsurround.capabilities.CapabilitySeasonInfo;
import org.orecruncher.dsurround.capabilities.season.ISeasonInfo;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.client.weather.Weather;
import org.orecruncher.dsurround.client.weather.wind.WindService;
import org.orecruncher.lib.expression.Dynamic;
import org.orecruncher.lib.expression.DynamicVariantList;

import net.minecraft.world.World;

public class WeatherVariables extends DynamicVariantList {

	public WeatherVariables() {
		add(new Dynamic.DynamicBoolean("weather.isRaining", () -> Weather.isRaining()));
		add(new Dynamic.DynamicBoolean("weather.isThundering", () -> Weather.isThundering()));
		add(new Dynamic.DynamicString("weather.temperature", () -> EnvironState.getBiomeTemperature().getValue()));
		add(new Dynamic.DynamicBoolean("weather.isNotRaining", () -> !Weather.isRaining()));
		add(new Dynamic.DynamicBoolean("weather.isNotThundering", () -> !Weather.isThundering()));
		add(new Dynamic.DynamicNumber("weather.rainfall", () -> (float) Weather.getIntensityLevel()));
		// The wind, from the 26.x line: a storm wind brings a wind loop where a biome has none of its own
		add(new Dynamic.DynamicBoolean("weather.isStormWind", () -> WindService.isStormWind()));
		add(new Dynamic.DynamicBoolean("weather.isWindy", () -> WindService.isWindy()));
		add(new Dynamic.DynamicNumber("weather.windSpeed", () -> (float) WindService.percentOfFullStorm()));
		add(new Dynamic.DynamicNumber("weather.temperatureValue", () -> {
			final World world = EnvironState.getWorld();
			final ISeasonInfo season = CapabilitySeasonInfo.getCapability(world);
			return season.getTemperature(EnvironState.getPlayerPosition());
		}));
		add(new Dynamic.DynamicBoolean("weather.canWaterFreeze", () -> {
			final World world = EnvironState.getWorld();
			final ISeasonInfo season = CapabilitySeasonInfo.getCapability(world);
			return season.canWaterFreeze(EnvironState.getPlayerPosition());
		}));
	}
}
