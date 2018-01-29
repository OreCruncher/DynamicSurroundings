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
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ServerDrivenTracker extends SimulationTracker {

	protected int nextRainChange = 0;
	protected float thunderStrength = 0.0F;
	protected int nextThunderChange = 0;

	@Override
	protected String type() {
		return "SERVER";
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

	public void update(@Nonnull final WeatherUpdateEvent event) {
		this.maxIntensityLevel = event.maxRainIntensity;
		this.nextRainChange = event.nextRainChange;
		this.thunderStrength = event.thunderStrength;
		this.nextThunderChange = event.nextThunderChange;
		this.nextThunderEvent = event.nextThunderEvent;
		setCurrentIntensity(event.rainIntensity);
	}

	@Override
	public void update() {
		// Don't want to do the simulation
	}

}
