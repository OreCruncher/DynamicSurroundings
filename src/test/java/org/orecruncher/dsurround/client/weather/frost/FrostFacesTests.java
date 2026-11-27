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

package org.orecruncher.dsurround.client.weather.frost;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

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
 * Tests for which sides of a window frost, where their glass is, and how far the window reaches beyond them. Ported
 * from the 26.x line's tests. A small world of real blocks stands in for the world: a column sees the sky when nothing
 * is above, and sky light is full there unless set. 1.12.2's panes take their connections from their neighbors, and a
 * pane on its own is only a post, so the panes here are given something to reach to.
 */
public class FrostFacesTests {

	static {
		Bootstrap.register();
	}

	private static final IBlockState GLASS = Blocks.GLASS.getDefaultState();
	private static final IBlockState STONE = Blocks.STONE.getDefaultState();
	private static final IBlockState PANE = Blocks.GLASS_PANE.getDefaultState();

	private static final class World implements IFrostWorld, IBlockAccess {
		private final Map<BlockPos, IBlockState> blocks = new HashMap<>();
		private final Map<BlockPos, Integer> sky = new HashMap<>();

		World set(final int x, final int y, final int z, @Nonnull final IBlockState state) {
			this.blocks.put(new BlockPos(x, y, z), state);
			return this;
		}

		World sky(final int x, final int y, final int z, final int light) {
			this.sky.put(new BlockPos(x, y, z), light);
			return this;
		}

		// A pane running east and west at a position, held between stone
		World paneEastWest(final int x, final int y, final int z) {
			return set(x, y, z, PANE).set(x - 1, y, z, STONE).set(x + 1, y, z, STONE);
		}

		List<FrostFace> faces(final int x, final int y, final int z) {
			final BlockPos pos = new BlockPos(x, y, z);
			final List<FrostFace> found = new ArrayList<>();
			FrostFaces.facesOf(this, pos, getBlockState(pos), found::add);
			return found;
		}

		@Override
		@Nonnull
		public IBlockAccess blocks() {
			return this;
		}

		@Override
		public boolean frosts(@Nonnull final IBlockState state) {
			return state.getBlock() == Blocks.GLASS || state.getBlock() == Blocks.GLASS_PANE
					|| state.getBlock() == Blocks.STAINED_GLASS;
		}

		@Override
		public boolean seesSky(@Nonnull final BlockPos pos) {
			for (final Map.Entry<BlockPos, IBlockState> entry : this.blocks.entrySet()) {
				final BlockPos at = entry.getKey();
				if (at.getX() == pos.getX() && at.getZ() == pos.getZ() && at.getY() > pos.getY()
						&& entry.getValue().getBlock() != Blocks.AIR)
					return false;
			}
			return true;
		}

		@Override
		public int skyLight(@Nonnull final BlockPos pos) {
			final Integer light = this.sky.get(pos);
			return light == null ? 15 : light;
		}

		@Override
		public int packedLight(@Nonnull final BlockPos pos) {
			return 0xF000F0;
		}

		@Override
		public float temperature(@Nonnull final BlockPos pos) {
			return 0F;
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

	/**
	 * A glass block in a stone wall running north and south, a roofed room to its west and the open to its east
	 */
	private static World houseWall() {
		return new World().set(0, 65, -1, STONE).set(0, 65, 1, STONE).set(0, 66, 0, STONE).set(0, 64, 0, STONE)
				.set(0, 65, 0, GLASS).set(-1, 67, 0, STONE).sky(-1, 65, 0, 13);
	}

	@Test
	public void aWindowInAHouseWallFrostsOutsideOnly() {
		final List<FrostFace> faces = houseWall().faces(0, 65, 0);
		assertEquals(1, faces.size());
		final FrostFace face = faces.get(0);
		assertEquals(EnumFacing.EAST, face.side);
		assertEquals(1F + FrostFaces.LIFT, face.plane, 1.0E-5F);
		assertEquals(0F, face.u0, 1.0E-5F);
		assertEquals(1F, face.u1, 1.0E-5F);
		assertEquals(65F, face.v0, 1.0E-5F);
		assertEquals(66F, face.v1, 1.0E-5F);
	}

	@Test
	public void underEavesTheOutsideStillFrosts() {
		// A porch roof over the outside too: more sky light reaches it than the room
		final World world = houseWall().set(1, 67, 0, STONE).sky(1, 65, 0, 14);
		final List<FrostFace> faces = world.faces(0, 65, 0);
		assertEquals(1, faces.size());
		assertEquals(EnumFacing.EAST, faces.get(0).side);
	}

	@Test
	public void underAGlassRoofTheInsideStaysClear() {
		// A greenhouse: its glass roof lets the sky light through, but is a roof
		final World world = houseWall().set(-1, 67, 0, GLASS).sky(-1, 65, 0, 15);
		final List<FrostFace> faces = world.faces(0, 65, 0);
		assertEquals(1, faces.size());
		assertEquals(EnumFacing.EAST, faces.get(0).side);
	}

	@Test
	public void glassBetweenTwoRoomsDoesNotFrost() {
		final World world = houseWall().set(1, 67, 0, STONE).sky(1, 65, 0, 0).sky(-1, 65, 0, 0);
		assertTrue(world.faces(0, 65, 0).isEmpty());
	}

	@Test
	public void aFreeStandingPaneFrostsOnBothSidesWhereItsGlassIs() {
		final List<FrostFace> faces = new World().paneEastWest(0, 65, 0).faces(0, 65, 0);
		// North and south; its ends are against the stone
		assertEquals(2, faces.size());
		for (final FrostFace face : faces) {
			assertTrue(face.side == EnumFacing.NORTH || face.side == EnumFacing.SOUTH);
			final float glass = face.side == EnumFacing.NORTH ? 7F / 16F - FrostFaces.LIFT : 9F / 16F + FrostFaces.LIFT;
			assertEquals(glass, face.plane, 1.0E-5F);
			assertEquals(0F, face.u0, 1.0E-5F);
			assertEquals(1F, face.u1, 1.0E-5F);
		}
	}

	@Test
	public void aPaneOnItsOwnIsTooThinToFrost() {
		// In 1.12.2 a pane that reaches nothing is only its post
		assertTrue(new World().set(0, 65, 0, PANE).faces(0, 65, 0).isEmpty());
	}

	@Test
	public void glassUnderWaterOrAgainstAWallDoesNotFrost() {
		final World world = new World().set(0, 65, 0, GLASS).set(1, 65, 0, STONE)
				.set(-1, 65, 0, Blocks.WATER.getDefaultState()).set(0, 65, 1, STONE).set(0, 65, -1, STONE);
		assertTrue(world.faces(0, 65, 0).isEmpty());
	}

	@Test
	public void aLayerOfSnowInFrontIsOpen() {
		final World world = houseWall().set(1, 65, 0, Blocks.SNOW_LAYER.getDefaultState());
		assertEquals(1, world.faces(0, 65, 0).size());
	}

	@Test
	public void aWindowsEdgesAreFoundBeyondAFace() {
		// A row of four glass blocks along z, standing in the open
		final World world = new World();
		for (int z = 0; z < 4; z++)
			world.set(0, 65, z, GLASS);
		final FrostFace second = eastFace(world.faces(0, 65, 1));
		// One block to the window's north edge, two of the three to its south end, as far as is looked
		assertEquals(0F, second.edgeU0, 1.0E-5F);
		assertEquals(2F + FrostFaces.EDGE_REACH, second.edgeU1, 1.0E-5F);
		assertEquals(65F, second.edgeV0, 1.0E-5F);
		assertEquals(66F, second.edgeV1, 1.0E-5F);
		final FrostFace first = eastFace(world.faces(0, 65, 0));
		assertEquals(0F, first.edgeU0, 1.0E-5F);
		assertEquals(3F, first.edgeU1, 1.0E-5F);
	}

	@Test
	public void aWindowEndsWhereTheGlassIsNoLongerInLine() {
		// A pane set into the middle of its block beside a glass block: not the same plane, so the window ends there
		final World world = new World().set(0, 65, 0, GLASS).set(0, 65, 1, PANE).set(0, 65, 2, STONE);
		final FrostFace face = eastFace(world.faces(0, 65, 0));
		assertEquals(1F, face.edgeU1, 1.0E-5F);
	}

	@Test
	public void aWindowEndsWhereTheGlassIsCovered() {
		// Glass beside it with a wall in front: frost doesn't carry on behind the wall
		final World world = new World().set(0, 65, 0, GLASS).set(0, 65, 1, GLASS).set(1, 65, 1, STONE);
		final FrostFace face = eastFace(world.faces(0, 65, 0));
		assertEquals(1F, face.edgeU1, 1.0E-5F);
	}

	@Test
	public void panesCarryAWindowOnUpAndDown() {
		final World world = new World().paneEastWest(0, 64, 0).paneEastWest(0, 65, 0).paneEastWest(0, 66, 0);
		FrostFace middle = null;
		for (final FrostFace face : world.faces(0, 65, 0))
			if (face.side == EnumFacing.NORTH)
				middle = face;
		assertEquals(64F, middle.edgeV0, 1.0E-5F);
		assertEquals(67F, middle.edgeV1, 1.0E-5F);
	}

	@Test
	public void whereAPanesArmsCrossTheGlassIsCutAround() {
		// Arms along both axes, as a pane joined on all four sides has: the side of one arm is cut where the other
		// crosses it, leaving the glass either side
		final AxisAlignedBB alongX = new AxisAlignedBB(0D, 0D, 7D / 16D, 1D, 1D, 9D / 16D);
		final AxisAlignedBB alongZ = new AxisAlignedBB(7D / 16D, 0D, 0D, 9D / 16D, 1D, 1D);
		final List<AxisAlignedBB> parts = FrostFaces.uncovered(Arrays.asList(alongX, alongZ), alongX,
				EnumFacing.NORTH, 7D / 16D);
		assertEquals(2, parts.size());
		assertEquals(0D, parts.get(0).minX, 1.0E-9D);
		assertEquals(7D / 16D, parts.get(0).maxX, 1.0E-9D);
		assertEquals(9D / 16D, parts.get(1).minX, 1.0E-9D);
		assertEquals(1D, parts.get(1).maxX, 1.0E-9D);
		// A straight pane is one face
		assertEquals(1, FrostFaces.uncovered(Arrays.asList(alongX), alongX, EnumFacing.NORTH, 7D / 16D).size());
	}

	private static FrostFace eastFace(@Nonnull final List<FrostFace> faces) {
		for (final FrostFace face : faces)
			if (face.side == EnumFacing.EAST)
				return face;
		throw new AssertionError("no east face");
	}
}
