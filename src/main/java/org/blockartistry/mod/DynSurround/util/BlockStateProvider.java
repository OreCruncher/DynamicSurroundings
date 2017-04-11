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
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * Simple provider that caches the last chunk referenced in hopes of getting a
 * hit on the next call. Goal is to speed up area scanning by assuming traversal
 * is on the Y axis first.
 */
public class BlockStateProvider {

	protected World world;
	protected Chunk chunk;

	public BlockStateProvider() {
		this(null);
	}

	public BlockStateProvider(final World world) {
		this.world = world;
	}

	@Nonnull
	protected Chunk resolveChunk(final int x, final int z) {
		final int cX = x >> 4;
		final int cZ = z >> 4;

		if (this.chunk == null || !this.chunk.isAtLocation(cX, cZ)) {
			this.chunk = this.world.getChunkFromChunkCoords(cX, cZ);
		}

		return this.chunk;
	}

	@Nonnull
	public BlockStateProvider setWorld(@Nonnull final World world) {
		if (this.world != world) {
			this.world = world;
			this.chunk = null;
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

	public boolean isAvailable(final int x, final int z) {
		final int cX = x >> 4;
		final int cZ = z >> 4;

		if (this.chunk == null || !this.chunk.isAtLocation(cX, cZ)) {
			this.chunk = this.world.getChunkProvider().getLoadedChunk(cX, cZ);
		}

		return this.chunk != null;
	}

	public boolean isAvailable(@Nonnull final BlockPos pos) {
		return isAvailable(pos.getX(), pos.getZ());
	}

	public int getLightFor(@Nonnull final EnumSkyBlock type, @Nonnull final BlockPos pos) {
		return resolveChunk(pos.getX(), pos.getZ()).getLightFor(type, pos);
	}
}
