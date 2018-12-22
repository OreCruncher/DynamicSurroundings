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

package org.orecruncher.dsurround.client.fx;

import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.client.fx.particle.system.ParticleJet;
import org.orecruncher.dsurround.client.handlers.EffectManager;
import org.orecruncher.dsurround.client.handlers.ParticleSystemHandler;
import org.orecruncher.dsurround.expression.ExpressionEngine;
import org.orecruncher.lib.chunk.IBlockAccessEx;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class JetEffect extends BlockEffect {

	protected static final int MAX_STRENGTH = 10;

	protected static int countBlocks(final IBlockAccessEx provider, final BlockPos pos,
			final Predicate<IBlockState> pred, final int step) {
		int count = 0;
		int idx = pos.getY();
		for (; count < MAX_STRENGTH
				&& pred.test(provider.getBlockState(pos.getX(), idx, pos.getZ())); count++, idx += step)
			;
		return count;
	}

	public JetEffect(final int chance) {
		super(chance);
	}

	@Override
	public boolean canTrigger(@Nonnull final IBlockAccessEx provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {
		if (alwaysExecute() || random.nextInt(getChance()) == 0) {
			final ParticleSystemHandler ps = EffectManager.instance().lookupService(ParticleSystemHandler.class);
			return ps.okToSpawn(pos) && ExpressionEngine.instance().check(getConditions());
		}
		return false;
	}

	protected void addEffect(final ParticleJet fx) {
		final ParticleSystemHandler ps = EffectManager.instance().lookupService(ParticleSystemHandler.class);
		ps.addSystem(fx);
	}

}
