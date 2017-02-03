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

package org.blockartistry.mod.DynSurround.facade;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.facade.FacadeHelper.IFacadeAccessor;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

final class EnderIOFacadeAccessor implements IFacadeAccessor {

	private static final String CLASS = "crazypants.enderio.paint.IPaintable";
	private static final String METHOD = "getPaintSource";

	private static Class<?> IFacadeClass;
	private static Method method;

	static {
		try {
			IFacadeClass = Class.forName(CLASS);
			if (IFacadeClass != null) {
				method = IFacadeClass.getMethod(METHOD, IBlockState.class, IBlockAccess.class, BlockPos.class);
			}
		} catch (@Nonnull final Throwable t) {
			ModLog.warn("Unable to locate %s.%s()", CLASS, METHOD);
			ModLog.catching(t);
		}
	}

	@Override
	@Nullable
	public IBlockState getBlockState(@Nonnull final IBlockState state, @Nonnull final World world,
			@Nonnull final BlockPos pos, @Nullable final EnumFacing side) {

		if (IFacadeClass == null || method == null)
			return null;

		final Block block = state.getBlock();

		try {
			if (IFacadeClass.isInstance(block))
				return (IBlockState) method.invoke(block, state, world, pos);
		} catch (@Nonnull final Exception ex) {
			ModLog.warn("Unable to invoke %s.%s()", CLASS, METHOD);
			ModLog.catching(ex);
			method = null;
		}
		return null;
	}

}
