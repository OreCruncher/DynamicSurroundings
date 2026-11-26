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

package org.orecruncher.dsurround.client.weather.particle;

/**
 * What precipitation needs to know about a column of the world. Kept apart from the world so the drops can be tested.
 * Ported from the 26.x line's particle weather.
 */
public interface IColumnSource {

	/**
	 * From {@link #dustColor}: the block isn't one dust is blown from
	 */
	int NOT_A_DUST_SOURCE = -1;

	/**
	 * The height precipitation stops at: the top of the highest block that blocks motion
	 */
	int groundHeight(int x, int z);

	/**
	 * What a storm brings to the position: rain, snow, dust where the biome is dry and sandy, or nothing
	 */
	PrecipitationKind precipitation(int x, int y, int z);

	/**
	 * In a rain column, the share of what falls that is snowflakes, 0 to 1: sleet, near the snow line. Asked right
	 * after {@link #precipitation} for the same position, and only where it said rain.
	 */
	default float snowShare(final int x, final int y, final int z) {
		return 0F;
	}

	/**
	 * Whether a drop can be at the position: nothing solid and no fluid there. Drops carried in under an overhang
	 * drift on through open air until they reach something that isn't. By default, open above the column's ground
	 * and solid below it, as if every column were solid all the way up.
	 */
	default boolean isOpen(final int x, final int y, final int z) {
		return y >= groundHeight(x, z);
	}

	/**
	 * Packed sky and block light at the position, as vanilla's renderers use it
	 */
	int light(int x, int y, int z);

	/**
	 * The color of the dust blown up from the block at the position, as 0xRRGGBB, or {@link #NOT_A_DUST_SOURCE} if
	 * dust isn't blown from it
	 */
	default int dustColor(final int x, final int y, final int z) {
		return NOT_A_DUST_SOURCE;
	}

	/**
	 * The color of the dust where none of the ground around gives it one, as 0xRRGGBB
	 */
	default int fallbackDustColor(final int x, final int y, final int z) {
		return 0xDBD3A0;
	}
}
