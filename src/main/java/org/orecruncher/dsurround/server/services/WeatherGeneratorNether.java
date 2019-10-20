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

package org.orecruncher.dsurround.server.services;

import javax.annotation.Nonnull;

import org.orecruncher.lib.ReflectedField.BooleanField;

import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

public class WeatherGeneratorNether extends WeatherGenerator {

	//formatter:off
	private static final BooleanField<WorldProvider> hasSkyLight = new BooleanField<>(
			WorldProvider.class,
			"hasSkyLight",
			"field_191067_f"
		);
	//formatter:on
	
	public WeatherGeneratorNether(@Nonnull final World world) {
		super(world);
	}

	@Override
	@Nonnull
	public String name() {
		return "NETHER";
	}

	// Need to manually turn the crank on the Nether since
	// it has no sky
	@Override
	protected void preProcess() {
		hasSkyLight.set(this.world.provider, true);
		try {
			this.world.updateWeatherBody();
		} catch (final Throwable t) {
			;
		} finally {
			hasSkyLight.set(this.world.provider, false);
		}
	}

	@Override
	protected void doAmbientThunder() {
		// No ambient thunder, either
	}

}
