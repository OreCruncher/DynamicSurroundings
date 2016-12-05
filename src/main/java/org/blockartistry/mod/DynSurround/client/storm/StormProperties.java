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

package org.blockartistry.mod.DynSurround.client.storm;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.Module;
import org.blockartistry.mod.DynSurround.client.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.data.DimensionEffectData;
import org.blockartistry.mod.DynSurround.util.SoundUtils;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum StormProperties {

	VANILLA, NONE(0.0F, "calm"), CALM(0.1F, "calm"), LIGHT(0.33F, "light"), NORMAL(0.66F, "normal"), HEAVY(1.0F,
			"heavy");

	private static float intensityLevel = 0.0F;
	private static StormProperties intensity = VANILLA;
	private static float fogDensity = 0.0F;
		
	public static void initialize() {

	}

	private final float level;
	private final ResourceLocation rainTexture;
	private final ResourceLocation snowTexture;
	private final ResourceLocation dustTexture;
	private final SoundEvent rainSound;
	private final SoundEvent dustSound;

	private StormProperties() {
		this.level = -10.0F;
		this.rainTexture = EntityRenderer.RAIN_TEXTURES;
		this.snowTexture = EntityRenderer.SNOW_TEXTURES;
		this.dustTexture = new ResourceLocation(Module.RESOURCE_ID, "textures/environment/dust_calm.png");
		this.rainSound = SoundUtils.getOrRegisterSound(new ResourceLocation(Module.RESOURCE_ID, "rain"));
		this.dustSound = SoundUtils.getOrRegisterSound(new ResourceLocation(Module.RESOURCE_ID, "dust"));
	}

	private StormProperties(final float level, final String intensity) {
		this.level = level;
		this.rainTexture = new ResourceLocation(Module.RESOURCE_ID,
				String.format("textures/environment/rain_%s.png", intensity));
		this.snowTexture = new ResourceLocation(Module.RESOURCE_ID,
				String.format("textures/environment/snow_%s.png", intensity));
		this.dustTexture = new ResourceLocation(Module.RESOURCE_ID,
				String.format("textures/environment/dust_%s.png", intensity));
		this.rainSound = SoundUtils.getOrRegisterSound(new ResourceLocation(Module.RESOURCE_ID, "rain"));
		this.dustSound = SoundUtils.getOrRegisterSound(new ResourceLocation(Module.RESOURCE_ID, "dust"));
	}

	public static StormProperties getIntensity() {
		return intensity;
	}

	public static float getIntensityLevel() {
		return intensityLevel;
	}

	public static float getFogDensity() {
		return fogDensity;
	}

	public SoundEvent getStormSound() {
		return this.rainSound;
	}

	public SoundEvent getDustSound() {
		return this.dustSound;
	}

	public static float getCurrentVolume() {
		return (doVanilla() ? 0.66F : intensityLevel) * ModOptions.soundLevel;
	}

	public static SoundEvent getCurrentStormSound() {
		return intensity.rainSound;
	}

	public static SoundEvent getCurrentDustSound() {
		return intensity.dustSound;
	}

	public static boolean doVanilla() {
		return intensity == VANILLA;
	}

	/**
	 * Sets the rain intensity based on the intensityLevel level provided. This
	 * is called by the packet handler when the server wants to set the
	 * intensity level on the client.
	 */
	public static void setIntensity(float level) {

		// If the level is Vanilla it means that
		// the rainfall in the dimension is to be
		// that of Vanilla.
		if (level == VANILLA.level) {
			intensity = VANILLA;
			intensityLevel = 0.0F;
			fogDensity = 0.0F;
			setTextures();
			return;
		}

		level = MathHelper.clamp_float(level, DimensionEffectData.MIN_INTENSITY, DimensionEffectData.MAX_INTENSITY);

		if (intensityLevel != level) {
			intensityLevel = level;
			if (level > 0) {
				level += 0.01;
				fogDensity = level * level * 0.13F;
			} else {
				fogDensity = 0.0F;
			}
			if (intensityLevel <= NONE.level)
				intensity = NONE;
			else if (intensityLevel < CALM.level)
				intensity = CALM;
			else if (intensityLevel < LIGHT.level)
				intensity = LIGHT;
			else if (intensityLevel < NORMAL.level)
				intensity = NORMAL;
			else
				intensity = HEAVY;
		}
	}

	/**
	 * Set precipitation textures based on the currentAurora intensity. This is
	 * invoked before rendering takes place.
	 */
	public static void setTextures() {
		// AT transform removed final and made public.
		StormRenderer.locationRainPng = intensity.rainTexture;
		StormRenderer.locationSnowPng = intensity.snowTexture;
		StormRenderer.locationDustPng = intensity.dustTexture;
	}

	public static String diagnostic() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Storm: ").append(intensity.name());
		builder.append(" level:").append(intensityLevel);
		builder.append(" dust:").append(fogDensity);
		builder.append(" str:").append(EnvironState.getWorld().getRainStrength(1.0F));
		return builder.toString();
	}
}