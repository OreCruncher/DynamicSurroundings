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

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

/**
 * Chunk cache implementation that is essentially a pass through to the
 * Minecraft ChunkCache implementation.
 */
public class PassThroughChunkCache implements IChunkCache, IBlockAccessEx {

	protected final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	protected int minCX;
	protected int minCZ;
	protected int maxCX;
	protected int maxCZ;

	protected World world;
	protected int worldRef;
	protected int ref;
	protected ChunkCache cache;

	protected boolean anyEmpty = true;

	public PassThroughChunkCache() {

	}

	@Override
	public void update(@Nonnull final World world, @Nonnull final BlockPos min, @Nonnull final BlockPos max) {
		final ChunkPos from = new ChunkPos(min);
		final ChunkPos to = new ChunkPos(max);

		if (this.anyEmpty || this.world != world || from.chunkXPos < this.minCX || from.chunkZPos < this.minCZ
				|| to.chunkXPos > this.maxCX || to.chunkZPos > this.maxCZ) {

			if (this.world != world)
				this.worldRef++;
			this.ref++;

			this.world = world;
			this.minCX = from.chunkXPos;
			this.minCZ = from.chunkZPos;
			this.maxCX = to.chunkXPos;
			this.maxCZ = to.chunkZPos;

			this.cache = new ChunkCache(world, min, max, 0);

			this.anyEmpty = false;
			for (int cX = this.minCX; cX <= this.maxCX; cX++)
				for (int cZ = this.minCZ; cZ <= this.maxCZ; cZ++)
					if (!this.world.isChunkGeneratedAt(cX, cZ)) {
						this.anyEmpty = true;
						break;
					}
		}
	}

	@Override
	public void clear() {
		this.cache = null;
		this.world = null;
	}

	@Override
	public TileEntity getTileEntity(@Nonnull final BlockPos pos) {
		return this.cache == null ? null : this.cache.getTileEntity(pos);
	}

	@Override
	public int getCombinedLight(@Nonnull final BlockPos pos, final int lightValue) {
		return this.cache == null ? lightValue : this.cache.getCombinedLight(pos, lightValue);
	}

	@Override
	public IBlockState getBlockState(@Nonnull final BlockPos pos) {
		return this.cache == null ? Blocks.AIR.getDefaultState() : this.cache.getBlockState(pos);
	}

	@Override
	public boolean isAirBlock(@Nonnull final BlockPos pos) {
		return this.cache != null && this.cache.isAirBlock(pos);
	}

	@Override
	public Biome getBiome(@Nonnull final BlockPos pos) {
		return this.cache == null ? Biomes.PLAINS : this.cache.getBiome(pos);
	}

	@Override
	public int getStrongPower(@Nonnull final BlockPos pos, @Nonnull final EnumFacing direction) {
		return this.cache == null ? 0 : this.cache.getStrongPower(pos, direction);
	}

	@Override
	public WorldType getWorldType() {
		return this.cache == null ? WorldType.DEFAULT : this.cache.getWorldType();
	}

	@Override
	public boolean isSideSolid(@Nonnull final BlockPos pos, @Nonnull final EnumFacing side, final boolean _default) {
		return this.cache != null && this.cache.isSideSolid(pos, side, _default);
	}

	@Override
	public int reference() {
		return this.ref;
	}

	@Override
	public int worldReference() {
		return this.worldRef;
	}

	@Override
	public World getWorld() {
		return this.world;
	}

	@Override
	public IBlockState getBlockState(final int x, final int y, final int z) {
		return this.cache == null ? Blocks.AIR.getDefaultState() : this.cache.getBlockState(this.pos.setPos(x, y, z));
	}

	@Override
	public int getLightFor(@Nonnull final EnumSkyBlock type, @Nonnull final BlockPos pos) {
		return this.cache == null ? 0 : this.cache.getLightFor(type, pos);
	}

	@Override
	public BlockPos getTopSolidOrLiquidBlock(@Nonnull final BlockPos pos) {
		return this.world == null ? pos : this.world.getTopSolidOrLiquidBlock(pos);
	}

	@Override
	public boolean isAvailable(final int x, final int z) {
		return this.world != null && this.world.isChunkGeneratedAt(x >> 4, z >> 4);
	}

	@Override
	public BlockPos getPrecipitationHeight(@Nonnull final BlockPos pos) {
		return this.world == null ? pos : this.world.getPrecipitationHeight(pos);
	}
}
