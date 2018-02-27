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

import java.lang.ref.WeakReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.client.weather.Weather;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Simple provider that caches the last chunk referenced in hopes of getting a
 * hit on the next call. Goal is to speed up area scanning by assuming traversal
 * is on the Y axis first.
 */
@SideOnly(Side.CLIENT)
public class BlockStateProvider implements IBlockAccessEx {

	public static final IBlockState AIR_STATE = Blocks.AIR.getDefaultState();

	protected static final WeakReference<Chunk> NULL_CHUNK = new WeakReference<>(NullChunk.NULL_CHUNK);

	protected final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
	protected WeakReference<World> world;
	protected WeakReference<Chunk> chunk = NULL_CHUNK;
	protected int worldRef;

	public BlockStateProvider() {
		this(null);
	}

	public BlockStateProvider(final World world) {
		this.world = new WeakReference<>(world);
	}

	@Nonnull
	protected Chunk resolveChunk(final int x, final int z) {
		Chunk c = null;
		final World w = getWorld();
		if (w != null) {
			final int cX = x >> 4;
			final int cZ = z >> 4;
			c = this.chunk.get();
			if (c == null || !c.isAtLocation(cX, cZ)) {
				this.chunk = new WeakReference<>(c = w.getChunkFromChunkCoords(cX, cZ));
			}
		} else {
			this.chunk = NULL_CHUNK;
			c = NullChunk.NULL_CHUNK;
		}
		return c;
	}

	@Nonnull
	protected Chunk resolveChunk(@Nonnull final BlockPos pos) {
		return resolveChunk(pos.getX(), pos.getZ());
	}

	@Nonnull
	public BlockStateProvider setWorld(@Nonnull final World world) {
		if (getWorld() != world) {
			this.worldRef++;
			this.world = new WeakReference<>(world);
			this.chunk = NULL_CHUNK;
		}
		return this;
	}

	@Override
	public int reference() {
		return 0;
	}
	
	@Override
	public int worldReference() {
		return this.worldRef;
	}
	
	@Override
	@Nullable
	public World getWorld() {
		return this.world.get();
	}

	@Override
	@Nonnull
	public IBlockState getBlockState(@Nonnull final BlockPos pos) {
		return getBlockState(pos.getX(), pos.getY(), pos.getZ());
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
	public IBlockState getBlockState(final int x, final int y, final int z) {
		return (y >= 0 && y < 256) ? getBlockState0(resolveChunk(x, z), x, y, z) : AIR_STATE;
	}

	public boolean isAvailable(final int x, final int z) {
		return !resolveChunk(x, z).isEmpty();
	}

	public boolean isAvailable(@Nonnull final BlockPos pos) {
		return isAvailable(pos.getX(), pos.getZ());
	}

	@Override
	public int getLightFor(@Nonnull final EnumSkyBlock type, @Nonnull final BlockPos pos) {
		return resolveChunk(pos.getX(), pos.getZ()).getLightFor(type, pos);
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
				final IBlockState state = getBlockState0(chunk, x, dY, z);
				final Material material = state.getMaterial();
				if (material.blocksMovement() && material != Material.LEAVES
						&& !state.getBlock().isFoliage(world, this.mutable.setPos(x, dY, z)))
					return this.mutable.toImmutable();
			}
		}
		return pos;
	}

	@Override
	@Nonnull
	public Biome getBiome(@Nonnull final BlockPos pos) {
		try {
			return resolveChunk(pos).getBiome(pos, getWorld().provider.biomeProvider);
		} catch (final Throwable t) {
			;
		}
		// Foobar
		return Biomes.PLAINS;
	}

	@Override
	@Nonnull
    public BlockPos getPrecipitationHeight(@Nonnull final BlockPos pos) {
		return resolveChunk(pos).getPrecipitationHeight(pos);
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return resolveChunk(pos).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
	}

	@Override
	public int getCombinedLight(BlockPos pos, int lightValue) {
		final int i = getLightForExt(EnumSkyBlock.SKY, pos);
		int j = getLightForExt(EnumSkyBlock.BLOCK, pos);

		if (j < lightValue) {
			j = lightValue;
		}

		return i << 20 | j << 4;
	}

	@SideOnly(Side.CLIENT)
	private int getLightForExt(@Nonnull final EnumSkyBlock type, @Nonnull final BlockPos pos) {
		if (type == EnumSkyBlock.SKY && getWorld().provider.getHasNoSky()) {
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
		return state.getBlock().isAir(state, getWorld(), pos);
	}

	@Override
	public int getStrongPower(@Nonnull final BlockPos pos, @Nonnull final EnumFacing direction) {
		final IBlockState state = getBlockState(pos);
		return state.getStrongPower(getWorld(), pos, direction);
	}

	@Override
	public WorldType getWorldType() {
		return getWorld().getWorldType();
	}

	@Override
	public boolean isSideSolid(@Nonnull final BlockPos pos, @Nonnull final EnumFacing side, final boolean _default) {
		if (pos.getY() < 0 || pos.getY() >= 256)
			return _default;

		final IBlockState state = getBlockState(pos);
		return state.isSideSolid(getWorld(), pos, side);
	}

	@Override
	public boolean isRainingAt(@Nonnull final BlockPos pos) {
		if (!Weather.isRaining()) {
			return false;
		} else if (!canSeeSky(pos)) {
			return false;
		} else if (getPrecipitationHeight(pos).getY() > pos.getY()) {
			return false;
		} else {
			final Biome biome = getBiome(pos);
			if (biome.getEnableSnow()) {
				return false;
			} else {
				return canSnowAt(pos, false) ? false : biome.canRain();
			}
		}
	}

	@Override
	public boolean canSeeSky(@Nonnull final BlockPos pos) {
		return resolveChunk(pos).canSeeSky(pos);
	}

	@Override
	public boolean canSnowAt(@Nonnull final BlockPos pos, boolean checkLight) {
		final World w = getWorld();
		return w == null ? false : w.provider.canSnowAt(pos, checkLight);
	}

	@Override
	public boolean canBlockFreeze(BlockPos pos, boolean noWaterAdjacent) {
		final World w = getWorld();
		return w == null ? false : w.provider.canBlockFreeze(pos, noWaterAdjacent);
	}
}
