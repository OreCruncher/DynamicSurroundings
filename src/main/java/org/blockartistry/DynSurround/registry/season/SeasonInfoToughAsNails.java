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

package org.blockartistry.DynSurround.registry.season;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.ClientChunkCache;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.registry.SeasonType;
import org.blockartistry.DynSurround.registry.TemperatureRating;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import toughasnails.api.season.Season;
import toughasnails.api.season.SeasonHelper;
import toughasnails.api.stat.capability.ITemperature;
import toughasnails.api.temperature.TemperatureHelper;

@SideOnly(Side.CLIENT)
public class SeasonInfoToughAsNails extends SeasonInfo {

	public SeasonInfoToughAsNails(@Nonnull final World world) {
		super(world);
	}

	@Override
	@Nonnull
	public SeasonType getSeasonType(@Nonnull final World world) {
		final Season.SubSeason season = SeasonHelper.getSeasonData(world).getSubSeason();
		switch (season) {
		case EARLY_SUMMER:
		case MID_SUMMER:
		case LATE_SUMMER:
			return SeasonType.SUMMER;
		case EARLY_AUTUMN:
		case MID_AUTUMN:
		case LATE_AUTUMN:
			return SeasonType.AUTUMN;
		case EARLY_WINTER:
		case MID_WINTER:
		case LATE_WINTER:
			return SeasonType.WINTER;
		case EARLY_SPRING:
		case MID_SPRING:
		case LATE_SPRING:
			return SeasonType.SPRING;
		default:
			return SeasonType.NONE;
		}
	}

	private Season getSeasonData(@Nonnull final World world) {
		return SeasonHelper.getSeasonData(world).getSubSeason().getSeason();
	}

	@Override
	public float getTemperature(@Nonnull final World world, @Nonnull final BlockPos pos) {
		final Biome biome = ClientChunkCache.INSTANCE.getBiome(pos);
		if (biome.getTemperature() <= 0.7F && getSeasonData(world) == Season.WINTER)
			return 0.0F;
		return biome.getFloatTemperature(pos);
	}

	@Nonnull
	@Override
	public TemperatureRating getPlayerTemperature(@Nonnull final World world) {
		final ITemperature data = TemperatureHelper.getTemperatureData(EnvironState.getPlayer());
		if (data == null)
			return super.getPlayerTemperature(world);

		switch (data.getTemperature().getRange()) {
		case ICY:
			return TemperatureRating.ICY;
		case COOL:
			return TemperatureRating.COOL;
		case MILD:
			return TemperatureRating.MILD;
		case WARM:
			return TemperatureRating.WARM;
		case HOT:
			return TemperatureRating.HOT;
		default:
			return TemperatureRating.MILD;
		}
	}

}
