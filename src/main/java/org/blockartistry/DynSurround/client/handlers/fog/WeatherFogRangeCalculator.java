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
package org.blockartistry.DynSurround.client.handlers.fog;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.weather.Weather;

import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Calculates the fog ranges based on current weather. The stronger the
 * intensity of the storm the foggier it gets.
 */
@SideOnly(Side.CLIENT)
public class WeatherFogRangeCalculator extends VanillaFogRangeCalculator {

	protected static final float START_IMPACT = 0.9F;
	protected static final float END_IMPACT = 0.4F;

	protected final FogResult cache = new FogResult();

	@Override
	@Nonnull
	public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
		// Start with what vanilla thinks
		this.cache.set(event);
		if (ModOptions.fog.enableWeatherFog) {
			final float rainStr = Weather.getIntensityLevel();
			if (rainStr > 0) {
				// Calculate our scaling factor
				final float startScale = 1F - (START_IMPACT * rainStr);
				final float endScale = 1F - (END_IMPACT * rainStr);
				this.cache.set(this.cache.getStart() * startScale, this.cache.getEnd() * endScale);
			}
		}

		return this.cache;
	}
}
