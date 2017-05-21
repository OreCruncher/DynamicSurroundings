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

package org.blockartistry.lib;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Works like ThreadLocal, but is based on the Minecraft side. Note that the
 * resulting value is Side safe, not thread safe. If multiple threads on the
 * same side are accessing the SideLocal it needs to be guarded.
 */
public abstract class SideLocal<T> {

	private final Object[] sideData = new Object[Side.values().length];

	protected abstract T initialValue(@Nonnull final Side side);

	public final T get() {
		return this.get(FMLCommonHandler.instance().getEffectiveSide());
	}

	@SuppressWarnings("unchecked")
	public final T get(@Nonnull final Side side) {
		final int idx = side.ordinal();
		Object result = this.sideData[idx];
		if (result == null)
			result = this.sideData[idx] = initialValue(side);

		return (T) result;
	}
	
	public final void clear() {
		this.clear(FMLCommonHandler.instance().getEffectiveSide());
	}
	
	public final void clear(@Nonnull final Side side) {
		this.sideData[side.ordinal()] = null;
	}
	
	public final boolean hasValue() {
		return this.hasValue(FMLCommonHandler.instance().getEffectiveSide());
	}
	
	public final boolean hasValue(@Nonnull final Side side) {
		return this.sideData[side.ordinal()] != null;
	}

}
