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

package org.orecruncher.dsurround.registry.blockstate;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;

import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * Helper class used to access and manipulate the reference to our data we have
 * referenced in the IBlockState implementation class.  Goal is to avoid all
 * the dictionary lookups and things.
 */
public final class BlockStateUtil {

	// This field was added by the core mod for our use
	private static Field blockStateInfo = ReflectionHelper.findField(BlockStateBase.class, "dsurround_blockstate_info");

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends BlockStateData> T getStateData(@Nonnull final IBlockState state) {
		try {
			return (T) blockStateInfo.get(state);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			ModBase.log().error("Unable to get hold of private field on BlockStateBase!", e);
		}
		return null;
	}

	public static <T extends BlockStateData> void setStateData(@Nonnull final IBlockState state, @Nonnull final T data) {
		try {
			blockStateInfo.set(state, data);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			ModBase.log().error("Unable to set private field on BlockStateBase!", e);
		}
	}

}
