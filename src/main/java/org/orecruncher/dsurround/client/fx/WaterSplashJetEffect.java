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
import org.orecruncher.lib.chunk.IBlockAccessEx;
import org.orecruncher.lib.math.MathStuff;

import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
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

	private static boolean isUnboundedLiquid(final IBlockAccessEx provider, final BlockPos pos) {
		final IBlockState AIR = Blocks.AIR.getDefaultState();
		final IBlockState WATER = Blocks.WATER.getDefaultState();
		final BlockPos.MutableBlockPos tp = new BlockPos.MutableBlockPos();
		for (int i = 0; i < cardinal_offsets.length; i++) {
			final Vec3i offset = cardinal_offsets[i];
			tp.setPos(pos.getX() + offset.getX(), pos.getY(), pos.getZ() + offset.getZ());
			final IBlockState state = provider.getBlockState(tp);
			if (state == AIR)
				return true;
			if (state.getBlock() instanceof BlockDynamicLiquid)
				return true;
			final Material material = state.getMaterial();
			if (material.isSolid() || state == WATER)
				continue;
			if (!material.isLiquid())
				return true;
			if (!provider.getBlockState(tp.move(EnumFacing.UP)).getMaterial().isLiquid())
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
		return isValidSpawnBlock(provider.getBlockState(pos), provider, pos);
	}

	public static boolean isValidSpawnBlock(final IBlockState state, final IBlockAccessEx provider,
			final BlockPos pos) {
		// If the current block under examination is not a liquid block
		// there can be no splash
		if (!state.getMaterial().isLiquid())
			return false;
		// The block above this one has to be liquid.  If it's not liquid
		// like being air or solid, the column isn't tall enough to
		// produce a splash.
		final BlockPos up = pos.up();
		final IBlockState upState = provider.getBlockState(up);
		if (!upState.getMaterial().isLiquid())
			return false;
		// Look at the block it is resting on.
		final BlockPos down = pos.down();
		final IBlockState downState = provider.getBlockState(down);
		final Material downMaterial = downState.getMaterial();
		// If it is solid, it means we have a surface strike and need
		// to check the adjacent blocks to see if they are full or not.
		// If not full the block is considered unbounded.
		if (downMaterial.isSolid())
			return isUnboundedLiquid(provider, pos);
		// If the block is not liquid (i.e. gas) then it is
		// not a valid strike block
		if (!downMaterial.isLiquid())
			return false;
		// The water block is resting on top of another water block.
		// This gets a little tricky.  If the current block is on top
		// of another unbounded block we do not want a splash.  This
		// can occur if the water is flowing downward and hasn't
		// reached a terminus.
		if (isUnboundedLiquid(provider, down))
			return false;
		// OK the strike block looks to be bounded on all sides.  All
		// that remains is to ensure that the location block is unbounded.
		return isUnboundedLiquid(provider, pos);
		//return provider.getBlockState(pos.up()).getBlock() instanceof BlockDynamicLiquid;
	}

	@Override
	public boolean canTrigger(@Nonnull final IBlockAccessEx provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {
		return isValidSpawnBlock(state, provider, pos) && super.canTrigger(provider, state, pos, random);
	}

	@Override
	public void doEffect(@Nonnull final IBlockAccessEx provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {
		final int strength = liquidBlockCount(provider, pos);
		if (strength <= 1)
			return;
		final float y = BlockLiquid.getLiquidHeight(state, provider, pos);
		final ParticleJet effect = new ParticleWaterSplash(strength, provider.getWorld(), pos, pos.getX() + 0.5D, y,
				pos.getZ() + 0.5D);
		addEffect(effect);
	}
}
