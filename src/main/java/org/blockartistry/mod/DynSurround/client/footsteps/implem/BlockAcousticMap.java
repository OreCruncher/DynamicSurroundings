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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.mod.DynSurround.registry.BlockInfo;
import org.blockartistry.mod.DynSurround.registry.BlockInfo.BlockInfoMutable;

import gnu.trove.map.hash.THashMap;
import net.minecraft.block.state.IBlockState;

public class BlockAcousticMap extends THashMap<BlockInfo, IAcoustic[]> {

	private final BlockInfoMutable key = new BlockInfoMutable();

	@Nullable
	public IAcoustic[] getBlockMap(@Nonnull final IBlockState state) {
		this.key.set(state);
		IAcoustic[] result = this.get(this.key);
		if (result == null && this.key.hasSubTypes()) {
			result = this.get(this.key.asGeneric());
		}
		return result;
	}

	@Nullable
	public IAcoustic[] getBlockMapWithSpecial(@Nonnull final IBlockState state) {
		this.key.set(state);
		IAcoustic[] result = this.get(this.key);
		if (result == null) {
			if (this.key.hasSubTypes()) {
				result = this.get(this.key.asGeneric());
			} else if (this.key.hasSpecialMeta()) {
				result = this.get(this.key.asSpecial());
			}
		}
		return result;
	}
}
