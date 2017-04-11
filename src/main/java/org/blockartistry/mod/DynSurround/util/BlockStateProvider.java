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

package org.blockartistry.mod.DynSurround.util;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * Simple provider that caches the last chunk referenced in hopes of getting a
 * hit on the next call. Goal is to speed up area scanning by assuming traversal
 * is on the Y axis first.
 */
public class BlockStateProvider {

	protected World world;
	protected int chunkX = -1;;
	protected int chunkZ = -1;
	protected Chunk chunk;

	public BlockStateProvider(final World world) {
		this.world = world;
	}

	@Nonnull
	protected Chunk resolveChunk(final int x, final int z) {
		final int cX = x >> 4;
		final int cZ = z >> 4;

		if (this.chunkX != cX || this.chunkZ != cZ) {
			this.chunkX = cX;
			this.chunkZ = cZ;
			this.chunk = this.world.getChunkFromChunkCoords(this.chunkX, this.chunkZ);
		}

		return this.chunk;
	}

	@Nonnull
	public BlockStateProvider setWorld(@Nonnull final World world) {
		if (this.world != world) {
			this.world = world;
			this.chunk = null;
			this.chunkX = this.chunkZ = -1;
		}
		return this;
	}

	@Nonnull
	public IBlockState getBlockState(@Nonnull final BlockPos pos) {
		return getBlockState(pos.getX(), pos.getY(), pos.getZ());
	}

	@Nonnull
	public IBlockState getBlockState(final int x, final int y, final int z) {
		return resolveChunk(x, z).getBlockState(x, y, z);
	}

}
