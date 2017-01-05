/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.blockartistry.mod.DynSurround.scanner;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;

import net.minecraft.util.math.BlockPos;

/**
 * Serves up random blocks in an area around the player. Concentration of block
 * selections are closer to the player.
 */
public abstract class RandomScanner extends Scanner {
	
	protected final Random rand = RANDOM.get();

	public RandomScanner(@Nonnull final String name, final int range) {
		super(name, range);
	}

	public RandomScanner(@Nonnull final String name, final int range, final int blocksPerTick) {
		super(name, range, blocksPerTick);
	}

	private int randomRange(final int range) {
		return this.rand.nextInt(range) - this.rand.nextInt(range);
	}

	@Override
	protected BlockPos nextPos() {
		return EnvironState.getPlayerPosition().add(randomRange(this.xRange), randomRange(this.yRange),
				randomRange(this.zRange));
	}

}
