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

import net.minecraft.util.EnumFacing;

/**
 * A side of a window that frosts: the outside of a glass block or pane, open in front. Ported from the 26.x line.
 * Where it is, in world coordinates: the plane of the glass, lifted a little off it, and its extent across ({@code u}:
 * x on a north or south side, z on a west or east one) and up ({@code v}: y). The window's edges are found too, as far
 * as {@link FrostFaces#EDGE_REACH} beyond it: frost grows in from where the glass meets its frame, so a face in the
 * middle of a large window is bare when its edges are frosted. Across and the plane are held as doubles: they are x or
 * z, which far from the world's middle a float can't hold finely enough.
 */
public final class FrostFace {

	final int x, y, z;
	final EnumFacing side;
	final double plane;
	final double u0, u1;
	final float v0, v1;
	// Where the window this face is part of ends, across and up; at most EDGE_REACH beyond the face
	final double edgeU0, edgeU1;
	final float edgeV0, edgeV1;
	// Its packed light, and the sky light in front of it as a share of full, and the temperature there
	final int light;
	final float skyShare;
	final float temperature;
	// How much frost it should have, and how much it has, 0 to 1
	float target;
	float shown;

	FrostFace(final int x, final int y, final int z, @Nonnull final EnumFacing side, final double plane,
			final double u0, final double u1, final float v0, final float v1, final double edgeU0, final double edgeU1,
			final float edgeV0, final float edgeV1, final int light, final float skyShare, final float temperature) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.side = side;
		this.plane = plane;
		this.u0 = u0;
		this.u1 = u1;
		this.v0 = v0;
		this.v1 = v1;
		this.edgeU0 = edgeU0;
		this.edgeU1 = edgeU1;
		this.edgeV0 = edgeV0;
		this.edgeV1 = edgeV1;
		this.light = light;
		this.skyShare = skyShare;
		this.temperature = temperature;
	}

	/**
	 * Which face this is, to carry its frost over when its section is looked at again: its block and side
	 */
	long key() {
		final long block = ((long) (this.x & 0x3FFFFFF) << 38) | ((long) (this.y & 0xFFF) << 26) | (this.z & 0x3FFFFFF);
		return block * 31L + this.side.getHorizontalIndex();
	}

	public float shown() {
		return this.shown;
	}

	/**
	 * Gives it this much frost at once, 0 to 1, without growing or melting into it
	 */
	public void show(final float frost) {
		this.target = frost;
		this.shown = frost;
	}
}
