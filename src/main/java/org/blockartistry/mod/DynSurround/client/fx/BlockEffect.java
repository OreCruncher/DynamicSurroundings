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

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockEffect {

	private int chance;

	public BlockEffect() {
		this(100);
	}

	public BlockEffect(final int chance) {
		this.chance = chance;
	}

	public void setChance(final int chance) {
		this.chance = chance;
	}

	public int getChance() {
		return this.chance;
	}

	public boolean trigger(final IBlockState state, final World world, final BlockPos pos, final Random random) {
		return random.nextInt(getChance()) == 0;
	}

	public abstract void doEffect(final IBlockState state, final World world, final BlockPos pos, final Random random);

	public void process(final IBlockState state, final World world, final BlockPos pos, final Random random) {
		if (trigger(state, world, pos, random))
			doEffect(state, world, pos, random);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("chance:").append(this.chance);
		builder.append(' ').append(this.getClass().getSimpleName());
		return builder.toString();
	}
}