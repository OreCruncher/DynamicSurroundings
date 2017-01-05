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
import javax.annotation.Nullable;

public class CuboidPointIterator extends Cuboid implements IPointIterator {
	
	protected Point current;

	public CuboidPointIterator(@Nonnull final Cuboid other) {
		super(other.minimum(), other.maximum());
		reset();
	}

	public CuboidPointIterator(@Nonnull final Point p1, @Nonnull final Point p2) {
		super(p1, p2);
		reset();
	}

	public CuboidPointIterator(int centerX, int centerY, int centerZ, int sizeX, int sizeY, int sizeZ) {
		super(centerX, centerY, centerZ, sizeX, sizeY, sizeZ);
		reset();
	}

	@Override
	@Nullable
	public Point next() {
		if (this.current.getX() >= this.maxPoint.getX())
			return null;

		final Point location = new Point(this.current);

		this.current.addZ(1);
		if (this.current.getZ() >= this.maxPoint.getZ()) {
			this.current.setZ(this.minPoint.getZ());
			this.current.addY(1);
		}
		if (this.current.getY() >= this.maxPoint.getY()) {
			this.current.setY(this.minPoint.getY());
			this.current.addX(1);
		}
		return location;
	}

	@Override
	@Nullable
	public Point peek() {
		if (this.current.getX() >= this.maxPoint.getX())
			return null;
		return new Point(this.current);
	}

	@Override
	@Nullable
	public void reset() {
		this.current = new Point(this.minPoint);
	}
}
