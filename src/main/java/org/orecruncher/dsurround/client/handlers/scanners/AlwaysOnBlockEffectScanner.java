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

package org.orecruncher.dsurround.client.handlers.scanners;

import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.client.fx.BlockEffect;
import org.orecruncher.dsurround.lib.scanner.ScanLocus;
import org.orecruncher.dsurround.lib.scanner.SphereScanner;
import org.orecruncher.dsurround.registry.blockstate.BlockStateUtil;
import org.orecruncher.lib.chunk.IBlockAccessEx;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This guy scans a large area around the player looking for blocks to spawn
 * "always on" effects such as waterfall splash and steam jets.
 *
 * The SphereScanner only scans blocks as they come into range: as the player
 * moves, as chunks load, and as blocks change.
 */
@SideOnly(Side.CLIENT)
public class AlwaysOnBlockEffectScanner extends SphereScanner {

	public AlwaysOnBlockEffectScanner(@Nonnull final ScanLocus locus, final int range) {
		super(locus, "AlwaysOnBlockEffectScanner", range);
		setLogger(ModBase.log());
	}

	@Override
	protected boolean interestingBlock(final IBlockState state) {
		return BlockStateUtil.getStateData(state).hasAlwaysOnEffects();
	}

	@Override
	public void blockScan(@Nonnull final IBlockState state, @Nonnull final BlockPos pos, @Nonnull final Random rand) {
		final IBlockAccessEx provider = this.locus.getWorld();
		final BlockEffect[] effects = BlockStateUtil.getStateData(state).getAlwaysOnEffects();
		for (final BlockEffect be : effects) {
			if (be.canTrigger(provider, state, pos, rand))
				be.doEffect(provider, state, pos, rand);
		}
	}

}
