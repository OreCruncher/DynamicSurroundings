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

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * How open to the weather a place is, 0 to 1: all of it out under the open sky, none in a cave or a building. The
 * wind reaches only as far. Ported from the 26.x line.
 * <p>
 * Sky light alone lets too much in: it fades only a level a block sideways, so a room with a window or a door is lit
 * nearly as the outside is, and glass doesn't dim it at all. So what is overhead counts too: the blocks above the
 * place, from the top of its column (the precipitation height: glass is in it, so a greenhouse roof is a roof) down.
 * Under a roof, only what comes in sideways reaches it: under an overhang, or through a doorway to the open sky a block
 * or two away. Leaves aren't a roof: under trees the sky light, which each layer of leaves dims, says how much gets
 * through. Nor is a fence, a fence gate, a wall, iron bars or a pane, a ladder, a fluid (water falling past), or the
 * place's own block (smoke starts inside a fire, mist inside the water).
 * <p>
 * Client thread only, which in Minecraft is also the render thread.
 */
public final class Shelter {

	// Sky light below which nothing reaches, and the range over which it builds to all of it
	static final int MIN_SKY_LIGHT = 8;
	static final float SKY_LIGHT_RANGE = 7F;
	// How deep cover is looked through for a roof; deeper is taken as one
	static final int MAX_COVER_DEPTH = 32;
	// Under a roof: the share that comes in a block from the open sky, falling off further in, as far as it reaches
	static final float ROOF_SHARE = 0.5F;
	static final int ROOF_REACH = 2;
	/**
	 * The share of its block a roof's shape covers, seen from below, at least
	 */
	static final double MIN_FOOTPRINT = 0.5D;

	private static final EnumFacing[] SIDES = { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST,
			EnumFacing.EAST };
	// Reused for every look at the world: client thread only
	private static final WorldColumns WORLD_COLUMNS = new WorldColumns();
	private static final BlockPos.MutableBlockPos SCRATCH = new BlockPos.MutableBlockPos();

	private Shelter() {
	}

	/**
	 * How open to the weather a place is, 0 to 1
	 */
	public static float exposure(@Nonnull final World world, @Nonnull final BlockPos pos) {
		final float open = exposure(world.getLightFor(EnumSkyBlock.SKY, pos));
		if (open == 0F)
			return 0F;
		WORLD_COLUMNS.world = world;
		try {
			return open * roofShare(WORLD_COLUMNS, pos);
		} finally {
			WORLD_COLUMNS.world = null;
		}
	}

	/**
	 * How open a place with the given sky light is, before what is overhead is looked at
	 */
	static float exposure(final int skyLight) {
		return MathHelper.clamp((skyLight - MIN_SKY_LIGHT) / SKY_LIGHT_RANGE, 0F, 1F);
	}

	/**
	 * What a roof over a place leaves, 0 to 1: all of it with none, otherwise what comes in sideways
	 */
	static float roofShare(@Nonnull final Columns columns, @Nonnull final BlockPos pos) {
		return isRoofed(columns, pos) ? sideways(columns, pos) : 1F;
	}

	// A roof above the place, below the top of its column
	private static boolean isRoofed(final Columns columns, final BlockPos pos) {
		final int top = columns.top(pos.getX(), pos.getZ());
		if (top - 1 - pos.getY() > MAX_COVER_DEPTH)
			return true;
		for (int y = top - 1; y > pos.getY(); y--) {
			SCRATCH.setPos(pos.getX(), y, pos.getZ());
			if (columns.isRoof(SCRATCH, columns.state(SCRATCH)))
				return true;
		}
		return false;
	}

	// The most that reaches in from the open sky beside a roofed place, through anything that doesn't block motion
	private static float sideways(final Columns columns, final BlockPos pos) {
		float share = 0F;
		for (final EnumFacing side : SIDES) {
			for (int step = 1; step <= ROOF_REACH; step++) {
				SCRATCH.setPos(pos.getX() + side.getFrontOffsetX() * step, pos.getY(), pos.getZ() + side.getFrontOffsetZ() * step);
				if (blocksMotion(columns.state(SCRATCH)))
					break;
				// Its own block doesn't block motion, so at most a fluid's surface there
				if (SCRATCH.getY() >= columns.top(SCRATCH.getX(), SCRATCH.getZ()) - 1) {
					share = Math.max(share, ROOF_SHARE / step);
					break;
				}
			}
		}
		return share;
	}

	/**
	 * Whether a block blocks motion, as the precipitation height takes it
	 */
	static boolean blocksMotion(@Nonnull final IBlockState state) {
		return state.getMaterial().blocksMovement();
	}

	/**
	 * Whether a block is a roof over what is below it: it blocks motion, spreads over at least {@link #MIN_FOOTPRINT}
	 * of its block seen from below (not a ladder), and isn't leaves, a fence, a fence gate, a wall, or iron bars or a
	 * pane: rain and wind get through trees, and past a fence rail or the top of a wall overhead
	 */
	static boolean isRoof(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos,
			@Nonnull final IBlockState state) {
		if (!blocksMotion(state))
			return false;
		final Block block = state.getBlock();
		if (block instanceof BlockLeaves || block instanceof BlockFence || block instanceof BlockFenceGate
				|| block instanceof BlockWall || block instanceof BlockPane)
			return false;
		final AxisAlignedBB box = state.getCollisionBoundingBox(world, pos);
		return box != null && (box.maxX - box.minX) * (box.maxZ - box.minZ) >= MIN_FOOTPRINT;
	}

	/**
	 * The world, as far as {@link #roofShare} looks at it
	 */
	interface Columns {
		/**
		 * The lowest y with nothing above it that blocks motion or holds a fluid
		 */
		int top(int x, int z);

		IBlockState state(BlockPos pos);

		/**
		 * Whether a block is a roof (see {@link Shelter#isRoof})
		 */
		boolean isRoof(BlockPos pos, IBlockState state);
	}

	private static final class WorldColumns implements Columns {
		private World world;
		private final BlockPos.MutableBlockPos column = new BlockPos.MutableBlockPos();

		@Override
		public int top(final int x, final int z) {
			return this.world.getPrecipitationHeight(this.column.setPos(x, 0, z)).getY();
		}

		@Override
		public IBlockState state(final BlockPos pos) {
			return this.world.getBlockState(pos);
		}

		@Override
		public boolean isRoof(final BlockPos pos, final IBlockState state) {
			return Shelter.isRoof(this.world, pos, state);
		}
	}
}
