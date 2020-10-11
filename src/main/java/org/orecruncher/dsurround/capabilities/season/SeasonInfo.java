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

package org.orecruncher.dsurround.capabilities.season;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.client.weather.Weather;
import org.orecruncher.dsurround.lib.compat.ModEnvironment;
import org.orecruncher.dsurround.registry.biome.BiomeInfo;
import org.orecruncher.dsurround.registry.biome.BiomeUtil;
import org.orecruncher.lib.Localization;
import org.orecruncher.lib.chunk.ClientChunkCache;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SeasonInfo implements ISeasonInfo {

	protected static final float FREEZE_TEMP = 0.15F;
	protected static final float BREATH_TEMP = 0.2F;
	protected static final String noSeason = Localization.loadString("dsurround.season.noseason");

	protected final World world;

	public SeasonInfo() {
		this.world = null;
	}

	public SeasonInfo(@Nonnull final World world) {
		this.world = world;
	}

	@Override
	@Nullable
	public World getWorld() {
		return this.world;
	}

	@Override
	@Nonnull
	public SeasonType getSeasonType() {
		return SeasonType.NONE;
	}

	@Override
	@Nonnull
	public SeasonType.SubType getSeasonSubType() {
		return SeasonType.SubType.NONE;
	}

	@Override
	@Nonnull
	public String getSeasonString() {
		final SeasonType season = getSeasonType();
		if (season == SeasonType.NONE)
			return noSeason;

		final SeasonType.SubType sub = getSeasonSubType();
		final String seasonStr = Localization.loadString("dsurround.season." + season.getValue());
		final String subSeasonStr = Localization.loadString("dsurround.season." + sub.getValue());
		return Localization.format("dsurround.season.format", subSeasonStr, seasonStr);
	}

	@Override
	@Nonnull
	public TemperatureRating getPlayerTemperature() {
		return getBiomeTemperature(EnvironState.getPlayerPosition());
	}

	@Override
	@Nonnull
	public TemperatureRating getBiomeTemperature(@Nonnull final BlockPos pos) {
		return TemperatureRating.fromTemp(getTemperature(pos));
	}

	@Override
	@Nonnull
	public BlockPos getPrecipitationHeight(@Nonnull final BlockPos pos) {
		return ClientChunkCache.instance().getPrecipitationHeight(pos);
	}

	@Override
	public float getFloatTemperature(@Nonnull final Biome biome, @Nonnull final BlockPos pos) {
		return BiomeUtil.getBiomeData(biome).getFloatTemperature(pos);
	}

	@Override
	public float getTemperature(@Nonnull final BlockPos pos) {
		final Biome biome = ClientChunkCache.instance().getBiome(pos);
		return getFloatTemperature(biome, pos);
	}

	/**
	 * Indicates if it is cold enough that water can freeze at the specified
	 * location.
	 *
	 * @return true if water can freeze, false otherwise
	 */
	@Override
	public boolean canWaterFreeze(@Nonnull final BlockPos pos) {
		return getTemperature(pos) < FREEZE_TEMP;
	}

	/**
	 * Indicates if frost breath is possible at the specified location.
	 *
	 * @return true if it is possible, false otherwise
	 */
	@Override
	public boolean showFrostBreath(@Nonnull final BlockPos pos) {
		return getTemperature(pos) < BREATH_TEMP;
	}

	protected boolean doDust(@Nonnull final BiomeInfo biome) {
		return ModOptions.fog.allowDesertFog && Weather.notDoVanilla() && biome.getHasDust();
	}

	/**
	 * Determines the type of precipitation to render for the specified world
	 * location/biome. The type is based on the heights block that precipitation can
	 * hit in the block column defined by the BlockPos.
	 *
	 * @param pos   Position in the world for which the determination is being made
	 * @param biome BiomeInfo reference for the biome in question
	 * @return The precipitation type to render when raining
	 */
	@Override
	public PrecipitationType getPrecipitationType(@Nonnull final BlockPos pos, @Nullable BiomeInfo biome) {

		if (biome == null)
			biome = BiomeUtil.getBiomeData(ClientChunkCache.instance().getBiome(pos));

		if (!biome.hasWeatherEffect())
			return PrecipitationType.NONE;

		if (doDust(biome))
			return PrecipitationType.DUST;

		return getFloatTemperature(biome.getBiome(), pos) < FREEZE_TEMP ? PrecipitationType.SNOW
				: PrecipitationType.RAIN;
	}

	public static SeasonInfo factory(@Nonnull final World world) {

		final String dimName = world.provider.getDimensionType().getName();

		if (world.provider.getDimension() == -1) {
			ModBase.log().info("Creating Nether SeasonInfo for dimension %s", dimName);
			return new SeasonInfoNether(world);
		}

		if (ModEnvironment.SereneSeasons.isLoaded()) {
			if (SeasonInfoSereneSeasons.isWorldWhitelisted(world)) {
				ModBase.log().info("Creating Serene Seasons SeasonInfo for dimension %s", dimName);
				return new SeasonInfoSereneSeasons(world);
			}
			ModBase.log().info("Serene Seasons is installed but the dimension %s is not whitelisted", dimName);
		}

		ModBase.log().info("Creating default SeasonInfo for dimension %s", dimName);
		return new SeasonInfo(world);
	}

}
