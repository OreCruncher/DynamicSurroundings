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

package org.blockartistry.DynSurround.client.weather;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.client.sound.Sounds;
import org.blockartistry.DynSurround.client.weather.tracker.ServerDrivenTracker;
import org.blockartistry.DynSurround.client.weather.tracker.SimulationTracker;
import org.blockartistry.DynSurround.client.weather.tracker.Tracker;
import org.blockartistry.DynSurround.data.DimensionEffectData;
import org.blockartistry.DynSurround.internal.events.WeatherUpdateEvent;
import org.blockartistry.lib.math.MathStuff;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Weather {

	public enum Properties {
		// Regular Vanilla rain - no modification
		VANILLA,
		//
		NONE(0.0F, "calm"),
		//
		CALM(0.165F, "calm"),
		//
		LIGHT(0.330F, "light"),
		//
		GENTLE(0.495F, "gentle"),
		//
		MODERATE(0.660F, "moderate"),
		//
		HEAVY(0.826F, "heavy"),
		//
		STRONG(1.0F, "strong");

		private final float level;
		private final ResourceLocation rainTexture;
		private final ResourceLocation snowTexture;
		private final ResourceLocation dustTexture;

		private Properties() {
			this.level = -10.0F;
			this.rainTexture = new ResourceLocation("textures/environment/rain.png");
			this.snowTexture = new ResourceLocation("textures/environment/snow.png");
			this.dustTexture = new ResourceLocation(DSurround.RESOURCE_ID, "textures/environment/dust_calm.png");
		}

		private Properties(final float level, @Nonnull final String intensity) {
			this.level = level;
			this.rainTexture = new ResourceLocation(DSurround.RESOURCE_ID,
					String.format("textures/environment/rain_%s.png", intensity));
			this.snowTexture = new ResourceLocation(DSurround.RESOURCE_ID,
					String.format("textures/environment/snow_%s.png", intensity));
			this.dustTexture = new ResourceLocation(DSurround.RESOURCE_ID,
					String.format("textures/environment/dust_%s.png", intensity));
		}

		public float getLevel() {
			return this.level;
		}

		@Nonnull
		public ResourceLocation getRainTexture() {
			return this.rainTexture;
		}

		@Nonnull
		public ResourceLocation getDustTexture() {
			return this.dustTexture;
		}

		@Nonnull
		public ResourceLocation getSnowTexture() {
			return this.snowTexture;
		}

		@Nonnull
		public SoundEvent getStormSound() {
			return Sounds.RAIN;
		}

		@Nonnull
		public SoundEvent getDustSound() {
			return Sounds.DUST;
		}

		public static Properties mapRainStrength(float str) {

			Properties result = Properties.VANILLA;

			// If the level is Vanilla it means that
			// the rainfall in the dimension is to be
			// that of Vanilla.
			if (str == Properties.VANILLA.getLevel()) {
				result = Properties.VANILLA;
			} else {

				str = MathStuff.clamp(str, DimensionEffectData.MIN_INTENSITY, DimensionEffectData.MAX_INTENSITY);
				result = Properties.NONE;

				for (int i = 0; i < Properties.values().length; i++) {
					final Properties p = Properties.values()[i];
					if (str <= p.getLevel()) {
						result = p;
						break;
					}
				}
			}

			return result;
		}

	}

	// Start with the VANILLA storm tracker
	private static Tracker tracker = new SimulationTracker();

	private static World getWorld() {
		return Minecraft.getMinecraft().world;
	}

	public static boolean isRaining() {
		return tracker.isRaining();
	}

	public static boolean isThundering() {
		return tracker.isThundering();
	}

	@Nonnull
	public static Properties getWeatherProperties() {
		return tracker.getWeatherProperties();
	}

	public static float getIntensityLevel() {
		return tracker.getIntensityLevel();
	}

	public static float getMaxIntensityLevel() {
		return tracker.getMaxIntensityLevel();
	}

	public static int getNextRainChange() {
		return tracker.getNextRainChange();
	}

	public static float getThunderStrength() {
		return tracker.getThunderStrength();
	}

	public static int getNextThunderChange() {
		return tracker.getNextThunderChange();
	}

	public static int getNextThunderEvent() {
		return tracker.getNextThunderEvent();
	}

	public static float getCurrentVolume() {
		return tracker.getCurrentVolume();
	}

	@Nonnull
	public static SoundEvent getCurrentStormSound() {
		return tracker.getCurrentStormSound();
	}

	@Nonnull
	public static SoundEvent getCurrentDustSound() {
		return tracker.getCurrentDustSound();
	}

	public static boolean doVanilla() {
		return tracker.doVanilla();
	}

	public static void update() {
		tracker.update();
	}

	@SubscribeEvent
	public static void onWeatherUpdateEvent(@Nonnull final WeatherUpdateEvent event) {
		final World world = getWorld();
		if (world == null || world.provider == null)
			return;

		if (world.provider.getDimension() != event.world.provider.getDimension())
			return;

		if (tracker instanceof ServerDrivenTracker)
			((ServerDrivenTracker) tracker).update(event);
	}

	public static void register(final boolean serverAvailable) {
		if (serverAvailable)
			tracker = new ServerDrivenTracker();
	}

	public static void unregister() {
		tracker = new SimulationTracker();
	}

	@Nonnull
	public static String diagnostic() {
		return tracker.toString();
	}
}