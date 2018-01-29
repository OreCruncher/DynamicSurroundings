/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher, Abastro
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
package org.blockartistry.DynSurround.client.weather.tracker;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.api.events.WeatherUpdateEvent;
import org.blockartistry.DynSurround.client.weather.Weather.Properties;
import org.blockartistry.DynSurround.data.DimensionEffectData;
import org.blockartistry.lib.math.MathStuff;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ServerDrivenTracker extends Tracker {

	protected float intensityLevel = 0.0F;
	protected float maxIntensityLevel = 0.0F;
	protected int nextRainChange = 0;
	protected float thunderStrength = 0.0F;
	protected int nextThunderChange = 0;
	protected int nextThunderEvent = 0;
	protected Properties intensity = Properties.NONE;

	public Properties getWeatherProperties() {
		return this.intensity;
	}

	@Override
	public float getIntensityLevel() {
		return this.intensityLevel;
	}

	@Override
	public float getMaxIntensityLevel() {
		return this.maxIntensityLevel;
	}

	@Override
	public int getNextRainChange() {
		return this.nextRainChange;
	}

	@Override
	public float getThunderStrength() {
		return this.thunderStrength;
	}

	@Override
	public int getNextThunderChange() {
		return this.nextThunderChange;
	}

	@Override
	public int getNextThunderEvent() {
		return this.nextThunderEvent;
	}

	@Override
	public float getCurrentVolume() {
		return 0.05F + 0.95F * this.intensityLevel;
	}

	@Override
	@Nonnull
	public SoundEvent getCurrentStormSound() {
		return this.intensity.getStormSound();
	}

	@Override
	@Nonnull
	public SoundEvent getCurrentDustSound() {
		return this.intensity.getDustSound();
	}

	@Override
	public boolean doVanilla() {
		return false;
	}

	public void update(@Nonnull final WeatherUpdateEvent event) {
		this.maxIntensityLevel = event.maxRainIntensity;
		this.nextRainChange = event.nextRainChange;
		this.thunderStrength = event.thunderStrength;
		this.nextThunderChange = event.nextThunderChange;
		this.nextThunderEvent = event.nextThunderEvent;
		setCurrentIntensity(event.rainIntensity);
	}

	/**
	 * Sets the rainIntensity based on the intensityLevel level provided. This is
	 * called by the packet handler when the server wants to set the rainIntensity
	 * level on the client.
	 */
	private void setCurrentIntensity(float level) {

		// If the level is Vanilla it means that
		// the rainfall in the dimension is to be
		// that of Vanilla.
		if (level == Properties.VANILLA.getLevel()) {
			this.intensity = Properties.VANILLA;
			this.intensityLevel = 0.0F;
		} else {

			level = MathStuff.clamp(level, DimensionEffectData.MIN_INTENSITY, DimensionEffectData.MAX_INTENSITY);

			if (this.intensityLevel != level) {
				this.intensityLevel = level;
				if (this.intensityLevel <= Properties.NONE.getLevel())
					this.intensity = Properties.NONE;
				else if (this.intensityLevel < Properties.CALM.getLevel())
					this.intensity = Properties.CALM;
				else if (this.intensityLevel < Properties.LIGHT.getLevel())
					this.intensity = Properties.LIGHT;
				else if (this.intensityLevel < Properties.NORMAL.getLevel())
					this.intensity = Properties.NORMAL;
				else
					this.intensity = Properties.HEAVY;
			}
		}
	}

}
