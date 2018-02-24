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
package org.blockartistry.DynSurround.expression;

import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.weather.Weather;
import org.blockartistry.DynSurround.registry.TemperatureRating;
import org.blockartistry.lib.expression.Dynamic;
import org.blockartistry.lib.expression.DynamicVariantList;

public class WeatherVariables extends DynamicVariantList {

	public WeatherVariables() {
		add(new Dynamic.DynamicBoolean("weather.isRaining") {
			@Override
			public void update() {
				this.value = Weather.isRaining();
			}
		});
		add(new Dynamic.DynamicBoolean("weather.isThundering") {
			@Override
			public void update() {
				this.value = Weather.isThundering();
			}
		});
		add(new Dynamic.DynamicBoolean("weather.isNotRaining") {
			@Override
			public void update() {
				this.value = !Weather.isRaining();
			}
		});
		add(new Dynamic.DynamicBoolean("weather.isNotThundering") {
			@Override
			public void update() {
				this.value = !Weather.isThundering();
			}
		});
		add(new Dynamic.DynamicNumber("weather.rainfall") {
			@Override
			public void update() {
				this.value = Weather.getIntensityLevel();
			}
		});
		add(new Dynamic.DynamicNumber("weather.temperatureValue") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerBiome().getFloatTemperature(EnvironState.getPlayerPosition());
			}
		});
		add(new Dynamic.DynamicString("weather.temperature") {
			@Override
			public void update() {
				this.value = TemperatureRating
						.fromTemp(EnvironState.getPlayerBiome().getFloatTemperature(EnvironState.getPlayerPosition()))
						.getValue();
			}
		});

	}
}
