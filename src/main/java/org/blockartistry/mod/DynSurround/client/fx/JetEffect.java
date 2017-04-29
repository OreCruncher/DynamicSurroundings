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

import org.blockartistry.mod.DynSurround.client.fx.particle.system.ParticleJet;
import org.blockartistry.mod.DynSurround.client.handlers.ParticleSystemHandler;
import org.blockartistry.mod.DynSurround.util.WorldUtils;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class JetEffect extends BlockEffect {

	protected static final int MAX_STRENGTH = 10;

	protected static int countBlocks(final World world, final BlockPos pos, final IBlockState state, final int dir) {
		int count = 0;
		int idx = pos.getY();
		while (count < MAX_STRENGTH) {
			if (WorldUtils.getBlockState(world, pos.getX(), idx, pos.getZ()).getBlock() != state.getBlock())
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

	protected void addEffect(final ParticleJet fx) {
		ParticleSystemHandler.INSTANCE.addSystem(fx);
	}

}
