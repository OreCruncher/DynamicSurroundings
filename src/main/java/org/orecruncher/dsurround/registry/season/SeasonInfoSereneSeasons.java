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

import org.orecruncher.dsurround.client.ClientChunkCache;
import org.orecruncher.dsurround.client.ClientRegistry;
import org.orecruncher.dsurround.registry.BiomeInfo;
import org.orecruncher.dsurround.registry.PrecipitationType;
import org.orecruncher.dsurround.registry.SeasonType;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.BiomeConfig;

@SideOnly(Side.CLIENT)
public class SeasonInfoSereneSeasons extends SeasonInfo {

	public SeasonInfoSereneSeasons(@Nonnull final World world) {
		super(world);
	}

	@Override
	@Nonnull
	public SeasonType getSeasonType(@Nonnull final World world) {
		final Season season = SeasonHelper.getSeasonState(world).getSeason();
		switch (season) {
		case SUMMER:
			return SeasonType.SUMMER;
		case AUTUMN:
			return SeasonType.AUTUMN;
		case WINTER:
			return SeasonType.WINTER;
		case SPRING:
			return SeasonType.SPRING;
		default:
			return SeasonType.NONE;
		}
	}

	@Override
	@Nonnull
	public SeasonType.SubType getSeasonSubType(@Nonnull final World world) {
		final Season.SubSeason sub = SeasonHelper.getSeasonState(world).getSubSeason();
		switch (sub) {
		case EARLY_SUMMER:
		case EARLY_AUTUMN:
		case EARLY_WINTER:
		case EARLY_SPRING:
			return SeasonType.SubType.EARLY;
		case MID_SUMMER:
		case MID_AUTUMN:
		case MID_WINTER:
		case MID_SPRING:
			return SeasonType.SubType.MID;
		case LATE_SUMMER:
		case LATE_AUTUMN:
		case LATE_WINTER:
		case LATE_SPRING:
			return SeasonType.SubType.LATE;
		default:
			return SeasonType.SubType.NONE;
		}
	}

	private Season getSeasonData(@Nonnull final World world) {
		return SeasonHelper.getSeasonState(world).getSeason();
	}

	@Override
	public boolean canWaterFreeze(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return SeasonHelper.canSnowAtTempInSeason(getSeasonData(world), getTemperature(world, pos));
	}

	@Override
	public boolean showFrostBreath(@Nonnull final World world, @Nonnull final BlockPos pos) {
		final float temp = getTemperature(world, pos);
		return temp < 0.2F || SeasonHelper.canSnowAtTempInSeason(getSeasonData(world), temp);
	}

	@Override
	public PrecipitationType getPrecipitationType(@Nonnull final World world, @Nonnull final BlockPos pos,
			@Nullable BiomeInfo biome) {

		if (biome == null)
			biome = ClientRegistry.BIOME.get(ClientChunkCache.INSTANCE.getBiome(pos));

		final Biome trueBiome = biome.getBiome();
		if (trueBiome != null && BiomeConfig.usesTropicalSeasons(trueBiome)) {
			final Season.TropicalSeason tropicalSeason = SeasonHelper.getSeasonState(world).getTropicalSeason();

			switch (tropicalSeason) {
			case MID_DRY:
				return PrecipitationType.NONE;

			case MID_WET:
				return PrecipitationType.RAIN;

			default:
				// Fall through
			}
		}

		return super.getPrecipitationType(world, pos, biome);
	}

}
