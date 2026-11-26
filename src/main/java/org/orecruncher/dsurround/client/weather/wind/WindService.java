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

package org.orecruncher.dsurround.client.weather.wind;

import java.util.Locale;
import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.capabilities.CapabilityDimensionInfo;
import org.orecruncher.dsurround.capabilities.CapabilitySeasonInfo;
import org.orecruncher.dsurround.capabilities.dimension.IDimensionInfo;
import org.orecruncher.dsurround.capabilities.season.ISeasonInfo;
import org.orecruncher.dsurround.capabilities.season.PrecipitationType;
import org.orecruncher.dsurround.client.weather.Weather;
import org.orecruncher.dsurround.client.weather.particle.PrecipitationField;
import org.orecruncher.dsurround.event.DiagnosticEvent;
import org.orecruncher.dsurround.registry.biome.BiomeUtil;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The one wind of the scene, as the 26.x line's wind service. Particle rain and snow are carried by it, and other
 * effects lean with it through {@link SceneWind}. It blows with the storm, a light breeze between storms, harder in a
 * thunderstorm and harder still where the storm raises dust, its direction starting from the day's (the same for
 * everyone in a world). It ticks whether or not particle weather is on, so the other effects have it either way; where
 * the dimension has no weather, the air is still. Client thread only.
 */
@SideOnly(Side.CLIENT)
public final class WindService {

	private static final Wind WIND = new Wind();
	private static final Random RANDOM = new XorShiftRandom();
	private static boolean still = true;

	private WindService() {
	}

	/**
	 * The wind, as of the last tick
	 */
	@Nonnull
	public static Wind wind() {
		return WIND;
	}

	/**
	 * Whether the air is still: the dimension has no weather, so nothing drifts whatever the wind is set to
	 */
	public static boolean isStill() {
		return still;
	}

	@SubscribeEvent
	public static void onClientTick(@Nonnull final TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		final Minecraft mc = Minecraft.getMinecraft();
		final World world = mc.world;
		final Entity camera = mc.getRenderViewEntity();
		if (world == null || camera == null || mc.isGamePaused())
			return;
		final IDimensionInfo info = CapabilityDimensionInfo.getCapability(world);
		still = info != null && !info.hasWeather();
		if (still) {
			// Still air: nothing drifts
			SceneWind.clear();
			return;
		}
		final float rainLevel = MathHelper.clamp(Weather.getIntensityLevel(), 0F, 1F);
		final double eyeY = camera.posY + camera.getEyeHeight();
		final boolean dusty = rainLevel > 0F && isDusty(world, new BlockPos(camera.posX, eyeY, camera.posZ));
		WIND.setReach(PrecipitationField.DEFAULT_RADIUS);
		WIND.tick(rainLevel, world.isThundering(), dusty, world.getWorldTime() / 24000L, RANDOM);
		SceneWind.update(WIND, camera.posX, camera.posZ);
	}

	/**
	 * Whether the storm raises dust at a place, as the storm renderer draws it
	 */
	private static boolean isDusty(@Nonnull final World world, @Nonnull final BlockPos pos) {
		final ISeasonInfo season = CapabilitySeasonInfo.getCapability(world);
		return season.getPrecipitationType(pos, BiomeUtil.getBiomeData(world.getBiome(pos))) == PrecipitationType.DUST;
	}

	@SubscribeEvent
	public static void onWorldUnload(@Nonnull final WorldEvent.Unload event) {
		if (event.getWorld().isRemote)
			SceneWind.clear();
	}

	/**
	 * The wind's line for the diagnostics overlay: its speed, the way it blows, and whether a gust is passing
	 */
	static String diagnosticText(@Nonnull final Wind wind) {
		return String.format(Locale.ROOT, "Wind: %.2f toward %s (%.0f)%s", wind.speed(),
				Wind.compassPoint(wind.bearing()), wind.bearing(), wind.isGusting() ? " gusting" : "");
	}

	@SubscribeEvent
	public static void diagnostics(@Nonnull final DiagnosticEvent.Gather event) {
		event.output.add(TextFormatting.AQUA + (still ? "Wind: none in this dimension" : diagnosticText(WIND)));
	}
}
