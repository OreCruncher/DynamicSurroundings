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
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleSteamJet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SteamJetEffect extends JetEffect {

	public SteamJetEffect(final int chance) {
		super(chance);
	}

	protected int lavaCount(final World world, final BlockPos pos) {
		int blockCount = 0;
		for (int i = -1; i <= 1; i++)
			for (int j = -1; j <= 1; j++)
				for (int k = -1; k <= 1; k++) {
					if (world.getBlockState(pos.add(i, j, k)).getBlock() == Blocks.LAVA)
						blockCount++;
				}
		return blockCount;
	}

	@Override
	@Nonnull
	public BlockEffectType getEffectType() {
		return BlockEffectType.STEAM_JET;
	}

	@Override
	public boolean trigger(final IBlockState state, final World world, final BlockPos pos, final Random random) {
		if (!super.trigger(state, world, pos, random) || !world.isAirBlock(pos.up()))
			return false;

		return lavaCount(world, pos) != 0;
	}

	@Override
	public void doEffect(final IBlockState state, final World world, final BlockPos pos, final Random random) {
		final int strength = lavaCount(world, pos);
		final double spawnHeight = jetSpawnHeight(state, pos);
		final ParticleJet effect = new ParticleSteamJet(strength, world, pos.getX() + 0.5D, spawnHeight,
				pos.getZ() + 0.5D);
		addEffect(effect);
	}
}
