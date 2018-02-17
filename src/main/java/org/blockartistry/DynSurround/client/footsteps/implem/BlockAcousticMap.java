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

package org.blockartistry.DynSurround.client.footsteps.implem;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.DynSurround.registry.BlockInfo;
import org.blockartistry.DynSurround.registry.BlockInfo.BlockInfoMutable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockAcousticMap {

	public static final IAcoustic[] NO_ACOUSTICS = {};

	/**
	 * A callback interface for when the acoustic map cannot resolve a blockState
	 * sound and needs external assistance to back fill. The results are added to
	 * the cache for future lookups.
	 */
	public static interface IAcousticResolver {
		IAcoustic[] resolve(@Nonnull final IBlockState state);
	}

	private final IAcousticResolver resolver;
	private final BlockInfoMutable key = new BlockInfoMutable();
	private Map<BlockInfo, IAcoustic[]> data = new HashMap<BlockInfo, IAcoustic[]>();
	private Map<IBlockState, IAcoustic[]> cache = new IdentityHashMap<IBlockState, IAcoustic[]>();
	private Map<IBlockState, IAcoustic[]> specialCache = new IdentityHashMap<IBlockState, IAcoustic[]>();

	public BlockAcousticMap() {
		this(null);
	}

	public BlockAcousticMap(@Nullable final IAcousticResolver resolver) {
		this.resolver = resolver;
		
		// Air is a very known quantity
		this.data.put(new BlockInfo(Blocks.AIR), AcousticsManager.NOT_EMITTER);
	}

	/**
	 * Obtain acoustic information for a block. If the block has variants (subtypes)
	 * it will fall back to searching for a generic if a specific one is not found.
	 */
	@Nullable
	public IAcoustic[] getBlockAcoustics(@Nonnull final IBlockState state) {
		IAcoustic[] result = this.cache.get(state);
		if (result == null) {
			result = this.data.get(this.key.set(state));
			if (result == null && this.key.hasSubTypes()) {
				result = this.data.get(this.key.asGeneric());
			}
			if (result == null && this.resolver != null)
				result = this.resolver.resolve(state);
			if (result == null)
				result = NO_ACOUSTICS;
			this.cache.put(state, result);
		}
		return result == NO_ACOUSTICS ? null : result;
	}

	/**
	 * Similar to getBlockMap(), but includes an additional check if the block has
	 * special metadata. For example, a BlockCrop may not have subtypes, but it
	 * would have growth data stored in the meta. An example of this is Wheat.
	 */
	@Nullable
	public IAcoustic[] getBlockAcousticsWithSpecial(@Nonnull final IBlockState state) {
		IAcoustic[] result = this.specialCache.get(state);
		if (result == null) {
			result = this.data.get(this.key.set(state));
			if (result == null) {
				if (this.key.hasSubTypes()) {
					result = this.data.get(this.key.asGeneric());
				} else if (this.key.hasSpecialMeta()) {
					result = this.data.get(this.key.asSpecial());
				}
			}
			if (result == null)
				result = NO_ACOUSTICS;
			this.specialCache.put(state, result);
		}
		return result == NO_ACOUSTICS ? null : result;
	}

	public void put(@Nonnull final BlockInfo info, final IAcoustic[] acoustics) {
		this.data.put(info, acoustics);
	}

	public void clear() {
		this.data = new HashMap<BlockInfo, IAcoustic[]>(this.data.size());
		this.cache = new IdentityHashMap<IBlockState, IAcoustic[]>(this.cache.size());
		this.specialCache = new IdentityHashMap<IBlockState, IAcoustic[]>(this.specialCache.size());
	}

	public void freeze() {
		this.data = new ImmutableMap.Builder<BlockInfo, IAcoustic[]>().putAll(this.data).build();
	}
}
