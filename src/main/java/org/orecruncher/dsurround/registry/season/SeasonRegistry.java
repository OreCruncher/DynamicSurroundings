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

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.registry.PrecipitationType;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.TemperatureRating;
import org.orecruncher.dsurround.registry.biome.BiomeInfo;
import org.orecruncher.dsurround.registry.config.ModConfiguration;
import org.orecruncher.lib.compat.ModEnvironment;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class SeasonRegistry extends Registry {

	private final Int2ObjectOpenHashMap<SeasonInfo> seasonData = new Int2ObjectOpenHashMap<>();

	public SeasonRegistry(@Nonnull final Side side) {
		super(side);
	}

	@Override
	public void configure(@Nonnull final ModConfiguration cfg) {
		// Nothing to configure
	}

	protected SeasonInfo factory(@Nonnull final World world) {

		if (world.provider.getDimension() == -1) {
			ModBase.log().info("Creating Nether SeasonInfo");
			return new SeasonInfoNether(world);
		}

		if (ModEnvironment.SereneSeasons.isLoaded()) {
			ModBase.log().info("Creating Serene Seasons SeasonInfo for dimension %s",
					world.provider.getDimensionType().getName());
			return new SeasonInfoSereneSeasons(world);
		}

		ModBase.log().info("Creating default SeasonInfo for dimension %s",
				world.provider.getDimensionType().getName());
		return new SeasonInfo(world);
	}

	@Nonnull
	public SeasonInfo getData(@Nonnull final World world) {
		SeasonInfo result = this.seasonData.get(world.provider.getDimension());
		if (result == null) {
			result = factory(world);
			this.seasonData.put(world.provider.getDimension(), result);
		}
		return result;
	}

	@Nonnull
	public TemperatureRating getBiomeTemperature(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return getData(world).getBiomeTemperature(world, pos);
	}

	@Nonnull
	public SeasonType getSeasonType(@Nonnull final World world) {
		return getData(world).getSeasonType(world);
	}

	@Nonnull
	public BlockPos getPrecipitationHeight(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return getData(world).getPrecipitationHeight(world, pos);
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
		return getData(world).canWaterFreeze(world, pos);
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
		return getData(world).getPrecipitationType(world, pos, biome);
	}

	/**
	 * Indicates if frost breath is possible at the specified location.
	 *
	 * @return true if it is possible, false otherwise
	 */
	public boolean showFrostBreath(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return getData(world).showFrostBreath(world, pos);
	}

}
