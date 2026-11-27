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

package org.orecruncher.dsurround.client.fx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

/**
 * Tests for where a waterfall lands and how strong it is, from the 26.x line's. A small world of real blocks; anything
 * not set is air.
 */
public class WaterfallColumnTests {

	static {
		Bootstrap.register();
	}

	private static final IBlockState STONE = Blocks.STONE.getDefaultState();
	private static final IBlockState SOURCE = water(0);
	private static final IBlockState FALLING = water(8);

	private static IBlockState water(final int level) {
		return Blocks.FLOWING_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL, level);
	}

	private static final class World implements IBlockAccess {
		private final Map<BlockPos, IBlockState> blocks = new HashMap<>();

		World set(final int x, final int y, final int z, @Nonnull final IBlockState state) {
			this.blocks.put(new BlockPos(x, y, z), state);
			return this;
		}

		// Falling water from y0 up to y1, at x 0 z 0
		World column(final int y0, final int y1) {
			for (int y = y0; y <= y1; y++)
				set(0, y, 0, FALLING);
			return this;
		}

		boolean lands(final int y) {
			return WaterfallColumn.isLandingSite(this, new BlockPos(0, y, 0));
		}

		int strength(final int y) {
			return WaterfallColumn.strength(this, new BlockPos(0, y, 0));
		}

		@Override
		@Nonnull
		public IBlockState getBlockState(@Nonnull final BlockPos pos) {
			final IBlockState state = this.blocks.get(pos);
			return state != null ? state : Blocks.AIR.getDefaultState();
		}

		@Override
		public boolean isAirBlock(@Nonnull final BlockPos pos) {
			return getBlockState(pos).getBlock() == Blocks.AIR;
		}

		@Override
		@Nullable
		public TileEntity getTileEntity(@Nonnull final BlockPos pos) {
			return null;
		}

		@Override
		public int getCombinedLight(@Nonnull final BlockPos pos, final int lightValue) {
			return 0;
		}

		@Override
		@Nonnull
		public Biome getBiome(@Nonnull final BlockPos pos) {
			return Biomes.PLAINS;
		}

		@Override
		public int getStrongPower(@Nonnull final BlockPos pos, @Nonnull final EnumFacing direction) {
			return 0;
		}

		@Override
		@Nonnull
		public WorldType getWorldType() {
			return WorldType.DEFAULT;
		}

		@Override
		public boolean isSideSolid(@Nonnull final BlockPos pos, @Nonnull final EnumFacing side, final boolean _default) {
			return getBlockState(pos).isSideSolid(this, pos, side);
		}
	}

	@Test
	public void waterFallingOntoStoneLandsAndIsAsStrongAsItsDrop() {
		// A lip of flowing water at 68 pouring over, falling water from 67 down to 64, stone under it
		final World world = new World().set(0, 63, 0, STONE).column(64, 67).set(0, 68, 0, water(1));
		assertTrue(world.lands(64));
		// The three blocks of falling water above where it lands; the landing block and the lip counted too before
		assertEquals(3, world.strength(64));
		// Partway down it is still falling, not landing
		assertFalse(world.lands(66));
	}

	@Test
	public void waterFallingIntoAPoolLandsOnIt() {
		final World world = new World().set(0, 63, 0, SOURCE).column(64, 66);
		assertTrue(world.lands(64));
		assertEquals(2, world.strength(64));
	}

	@Test
	public void waterStillFallingIntoTheAirDoesNotLand() {
		final World world = new World().column(64, 66);
		assertFalse(world.lands(64));
	}

	@Test
	public void theBottomOfAPoolBesideFlowingWaterIsNoWaterfall() {
		// Flowing water spreading across above a pool's source block: any flowing water above counted before
		final World world = new World().set(0, 63, 0, STONE).set(0, 64, 0, SOURCE).set(0, 65, 0, water(2));
		assertFalse(world.lands(64));
	}

	@Test
	public void waterHemmedInOnEverySideIsNoWaterfall() {
		final World world = new World().set(0, 63, 0, STONE).column(64, 66);
		for (final EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
			final BlockPos at = new BlockPos(0, 64, 0).offset(side);
			world.set(at.getX(), at.getY(), at.getZ(), STONE);
		}
		assertFalse(world.lands(64));
		// Flowing water spreading out to one side is room enough
		world.set(1, 64, 0, water(3));
		assertTrue(world.lands(64));
	}

	@Test
	public void strengthStopsAtTheMost() {
		final World world = new World().set(0, 63, 0, STONE).column(64, 90);
		assertEquals(WaterfallColumn.MAX_STRENGTH, world.strength(64));
	}
}
