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

package org.blockartistry.DynSurround.client.handlers.scanners;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.fx.BlockEffect;
import org.blockartistry.DynSurround.registry.BlockProfile;
import org.blockartistry.lib.scanner.CuboidScanner;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This guy scans a large area around the player looking for blocks to spawn
 * "always on" effects such as waterfall splash and steam jets.
 * 
 * The CuboidScanner tries to only scan new blocks that come into range as the
 * player moves. Once all the blocks are scanned in the region (cuboid) it will
 * stop. It will start again once the player moves location.
 */
@SideOnly(Side.CLIENT)
public class AlwaysOnBlockEffectScanner extends CuboidScanner {

	protected BlockProfile profile = null;
	protected IBlockState lastState = null;

	public AlwaysOnBlockEffectScanner(final int range) {
		super(ClientPlayerLocus.INSTANCE, "AlwaysOnBlockEffectScanner", range, 0);
		this.setLogger(DSurround.log());
	}

	@Override
	protected boolean interestingBlock(final IBlockState state) {
		if (state == AIR_BLOCK)
			return false;
		if (this.lastState != state) {
			this.lastState = state;
			this.profile = ClientRegistry.BLOCK.findProfile(state);
		}
		return this.profile.hasAlwaysOnEffects();
	}

	@Override
	public void blockScan(@Nonnull final IBlockState state, @Nonnull final BlockPos pos, @Nonnull final Random rand) {
		final BlockEffect[] effects = this.profile.getAlwaysOnEffects();
		for (int i = 0; i < effects.length; i++) {
			final BlockEffect be = effects[i];
			if (be.canTrigger(this.blockProvider, state, pos, rand))
				be.doEffect(this.blockProvider, state, pos, rand);
		}
	}

}
