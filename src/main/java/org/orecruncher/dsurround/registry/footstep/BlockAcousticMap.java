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

package org.orecruncher.dsurround.registry.footstep;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.registry.acoustics.AcousticRegistry;
import org.orecruncher.dsurround.registry.acoustics.IAcoustic;
import org.orecruncher.dsurround.registry.blockstate.BlockStateMatcher;
import org.orecruncher.lib.collections.ObjectArray;

import com.google.common.base.MoreObjects;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class BlockAcousticMap {

	/**
	 * A callback interface for when the acoustic map cannot resolve a blockState
	 * sound and needs external assistance to back fill. The results are added to
	 * the cache for future lookups.
	 */
	public static interface IAcousticResolver {
		@Nullable
		IAcoustic[] resolve(@Nonnull final IBlockState state);
	}

	protected final Map<IBlockState, IAcoustic[]> cache = new Reference2ObjectOpenHashMap<>();

	protected final IAcousticResolver resolver;
	protected final Map<Block, ObjectArray<BlockMapEntry>> data = new Reference2ObjectOpenHashMap<>();

	/**
	 * CTOR for building a map that has no resolver and performs special lookups
	 * based on block properties (i.e. used for substrate maps).
	 */
	public BlockAcousticMap() {
		this((state) -> AcousticRegistry.EMPTY);
	}

	public BlockAcousticMap(@Nonnull final IAcousticResolver resolver) {
		this.resolver = resolver;
		put(BlockStateMatcher.AIR, AcousticRegistry.NOT_EMITTER);
	}

	@Nonnull
	protected IAcoustic[] cacheMiss(@Nonnull final IBlockState state) {
		IAcoustic[] result = null;
		final ObjectArray<BlockMapEntry> entries = this.data.get(state.getBlock());
		if (entries != null) {
			final BlockStateMatcher matcher = BlockStateMatcher.create(state);
			result = find(entries, matcher);
			if (result != null)
				return result;
			if (matcher.hasSubtypes())
				result = find(entries, BlockStateMatcher.asGeneric(state));
			if (result != null)
				return result;
		}
		if (this.resolver != null)
			result = this.resolver.resolve(state);
		return MoreObjects.firstNonNull(result, AcousticRegistry.EMPTY);
	}

	@Nullable
	private IAcoustic[] find(@Nonnull final ObjectArray<BlockMapEntry> entries,
			@Nonnull final BlockStateMatcher matcher) {
		// Search backwards.  In general highly specified states are at
		// the end of the array.
		for (int i = entries.size() - 1; i >= 0; i--) {
			final BlockMapEntry e = entries.get(i);
			if (matcher.equals(e.matcher))
				return e.acoustics;
		}
		return null;
	}

	/**
	 * Obtain acoustic information for a block. If the block has variants (subtypes)
	 * it will fall back to searching for a generic if a specific one is not found.
	 */
	@Nonnull
	public IAcoustic[] getBlockAcoustics(@Nonnull final IBlockState state) {
		IAcoustic[] result = this.cache.get(state);
		if (result == null) {
			result = cacheMiss(state);
			this.cache.put(state, result);
		}
		return result;
	}

	public void put(@Nonnull final BlockStateMatcher info, @Nonnull final IAcoustic[] acoustics) {
		ObjectArray<BlockMapEntry> entry = this.data.get(info.getBlock());
		if (entry == null) {
			this.data.put(info.getBlock(), entry = new ObjectArray<>(2));
		}
		entry.add(new BlockMapEntry(info, acoustics));
	}

	public void clear() {
		this.data.clear();
		this.cache.clear();
	}

	private static class BlockMapEntry {
		public final BlockStateMatcher matcher;
		public final IAcoustic[] acoustics;

		public BlockMapEntry(@Nonnull final BlockStateMatcher matcher, @Nonnull final IAcoustic[] acoustics) {
			this.matcher = matcher;
			this.acoustics = acoustics;
		}
	}
}