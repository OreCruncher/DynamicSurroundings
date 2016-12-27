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
import org.blockartistry.mod.DynSurround.registry.BiomeRegistry;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SeasonInfo {

	public static enum SeasonType {
		NONE("noseason"), SPRING("spring"), SUMMER("summer"), AUTUMN("autumn"), WINTER("winter");

		private final String val;

		SeasonType(@Nonnull final String val) {
			this.val = val;
		}

		@Nonnull
		public String getValue() {
			return this.val;
		}
	}

	public static enum TemperatureRating {
		ICY("icy", 0.0F), COOL("cool", 0.3F), MILD("mild", 1.0F), WARM("warm", 1.3F), HOT("hot", 100.F);

		private final String val;
		private final float tempRange;

		TemperatureRating(@Nonnull final String val, final float tempRange) {
			this.val = val;
			this.tempRange = tempRange;
		}

		@Nonnull
		public String getValue() {
			return this.val;
		}

		public float getTempRange() {
			return this.tempRange;
		}

		@Nonnull
		public static TemperatureRating fromTemp(final float temp) {
			for (final TemperatureRating rating : values())
				if (temp <= rating.getTempRange())
					return rating;
			return TemperatureRating.MILD;
		}
	}

	protected final BiomeRegistry biomes;
	protected final World world;

	public SeasonInfo(@Nonnull final World world) {
		this.world = world;
		this.biomes = RegistryManager.get(RegistryType.BIOME);
	}

	@Nonnull
	public SeasonType getSeasonType() {
		return SeasonType.NONE;
	}

	@Nonnull
	public String getSeasonName() {
		return getSeasonType().getValue();
	}

	@Nonnull
	public TemperatureRating getPlayerTemperature() {
		return getBiomeTemperature(EnvironState.getPlayerPosition());
	}

	@Nonnull
	public TemperatureRating getBiomeTemperature(@Nonnull final BlockPos pos) {
		return TemperatureRating.fromTemp(getTemperature(pos));
	}

	@Nonnull
	public BlockPos getPrecipitationHeight(@Nonnull final BlockPos pos) {
		return this.world.getPrecipitationHeight(pos);
	}

	public float getTemperature(@Nonnull final BlockPos pos) {
		final float biomeTemp = this.biomes.get(this.world.getBiome(pos)).getFloatTemperature(pos);
		final float heightTemp = this.world.getBiomeProvider().getTemperatureAtHeight(biomeTemp,
				getPrecipitationHeight(pos).getY());
		return heightTemp;
	}

	/*
	 * Indicates if rain is striking at the specified position.
	 */
	public boolean isRainingAt(@Nonnull final BlockPos pos) {
		return this.world.isRainingAt(pos);
	}

	/*
	 * Indicates if it is cold enough that water can freeze. Could result in
	 * snow or frozen ice. Does not take into account any other environmental
	 * factors - just whether its cold enough. If environmental sensitive
	 * versions are needed look at canBlockFreeze() and canSnowAt().
	 */
	public boolean canWaterFreeze(@Nonnull final BlockPos pos) {
		return getTemperature(pos) < 0.15F;
	}

	/*
	 * Essentially snow layer stuff.
	 */
	public boolean canSnowAt(@Nonnull final BlockPos pos) {
		return this.world.canSnowAt(pos, false);
	}

	public boolean canBlockFreeze(@Nonnull final BlockPos pos, final boolean noWaterAdjacent) {
		return this.world.canBlockFreeze(pos, noWaterAdjacent);
	}

}
