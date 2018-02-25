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

package org.blockartistry.lib.scanner;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.lib.random.LCGRandom;

import net.minecraft.util.math.BlockPos;

/**
 * Serves up random blocks in an area around the player. Concentration of block
 * selections are closer to the player.
 */
public abstract class RandomScanner extends Scanner {

	private final LCGRandom lcg = new LCGRandom();

	private int playerX;
	private int playerY;
	private int playerZ;

	public RandomScanner(@Nonnull final ScanLocus locus, @Nonnull final String name, final int range) {
		super(locus, name, range);
	}

	public RandomScanner(@Nonnull final ScanLocus locus, @Nonnull final String name, final int range,
			final int blocksPerTick) {
		super(locus, name, range, blocksPerTick);
	}

	private int randomRange(final int range) {
		return this.lcg.nextInt(range) - this.lcg.nextInt(range);
	}

	@Override
	public void preScan() {
		final BlockPos pos = this.locus.getCenter();
		this.playerX = pos.getX();
		this.playerY = pos.getY();
		this.playerZ = pos.getZ();
	}

	@Override
	@Nonnull
	protected BlockPos nextPos(@Nonnull final BlockPos.MutableBlockPos workingPos, @Nonnull final Random rand) {
		return workingPos.setPos(this.playerX + randomRange(this.xRange), this.playerY + randomRange(this.yRange),
				this.playerZ + randomRange(this.zRange));
	}

}
