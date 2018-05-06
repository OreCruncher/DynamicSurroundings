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
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

/**
 * Chunk cache implementation that performs no caching and passes the call directly
 * to the World object.
 */
public class DirectChunkCache implements IChunkCache, IBlockAccessEx {

	protected final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	protected World world;
	protected int worldRef;

	public DirectChunkCache() {

	}

	@Override
	public void update(@Nonnull final World world, @Nonnull final BlockPos min, @Nonnull final BlockPos max) {
		if (this.world != world) {
			this.worldRef++;
			this.world = world;
		}
	}

	@Override
	public void clear() {
		this.world = null;
	}

	@Override
	public TileEntity getTileEntity(@Nonnull final BlockPos pos) {
		return this.world == null ? null : this.world.getTileEntity(pos);
	}

	@Override
	public int getCombinedLight(@Nonnull final BlockPos pos, final int lightValue) {
		return this.world == null ? lightValue : this.world.getCombinedLight(pos, lightValue);
	}

	@Override
	public IBlockState getBlockState(@Nonnull final BlockPos pos) {
		return this.world == null ? Blocks.AIR.getDefaultState() : this.world.getBlockState(pos);
	}

	@Override
	public boolean isAirBlock(@Nonnull final BlockPos pos) {
		return this.world != null && this.world.isAirBlock(pos);
	}

	@Override
	public Biome getBiome(@Nonnull final BlockPos pos) {
		if (this.world != null)
			try {
				return this.world.getBiome(pos);
			} catch (@Nonnull final Throwable t) {

			}
		return Biomes.PLAINS;
	}

	@Override
	public int getStrongPower(@Nonnull final BlockPos pos, @Nonnull final EnumFacing direction) {
		return this.world == null ? 0 : this.world.getStrongPower(pos, direction);
	}

	@Override
	public WorldType getWorldType() {
		return this.world == null ? WorldType.DEFAULT : this.world.getWorldType();
	}

	@Override
	public boolean isSideSolid(@Nonnull final BlockPos pos, @Nonnull final EnumFacing side, final boolean _default) {
		return this.world != null && this.world.isSideSolid(pos, side, _default);
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
	public World getWorld() {
		return this.world;
	}

	@Override
	public IBlockState getBlockState(final int x, final int y, final int z) {
		return this.world == null ? Blocks.AIR.getDefaultState() : this.world.getBlockState(this.pos.setPos(x, y, z));
	}

	@Override
	public int getLightFor(@Nonnull final EnumSkyBlock type, @Nonnull final BlockPos pos) {
		return this.world == null ? 0 : this.world.getLightFor(type, pos);
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
