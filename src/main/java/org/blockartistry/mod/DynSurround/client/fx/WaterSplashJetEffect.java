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

package org.blockartistry.mod.DynSurround.client.fx;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.api.effects.BlockEffectType;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleJet;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleWaterSplash;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WaterSplashJetEffect extends JetEffect {

	public WaterSplashJetEffect(final int chance) {
		super(chance);
	}

	@Override
	@Nonnull
	public BlockEffectType getEffectType() {
		return BlockEffectType.SPLASH_JET;
	}

	private static boolean isLiquidBlock(final Block block) {
		return BlockLiquid.class.isAssignableFrom(block.getClass());
	}

	private static boolean partialLiquidOrAir(final World world, final BlockPos pos) {
		final IBlockState state = world.getBlockState(pos);
		if (state.getMaterial() == Material.AIR)
			return true;

		if (!isLiquidBlock(state.getBlock()))
			return false;

		// getLiquidHeightPercent() returns the percentage of *air* in the
		// block,
		// not how much water is in it.
		final float height = BlockLiquid.getLiquidHeightPercent(state.getBlock().getMetaFromState(state));
		return height >= 0.12F;
	}

	private static boolean isUnboundedLiquid(final World world, final BlockPos pos) {
		return partialLiquidOrAir(world, pos.north()) || partialLiquidOrAir(world, pos.south())
				|| partialLiquidOrAir(world, pos.east()) || partialLiquidOrAir(world, pos.west());
	}

	private int liquidBlockCount(final World world, final BlockPos pos) {
		BlockPos workBlock = pos;

		int count;
		for (count = 0; count < MAX_STRENGTH; count++) {
			final IBlockState state = world.getBlockState(workBlock);
			if (!isLiquidBlock(state.getBlock()) || !isUnboundedLiquid(world, workBlock))
				break;
			workBlock = workBlock.up();
		}

		return count;
	}

	public static boolean isValidSpawnBlock(final World world, final BlockPos pos) {
		if (world.getBlockState(pos).getMaterial() != Material.WATER)
			return false;
		final boolean unbounded = isUnboundedLiquid(world, pos);
		return (unbounded && world.getBlockState(pos.down()).getMaterial().isSolid())
				|| (!unbounded && world.getBlockState(pos.up()).getBlock() instanceof BlockDynamicLiquid);
	}

	@Override
	public boolean trigger(final IBlockState state, final World world, final BlockPos pos, final Random random) {
		return isValidSpawnBlock(world, pos) && super.trigger(state, world, pos, random);
	}

	@Override
	public void doEffect(final IBlockState state, final World world, final BlockPos pos, final Random random) {
		final boolean isUnbounded = isUnboundedLiquid(world, pos);
		final int strength = liquidBlockCount(world, pos.up()) + (isUnbounded ? 1 : 0);

		if (strength < 2)
			return;

		final float height = BlockLiquid.getLiquidHeightPercent(state.getBlock().getMetaFromState(state));
		final double y;
		if (isUnbounded && height < 0.8)
			y = pos.getY() + height + 0.1D;
		else if (!isUnbounded)
			y = pos.getY() + 1.1D;
		else
			y = pos.getY() + 0.1D;

		final ParticleJet effect = new ParticleWaterSplash(strength, world, pos.getX() + 0.5D, y, pos.getZ() + 0.5D);
		addEffect(effect);
	}
}
