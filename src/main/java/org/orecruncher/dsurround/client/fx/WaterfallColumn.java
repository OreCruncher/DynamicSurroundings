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

import javax.annotation.Nonnull;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The shape falling water makes where it lands, from the 26.x line's WaterfallColumn: whether a waterfall lands at a
 * block, and how strong it is (the height of its drop). Kept apart from the effect so it can be tested with a small
 * world.
 * <p>
 * Vanilla's water says whether it is falling (a level of 8 and up). A mod's fluid (Forge's fluid blocks: Streams'
 * rivers, the Betweenlands' swamp water) doesn't, so it is taken as falling where the same fluid goes on above it.
 */
@SideOnly(Side.CLIENT)
public final class WaterfallColumn {

	/**
	 * The strongest a waterfall is: a drop this many blocks high, or higher
	 */
	public static final int MAX_STRENGTH = 10;

	private WaterfallColumn() {
	}

	/**
	 * Whether the liquid at {@code pos} is falling
	 */
	static boolean isFalling(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos,
			@Nonnull final IBlockState state) {
		if (!state.getMaterial().isLiquid())
			return false;
		if (state.getBlock() instanceof BlockLiquid)
			return state.getValue(BlockLiquid.LEVEL) >= 8;
		return !isSource(state) && world.getBlockState(pos.up()).getBlock() == state.getBlock();
	}

	/**
	 * Whether a liquid block is a source (a still pool)
	 */
	static boolean isSource(@Nonnull final IBlockState state) {
		if (state.getBlock() instanceof BlockLiquid)
			return state.getValue(BlockLiquid.LEVEL) == 0;
		if (state.getPropertyKeys().contains(BlockFluidBase.LEVEL))
			return state.getValue(BlockFluidBase.LEVEL) == 0;
		return false;
	}

	/**
	 * Whether a liquid block only partly fills its block (flowing water spreading out), so more can spread into it
	 */
	static boolean isPartlyFull(@Nonnull final IBlockState state) {
		if (!state.getMaterial().isLiquid() || isSource(state))
			return false;
		if (state.getBlock() instanceof BlockLiquid)
			return state.getValue(BlockLiquid.LEVEL) < 8;
		return true;
	}

	/**
	 * Whether falling water at {@code pos} has something to land on: a source below (a pool) or a block with a solid
	 * top. Otherwise it is still falling.
	 */
	static boolean landsOn(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos) {
		final BlockPos below = pos.down();
		final IBlockState state = world.getBlockState(below);
		if (state.getMaterial().isLiquid())
			return isSource(state);
		return state.getBlockFaceShape(world, below, EnumFacing.UP) == BlockFaceShape.SOLID;
	}

	/**
	 * Whether water at {@code pos} can spread sideways: a side is air, or water that only partly fills its block.
	 * Water hemmed in on every side (a full pool, or walls) makes no waterfall.
	 */
	static boolean canSpread(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos) {
		for (final EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
			final IBlockState state = world.getBlockState(pos.offset(side));
			if (state.getMaterial() == Material.AIR || isPartlyFull(state))
				return true;
		}
		return false;
	}

	/**
	 * Whether a waterfall lands at {@code pos}: liquid here, falling liquid directly above, room to spread sideways,
	 * and something to land on. Any flowing water above counted before, so the bottom of a still pool beside moving
	 * water made a waterfall.
	 */
	public static boolean isLandingSite(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos) {
		if (!world.getBlockState(pos).getMaterial().isLiquid())
			return false;
		final BlockPos above = pos.up();
		return isFalling(world, above, world.getBlockState(above)) && canSpread(world, pos) && landsOn(world, pos);
	}

	/**
	 * A waterfall's strength: the height of its drop, as the blocks of falling liquid directly above where it lands,
	 * up to {@link #MAX_STRENGTH}; 0 if there are none. Every liquid block up from the landing block counted before,
	 * the landing block and the lip the water pours over among them, which made a waterfall 2 stronger than its drop.
	 */
	public static int strength(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos) {
		final BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos(pos);
		int count = 0;
		while (count < MAX_STRENGTH) {
			at.setY(at.getY() + 1);
			if (!isFalling(world, at, world.getBlockState(at)))
				break;
			count++;
		}
		return count;
	}
}
