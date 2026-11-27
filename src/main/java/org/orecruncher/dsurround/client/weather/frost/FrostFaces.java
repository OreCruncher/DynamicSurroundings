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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Finds the sides of a window that frost: the outside of the glass. Ported from the 26.x line. A side is outside when
 * it is open in front and either nothing above that stops the rain, or more sky light reaches it than the other side
 * (under eaves, or a porch roof). So a window in a house wall frosts on the outside only, a free standing pane on both
 * sides, and glass between two rooms, or in a greenhouse under its glass roof, on neither of the inner ones.
 * <p>
 * The glass is where the block's shape is: a pane's is in the middle of its block, and spans as much of it as the
 * pane's connections reach. Faces of the shape too small to show frost (a pane's edge, or a pane on its own, only a
 * post in 1.12.2) are left out, and so is what is hidden inside the shape (where two arms of a pane cross).
 */
public final class FrostFaces {

	/**
	 * How far beyond a face its window's edges are looked for: frost grows in from the edges no further than this
	 */
	static final float EDGE_REACH = 2F;
	// Just off the glass, so the glass doesn't show through
	static final float LIFT = 0.006F;
	// A face narrower or shorter than this is the edge of a pane, too thin to show frost
	static final double MIN_EXTENT = 3D / 16D;
	// How close two planes, or a face and a block's edge, are taken as the same
	private static final double SAME = 1.0E-4D;
	// Where a pane's glass is across its block, as the game's collision boxes have it
	private static final double PANE_MIN = 7D / 16D;
	private static final double PANE_MAX = 9D / 16D;
	private static final EnumFacing[] SIDES = { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST,
			EnumFacing.EAST };

	private FrostFaces() {
	}

	/**
	 * The sides of a window block that frost, each handed to {@code out}
	 */
	public static void facesOf(@Nonnull final IFrostWorld world, @Nonnull final BlockPos pos,
			@Nonnull final IBlockState state, @Nonnull final Consumer<FrostFace> out) {
		final IBlockAccess blocks = world.blocks();
		List<AxisAlignedBB> boxes = null;
		for (final EnumFacing side : SIDES) {
			final BlockPos front = pos.offset(side);
			if (!isOpen(world, front) || !isOutside(world, pos, side))
				continue;
			if (boxes == null) {
				boxes = shapeOf(blocks, pos, state);
				if (boxes.isEmpty())
					return;
			}
			final int light = world.packedLight(front);
			final float skyShare = world.skyLight(front) / 15F;
			final float temperature = world.temperature(front);
			for (final AxisAlignedBB box : boxes) {
				if (box.maxY - box.minY < MIN_EXTENT)
					continue;
				final double plane = plane(box, side);
				for (final AxisAlignedBB part : uncovered(boxes, box, side, plane))
					out.accept(face(world, pos, side, part, plane, light, skyShare, temperature));
			}
		}
	}

	/**
	 * The boxes of a block's shape within its block, 0 to 1. A pane's are worked out from its connections as the
	 * game's collision boxes are, an arm along each axis it reaches along, with the post folded into them, so a pane
	 * across its block is one box; where the arms cross, the two overlap (see {@link #uncovered}). Any other block's is its bounding box.
	 */
	@Nonnull
	static List<AxisAlignedBB> shapeOf(@Nonnull final IBlockAccess blocks, @Nonnull final BlockPos pos,
			@Nonnull final IBlockState state) {
		if (state.getBlock() instanceof BlockPane) {
			final IBlockState actual = state.getActualState(blocks, pos);
			final boolean north = actual.getValue(BlockPane.NORTH), south = actual.getValue(BlockPane.SOUTH);
			final boolean west = actual.getValue(BlockPane.WEST), east = actual.getValue(BlockPane.EAST);
			final List<AxisAlignedBB> boxes = new ArrayList<>(2);
			if (west || east)
				boxes.add(new AxisAlignedBB(west ? 0D : PANE_MIN, 0D, PANE_MIN, east ? 1D : PANE_MAX, 1D, PANE_MAX));
			if (north || south)
				boxes.add(new AxisAlignedBB(PANE_MIN, 0D, north ? 0D : PANE_MIN, PANE_MAX, 1D, south ? 1D : PANE_MAX));
			if (boxes.isEmpty())
				boxes.add(new AxisAlignedBB(PANE_MIN, 0D, PANE_MIN, PANE_MAX, 1D, PANE_MAX));
			return boxes;
		}
		final AxisAlignedBB box = state.getBoundingBox(blocks, pos);
		return box == null ? Collections.emptyList() : Collections.singletonList(box);
	}

	/**
	 * Whether a side of a window is its outside: in front of it, nothing overhead, or more sky light than behind it
	 */
	static boolean isOutside(@Nonnull final IFrostWorld world, @Nonnull final BlockPos pos,
			@Nonnull final EnumFacing side) {
		final BlockPos front = pos.offset(side);
		if (world.seesSky(front))
			return true;
		final BlockPos back = pos.offset(side.getOpposite());
		final int behind = isOpen(world, back) ? world.skyLight(back) : 0;
		return world.skyLight(front) > behind;
	}

	/**
	 * Whether a block is open: nothing solid, and no fluid, that frost could be on the other side of. A layer of snow
	 * is open: 1.12.2 gives the thinnest a collision box with no height, where the 26.x line gives it none.
	 */
	static boolean isOpen(@Nonnull final IFrostWorld world, @Nonnull final BlockPos pos) {
		final IBlockAccess blocks = world.blocks();
		final IBlockState state = blocks.getBlockState(pos);
		if (state.getBlock().isAir(state, blocks, pos))
			return true;
		if (state.getMaterial().isLiquid())
			return false;
		final AxisAlignedBB box = state.getCollisionBoundingBox(blocks, pos);
		return box == null || box.maxY - box.minY < SAME;
	}

	@Nonnull
	private static FrostFace face(@Nonnull final IFrostWorld world, @Nonnull final BlockPos pos,
			@Nonnull final EnumFacing side, @Nonnull final AxisAlignedBB box, final double plane, final int light,
			final float skyShare, final float temperature) {
		final EnumFacing.Axis across = acrossAxis(side);
		final EnumFacing lower = EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, across);
		final EnumFacing upper = lower.getOpposite();
		final int base = across == EnumFacing.Axis.X ? pos.getX() : pos.getZ();
		final double u0 = base + min(box, across);
		final double u1 = base + max(box, across);
		final float v0 = (float) (pos.getY() + box.minY);
		final float v1 = (float) (pos.getY() + box.maxY);
		final int planeBase = side.getAxis() == EnumFacing.Axis.X ? pos.getX() : pos.getZ();
		final double lifted = planeBase + plane
				+ (side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? LIFT : -LIFT);
		return new FrostFace(pos.getX(), pos.getY(), pos.getZ(), side, lifted, u0, u1, v0, v1,
				u0 - reach(world, pos, side, box, plane, lower), u1 + reach(world, pos, side, box, plane, upper),
				v0 - reach(world, pos, side, box, plane, EnumFacing.DOWN),
				v1 + reach(world, pos, side, box, plane, EnumFacing.UP), light, skyShare, temperature);
	}

	/**
	 * How far the window carries on beyond a face, one way along the glass, up to {@link #EDGE_REACH}: through the
	 * blocks next to it that are frosting glass, open in front, with glass in the same plane meeting this face's
	 */
	static float reach(@Nonnull final IFrostWorld world, @Nonnull final BlockPos pos, @Nonnull final EnumFacing side,
			@Nonnull final AxisAlignedBB box, final double plane, @Nonnull final EnumFacing along) {
		if (!reachesEdge(box, along))
			return 0F;
		final IBlockAccess blocks = world.blocks();
		final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(pos);
		AxisAlignedBB previous = box;
		float reach = 0F;
		while (reach < EDGE_REACH) {
			cursor.move(along);
			final IBlockState state = blocks.getBlockState(cursor);
			if (!world.frosts(state) || !isOpen(world, cursor.offset(side)))
				break;
			final AxisAlignedBB next = meeting(shapeOf(blocks, cursor.toImmutable(), state), previous, side, plane,
					along);
			if (next == null)
				break;
			reach += (float) (max(next, along.getAxis()) - min(next, along.getAxis()));
			if (!reachesEdge(next, along))
				break;
			previous = next;
		}
		return Math.min(reach, EDGE_REACH);
	}

	/**
	 * The box of the next block's shape that carries a face on: its side in the same plane, starting at the edge the
	 * face ends at, and alongside it
	 */
	@Nullable
	private static AxisAlignedBB meeting(@Nonnull final List<AxisAlignedBB> boxes, @Nonnull final AxisAlignedBB from,
			@Nonnull final EnumFacing side, final double plane, @Nonnull final EnumFacing along) {
		final EnumFacing.Axis other = along.getAxis() == EnumFacing.Axis.Y ? acrossAxis(side) : EnumFacing.Axis.Y;
		for (final AxisAlignedBB box : boxes) {
			if (Math.abs(plane(box, side) - plane) > SAME || !reachesEdge(box, along.getOpposite()))
				continue;
			if (max(box, other) > min(from, other) + SAME && min(box, other) < max(from, other) - SAME)
				return box;
		}
		return null;
	}

	/**
	 * Whether a box reaches its block's edge one way
	 */
	private static boolean reachesEdge(@Nonnull final AxisAlignedBB box, @Nonnull final EnumFacing way) {
		final EnumFacing.Axis axis = way.getAxis();
		return way.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? max(box, axis) > 1D - SAME
				: min(box, axis) < SAME;
	}

	/**
	 * The parts of a box's side that show: its extent across, less where just in front of it is inside another box of
	 * the shape (where the arms of a pane cross), each part as the box narrowed to it; parts too narrow to show frost
	 * are left out
	 */
	@Nonnull
	static List<AxisAlignedBB> uncovered(@Nonnull final List<AxisAlignedBB> boxes, @Nonnull final AxisAlignedBB box,
			@Nonnull final EnumFacing side, final double plane) {
		final EnumFacing.Axis across = acrossAxis(side);
		final EnumFacing.Axis looking = side.getAxis();
		final double front = plane + (side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 0.01D : -0.01D);
		List<double[]> spans = new ArrayList<>(2);
		spans.add(new double[] { min(box, across), max(box, across) });
		for (final AxisAlignedBB other : boxes) {
			if (other == box || front < min(other, looking) || front >= max(other, looking)
					|| other.maxY <= box.minY + SAME || other.minY >= box.maxY - SAME)
				continue;
			final double cut0 = min(other, across), cut1 = max(other, across);
			final List<double[]> left = new ArrayList<>(spans.size() + 1);
			for (final double[] span : spans) {
				if (cut1 <= span[0] || cut0 >= span[1]) {
					left.add(span);
					continue;
				}
				if (cut0 > span[0])
					left.add(new double[] { span[0], cut0 });
				if (cut1 < span[1])
					left.add(new double[] { cut1, span[1] });
			}
			spans = left;
		}
		final List<AxisAlignedBB> parts = new ArrayList<>(spans.size());
		for (final double[] span : spans) {
			if (span[1] - span[0] < MIN_EXTENT)
				continue;
			parts.add(across == EnumFacing.Axis.X
					? new AxisAlignedBB(span[0], box.minY, box.minZ, span[1], box.maxY, box.maxZ)
					: new AxisAlignedBB(box.minX, box.minY, span[0], box.maxX, box.maxY, span[1]));
		}
		return parts;
	}

	/**
	 * Where a box's side is, along the axis the side looks along, within the block
	 */
	private static double plane(@Nonnull final AxisAlignedBB box, @Nonnull final EnumFacing side) {
		return side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? max(box, side.getAxis())
				: min(box, side.getAxis());
	}

	/**
	 * The axis across a side, level with the ground: x for a north or south side, z for a west or east one
	 */
	@Nonnull
	static EnumFacing.Axis acrossAxis(@Nonnull final EnumFacing side) {
		return side.getAxis() == EnumFacing.Axis.X ? EnumFacing.Axis.Z : EnumFacing.Axis.X;
	}

	static double min(@Nonnull final AxisAlignedBB box, @Nonnull final EnumFacing.Axis axis) {
		return axis == EnumFacing.Axis.X ? box.minX : axis == EnumFacing.Axis.Y ? box.minY : box.minZ;
	}

	static double max(@Nonnull final AxisAlignedBB box, @Nonnull final EnumFacing.Axis axis) {
		return axis == EnumFacing.Axis.X ? box.maxX : axis == EnumFacing.Axis.Y ? box.maxY : box.maxZ;
	}
}
