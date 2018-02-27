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
package org.blockartistry.lib.chunk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IBlockAccessEx extends IBlockAccess {

	/**
	 * Obtains a reference count for the underlying cache system. Used as a token to
	 * indicate when the cache is refreshed. If the reference changes between ticks
	 * the cache has been reloaded.
	 * 
	 * @return Current reference count of the underlying cache
	 */
	int reference();

	/**
	 * Obtains a reference count for the cache as it relates to world changes (like
	 * going from Overworld to the Nether).
	 * 
	 * @return Current reference count of the underlying cache for world changes
	 */
	int worldReference();

	/**
	 * Gets the underlying world object that the cache uses to obtain chunks.
	 * 
	 * @return World reference, if any
	 */
	@Nullable
	World getWorld();

	/**
	 * Obtains a block state for the given coordinates. If the chunk is not
	 * available within the cache (when out of bounds or if the chunk has yet to be
	 * loaded) AIR is returned.
	 * 
	 * @param x
	 *            X coordinate of the block
	 * @param y
	 *            Y coordinate of the block
	 * @param z
	 *            Z coordinate of the block
	 * @return Block state for the given block
	 */
	@Nonnull
	IBlockState getBlockState(final int x, final int y, final int z);

	/**
	 * Obtains the specified light level for the given position
	 * 
	 * @param type
	 *            Type of light to obtain
	 * @param pos
	 *            Position for which the light value is being queried
	 * @return Light level for the specified position (0-15)
	 */
	int getLightFor(@Nonnull final EnumSkyBlock type, @Nonnull final BlockPos pos);

	/**
	 * Obtains the top solid or liquid block for the vertical column defined by the
	 * provided block position.
	 * 
	 * @param pos
	 *            Position for which the block is to be identified
	 * @return Position of the top solid or liquid block, or the provided position
	 *         if there isn't any
	 */
	@Nonnull
	BlockPos getTopSolidOrLiquidBlock(@Nonnull final BlockPos pos);

	/**
	 * Indicates if the chunk for the block coordinates is available. A chunk may
	 * not be available because the coordinates are out of bounds, or the chunk may
	 * not have been loaded.
	 * 
	 * @param x
	 *            Block X coordinate to be tested
	 * @param z
	 *            Block Z coordinate to be tested
	 * @return true if the chunk is available in the cache; false otherwise
	 */
	boolean isAvailable(final int x, final int z);

	/**
	 * Indicates if the chunk for the block position is available. A chunk may not
	 * be available because the coordinates are out of bounds, or the chunk may not
	 * have been loaded.
	 * 
	 * @param pos
	 *            Block position to be tested
	 * @return true if the chunk is available in the cache; false otherwise
	 */
	default boolean isAvailable(@Nonnull final BlockPos pos) {
		return isAvailable(pos.getX(), pos.getZ());
	}

	/**
	 * Gets the height of precipitation at the specified block location.
	 * 
	 * @param pos
	 *            Position containing the X/Z column coordinate to check
	 * @return BlockPos of the height of precipitation
	 */
	@Nonnull
	BlockPos getPrecipitationHeight(@Nonnull final BlockPos pos);

	/**
	 * Determines if rainfall (precipitation) is occurring at the specified
	 * BlockPos.
	 * 
	 * @param pos
	 *            Position to check for rainfall
	 * @return true if it is raining at the specified position, false otherwise
	 */
	boolean isRainingAt(@Nonnull final BlockPos pos);

	/**
	 * Determines if the sky can be seen from the specified location
	 * 
	 * @param pos
	 *            Position to check sky visibility from
	 * @return true if the sky can be seen, false otherwise
	 */
	boolean canSeeSky(@Nonnull final BlockPos pos);

	/**
	 * Indicates if snow can fall at the specified location.
	 * 
	 * @param pos
	 *            Position to check whether snow can fall
	 * @param checkLight
	 *            Flag indicating to check the light levels for snow
	 * @return true if snowfall can occur, false otherwise
	 */
	boolean canSnowAt(@Nonnull final BlockPos pos, final boolean checkLight);

	/**
	 * Checks whether water can freeze at the specified location
	 * 
	 * @param pos
	 *            Block location to check for freeze
	 * @param noWaterAdjacent
	 *            Flag indicating whether there is water adjacent or not
	 * @return true if water can feeze, false otherwise
	 */
	boolean canBlockFreeze(@Nonnull final BlockPos pos, final boolean noWaterAdjacent);

}
