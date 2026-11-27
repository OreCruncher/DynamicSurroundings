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

package org.orecruncher.dsurround.client.weather.streaks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

/**
 * Tests for rain on windows: which side of a window the wind blows rain onto, and where a drop meets the glass of a
 * block or a pane. Ported from the 26.x line's tests. A small world of real blocks stands in for the world; a pane on
 * its own is only its post in 1.12.2, in the middle of its block.
 */
public class RainOnWindowsTests {

	static {
		Bootstrap.register();
	}

	private static final IBlockState GLASS = Blocks.GLASS.getDefaultState();
	private static final IBlockState PANE = Blocks.GLASS_PANE.getDefaultState();
	private static final IBlockState STONE = Blocks.STONE.getDefaultState();

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
		public boolean isSideSolid(@Nonnull final BlockPos pos, @Nonnull final EnumFacing side,
				final boolean _default) {
			return getBlockState(pos).isSideSolid(this, pos, side);
		}
	}

	private static RayTraceResult trace(@Nonnull final World world, @Nonnull final Vec3d from, final float dx,
			final float dy, final float dz) {
		return RayWalk.trace(world, from, dx, dy, dz, RainOnWindows.REACH);
	}

	@Test
	public void theWindBlowsRainOntoTheSideFacingIt() {
		// Blowing east: onto west faces; blowing north (negative z): onto south faces
		assertEquals(EnumFacing.WEST, RainOnWindows.windwardFace(1F, 0F, 1F, 0.5F, 0));
		assertEquals(EnumFacing.EAST, RainOnWindows.windwardFace(-1F, 0F, 1F, 0.5F, 0));
		assertEquals(EnumFacing.SOUTH, RainOnWindows.windwardFace(0F, -1F, 1F, 0.5F, 0));
		assertEquals(EnumFacing.NORTH, RainOnWindows.windwardFace(0F, 1F, 1F, 0.5F, 0));
	}

	@Test
	public void aSlantingWindReachesBothSidesInProportion() {
		// Mostly east, a little south: west faces most of the time, north faces the rest
		final float x = 0.8F, z = 0.6F;
		int west = 0, north = 0;
		for (int i = 0; i < 100; i++) {
			final EnumFacing side = RainOnWindows.windwardFace(x, z, 1F, i / 100F, 0);
			if (side == EnumFacing.WEST)
				west++;
			else if (side == EnumFacing.NORTH)
				north++;
			else
				fail("rain blown onto the lee side: " + side);
		}
		assertEquals(57, west, 1);
		assertEquals(43, north, 1);
	}

	@Test
	public void inStillAirAnySide() {
		for (int i = 0; i < 4; i++)
			assertTrue(RainOnWindows.windwardFace(1F, 0F, 0F, 0.5F, i).getAxis().isHorizontal());
		assertNotEquals(RainOnWindows.windwardFace(1F, 0F, 0F, 0.5F, 0),
				RainOnWindows.windwardFace(1F, 0F, 0F, 0.5F, 2));
	}

	@Test
	public void aDropMeetsAGlassBlockOnItsFace() {
		final World world = new World().set(0, 70, 0, GLASS);
		final RayTraceResult hit = trace(world, new Vec3d(-2.5, 70.6, 0.5), 0.3F, -0.05F, 0F);
		assertNotNull(hit);
		assertEquals(EnumFacing.WEST, hit.sideHit);
		assertEquals(new BlockPos(0, 70, 0), hit.getBlockPos());
		assertEquals(0D, hit.hitVec.x, 1e-6);
	}

	@Test
	public void aDropMeetsAPaneWhereItsGlassIs() {
		// A pane on its own stands in the middle of its block: a drop meets it there, not at the block's edge
		final World world = new World().set(0, 70, 0, PANE);
		final RayTraceResult hit = trace(world, new Vec3d(-2.5, 70.6, 0.5), 0.3F, -0.05F, 0F);
		assertNotNull(hit);
		assertEquals(EnumFacing.WEST, hit.sideHit);
		assertTrue(hit.hitVec.x > 0.3D && hit.hitVec.x < 0.5D, () -> "met at " + hit.hitVec.x);
	}

	@Test
	public void aLineMeetsTheNearestThingAndAFluidsSurface() {
		final World world = new World().set(3, 70, 0, STONE).set(5, 70, 0, GLASS);
		final RayTraceResult hit = trace(world, new Vec3d(0.5, 70.5, 0.5), 1F, 0F, 0F);
		assertEquals(new BlockPos(3, 70, 0), hit.getBlockPos());
		// Down onto still water: its surface, a little under the top of its block
		final World pond = new World().set(0, 68, 0, Blocks.WATER.getDefaultState());
		final RayTraceResult surface = trace(pond, new Vec3d(0.5, 70.5, 0.5), 0F, -1F, 0F);
		assertEquals(EnumFacing.UP, surface.sideHit);
		assertTrue(surface.hitVec.y > 68.8D && surface.hitVec.y < 69D, () -> "met at " + surface.hitVec.y);
	}

	@Test
	public void aLineStartingInsideSomethingMeetsNothing() {
		final World world = new World().set(0, 70, 0, STONE);
		assertNull(trace(world, new Vec3d(0.5, 70.5, 0.5), 1F, 0F, 0F));
		assertNull(trace(world, new Vec3d(-2.5, 70.5, 0.5), 0F, 0F, 0F), "no direction");
	}

	private static boolean isGlass(@Nonnull final IBlockState state) {
		return state.getBlock() == Blocks.GLASS || state.getBlock() == Blocks.GLASS_PANE;
	}

	@Test
	public void aDropRunsDownGlassWhoseFaceCarriesOn() {
		// Three blocks of glass, one above the other: from the top one, two more below
		final World world = new World().set(0, 70, 0, GLASS).set(0, 69, 0, GLASS).set(0, 68, 0, GLASS);
		final Vec3d at = new Vec3d(0, 70.5, 0.5);
		assertEquals(2, RainOnWindows.glassBelow(world, new BlockPos(0, 70, 0), EnumFacing.WEST, at,
				RainOnWindowsTests::isGlass));
	}

	@Test
	public void notOntoAPaneSetBackBelowIt() {
		// A pane under a glass block counted as more of it, though its glass is in the middle of its block, and a drop
		// ran on down the open air in front of it (a regression in the 26.x line)
		final World world = new World().set(0, 70, 0, GLASS).set(0, 69, 0, PANE);
		assertEquals(0, RainOnWindows.glassBelow(world, new BlockPos(0, 70, 0), EnumFacing.WEST,
				new Vec3d(0, 70.5, 0.5), RainOnWindowsTests::isGlass));
	}

	@Test
	public void notBehindSomethingInFrontOfIt() {
		final World world = new World().set(0, 70, 0, GLASS).set(0, 69, 0, GLASS).set(-1, 69, 0, STONE);
		assertEquals(0, RainOnWindows.glassBelow(world, new BlockPos(0, 70, 0), EnumFacing.WEST,
				new Vec3d(0, 70.5, 0.5), RainOnWindowsTests::isGlass));
	}

	@Test
	public void notOntoAnythingElse() {
		final World world = new World().set(0, 70, 0, GLASS).set(0, 69, 0, STONE);
		assertEquals(0, RainOnWindows.glassBelow(world, new BlockPos(0, 70, 0), EnumFacing.WEST,
				new Vec3d(0, 70.5, 0.5), RainOnWindowsTests::isGlass));
	}

	@Test
	public void itDripsOffAnEdgeOverOpenAir() {
		final World world = new World().set(0, 70, 0, GLASS);
		assertTrue(RainOnWindows.dripsOff(world, new Vec3d(0, 70.5, 0.5), EnumFacing.WEST, 70));
		// Ground far below doesn't stop it
		world.set(0, 60, 0, STONE).set(-1, 60, 0, STONE);
		assertTrue(RainOnWindows.dripsOff(world, new Vec3d(0, 70.5, 0.5), EnumFacing.WEST, 70));
	}

	@Test
	public void itRunsOntoASillOrTheWallBelow() {
		// The wall below, flush with the glass: the drop runs on down it
		final World wall = new World().set(0, 70, 0, GLASS).set(0, 69, 0, STONE);
		assertFalse(RainOnWindows.dripsOff(wall, new Vec3d(0, 70.5, 0.5), EnumFacing.WEST, 70));
		// A sill standing out under it, a slab's top a little lower
		final World sill = new World().set(0, 70, 0, GLASS).set(-1, 69, 0,
				Blocks.STONE_SLAB.getDefaultState().withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP));
		assertFalse(RainOnWindows.dripsOff(sill, new Vec3d(0, 70.5, 0.5), EnumFacing.WEST, 70));
	}

	@Test
	public void itDripsOffInFrontOfSomethingSetBack() {
		// A pane under a glass block: its glass is in the middle of its block, behind the edge the drop runs off
		final World world = new World().set(0, 70, 0, GLASS).set(0, 69, 0, PANE);
		assertTrue(RainOnWindows.dripsOff(world, new Vec3d(0, 70.5, 0.5), EnumFacing.WEST, 70));
	}

	@Test
	public void underAPaneItDripsWhereThePaneIs() {
		// A pane over a full block: the block's side is flush with the pane's block, well in front of the pane's glass,
		// so the drop runs onto its top
		final World world = new World().set(0, 70, 0, PANE).set(0, 69, 0, STONE);
		assertFalse(RainOnWindows.dripsOff(world, new Vec3d(0.4375, 70.5, 0.5), EnumFacing.WEST, 70));
		final World hanging = new World().set(0, 70, 0, PANE);
		assertTrue(RainOnWindows.dripsOff(hanging, new Vec3d(0.4375, 70.5, 0.5), EnumFacing.WEST, 70));
	}

	@Test
	public void manyTriesAreWholeAndAChanceOfOneMore() {
		assertEquals(2, RainOnWindows.tries(2.4F, 0.5F));
		assertEquals(3, RainOnWindows.tries(2.4F, 0.3F));
		assertEquals(0, RainOnWindows.tries(0F, 0F));
	}
}
