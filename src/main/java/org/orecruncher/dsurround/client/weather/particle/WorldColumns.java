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

package org.orecruncher.dsurround.client.weather.particle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.capabilities.CapabilitySeasonInfo;
import org.orecruncher.dsurround.capabilities.season.ISeasonInfo;
import org.orecruncher.dsurround.capabilities.season.PrecipitationType;
import org.orecruncher.dsurround.client.weather.compat.RandomThings;
import org.orecruncher.dsurround.registry.biome.BiomeInfo;
import org.orecruncher.dsurround.registry.biome.BiomeUtil;
import org.orecruncher.lib.chunk.ClientChunkCache;
import org.orecruncher.lib.chunk.IBlockAccessEx;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The columns of the player's world, for the drop field: where precipitation stops and what falls, as the season
 * info says (so a seasons mod's snow is the drops' snow), and the light, from the client's chunk cache. Client thread
 * only.
 * <p>
 * Dust isn't the drops' here: a dusty column counts as having nothing, and the dust is still drawn as sheets by the
 * storm renderer.
 */
@SideOnly(Side.CLIENT)
public final class WorldColumns implements IColumnSource {

	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	private World world;
	private ISeasonInfo season;

	/**
	 * The world the columns are in; null when there is none
	 */
	public void setWorld(@Nullable final World world) {
		this.world = world;
		this.season = world == null ? null : CapabilitySeasonInfo.getCapability(world);
	}

	@Override
	public int groundHeight(final int x, final int z) {
		return this.season.getPrecipitationHeight(this.pos.setPos(x, 0, z)).getY();
	}

	@Override
	@Nonnull
	public PrecipitationKind precipitation(final int x, final int y, final int z) {
		// Random Things' rain shield keeps the rain off around it
		if (RandomThings.shouldRain(this.world, this.pos.setPos(x, 0, z)))
			return PrecipitationKind.NONE;
		final BiomeInfo biome = BiomeUtil.getBiomeData(ClientChunkCache.instance().getBiome(this.pos));
		final PrecipitationType type = this.season.getPrecipitationType(this.pos.setPos(x, y, z), biome);
		if (type == PrecipitationType.RAIN)
			return PrecipitationKind.RAIN;
		if (type == PrecipitationType.SNOW)
			return PrecipitationKind.SNOW;
		return PrecipitationKind.NONE;
	}

	/**
	 * Whether the storm raises dust at the position, as the storm renderer draws it
	 */
	public boolean isDusty(final int x, final int y, final int z) {
		final BiomeInfo biome = BiomeUtil.getBiomeData(ClientChunkCache.instance().getBiome(this.pos.setPos(x, y, z)));
		return this.season.getPrecipitationType(this.pos, biome) == PrecipitationType.DUST;
	}

	@Override
	public boolean isOpen(final int x, final int y, final int z) {
		final IBlockAccessEx blocks = ClientChunkCache.instance();
		this.pos.setPos(x, y, z);
		final IBlockState state = blocks.getBlockState(this.pos);
		return !state.getMaterial().isLiquid() && state.getCollisionBoundingBox(blocks, this.pos) == null;
	}

	@Override
	public int light(final int x, final int y, final int z) {
		return ClientChunkCache.instance().getCombinedLight(this.pos.setPos(x, y, z), 0);
	}
}
