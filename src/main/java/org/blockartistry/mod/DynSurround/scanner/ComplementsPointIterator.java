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

/**
 * For 2 Cuboid objects of equal dimensions displaced in space (c1,c2) that
 * intersect to form the cuboid i there are at most 3 cuboids describing the
 * space in c1 not in i and at most 3 cuboids describing the space in c2 not in
 * i. This class builds those 3 cuboids as "segments" so that the space in c1 or
 * c2 but not in i can be iterated efficiently.
 */
public class ComplementsPointIterator implements IPointIterator {

	protected List<CuboidPointIterator> segments;

	public ComplementsPointIterator(@Nonnull final Cuboid volume, @Nonnull final Cuboid intersect) {
		// This function makes some important assumptions about volume and
		// intersect:
		// 1) Intersect is completely contained within volume
		// 2) Intersect always shares at least 3 faces with volume (shares at
		// least 1 "corner")
		// These conditions are met easily by always taking intersects of two
		// cuboids
		// of the same size ie:
		//
		// Cuboid oldVolume = new Cuboid(0,0,0,64,32,64);
		// Cuboid newVolume = oldVolume.translated(3,10,20);
		// Cuboid intersect = newVolume.intersect(oldVolume);
		// ComplementsPointIterator outOfRange = new
		// ComplementsPointIterator(oldVolume,intersect);
		// ComplementsPointIterator inRange = new
		// ComplementsPointIterator(newVolume,intersect);
		//

		this.segments = new ArrayList<CuboidPointIterator>();

		final Point vmax = volume.maximum();
		final Point imax = intersect.maximum();
		final Point vmin = volume.minimum();
		final Point imin = intersect.minimum();

		if (vmax.getX() != imax.getX() || vmin.getX() != imin.getX()) {
			if (vmax.getX() > imax.getX())
				this.segments.add(new CuboidPointIterator(new Point(imax.getX(), vmin.getY(), vmin.getZ()),
						new Point(vmax.getX(), vmax.getY(), vmax.getZ())));
			else
				this.segments.add(new CuboidPointIterator(new Point(vmin.getX(), vmin.getY(), vmin.getZ()),
						new Point(imin.getX(), vmax.getY(), vmax.getZ())));
		}

		if (vmax.getY() != imax.getY() || vmin.getY() != imin.getY()) {
			if (vmax.getY() > imax.getY())
				this.segments.add(new CuboidPointIterator(new Point(imin.getX(), imax.getY(), vmin.getZ()),
						new Point(imax.getX(), vmax.getY(), vmax.getZ())));
			else
				this.segments.add(new CuboidPointIterator(new Point(imin.getX(), vmin.getY(), vmin.getZ()),
						new Point(imax.getX(), imin.getY(), vmax.getZ())));
		}

		if (vmax.getZ() != imax.getZ() || vmin.getZ() != imin.getZ()) {
			if (vmax.getZ() > imax.getZ())
				this.segments.add(new CuboidPointIterator(new Point(imin.getX(), imin.getY(), imax.getZ()),
						new Point(imax.getX(), imax.getY(), vmax.getZ())));
			else
				this.segments.add(new CuboidPointIterator(new Point(imin.getX(), imin.getY(), vmin.getZ()),
						new Point(imax.getX(), imax.getY(), imin.getZ())));
		}
	}

	@Override
	@Nullable
	public Point peek() {
		for (final CuboidPointIterator i : this.segments) {
			final Point rv = i.peek();
			if (rv != null)
				return rv;
		}
		return null;
	}

	@Override
	@Nullable
	public Point next() {
		for (final CuboidPointIterator i : this.segments) {
			final Point rv = i.next();
			if (rv != null)
				return rv;
		}
		return null;
	}

	@Override
	public void reset() {
		for (CuboidPointIterator i : segments)
			i.reset();
	}
}