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

package org.orecruncher.dsurround.client.weather.frost;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * What frost needs to know of the world: in the game, the client's world; in tests, a fake. Ported from the 26.x line.
 */
public interface IFrostWorld {

	@Nonnull
	IBlockAccess blocks();

	/**
	 * Whether a block is glass that frosts
	 */
	boolean frosts(@Nonnull IBlockState state);

	/**
	 * Whether nothing above a position stops rain or snow falling to it: no roof, glass included, and no tree
	 */
	boolean seesSky(@Nonnull BlockPos pos);

	/**
	 * The sky light at a position, 0 to 15
	 */
	int skyLight(@Nonnull BlockPos pos);

	/**
	 * The packed light at a position, as the lightmap takes it
	 */
	int packedLight(@Nonnull BlockPos pos);

	/**
	 * The temperature at a position, as the game's (a snowy plain is 0, a plain 0.8), with the season's
	 */
	float temperature(@Nonnull BlockPos pos);
}
