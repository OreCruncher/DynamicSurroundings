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

package org.blockartistry.mod.DynSurround.scanner;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class Cuboid {

	protected final AxisAlignedBB box;
	protected final List<Point> vertices;
	protected final int volume;
	protected final Point minPoint;
	protected final Point maxPoint;

	public Cuboid(final int centerX, final int centerY, final int centerZ, final int width, final int height,
			final int length) {
		this(new AxisAlignedBB(centerX - width / 2, centerY - height / 2, centerZ - length / 2, centerX + width / 2,
				centerY + height / 2, centerZ + length / 2));
	}

	public Cuboid(@Nonnull final BlockPos vx1, @Nonnull final BlockPos vx2) {
		this(new AxisAlignedBB(vx1, vx2));
	}

	// The AABB is aligned on a block boundary
	protected Cuboid(@Nonnull final AxisAlignedBB box) {
		this.box = box;

		this.minPoint = new Point(this.box.minX, this.box.minY, this.box.minZ);
		this.maxPoint = new Point(this.box.maxX, this.box.maxY, this.box.maxZ);

		final int l = this.maxPoint.getX() - this.minPoint.getX();
		final int h = this.maxPoint.getY() - this.minPoint.getY();
		final int w = this.maxPoint.getZ() - this.minPoint.getZ();

		this.volume = l * h * w;
		
		this.vertices = new ArrayList<Point>();
        this.vertices.add(new Point(this.minPoint.getX(), this.minPoint.getY(), this.minPoint.getZ()));
        this.vertices.add(new Point(this.maxPoint.getX(), this.maxPoint.getY(), this.maxPoint.getZ()));
        this.vertices.add(new Point(this.minPoint.getX(), this.maxPoint.getY(), this.maxPoint.getZ()));
        this.vertices.add(new Point(this.maxPoint.getX(), this.minPoint.getY(), this.minPoint.getZ()));
        this.vertices.add(new Point(this.maxPoint.getX(), this.minPoint.getY(), this.maxPoint.getZ()));
        this.vertices.add(new Point(this.minPoint.getX(), this.minPoint.getY(), this.maxPoint.getZ()));
        this.vertices.add(new Point(this.minPoint.getX(), this.maxPoint.getY(), this.minPoint.getZ()));
        this.vertices.add(new Point(this.maxPoint.getX(), this.maxPoint.getY(), this.minPoint.getZ()));
	}

	public boolean contains(@Nonnull final BlockPos p) {
		// box.isVecInside() is not inclusive of the boundaries.
		// So we roll our own.
		return p.getX() >= this.box.minX && p.getX() <= this.box.maxX ? (p.getY() >= this.box.minY && p.getY() <= this.box.maxY ? p.getZ() >= this.box.minZ && p.getZ() <= this.box.maxZ : false) : false;
	}

	public Cuboid translate(final int dx, final int dy, final int dz) {
		return new Cuboid(this.box.offset(dx, dy, dz));
	}

	@Nonnull
	public Point maximum() {
		return new Point(this.maxPoint);
	}

	@Nonnull
	public Point minimum() {
		return new Point(this.minPoint);
	}

	public long volume() {
		return this.volume;
	}

	@Nullable
	public Cuboid intersection(@Nonnull final Cuboid o) {
		Point vx1 = null;
		for (final Point vx : this.vertices) {
			if (o.contains(vx)) {
				vx1 = new Point(vx);
				break;
			}
		}
		
		if(vx1 == null)
			return null;
		
		Point vx2 = null;
		for (final Point vx : o.vertices) {
			if (this.contains(vx) && vx.canFormCuboid(vx1)) {
				vx2 = new Point(vx);
				break;
			}
		}
		
		return vx2 == null ? null : new Cuboid(vx1, vx2);
	}

	public boolean equals(@Nonnull final Cuboid o) {
		return this.box.equals(o.box);
	}
}