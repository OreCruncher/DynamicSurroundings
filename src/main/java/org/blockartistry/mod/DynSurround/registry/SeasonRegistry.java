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

package org.blockartistry.mod.DynSurround.registry;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.registry.season.SeasonInfo;
import org.blockartistry.mod.DynSurround.registry.season.SeasonInfoCalendar;
import org.blockartistry.mod.DynSurround.registry.season.SeasonInfoNether;
import org.blockartistry.mod.DynSurround.registry.season.SeasonInfoToughAsNails;
import org.blockartistry.mod.DynSurround.registry.season.SeasonInfo.Season;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public final class SeasonRegistry extends Registry {

	private static final boolean CALENDAR_API = Loader.isModLoaded("CalendarAPI");
	private static final boolean TOUGHASNAILS_API = Loader.isModLoaded("toughasnails");

	private final TIntObjectHashMap<SeasonInfo> seasonData = new TIntObjectHashMap<SeasonInfo>();

	protected SeasonInfo factory(@Nonnull final World world) {

		if (world.provider.getDimension() == -1)
			return new SeasonInfoNether(world);

		if (TOUGHASNAILS_API)
			return new SeasonInfoToughAsNails(world);

		if (CALENDAR_API)
			return new SeasonInfoCalendar(world);

		return new SeasonInfo(world);
	}

	public SeasonInfo getData(@Nonnull final World world) {
		SeasonInfo result = this.seasonData.get(world.provider.getDimension());
		if (result == null) {
			result = factory(world);
			this.seasonData.put(world.provider.getDimension(), result);
		}
		return result;
	}
	
	public String getSeason() {
		return Season.NONE.getValue();
	}

	@Nonnull
	public BlockPos getPrecipitationHeight(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return getData(world).getPrecipitationHeight(pos);
	}

	public float getTemperature(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return getData(world).getTemperature(pos);
	}
	
	/*
	 * Indicates if it is cold enough that water can freeze.  Could result
	 * in snow or frozen ice.  Does not take into account any other environmental
	 * factors - just whether its cold enough.  If environmental sensitive
	 * versions are needed look at canBlockFreeze() and canSnowAt().
	 */
	public boolean canWaterFreeze(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return getData(world).canWaterFreeze(pos);
	}
	
	/*
	 * Essentially snow layer stuff.
	 */
	public boolean canSnowAt(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return getData(world).canSnowAt(pos);
	}
	
	public boolean canBlockFreeze(@Nonnull final World world, @Nonnull final BlockPos pos, final boolean noWaterAdjacent) {
		return getData(world).canBlockFreeze(pos, noWaterAdjacent);
	}
}
