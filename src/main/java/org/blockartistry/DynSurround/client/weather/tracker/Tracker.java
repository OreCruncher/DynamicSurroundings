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

import org.blockartistry.DynSurround.client.weather.Weather.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Tracker {

	public Tracker() {

	}

	protected String type() {
		return "VANILLA";
	}

	public final boolean isRaining() {
		return getIntensityLevel() > 0F;
	}

	public final boolean isThundering() {
		return getWorld().isThundering();
	}

	public Properties getWeatherProperties() {
		return Properties.VANILLA;
	}

	public float getIntensityLevel() {
		return getWorld().getRainStrength(1.0F);
	}

	public float getMaxIntensityLevel() {
		return 1.0F;
	}

	public int getNextRainChange() {
		return getWorld().getWorldInfo().getRainTime();
	}

	public float getThunderStrength() {
		return getWorld().getThunderStrength(1.0F);
	}

	public int getNextThunderChange() {
		return getWorld().getWorldInfo().getThunderTime();
	}

	public int getNextThunderEvent() {
		return 0;
	}

	public float getCurrentVolume() {
		return 0.66F;
	}

	@Nonnull
	public SoundEvent getCurrentStormSound() {
		return Properties.VANILLA.getStormSound();
	}

	@Nonnull
	public SoundEvent getCurrentDustSound() {
		return Properties.VANILLA.getDustSound();
	}

	public boolean doVanilla() {
		return true;
	}

	@Override
	public String toString() {
		final Properties props = getWeatherProperties();
		final StringBuilder builder = new StringBuilder();
		builder.append("Storm: ").append(props.name());
		builder.append(" level: ").append(getIntensityLevel()).append('/').append(getMaxIntensityLevel());
		builder.append(" [vanilla strength: ").append(getWorld().getRainStrength(1.0F)).append("] (")
				.append(this.type()).append(')');
		return builder.toString();
	}

	protected static World getWorld() {
		return Minecraft.getMinecraft().theWorld;
	}

}
