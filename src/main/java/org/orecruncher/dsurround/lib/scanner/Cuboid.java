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

package org.orecruncher.dsurround.lib.scanner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.lib.BlockPosHelper;

import net.minecraft.util.math.BlockPos;

public class Cuboid {

	protected final BlockPos[] vertices = new BlockPos[8];
	protected final int volume;
	protected final BlockPos minPoint;
	protected final BlockPos maxPoint;

	public Cuboid(@Nonnull final BlockPos[] points) {
		this(points[0], points[1]);
	}

	public Cuboid(@Nonnull final BlockPos vx1, @Nonnull final BlockPos vx2) {

		this.minPoint = BlockPosHelper.createMinPoint(vx1, vx2);
		this.maxPoint = BlockPosHelper.createMaxPoint(vx1, vx2);

		final BlockPos t = this.maxPoint.subtract(this.minPoint);
		this.volume = t.getX() * t.getY() * t.getZ();

		this.vertices[0] = this.minPoint;
		this.vertices[1] = this.maxPoint;
		this.vertices[2] = new BlockPos(this.minPoint.getX(), this.maxPoint.getY(), this.maxPoint.getZ());
		this.vertices[3] = new BlockPos(this.maxPoint.getX(), this.minPoint.getY(), this.minPoint.getZ());
		this.vertices[4] = new BlockPos(this.maxPoint.getX(), this.minPoint.getY(), this.maxPoint.getZ());
		this.vertices[5] = new BlockPos(this.minPoint.getX(), this.minPoint.getY(), this.maxPoint.getZ());
		this.vertices[6] = new BlockPos(this.minPoint.getX(), this.maxPoint.getY(), this.minPoint.getZ());
		this.vertices[7] = new BlockPos(this.maxPoint.getX(), this.maxPoint.getY(), this.minPoint.getZ());
	}

	public boolean contains(@Nonnull final BlockPos p) {
		return BlockPosHelper.contains(p, this.minPoint, this.maxPoint);
	}

	@Nonnull
	public BlockPos maximum() {
		return this.maxPoint;
	}

	@Nonnull
	public BlockPos minimum() {
		return this.minPoint;
	}

	public long volume() {
		return this.volume;
	}

	@Nullable
	public Cuboid intersection(@Nonnull final Cuboid o) {
		BlockPos vx1 = null;
		for (final BlockPos vx : this.vertices) {
			if (o.contains(vx)) {
				vx1 = vx;
				break;
			}
		}

		if (vx1 == null)
			return null;

		BlockPos vx2 = null;
		for (final BlockPos vx : o.vertices) {
			if (contains(vx) && BlockPosHelper.canFormCuboid(vx, vx1)) {
				vx2 = vx;
				break;
			}
		}

		return vx2 == null ? null : new Cuboid(vx1, vx2);
	}

}