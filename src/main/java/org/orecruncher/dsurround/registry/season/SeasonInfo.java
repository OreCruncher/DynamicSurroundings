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

package org.orecruncher.dsurround.registry.season;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.ClientRegistry;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.client.weather.Weather;
import org.orecruncher.dsurround.registry.PrecipitationType;
import org.orecruncher.dsurround.registry.TemperatureRating;
import org.orecruncher.dsurround.registry.biome.BiomeInfo;
import org.orecruncher.lib.Localization;
import org.orecruncher.lib.chunk.ClientChunkCache;

import com.google.common.base.MoreObjects;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SeasonInfo {

	protected static final String noSeason = Localization.loadString("dsurround.season.noseason");

	protected final String dimensionName;

	public SeasonInfo(@Nonnull final World world) {
		this.dimensionName = world.provider.getDimensionType().getName();
	}

	@Nonnull
	public SeasonType getSeasonType(@Nonnull final World world) {
		return SeasonType.NONE;
	}

	@Nonnull
	public SeasonType.SubType getSeasonSubType(@Nonnull final World world) {
		return SeasonType.SubType.NONE;
	}

	@Nonnull
	public String getSeasonString(@Nonnull final World world) {
		final SeasonType season = getSeasonType(world);
		if (season == SeasonType.NONE)
			return noSeason;

		final SeasonType.SubType sub = getSeasonSubType(world);
		final String seasonStr = Localization.loadString("dsurround.season." + season.getValue());
		final String subSeasonStr = Localization.loadString("dsurround.season." + sub.getValue());
		return Localization.format("dsurround.season.format", subSeasonStr, seasonStr);
	}

	@Nonnull
	public TemperatureRating getPlayerTemperature(@Nonnull final World world) {
		return getBiomeTemperature(world, EnvironState.getPlayerPosition());
	}

	@Nonnull
	public TemperatureRating getBiomeTemperature(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return TemperatureRating.fromTemp(getTemperature(world, pos));
	}

	@Nonnull
	public BlockPos getPrecipitationHeight(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return ClientChunkCache.instance().getPrecipitationHeight(pos);
	}

	public float getTemperature(@Nonnull final World world, @Nonnull final BlockPos pos) {
		final Biome biome = ClientChunkCache.instance().getBiome(pos);
		final float biomeTemp = ClientRegistry.BIOME.get(biome).getFloatTemperature(pos);
		final float heightTemp = world.getBiomeProvider().getTemperatureAtHeight(biomeTemp,
				getPrecipitationHeight(world, pos).getY());
		return heightTemp;
	}

	/**
	 * Indicates if it is cold enough that water can freeze. Could result in snow or
	 * frozen ice. Does not take into account any other environmental factors - just
	 * whether its cold enough. If environmental sensitive versions are needed look
	 * at canBlockFreeze() and canSnowAt().
	 *
	 * @return true if water can freeze, false otherwise
	 */
	public boolean canWaterFreeze(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return getTemperature(world, pos) < 0.15F;
	}

	/**
	 * Indicates if frost breath is possible at the specified location.
	 *
	 * @return true if it is possible, false otherwise
	 */
	public boolean showFrostBreath(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return getTemperature(world, pos) < 0.2F;
	}

	protected boolean doDust(@Nonnull final BiomeInfo biome) {
		return ModOptions.fog.allowDesertFog && !Weather.doVanilla() && biome.getHasDust();
	}

	/**
	 * Determines the type of precipitation to render for the specified world
	 * location/biome
	 *
	 * @param world
	 *            The current client world
	 * @param pos
	 *            Position in the world for which the determination is being made
	 * @param biome
	 *            BiomeInfo reference for the biome in question
	 * @return The precipitation type to render when raining
	 */
	public PrecipitationType getPrecipitationType(@Nonnull final World world, @Nonnull final BlockPos pos,
			@Nullable BiomeInfo biome) {

		if (biome == null)
			biome = ClientRegistry.BIOME.get(ClientChunkCache.instance().getBiome(pos));

		if (!biome.hasWeatherEffect())
			return PrecipitationType.NONE;

		if (doDust(biome))
			return PrecipitationType.DUST;

		return canWaterFreeze(world, pos) ? PrecipitationType.SNOW : PrecipitationType.RAIN;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("name", this.dimensionName).toString();
	}

}
