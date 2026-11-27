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

package org.orecruncher.dsurround.client.weather;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

/**
 * Which blocks are windows: glass blocks and panes, stained ones included, for frost and rain on the glass. 1.12.2 has
 * no block tags, so the 26.x line's {@code c:glass_blocks} and {@code c:glass_panes} are found by class: most mods'
 * glass extends the game's. Glowstone and sea lanterns, glass by material, are not windows; nor are iron bars, a pane of
 * iron.
 */
public final class Windows {

	private Windows() {
	}

	public static boolean isWindow(@Nonnull final IBlockState state) {
		return isWindow(state.getBlock(), state.getMaterial());
	}

	static boolean isWindow(@Nonnull final Block block, @Nonnull final Material material) {
		if (block instanceof BlockGlass || block instanceof BlockStainedGlass || block instanceof BlockStainedGlassPane)
			return true;
		return block instanceof BlockPane && material == Material.GLASS;
	}
}
