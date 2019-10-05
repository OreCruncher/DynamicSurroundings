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

import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.client.fx.particle.system.ParticleJet;
import org.orecruncher.dsurround.client.fx.particle.system.ParticleWaterSplash;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.chunk.IBlockAccessEx;
import org.orecruncher.lib.math.MathStuff;

import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WaterSplashJetEffect extends JetEffect {

	//@formatter:off
	private final static Vec3i[] cardinal_offsets = {
		new Vec3i(-1, 0, 0),
		new Vec3i(1, 0, 0),
		new Vec3i(0, 0, -1),
		new Vec3i(0, 0, 1)
	};
	//@formatter:on

	public WaterSplashJetEffect(final int chance) {
		super(chance);
	}

	@Override
	@Nonnull
	public BlockEffectType getEffectType() {
		return BlockEffectType.SPLASH_JET;
	}

	private static boolean isLiquidBlock(final IBlockState state) {
		return state.getMaterial().isLiquid();
	}

	private static boolean isUnboundedLiquid(final IBlockAccessEx provider, final BlockPos pos) {
		for (int i = 0; i < cardinal_offsets.length; i++) {
			final BlockPos tp = pos.add(cardinal_offsets[i]);
			final IBlockState state = provider.getBlockState(tp);
			if (state.getMaterial() == Material.AIR)
				return true;
			if (isLiquidBlock(state) && !WorldUtils.isFullWaterBlock(state)
					&& !provider.getBlockState(tp.up()).getMaterial().isLiquid())
				return true;
		}

		return false;
	}

	private int liquidBlockCount(final IBlockAccessEx provider, final BlockPos pos) {
		final BlockPos.MutableBlockPos workBlock = new BlockPos.MutableBlockPos(pos);

		int count;
		for (count = 0; count < MAX_STRENGTH; count++) {
			if (!provider.getBlockState(workBlock).getMaterial().isLiquid())
				break;
			workBlock.setY(workBlock.getY() + 1);
		}

		return MathStuff.clamp(count, 0, MAX_STRENGTH);
	}

	
	public static boolean isValidSpawnBlock(final IBlockAccessEx provider, final BlockPos pos) {
		return isValidSpawnBlock(provider, provider.getBlockState(pos), pos);
	}
	
	private static boolean isValidSpawnBlock(final IBlockAccessEx provider, final IBlockState state, final BlockPos pos) {
		if (state.getMaterial() != Material.WATER)
			return false;
		if (isUnboundedLiquid(provider, pos)) {
			final BlockPos down = pos.down();
			if (provider.getBlockState(down).getMaterial().isSolid())
				return true;
			return !isUnboundedLiquid(provider, down);
		}
		return provider.getBlockState(pos.up()).getBlock() instanceof BlockDynamicLiquid;
	}

	@Override
	public boolean canTrigger(@Nonnull final IBlockAccessEx provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {
		return isValidSpawnBlock(provider, state, pos) && super.canTrigger(provider, state, pos, random);
	}

	@Override
	public void doEffect(@Nonnull final IBlockAccessEx provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {

		final int strength = liquidBlockCount(provider, pos);
		if (strength <= 1)
			return;

		final float height = BlockLiquid.getLiquidHeightPercent(state.getBlock().getMetaFromState(state)) + 0.1F;
		final double y = height + pos.getY();

		final ParticleJet effect = new ParticleWaterSplash(strength, provider.getWorld(), pos, pos.getX() + 0.5D, y,
				pos.getZ() + 0.5D);
		addEffect(effect);
	}
}
