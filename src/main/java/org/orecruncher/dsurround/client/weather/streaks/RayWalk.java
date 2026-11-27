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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;

/**
 * Follows a line through the blocks to the first thing in its way: a block's collision box, or a fluid's surface, as
 * the 26.x line's clip with a collider and any fluid. 1.12.2's own ray trace is on {@code World} only, and tests have a
 * fake block access. Blocks are walked in the order the line enters them, so the first box met is the nearest (a fence
 * or wall's box, taller than its block, can be met late).
 */
public final class RayWalk {

	private RayWalk() {
	}

	/**
	 * What a line from a point the way given meets within a reach: the block, the side of it met and where. Null if
	 * nothing is within reach, it started inside something, or it has no direction.
	 */
	@Nullable
	public static RayTraceResult trace(@Nonnull final IBlockAccess blocks, @Nonnull final Vec3d from, final float dx,
			final float dy, final float dz, final double reach) {
		final float length = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
		if (length <= 0F)
			return null;
		final double ux = dx / length, uy = dy / length, uz = dz / length;
		final Vec3d to = from.addVector(ux * reach, uy * reach, uz * reach);

		int x = MathHelper.floor(from.x), y = MathHelper.floor(from.y), z = MathHelper.floor(from.z);
		final int stepX = ux > 0 ? 1 : ux < 0 ? -1 : 0;
		final int stepY = uy > 0 ? 1 : uy < 0 ? -1 : 0;
		final int stepZ = uz > 0 ? 1 : uz < 0 ? -1 : 0;
		// How far along the line, in blocks, the next boundary each way is, and how far apart they are
		double nextX = boundary(from.x, x, ux), nextY = boundary(from.y, y, uy), nextZ = boundary(from.z, z, uz);
		final double gapX = stepX == 0 ? Double.MAX_VALUE : Math.abs(1D / ux);
		final double gapY = stepY == 0 ? Double.MAX_VALUE : Math.abs(1D / uy);
		final double gapZ = stepZ == 0 ? Double.MAX_VALUE : Math.abs(1D / uz);

		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		double travelled = 0D;
		while (travelled <= reach) {
			pos.setPos(x, y, z);
			final AxisAlignedBB shape = shapeOf(blocks, pos);
			if (shape != null) {
				final AxisAlignedBB box = shape.offset(x, y, z);
				if (box.contains(from))
					return null;
				final RayTraceResult hit = box.calculateIntercept(from, to);
				if (hit != null)
					return new RayTraceResult(hit.hitVec, hit.sideHit, pos.toImmutable());
			}
			if (nextX <= nextY && nextX <= nextZ) {
				travelled = nextX;
				nextX += gapX;
				x += stepX;
			} else if (nextY <= nextZ) {
				travelled = nextY;
				nextY += gapY;
				y += stepY;
			} else {
				travelled = nextZ;
				nextZ += gapZ;
				z += stepZ;
			}
		}
		return null;
	}

	private static double boundary(final double start, final int block, final double way) {
		if (way > 0)
			return (block + 1 - start) / way;
		if (way < 0)
			return (block - start) / way;
		return Double.MAX_VALUE;
	}

	/**
	 * What of a block stops the line, within its block: a fluid up to its surface, otherwise its collision box
	 */
	@Nullable
	static AxisAlignedBB shapeOf(@Nonnull final IBlockAccess blocks, @Nonnull final BlockPos pos) {
		final IBlockState state = blocks.getBlockState(pos);
		if (state.getMaterial().isLiquid()) {
			final int level = state.getPropertyKeys().contains(BlockLiquid.LEVEL) ? state.getValue(BlockLiquid.LEVEL)
					: 0;
			return new AxisAlignedBB(0D, 0D, 0D, 1D, 1D - BlockLiquid.getLiquidHeightPercent(level), 1D);
		}
		return state.getCollisionBoundingBox(blocks, pos);
	}
}
