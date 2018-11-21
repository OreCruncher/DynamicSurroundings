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

package org.orecruncher.dsurround.client.footsteps;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.registry.acoustics.AcousticRegistry;
import org.orecruncher.dsurround.registry.acoustics.IAcoustic;
import org.orecruncher.dsurround.registry.blockstate.BlockStateMatcher;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
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
		IAcoustic[] resolve(@Nonnull final IBlockState state);
	}

	protected final IAcousticResolver resolver;
	protected Map<BlockStateMatcher, IAcoustic[]> data = new Object2ObjectOpenHashMap<>();
	protected Map<IBlockState, IAcoustic[]> cache = new Reference2ObjectOpenHashMap<>();

	/**
	 * CTOR for building a map that has no resolver and performs special lookups
	 * based on block properties (i.e. used for substrate maps).
	 */
	public BlockAcousticMap() {
		this(null);
	}

	public BlockAcousticMap(@Nonnull final IAcousticResolver resolver) {
		this.resolver = resolver;

		// Air is a very known quantity
		this.data.put(BlockStateMatcher.AIR, AcousticRegistry.NOT_EMITTER);
	}

	@Nonnull
	protected IAcoustic[] cacheMiss(@Nonnull final IBlockState state) {
		final BlockStateMatcher matcher = BlockStateMatcher.create(state);
		IAcoustic[] result = this.data.get(matcher);
		if (result != null)
			return result;
		if (matcher.hasSubtypes())
			result = this.data.get(BlockStateMatcher.asGeneric(state));
		if (result != null)
			return result;
		if (this.resolver != null)
			result = this.resolver.resolve(state);
		return result != null ? result : AcousticRegistry.EMPTY;
	}

	/**
	 * Obtain acoustic information for a block. If the block has variants (subtypes)
	 * it will fall back to searching for a generic if a specific one is not found.
	 */
	@Nullable
	public IAcoustic[] getBlockAcoustics(@Nonnull final IBlockState state) {
		IAcoustic[] result = this.cache.get(state);
		if (result == null) {
			result = cacheMiss(state);
			this.cache.put(state, result);
		}
		return result;
	}

	public void put(@Nonnull final BlockStateMatcher info, @Nonnull final IAcoustic[] acoustics) {
		this.data.put(info, acoustics);
	}

	public void clear() {
		this.data = new Object2ObjectOpenHashMap<>(this.data.size());
		this.cache = new Reference2ObjectOpenHashMap<>(this.cache.size());
	}
}
