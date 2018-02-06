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

package org.blockartistry.DynSurround.facade;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.lib.compat.ModEnvironment;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FacadeHelper {

	public static interface IFacadeAccessor {
		IBlockState getBlockState(@Nonnull final IBlockState state, @Nonnull final World world,
				@Nonnull final BlockPos pos, @Nullable final EnumFacing side);
	}

	private static final List<IFacadeAccessor> accessors = new ArrayList<IFacadeAccessor>();
	static {

		if (ModEnvironment.ChiselAPI.isLoaded())
			accessors.add(new ChiselAPIFacadeAccessor());
		else if (ModEnvironment.Chisel.isLoaded())
			accessors.add(new ChiselFacadeAccessor());

		if (ModEnvironment.EnderIO.isLoaded())
			accessors.add(new EnderIOFacadeAccessor());

		if (ModEnvironment.CoFHCore.isLoaded())
			accessors.add(new CoFHCoreCoverAccessor());
	}

	protected FacadeHelper() {

	}

	@Nonnull
	public static IBlockState resolveState(@Nonnull final IBlockState state, @Nonnull final World world,
			@Nonnull final BlockPos pos, @Nullable final EnumFacing side) {
		for (int i = 0; i < accessors.size(); i++) {
			final IBlockState newState = accessors.get(i).getBlockState(state, world, pos, side);
			if (newState != null)
				return newState;
		}

		return state;
	}

}
