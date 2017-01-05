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

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.util.MathStuff;

import net.minecraft.util.math.BlockPos;

public class Point extends BlockPos.MutableBlockPos {

	public Point(@Nonnull final Point p) {
		super(p);
	}

	public Point(final double x, final double y, final double z) {
		super(MathStuff.floor_double(x), MathStuff.floor_double(y), MathStuff.floor_double(z));
	}

	public Point(final int x, final int y, final int z) {
		super(x, y, z);
	}

	public Point translate(int dx, int dy, int dz) {
		this.add(dx, dy, dz);
		return this;
	}

	public void addX(int dx) {
		this.x += dx;
	}

	public void addY(int dy) {
		this.y += dy;
	}

	public void addZ(int dz) {
		this.z += dz;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public boolean canFormCuboid(@Nonnull final Point p) {
		return !(x == p.x || z == p.z || y == p.y);
	}

	public BlockPos asBlockPos() {
		return this.toImmutable();
	}
}