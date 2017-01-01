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

package org.blockartistry.mod.DynSurround.registry.season;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.registry.SeasonType;
import org.blockartistry.mod.DynSurround.registry.TemperatureRating;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import toughasnails.api.season.Season;
import toughasnails.api.season.SeasonHelper;
import toughasnails.api.stat.capability.ITemperature;
import toughasnails.api.temperature.TemperatureHelper;

public class SeasonInfoToughAsNails extends SeasonInfo {

	// Lifted from the 1.11.x branch of TaN. Will be removed when Dynamic
	// Surroundings gets to 1.11.x
	// TODO: Cleanup for 1.11.x version of Dynamic Surroundings
	private static class Hooks {

		public static boolean canSnowAtInSeason(World world, BlockPos pos, boolean checkLight, Season season) {
			try {
				return (Boolean) Class.forName("toughasnails.season.SeasonASMHelper")
						.getMethod("canSnowAtInSeason", World.class, BlockPos.class, Boolean.class, Season.class)
						.invoke(null, world, pos, checkLight, season);
			} catch (Exception e) {
				throw new RuntimeException("An error occurred calling canSnowAtInSeason", e);
			}
		}

		public static boolean canBlockFreezeInSeason(World world, BlockPos pos, boolean noWaterAdj, Season season) {
			try {
				return (Boolean) Class.forName("toughasnails.season.SeasonASMHelper")
						.getMethod("canBlockFreezeInSeason", World.class, BlockPos.class, Boolean.class, Season.class)
						.invoke(null, world, pos, noWaterAdj, season);
			} catch (Exception e) {
				throw new RuntimeException("An error occurred calling canBlockFreezeInSeason", e);
			}
		}

		public static boolean isRainingAtInSeason(World world, BlockPos pos, Season season) {
			try {
				return (Boolean) Class.forName("toughasnails.season.SeasonASMHelper")
						.getMethod("isRainingAtInSeason", World.class, BlockPos.class, Season.class)
						.invoke(null, world, pos, season);
			} catch (Exception e) {
				throw new RuntimeException("An error occurred calling isRainingAtInSeason", e);
			}
		}
	}

	public SeasonInfoToughAsNails(@Nonnull final World world) {
		super(world);
	}

	@Override
	@Nonnull
	public SeasonType getSeasonType() {
		final Season.SubSeason season = SeasonHelper.getSeasonData(this.world).getSubSeason();
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

	private Season getSeasonData() {
		return SeasonHelper.getSeasonData(this.world).getSubSeason().getSeason();
	}

	@Override
	public float getTemperature(@Nonnull final BlockPos pos) {
		final Biome biome = this.world.getBiome(pos);

		if (biome.getTemperature() <= 0.7F && getSeasonData() == Season.WINTER)
			return 0.0F;

		return biome.getFloatTemperature(pos);
	}

	@Nonnull
	@Override
	public TemperatureRating getPlayerTemperature() {
		final ITemperature data = TemperatureHelper.getTemperatureData(EnvironState.getPlayer());
		if (data == null)
			return super.getPlayerTemperature();

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

	/*
	 * Indicates if rain is striking at the specified position.
	 */
	@Override
	public boolean isRainingAt(@Nonnull final BlockPos pos) {
		return Hooks.isRainingAtInSeason(this.world, pos, getSeasonData());
	}

	/*
	 * Indicates if it is cold enough that water can freeze. Could result in
	 * snow or frozen ice. Does not take into account any other environmental
	 * factors - just whether its cold enough. If environmental sensitive
	 * versions are needed look at canBlockFreeze() and canSnowAt().
	 */
	@Override
	public boolean canWaterFreeze(@Nonnull final BlockPos pos) {
		return getTemperature(pos) < 0.15F;
	}

	/*
	 * Essentially snow layer stuff.
	 */
	public boolean canSnowAt(@Nonnull final BlockPos pos) {
		return Hooks.canSnowAtInSeason(this.world, pos, true, getSeasonData());
	}

	public boolean canBlockFreeze(@Nonnull final BlockPos pos, final boolean noWaterAdjacent) {
		return Hooks.canBlockFreezeInSeason(this.world, pos, noWaterAdjacent, getSeasonData());
	}
}
