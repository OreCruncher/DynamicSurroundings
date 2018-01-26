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
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.registry.BlockProfile;
import org.blockartistry.lib.scanner.RandomScanner;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RandomBlockEffectScanner extends RandomScanner {

	// Vanilla had a range of 16 in doVoidParticles() and it iterated 1000
	// times.
	//
	// The new doVoidParticles() in 1.10x is different. Loops less, but has two
	// ranges it works (near and far). Not going to change it as the data in the
	// config files it tuned to this existing behavior.
	private static final float RATIO = 1000.0F / (16.0F * 16.0F * 16.0F);

	protected BlockProfile profile = null;
	protected IBlockState lastState = null;

	public RandomBlockEffectScanner(final int range) {
		super(ClientPlayerLocus.INSTANCE, "RandomBlockEffectScanner", range, (int) (range * range * range * RATIO));
		this.setLogger(DSurround.log());
	}

	@Override
	protected boolean interestingBlock(@Nonnull final IBlockState state) {
		if (state == AIR_BLOCK)
			return false;
		if (this.lastState != state) {
			this.lastState = state;
			this.profile = ClientRegistry.BLOCK.findProfile(state);
		}
		return this.profile.hasSoundsOrEffects();
	}

	@Override
	public void blockScan(@Nonnull final IBlockState state, @Nonnull final BlockPos pos, @Nonnull final Random rand) {

		final BlockEffect[] effects = this.profile.getEffects();
		for (int i = 0; i < effects.length; i++) {
			final BlockEffect be = effects[i];
			if (be.canTrigger(this.blockProvider, state, pos, rand))
				be.doEffect(this.blockProvider, state, pos, rand);
		}

		final SoundEffect sound = this.profile.getSoundToPlay(rand);
		if (sound != null)
			sound.doEffect(this.blockProvider, state, pos, rand);
	}

}
