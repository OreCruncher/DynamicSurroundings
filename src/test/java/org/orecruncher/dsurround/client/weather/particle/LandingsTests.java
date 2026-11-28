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

package org.orecruncher.dsurround.client.weather.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

/**
 * Tests for what particle weather leaves where it lands: flecks on the face a snowflake hit, on top of a slab, none on
 * water or snow, and rain blown into a window starting a streak. Partly ported from the 26.x line's tests. A small
 * world of real blocks stands in for the world.
 */
public class LandingsTests {

	static {
		Bootstrap.register();
	}

	private static final IBlockState STONE = Blocks.STONE.getDefaultState();
	private static final IBlockState GLASS = Blocks.GLASS.getDefaultState();
	private static final int LIGHT = 0xF000F0;

	private static final class World implements IBlockAccess {
		private final Map<BlockPos, IBlockState> blocks = new HashMap<>();

		World set(final int x, final int y, final int z, @Nonnull final IBlockState state) {
			this.blocks.put(new BlockPos(x, y, z), state);
			return this;
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
			return LIGHT;
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
		public boolean isSideSolid(@Nonnull final BlockPos pos, @Nonnull final EnumFacing side,
				final boolean _default) {
			return getBlockState(pos).isSideSolid(this, pos, side);
		}
	}

	@Test
	public void aFleckLiesOnTheFaceThatWasHit() {
		assertEquals(SnowFlecks.FACE_TOP, Landings.face(EnumFacing.UP));
		assertEquals(SnowFlecks.FACE_WEST, Landings.face(EnumFacing.WEST));
		assertEquals(SnowFlecks.FACE_EAST, Landings.face(EnumFacing.EAST));
		assertEquals(SnowFlecks.FACE_NORTH, Landings.face(EnumFacing.NORTH));
		assertEquals(SnowFlecks.FACE_SOUTH, Landings.face(EnumFacing.SOUTH));
		assertEquals(-1, Landings.face(EnumFacing.DOWN), "nothing settles on the underside of anything");
	}

	@Test
	public void theSurfaceIsTheTopOfTheBoxWhereItLanded() {
		final AxisAlignedBB slab = new AxisAlignedBB(0D, 0D, 0D, 1D, 0.5D, 1D);
		assertEquals(0.5D, Landings.surfaceTop(slab, 0.3D, 0.7D), 1e-9D);
		// A fence post's box, landed beside it; and no box at all: the top of the block
		final AxisAlignedBB post = new AxisAlignedBB(0.375D, 0D, 0.375D, 0.625D, 1.5D, 0.625D);
		assertEquals(1D, Landings.surfaceTop(post, 0.1D, 0.1D), 1e-9D);
		assertEquals(1.5D, Landings.surfaceTop(post, 0.5D, 0.5D), 1e-9D);
		assertEquals(1D, Landings.surfaceTop(null, 0.5D, 0.5D), 1e-9D);
	}

	@Test
	public void aFlakeOnASlabLiesOnItsTop() {
		final World world = new World().set(0, 63, 0,
				Blocks.STONE_SLAB.getDefaultState().withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM));
		final SnowFlecks flecks = new SnowFlecks(10);
		new Landings().snowLanded(world, 0.5D, 64, 0.5D, 0.5F, 0.5F, LIGHT, flecks, new Random(1));
		assertEquals(1, flecks.count());
		assertTrue(flecks.y[0] > 63.5F && flecks.y[0] < 63.52F, "on the slab's top, just above it");
		assertEquals(SnowFlecks.FACE_TOP, flecks.face[0]);
	}

	@Test
	public void noFleckOnWaterSnowOrMagma() {
		final World world = new World().set(0, 63, 0, Blocks.WATER.getDefaultState())
				.set(1, 63, 0, Blocks.SNOW.getDefaultState()).set(2, 63, 0, Blocks.SNOW_LAYER.getDefaultState())
				.set(3, 63, 0, Blocks.MAGMA.getDefaultState());
		final SnowFlecks flecks = new SnowFlecks(10);
		final Landings landings = new Landings();
		for (int x = 0; x < 4; x++)
			landings.snowLanded(world, x + 0.5D, 64, 0.5D, 0.5F, 0.5F, LIGHT, flecks, new Random(1));
		assertEquals(0, flecks.count());
	}

	@Test
	public void aFlakeBlownIntoAWallSticksToItsSide() {
		// A wall at x 5, a flake blowing east into it from in front
		final World world = new World();
		for (int y = 60; y < 70; y++)
			world.set(5, y, 0, STONE);
		final SnowFlecks flecks = new SnowFlecks(10);
		new Landings().snowBlownIn(world, 4.5D, 64.5D, 0.5D, 0.3F, -0.05F, 0F, 0.5F, 0.25F, true, flecks,
				new Random(2));
		assertEquals(1, flecks.count());
		assertEquals(SnowFlecks.FACE_WEST, flecks.face[0]);
		assertTrue(flecks.x[0] < 5D && flecks.x[0] > 5D - 0.02D, "just off the wall's face");
		assertEquals(0.25F, flecks.variant[0]);
		assertEquals(LIGHT, flecks.light[0]);
	}

	@Test
	public void withSidesOffItOnlySettlesOnTop() {
		final World world = new World();
		for (int y = 60; y < 70; y++)
			world.set(5, y, 0, STONE);
		world.set(0, 63, 3, STONE);
		final SnowFlecks flecks = new SnowFlecks(10);
		final Landings landings = new Landings();
		landings.snowBlownIn(world, 4.5D, 64.5D, 0.5D, 0.3F, -0.05F, 0F, 0.5F, 0.25F, false, flecks, new Random(2));
		assertEquals(0, flecks.count(), "not on the wall");
		// In under something, down to the ground
		landings.snowBlownIn(world, 0.5D, 65D, 3.5D, 0.01F, -0.1F, 0F, 0.5F, 0.25F, false, flecks, new Random(2));
		assertEquals(1, flecks.count());
		assertEquals(SnowFlecks.FACE_TOP, flecks.face[0]);
		assertTrue(flecks.y[0] > 64F && flecks.y[0] < 64.02F);
	}

	@Test
	public void rainBlownIntoGlassRunsDownIt() {
		final World world = new World().set(5, 64, 0, GLASS).set(5, 64, 2, STONE);
		final List<EnumFacing> hits = new ArrayList<>();
		final Landings.WindowHit windows = (window, side, at, size) -> {
			assertEquals(new BlockPos(5, 64, 0), window);
			assertEquals(5D, at.x, 1e-9D);
			hits.add(side);
		};
		Landings.rainBlownIn(world, 4.5D, 64.6D, 0.5D, 0.4F, -0.1F, 0F, 0.5F, windows);
		assertEquals(1, hits.size());
		assertEquals(EnumFacing.WEST, hits.get(0));
		// Into stone, it runs off unseen
		Landings.rainBlownIn(world, 4.5D, 64.6D, 2.5D, 0.4F, -0.1F, 0F, 0.5F, windows);
		assertEquals(1, hits.size());
	}
}
