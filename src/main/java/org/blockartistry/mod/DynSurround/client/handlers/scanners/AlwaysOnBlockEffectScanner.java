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

package org.blockartistry.mod.DynSurround.client.handlers.scanners;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.fx.BlockEffect;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.registry.BlockInfo;
import org.blockartistry.mod.DynSurround.registry.BlockRegistry;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.mod.DynSurround.scanner.CuboidScanner;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This guy scans a large area around the player looking for blocks to spawn
 * "always on" effects. Currently there is only one, the water splash for
 * waterfalls.
 * 
 * The CuboidScanner tries to only scan new blocks that come into range as the
 * player moves. Once all the blocks are scanned in the region (cuboid) it will
 * stop. It will start again once the player moves location.
 */
@SideOnly(Side.CLIENT)
public class AlwaysOnBlockEffectScanner extends CuboidScanner {

	protected final BlockRegistry blocks = RegistryManager.get(RegistryType.BLOCK);
	protected final BlockInfo.BlockInfoMutable blockInfo = new BlockInfo.BlockInfoMutable();

	public AlwaysOnBlockEffectScanner(final int range) {
		super("AlwaysOnBlockEffectScanner", range, 0);
	}

	@Override
	protected boolean interestingBlock(final IBlockState state) {
		return state.getBlock() != Blocks.AIR && this.blocks.hasAlwaysOnEffects(this.blockInfo.set(state));
	}

	@Override
	public void blockScan(@Nonnull final IBlockState state, @Nonnull final BlockPos pos, @Nonnull final Random rand) {
		final World world = EnvironState.getWorld();
		final BlockEffect[] effects = this.blocks.getAlwaysOnEffects(state);
		for (final BlockEffect be : effects)
			if (be.canTrigger(state, world, pos, rand))
				be.doEffect(state, world, pos, rand);
	}

}
