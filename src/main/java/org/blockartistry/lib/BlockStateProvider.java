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

package org.blockartistry.lib;

import java.lang.ref.WeakReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/**
 * Simple provider that caches the last chunk referenced in hopes of getting a
 * hit on the next call. Goal is to speed up area scanning by assuming traversal
 * is on the Y axis first.
 */
public class BlockStateProvider {

	protected static final IBlockState AIR_STATE = Blocks.AIR.getDefaultState();
	protected static final WeakReference<Chunk> NULL_CHUNK = new WeakReference<Chunk>(null);

	protected final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
	protected WeakReference<World> world;
	protected WeakReference<Chunk> chunk;

	public BlockStateProvider() {
		this(null);
	}

	public BlockStateProvider(final World world) {
		this.world = new WeakReference<World>(world);
		this.chunk = NULL_CHUNK;
	}

	@Nonnull
	protected Chunk resolveChunk(final int x, final int z) {
		final int cX = x >> 4;
		final int cZ = z >> 4;

		Chunk c = null;
		if ((c = this.chunk.get()) == null || !c.isAtLocation(cX, cZ)) {
			final World w = this.world.get();
			if (w == null) {
				this.chunk = NULL_CHUNK;
				c = null;
			} else {
				this.chunk = new WeakReference<Chunk>(c = w.getChunkFromChunkCoords(cX, cZ));
			}
		}

		return c;
	}

	@Nonnull
	public BlockStateProvider setWorld(@Nonnull final World world) {
		if (this.world.get() != world) {
			this.world = new WeakReference<World>(world);
			this.chunk = NULL_CHUNK;
		}
		return this;
	}

	@Nullable
	public World getWorld() {
		return this.world.get();
	}

	@Nonnull
	public IBlockState getBlockState(@Nonnull final BlockPos pos) {
		return getBlockState(pos.getX(), pos.getY(), pos.getZ());
	}

	@Nonnull
	private IBlockState getBlockState0(final Chunk chunk, final int x, final int y, final int z) {
		if (chunk != null) {
			final ExtendedBlockStorage[] storageArrays = chunk.getBlockStorageArray();
			final int idx = y >> 4;

			if (idx < storageArrays.length) {

				final ExtendedBlockStorage extendedblockstorage = storageArrays[idx];

				if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE) {
					return extendedblockstorage.get(x & 15, y & 15, z & 15);
				}
			}
		}
		return AIR_STATE;
	}

	@Nonnull
	public IBlockState getBlockState(final int x, final int y, final int z) {
		return (y >= 0 && y < 256) ? getBlockState0(resolveChunk(x, z), x, y, z) : AIR_STATE;
	}

	public boolean isAvailable(final int x, final int z) {
		final int cX = x >> 4;
		final int cZ = z >> 4;

		final World w = this.world.get();
		if (w == null)
			return false;

		Chunk c;
		if ((c = this.chunk.get()) == null || !c.isAtLocation(cX, cZ)) {
			c = w.getChunkProvider().getLoadedChunk(cX, cZ);
			if (c == null) {
				this.chunk = NULL_CHUNK;
			} else {
				this.chunk = new WeakReference<Chunk>(c);
			}
		}

		return c != null;
	}

	public boolean isAvailable(@Nonnull final BlockPos pos) {
		return isAvailable(pos.getX(), pos.getZ());
	}

	public int getLightFor(@Nonnull final EnumSkyBlock type, @Nonnull final BlockPos pos) {
		final Chunk chunk = resolveChunk(pos.getX(), pos.getZ());
		return chunk != null ? chunk.getLightFor(type, pos) : type.defaultLightValue;
	}

	public BlockPos getTopSolidOrLiquidBlock(@Nonnull final BlockPos pos) {
		final int x = pos.getX();
		final int z = pos.getZ();
		final Chunk chunk = resolveChunk(x, z);

		if (chunk == null)
			return pos;

		final World world = this.getWorld();

		for (int dY = chunk.getTopFilledSegment() + 16 - 1; dY >= 0; dY--) {
			final IBlockState state = getBlockState0(chunk, x, dY, z);
			final Material material = state.getMaterial();
			if (material.blocksMovement() && material != Material.LEAVES
					&& !state.getBlock().isFoliage(world, this.mutable.setPos(x, dY, z)))
				return this.mutable.toImmutable();

		}
		return pos;
	}

	public Biome getBiome(@Nonnull final BlockPos pos) {
		final int x = pos.getX();
		final int z = pos.getZ();
		final Chunk chunk = resolveChunk(x, z);

		if (chunk != null) {
			try {
				final World world = this.getWorld();
				return chunk.getBiome(pos, world.provider.biomeProvider);
			} catch (@Nonnull final Throwable t) {
				;
			}
		}

		// Foobar
		return Biomes.PLAINS;
	}
}
