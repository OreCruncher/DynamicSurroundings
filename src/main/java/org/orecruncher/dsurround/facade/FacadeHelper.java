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

package org.orecruncher.dsurround.facade;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FacadeHelper {

	private static final Map<Block, FacadeAccessor> crackers = new IdentityHashMap<>();

	private static void addAccessor(@Nonnull final List<FacadeAccessor> accessors,
			@Nonnull final FacadeAccessor accessor) {
		if (accessor.isValid()) {
			ModBase.log().info("Facade Accessor: %s", accessor.getName());
			accessors.add(accessor);
		}
	}

	static {

		final List<FacadeAccessor> accessors = new ArrayList<>();

		// Run down the list of supported accessors. The instance will
		// tell us if it is valid or not.
		addAccessor(accessors, new EnderIOFacadeAccessor());
		addAccessor(accessors, new CoFHCoreCoverAccessor());
		addAccessor(accessors, new ChiselAPIFacadeAccessor());
		addAccessor(accessors, new ChiselFacadeAccessor());

		// Iterate through the block list filling out our cracker list.
		if (accessors.size() > 0) {
			final Iterator<Block> itr = Block.REGISTRY.iterator();
			while (itr.hasNext()) {
				final Block b = itr.next();
				for (int i = 0; i < accessors.size(); i++) {
					final FacadeAccessor accessor = accessors.get(i);
					if (accessor.instanceOf(b)) {
						crackers.put(b, accessor);
						break;
					}
				}
			}
		}

	}

	protected FacadeHelper() {

	}

	@Nonnull
	public static IBlockState resolveState(@Nonnull final IBlockState state, @Nonnull final World world,
			@Nonnull final BlockPos pos, @Nullable final EnumFacing side) {
		if (crackers.size() > 0) {
			final FacadeAccessor accessor = crackers.get(state.getBlock());
			if (accessor != null && accessor.isValid()) {
				final IBlockState newState = accessor.getBlockState(state, world, pos, side);
				if (newState != null)
					return newState;
			}
		}
		return state;
	}

}
