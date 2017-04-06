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

package org.blockartistry.mod.DynSurround.client.footsteps.implem;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.mod.DynSurround.registry.BlockInfo;
import org.blockartistry.mod.DynSurround.registry.BlockInfo.BlockInfoMutable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockAcousticMap {

	private final BlockInfoMutable key = new BlockInfoMutable();
	private Map<BlockInfo, IAcoustic[]> data = new HashMap<BlockInfo, IAcoustic[]>();

	/**
	 * Obtain acoustic information for a block.  If the block has
	 * variants (subtypes) it will fall back to searching for a
	 * generic if a specific one is not found.
	 */
	@Nullable
	public IAcoustic[] getBlockAcoustics(@Nonnull final IBlockState state) {
		this.key.set(state);
		IAcoustic[] result = this.data.get(this.key);
		if (result == null && this.key.hasSubTypes()) {
			result = this.data.get(this.key.asGeneric());
		}
		return result;
	}

	/**
	 * Similar to getBlockMap(), but includes an additional check if
	 * the block has special metadata.  For example, a BlockCrop may
	 * not have subtypes, but it would have growth data stored in
	 * the meta.  An example of this is Wheat.
	 */
	@Nullable
	public IAcoustic[] getBlockAcousticsWithSpecial(@Nonnull final IBlockState state) {
		this.key.set(state);
		IAcoustic[] result = this.data.get(this.key);
		if (result == null) {
			if (this.key.hasSubTypes()) {
				result = this.data.get(this.key.asGeneric());
			} else if (this.key.hasSpecialMeta()) {
				result = this.data.get(this.key.asSpecial());
			}
		}
		return result;
	}
	
	public void put(@Nonnull final BlockInfo info, final IAcoustic[] acoustics) {
		this.data.put(info, acoustics);
	}
	
	public void clear() {
		this.data = new HashMap<BlockInfo, IAcoustic[]>(); 
	}
	
	public void freeze() {
		this.data = new ImmutableMap.Builder<BlockInfo, IAcoustic[]>().putAll(this.data).build();
	}
}
