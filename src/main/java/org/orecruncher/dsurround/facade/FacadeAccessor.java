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

import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

class FacadeAccessor {

	protected Class<?> IFacadeClass;
	protected Method accessor;

	public FacadeAccessor(@Nonnull final String clazz, @Nonnull final String method) {
		try {
			this.IFacadeClass = Class.forName(clazz);
			if (this.IFacadeClass != null)
				this.accessor = getMethod(method);
			else
				this.accessor = null;
		} catch (@Nonnull final Throwable t) {
			this.IFacadeClass = null;
			this.accessor = null;
		}
	}

	public String getName() {
		return isValid() ? this.IFacadeClass.getName() : "INVALID";
	}

	public boolean instanceOf(@Nonnull final Block block) {
		return isValid() && this.IFacadeClass.isInstance(block);
	}

	protected Method getMethod(@Nonnull final String method) throws Throwable {
		return this.IFacadeClass.getMethod(method, IBlockAccess.class, BlockPos.class, EnumFacing.class);
	}

	protected IBlockState call(@Nonnull final IBlockState state, @Nonnull final World world,
			@Nonnull final BlockPos pos, @Nullable final EnumFacing side) throws Throwable {
		return (IBlockState) this.accessor.invoke(state.getBlock(), world, pos, side);
	}

	public boolean isValid() {
		return this.accessor != null;
	}

	@Nullable
	public IBlockState getBlockState(@Nonnull final IBlockState state, @Nonnull final World world,
			@Nonnull final BlockPos pos, @Nullable final EnumFacing side) {
		if (isValid())
			try {
				if (instanceOf(state.getBlock()))
					return call(state, world, pos, side);
			} catch (@Nonnull final Throwable ex) {
				ModBase.log().catching(ex);
				this.IFacadeClass = null;
				this.accessor = null;
			}

		return null;
	}

}
