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
import java.util.Set;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.fx.particle.system.ParticleJet;
import org.blockartistry.DynSurround.client.fx.particle.system.ParticleSteamJet;
import org.blockartistry.lib.chunk.BlockStateProvider;
import org.blockartistry.lib.collections.IdentityHashSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SteamJetEffect extends JetEffect {

	private static final Set<Block> hotBlocks = new IdentityHashSet<>();

	static {
		hotBlocks.add(Blocks.LAVA);
		hotBlocks.add(Blocks.MAGMA);
	}

	public SteamJetEffect(final int chance) {
		super(chance);
	}

	protected static int lavaCount(final BlockStateProvider provider, final BlockPos pos) {
		int blockCount = 0;
		for (int i = -1; i <= 1; i++)
			for (int j = -1; j <= 1; j++)
				for (int k = -1; k <= 1; k++) {
					final Block theBlock = provider.getBlockState(pos.getX() + i, pos.getY() + j, pos.getZ() + k)
							.getBlock();
					if (hotBlocks.contains(theBlock))
						blockCount++;
				}
		return blockCount;
	}

	@Override
	@Nonnull
	public BlockEffectType getEffectType() {
		return BlockEffectType.STEAM_JET;
	}

	public static boolean isValidSpawnBlock(@Nonnull final BlockStateProvider provider, @Nonnull final BlockPos pos) {
		if (!provider.getBlockState(pos).getMaterial().isLiquid())
			return false;

		if (provider.getBlockState(pos.getX(), pos.getY() + 1, pos.getZ()).getMaterial() != Material.AIR)
			return false;

		return lavaCount(provider, pos) > 0;
	}

	@Override
	public boolean canTrigger(@Nonnull final BlockStateProvider provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {
		return isValidSpawnBlock(provider, pos) && super.canTrigger(provider, state, pos, random);
	}

	@Override
	public void doEffect(@Nonnull final BlockStateProvider provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {
		final int strength = lavaCount(provider, pos);
		final double spawnHeight = jetSpawnHeight(state, pos);
		final ParticleJet effect = new ParticleSteamJet(strength, provider.getWorld(), pos.getX() + 0.5D, spawnHeight,
				pos.getZ() + 0.5D);
		addEffect(effect);
	}
}
