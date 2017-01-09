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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.fx.BlockEffect;
import org.blockartistry.mod.DynSurround.client.fx.ISpecialEffect;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.registry.BlockRegistry;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.mod.DynSurround.scanner.RandomScanner;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RandomBlockEffectScanner extends RandomScanner {

	// Vanilla had a range of 16 in doVoidParticles() and it iterated 1000
	// times.
	// The new doVoidParticles() in 1.10x is different. Loops less, but has two
	// ranges it works (near and far). Not going to change it as the data in the
	// config files it tuned to this existing behavior.
	private static final float RATIO = 1000.0F / (16.0F * 16.0F * 16.0F);

	protected final BlockRegistry blocks = RegistryManager.get(RegistryType.BLOCK);

	public RandomBlockEffectScanner(final int range) {
		super("RandomBlockEffectScanner", range, (int) (range * range * range * RATIO));
	}

	@Override
	protected boolean interestingBlock(@Nonnull final IBlockState state) {
		return state.getBlock() != Blocks.AIR && this.blocks.hasEffectsOrSounds(state);
	}

	protected List<ISpecialEffect> getEffectsToImplement(@Nonnull final World world, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random rand) {

		final List<ISpecialEffect> results = new ArrayList<ISpecialEffect>();
		final List<BlockEffect> chain = this.blocks.getEffects(state);

		for (final BlockEffect effect : chain)
			if (effect.canTrigger(state, world, pos, rand))
				results.add(effect);

		final SoundEffect sound = this.blocks.getSound(state, rand);
		if (sound != null)
			sound.doEffect(state, world, pos, rand);

		return results;
	}

	@Override
	public void blockScan(@Nonnull final IBlockState state, @Nonnull final BlockPos pos, @Nonnull final Random rand) {

		final World world = EnvironState.getWorld();
		final List<ISpecialEffect> effects = getEffectsToImplement(world, state, pos, rand);
		for (final ISpecialEffect effect : effects)
			effect.doEffect(state, world, pos, rand);

	}

}
