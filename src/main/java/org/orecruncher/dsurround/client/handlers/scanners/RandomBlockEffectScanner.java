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
import org.orecruncher.dsurround.client.ClientRegistry;
import org.orecruncher.dsurround.client.fx.BlockEffect;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.registry.BlockProfile;
import org.orecruncher.lib.chunk.IBlockAccessEx;
import org.orecruncher.lib.scanner.RandomScanner;
import org.orecruncher.lib.scanner.ScanLocus;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Modeled after WorldClient::doVoidFogParticles() which handles the random
 * block update client side as well as barrier particles.
 *
 * The routine iterates 667 times. During each iteration it creates particles up
 * to a max of 16 blocks, and then up to a max of 32 blocks. There is some
 * overlap with the 16 block range when generating the 32 block version, but
 * since the iteration has been reduce to 667 (from 1000 in MC 1.7.10) it should
 * compensate.
 */
@SideOnly(Side.CLIENT)
public class RandomBlockEffectScanner extends RandomScanner {

	private static final int ITERATION_COUNT = 667;

	public static final int NEAR_RANGE = 16;
	public static final int FAR_RANGE = 32;

	protected BlockProfile profile = null;
	protected IBlockState lastState = null;

	public RandomBlockEffectScanner(@Nonnull final ScanLocus locus, final int range) {
		super(locus, "RandomBlockScanner: " + range, range, ITERATION_COUNT);
		setLogger(ModBase.log());
	}

	@Override
	protected boolean interestingBlock(@Nonnull final IBlockState state) {
		if (state == Blocks.AIR.getDefaultState())
			return false;
		if (this.lastState != state) {
			this.lastState = state;
			this.profile = ClientRegistry.BLOCK.findProfile(state);
		}
		return this.profile.hasSoundsOrEffects();
	}

	@Override
	public void blockScan(@Nonnull final IBlockState state, @Nonnull final BlockPos pos, @Nonnull final Random rand) {

		final IBlockAccessEx provider = this.locus.getWorld();
		final BlockEffect[] effects = this.profile.getEffects();
		for (int i = 0; i < effects.length; i++) {
			final BlockEffect be = effects[i];
			if (be.canTrigger(provider, state, pos, rand))
				be.doEffect(provider, state, pos, rand);
		}

		final SoundEffect sound = this.profile.getSoundToPlay(rand);
		if (sound != null)
			sound.doEffect(provider, state, pos, rand);
	}

}
