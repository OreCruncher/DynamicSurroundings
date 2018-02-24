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

package org.blockartistry.DynSurround.client.fx;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.fx.particle.system.ParticleJet;
import org.blockartistry.DynSurround.client.handlers.ParticleSystemHandler;
import org.blockartistry.DynSurround.expression.ExpressionEngine;
import org.blockartistry.lib.BlockStateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class JetEffect extends BlockEffect {

	protected static final int MAX_STRENGTH = 10;

	protected static int countBlocks(final BlockStateProvider provider, final BlockPos pos, final IBlockState state,
			final int dir) {
		int count = 0;
		int idx = pos.getY();
		while (count < MAX_STRENGTH) {
			final Block block = provider.getBlockState(pos.getX(), idx, pos.getZ()).getBlock();
			if (block != state.getBlock())
				return count;
			count++;
			idx += dir;
		}
		return count;
	}

	// Takes into account partial blocks because of flow
	protected static double jetSpawnHeight(final IBlockState state, final BlockPos pos) {
		final int meta = state.getBlock().getMetaFromState(state);
		return 1.1D - BlockLiquid.getLiquidHeightPercent(meta) + pos.getY();
	}

	public JetEffect(final int chance) {
		super(chance);
	}

	@Override
	public boolean canTrigger(@Nonnull final BlockStateProvider provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {
		return (alwaysExecute() || random.nextInt(getChance()) == 0) && ParticleSystemHandler.INSTANCE.okToSpawn(pos)
				&& ExpressionEngine.instance().check(getConditions());
	}

	protected void addEffect(final ParticleJet fx) {
		ParticleSystemHandler.INSTANCE.addSystem(fx);
	}

}
