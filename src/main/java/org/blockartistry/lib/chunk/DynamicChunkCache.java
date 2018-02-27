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
package org.blockartistry.lib.chunk;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Based on ChunkCache but allows for altering the range dynamically without
 * reallocation. Also provides additional extension methods for use.
 *
 * NOTE: Logic should not hold onto a DynamicChunkCache reference beyond the
 * scope of a session as it holds concrete references to World and Chunk!
 */
public class DynamicChunkCache implements IBlockAccessEx {

	public static final IBlockState AIR_STATE = Blocks.AIR.getDefaultState();

	protected final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

	protected int minCX;
	protected int minCZ;
	protected int maxCX;
	protected int maxCZ;

	protected int sizeX;
	protected int sizeZ;

	protected int ref;
	protected int worldRef;
	protected Chunk[] chunkArray;
	protected World world;
	protected boolean anyEmpty = true;

	public DynamicChunkCache() {

	}

	public DynamicChunkCache(@Nonnull final World world, @Nonnull final BlockPos min, @Nonnull final BlockPos max) {
		update(world, min, max);
	}

	public void clear() {
		this.world = null;
		this.chunkArray = null;
	}

	public void update(@Nonnull final World world, @Nonnull final BlockPos min, @Nonnull final BlockPos max) {

		final ChunkPos from = new ChunkPos(min);
		final ChunkPos to = new ChunkPos(max);

		if (this.world != world || from.chunkXPos < this.minCX || from.chunkZPos < this.minCZ
				|| to.chunkXPos > this.maxCX || to.chunkZPos > this.maxCZ) {
			this.ref++;
			if (this.world != world)
				this.worldRef++;

			this.world = world;
			this.minCX = from.chunkXPos;
			this.minCZ = from.chunkZPos;
			this.maxCX = to.chunkXPos;
			this.maxCZ = to.chunkZPos;
			this.sizeX = this.maxCX - this.minCX + 1;
			this.sizeZ = this.maxCZ - this.minCZ + 1;
			this.chunkArray = new Chunk[this.sizeX * this.sizeZ];
			Arrays.fill(this.chunkArray, NullChunk.NULL_CHUNK);
			this.anyEmpty = true;
		}

		if (this.anyEmpty) {
			this.anyEmpty = false;
			for (int k = this.minCX; k <= this.maxCX; ++k) {
				for (int l = this.minCZ; l <= this.maxCZ; ++l) {
					final int idx = (k - this.minCX) * this.sizeZ + (l - this.minCZ);
					if (this.chunkArray[idx].isEmpty()) {
						Chunk c = world.getChunkFromChunkCoords(k, l);
						if (c == null)
							c = NullChunk.NULL_CHUNK;
						this.chunkArray[idx] = c;
						this.anyEmpty |= c.isEmpty();
					}
				}
			}

		}
	}

	@Nonnull
	protected final Chunk resolveChunk(final int x, final int z) {
		if (this.world != null) {
			final int i = (x >> 4) - this.minCX;
			final int j = (z >> 4) - this.minCZ;
			if (withinBounds(i, j))
				return this.chunkArray[i * this.sizeZ + j];
		}
		return NullChunk.NULL_CHUNK;
	}

	@Nonnull
	protected final Chunk resolveChunk(@Nonnull final BlockPos pos) {
		return resolveChunk(pos.getX(), pos.getZ());
	}

	@Override
	@Nullable
	public TileEntity getTileEntity(@Nonnull final BlockPos pos) {
		return getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
	}

	@Nullable
	public TileEntity getTileEntity(@Nonnull final BlockPos pos, @Nonnull final Chunk.EnumCreateEntityType type) {
		return resolveChunk(pos).getTileEntity(pos, type);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getCombinedLight(@Nonnull final BlockPos pos, final int lightValue) {
		final int i = getLightForExt(EnumSkyBlock.SKY, pos);
		int j = getLightForExt(EnumSkyBlock.BLOCK, pos);

		if (j < lightValue) {
			j = lightValue;
		}

		return i << 20 | j << 4;
	}

	@Nonnull
	private IBlockState getBlockState0(final Chunk chunk, final int x, final int y, final int z) {
		final ExtendedBlockStorage[] storageArrays = chunk.getBlockStorageArray();
		final int idx = y >> 4;

		if (idx < storageArrays.length) {

			final ExtendedBlockStorage extendedblockstorage = storageArrays[idx];

			if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE) {
				return extendedblockstorage.get(x & 15, y & 15, z & 15);
			}
		}
		return AIR_STATE;
	}

	@Override
	@Nonnull
	public IBlockState getBlockState(@Nonnull final BlockPos pos) {
		if (pos.getY() >= 0 && pos.getY() < 256) {
			return getBlockState0(resolveChunk(pos), pos.getX(), pos.getY(), pos.getZ());
		}

		return AIR_STATE;
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nonnull
	public Biome getBiome(@Nonnull final BlockPos pos) {
		return resolveChunk(pos).getBiome(pos, this.world.getBiomeProvider());
	}

	@SideOnly(Side.CLIENT)
	private int getLightForExt(@Nonnull final EnumSkyBlock type, @Nonnull final BlockPos pos) {
		if (type == EnumSkyBlock.SKY && !this.world.provider.hasSkyLight()) {
			return 0;
		} else if (pos.getY() >= 0 && pos.getY() < 256) {
			if (getBlockState(pos).useNeighborBrightness()) {
				int l = 0;

				for (final EnumFacing enumfacing : EnumFacing.values()) {
					final int k = getLightFor(type, pos.offset(enumfacing));

					if (k > l) {
						l = k;
					}

					if (l >= 15) {
						return l;
					}
				}

				return l;
			} else {
				return resolveChunk(pos).getLightFor(type, pos);
			}
		} else {
			return type.defaultLightValue;
		}
	}

	@Override
	public boolean isAirBlock(@Nonnull final BlockPos pos) {
		final IBlockState state = getBlockState(pos);
		return state.getBlock().isAir(state, this, pos);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getLightFor(@Nonnull final EnumSkyBlock type, @Nonnull final BlockPos pos) {
		if (pos.getY() >= 0 && pos.getY() < 256) {
			return resolveChunk(pos).getLightFor(type, pos);
		}
		return type.defaultLightValue;
	}

	@Override
	public int getStrongPower(@Nonnull final BlockPos pos, @Nonnull final EnumFacing direction) {
		return getBlockState(pos).getStrongPower(this, pos, direction);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public WorldType getWorldType() {
		return this.world.getWorldType();
	}

	@Override
	public boolean isSideSolid(@Nonnull final BlockPos pos, @Nonnull final EnumFacing side, final boolean _default) {
		if (pos.getY() >= 0 && pos.getY() < 256) {
			final IBlockState state = getBlockState(pos);
			return state.isSideSolid(this.world, pos, side);
		}
		return _default;
	}

	@Override
	@Nullable
	public World getWorld() {
		return this.world;
	}

	@Override
	@Nonnull
	public IBlockState getBlockState(int x, int y, int z) {
		return getBlockState0(resolveChunk(x, z), x, y, z);
	}

	@Override
	@Nonnull
	public BlockPos getTopSolidOrLiquidBlock(@Nonnull final BlockPos pos) {
		final World world = getWorld();
		if (world != null) {
			final int x = pos.getX();
			final int z = pos.getZ();
			final Chunk chunk = resolveChunk(x, z);

			for (int dY = chunk.getTopFilledSegment() + 16 - 1; dY >= 0; dY--) {
				final IBlockState state = chunk.getBlockState(x, dY, z);
				final Material material = state.getMaterial();
				if (material.blocksMovement() && material != Material.LEAVES
						&& !state.getBlock().isFoliage(world, this.mutable.setPos(x, dY, z)))
					return this.mutable.toImmutable();
			}
		}
		return pos;
	}

	@Override
	public boolean isAvailable(final int x, final int z) {
		return !resolveChunk(x, z).isEmpty();
	}

	@Override
	public int reference() {
		return this.ref;
	}

	@Override
	public int worldReference() {
		return this.worldRef;
	}

	private final boolean withinBounds(final int x, final int z) {
		return x >= 0 && x < this.sizeX && z >= 0 && z < this.sizeZ;
	}

}