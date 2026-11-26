/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.orecruncher.dsurround.client.weather.wind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import net.minecraft.block.Block;
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
 * Tests for how open to the weather a place is. Ported from the 26.x line's tests, with 1.12.2's blocks: no
 * campfire (a furnace), no lantern or chain (a ladder, which is too thin to be a roof).
 */
public class ShelterTests {

	static {
		Bootstrap.register();
	}

	@Test
	public void theOpenSkyLetsTheWeatherIn() {
		assertEquals(1F, Shelter.exposure(15));
		assertEquals(0F, Shelter.exposure(8), "in a cave or a building");
		assertEquals(0F, Shelter.exposure(0));
		final float part = Shelter.exposure(11);
		assertTrue(part > 0F && part < 1F);
	}

	@Test
	public void outUnderTheSkyAllOfIt() {
		final World world = new World();
		world.put(0, 0, 0, Blocks.GRASS);
		assertEquals(1F, Shelter.roofShare(world, new BlockPos(0, 1, 0)));
	}

	@Test
	public void aGlassRoofKeepsTheWeatherOut() {
		// Glass lets all the sky light through, so the light alone would let it in
		final World world = new World();
		world.room(Blocks.GLASS);
		assertEquals(0F, Shelter.roofShare(world, new BlockPos(0, 1, 0)));
	}

	@Test
	public void treesAreNotARoof() {
		// The sky light under them, which each layer of leaves dims, says how much gets through
		final World world = new World();
		world.put(0, 5, 0, Blocks.LEAVES);
		world.put(0, 7, 0, Blocks.LEAVES);
		assertEquals(1F, Shelter.roofShare(world, new BlockPos(0, 1, 0)));
		for (int x = -2; x <= 2; x++)
			for (int z = -2; z <= 2; z++)
				world.put(x, 3, z, Blocks.PLANKS);
		assertEquals(0F, Shelter.roofShare(world, new BlockPos(0, 1, 0)), "a roof under the trees");
	}

	@Test
	public void underAnOverhangItComesInFromTheSide() {
		// A roof over x <= 0, open sky beyond
		final World world = new World();
		for (int x = -5; x <= 0; x++)
			for (int z = -5; z <= 5; z++)
				world.put(x, 3, z, Blocks.STONE);
		assertEquals(Shelter.ROOF_SHARE, Shelter.roofShare(world, new BlockPos(0, 1, 0)), "at the edge");
		assertEquals(Shelter.ROOF_SHARE / 2, Shelter.roofShare(world, new BlockPos(-1, 1, 0)), "a block in");
		assertEquals(0F, Shelter.roofShare(world, new BlockPos(-2, 1, 0)), "further in");
	}

	@Test
	public void aWallKeepsItOutFromTheSide() {
		final World world = new World();
		world.room(Blocks.STONE);
		world.put(-2, 1, 0, Blocks.AIR);
		assertEquals(Shelter.ROOF_SHARE / 2, Shelter.roofShare(world, new BlockPos(-1, 1, 0)), "by an open door");
		world.put(-2, 1, 0, Blocks.GLASS_PANE);
		assertEquals(0F, Shelter.roofShare(world, new BlockPos(-1, 1, 0)), "by a window");
	}

	@Test
	public void whatSomethingStartsInIsNotCover() {
		// Smoke starts inside the furnace, mist inside the water
		final World world = new World();
		world.put(0, 0, 0, Blocks.LIT_FURNACE);
		world.put(1, 0, 0, Blocks.WATER);
		assertTrue(Shelter.blocksMotion(Blocks.LIT_FURNACE.getDefaultState()), "the furnace is the top of its column");
		assertEquals(1F, Shelter.roofShare(world, new BlockPos(0, 0, 0)));
		assertEquals(1F, Shelter.roofShare(world, new BlockPos(1, 0, 0)));
	}

	@Test
	public void fallingWaterIsNotARoof() {
		final World world = new World();
		for (int y = 1; y <= 10; y++)
			world.put(0, y, 0, Blocks.WATER);
		assertEquals(1F, Shelter.roofShare(world, new BlockPos(0, 0, 0)));
	}

	@Test
	public void aWaterSurfaceBesideAnOverhangIsOpenSky() {
		// The water tops its column, but nothing is over it
		final World world = new World();
		for (int x = -5; x <= 0; x++)
			for (int z = -5; z <= 5; z++)
				world.put(x, 3, z, Blocks.STONE);
		world.put(1, 1, 0, Blocks.WATER);
		assertEquals(Shelter.ROOF_SHARE, Shelter.roofShare(world, new BlockPos(0, 1, 0)));
	}

	@Test
	public void whatHangsOverheadIsNotARoof() {
		// A ladder blocks motion but is too thin to keep the weather off; nor does a fence rail, the top of a wall or
		// iron bars
		final World world = new World();
		world.put(0, 3, 0, Blocks.LADDER);
		assertEquals(1F, Shelter.roofShare(world, new BlockPos(0, 1, 0)), "a ladder");
		world.put(1, 3, 0, Blocks.OAK_FENCE);
		world.put(2, 3, 0, Blocks.COBBLESTONE_WALL);
		assertEquals(1F, Shelter.roofShare(world, new BlockPos(1, 1, 0)), "a fence");
		assertEquals(1F, Shelter.roofShare(world, new BlockPos(2, 1, 0)), "a wall");
		world.put(3, 3, 0, Blocks.IRON_BARS);
		assertEquals(1F, Shelter.roofShare(world, new BlockPos(3, 1, 0)), "iron bars");
		world.put(4, 3, 0, Blocks.TRAPDOOR);
		assertEquals(Shelter.ROOF_SHARE, Shelter.roofShare(world, new BlockPos(4, 1, 0)),
				"a closed trapdoor is: open only at the side");
		world.put(0, 5, 0, Blocks.STONE_SLAB);
		assertEquals(Shelter.ROOF_SHARE, Shelter.roofShare(world, new BlockPos(0, 1, 0)), "a slab above them is");
	}

	@Test
	public void coverTooDeepToLookThroughIsARoof() {
		// Glass blocks motion but, a pane, isn't a roof: only how deep the cover goes says
		final World world = new World();
		world.put(0, 1 + Shelter.MAX_COVER_DEPTH, 0, Blocks.GLASS_PANE);
		assertEquals(1F, Shelter.roofShare(world, new BlockPos(0, 1, 0)), "as deep as it looks");
		world.put(0, 1 + Shelter.MAX_COVER_DEPTH, 0, Blocks.AIR);
		world.put(0, 2 + Shelter.MAX_COVER_DEPTH, 0, Blocks.GLASS_PANE);
		assertEquals(Shelter.ROOF_SHARE, Shelter.roofShare(world, new BlockPos(0, 1, 0)),
				"deeper: open only at the side");
	}

	/**
	 * Blocks by position; the top of a column is above the highest that blocks motion or holds a fluid, as the
	 * precipitation height has it
	 */
	private static final class World implements Shelter.Columns, IBlockAccess {
		private final Map<BlockPos, IBlockState> blocks = new HashMap<>();

		void put(final int x, final int y, final int z, @Nonnull final Block block) {
			this.blocks.put(new BlockPos(x, y, z), block.getDefaultState());
		}

		// A 3x3 room around (0, 1, 0): walls of stone two high at +-2, roofed with the given block at y 3
		void room(@Nonnull final Block roof) {
			for (int x = -2; x <= 2; x++)
				for (int z = -2; z <= 2; z++) {
					put(x, 3, z, roof);
					if (Math.abs(x) == 2 || Math.abs(z) == 2) {
						put(x, 1, z, Blocks.STONE);
						put(x, 2, z, Blocks.STONE);
					}
				}
		}

		@Override
		public int top(final int x, final int z) {
			int top = 0;
			for (final Map.Entry<BlockPos, IBlockState> entry : this.blocks.entrySet()) {
				final BlockPos pos = entry.getKey();
				final IBlockState state = entry.getValue();
				if (pos.getX() == x && pos.getZ() == z
						&& (Shelter.blocksMotion(state) || state.getMaterial().isLiquid()))
					top = Math.max(top, pos.getY() + 1);
			}
			return top;
		}

		@Override
		public IBlockState state(final BlockPos pos) {
			return getBlockState(pos);
		}

		@Override
		public boolean isRoof(final BlockPos pos, final IBlockState state) {
			return Shelter.isRoof(this, pos, state);
		}

		@Override
		@Nonnull
		public IBlockState getBlockState(@Nonnull final BlockPos pos) {
			final IBlockState state = this.blocks.get(pos.toImmutable());
			return state == null ? Blocks.AIR.getDefaultState() : state;
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
		public boolean isAirBlock(@Nonnull final BlockPos pos) {
			return getBlockState(pos).getBlock() == Blocks.AIR;
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
}
