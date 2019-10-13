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

import org.orecruncher.dsurround.client.fx.particle.system.ParticleFireJet;
import org.orecruncher.dsurround.client.fx.particle.system.ParticleJet;
import org.orecruncher.lib.chunk.IBlockAccessEx;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FireJetEffect extends JetEffect {

	public FireJetEffect(final int chance) {
		super(chance);
	}

	@Override
	@Nonnull
	public BlockEffectType getEffectType() {
		return BlockEffectType.FIRE_JET;
	}

	@Override
	public boolean canTrigger(@Nonnull final IBlockAccessEx provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {
		if (state.getMaterial().isSolid() || state.getMaterial().isLiquid())
			return provider.isAirBlock(pos.up()) && super.canTrigger(provider, state, pos, random);
		return false;
	}

	@Override
	public void doEffect(@Nonnull final IBlockAccessEx provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {
		
		final Material blockMaterial = state.getMaterial();
		final int blockCount;
		final float spawnHeight;
		
		if (blockMaterial.isSolid()) {
			blockCount = 2;
			spawnHeight = pos.getY() + 1.1F;
		} else if (blockMaterial.isLiquid()) {
			blockCount = countBlocks(provider, pos, s -> s.getMaterial() == blockMaterial, -1);
			spawnHeight = BlockLiquid.getLiquidHeight(state, provider, pos);
		} else {
			// Fail safe - shouldn't get here
			return;
		}
		
		if (blockCount > 0) {
			final ParticleJet effect = new ParticleFireJet(blockCount, provider.getWorld(), pos.getX() + 0.5D, spawnHeight,
					pos.getZ() + 0.5D);
			addEffect(effect);
		}
	}
}
