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

import net.minecraft.util.math.BlockPos;

/**
 * The block positions within {@code radius} blocks of a center: those whose squared distance from it, center to
 * center, is at most {@code radius * radius}. Each row of the sphere along X is a single span, which is how the
 * scanner walks it (see {@link #halfWidth}). Ported from the 26.x line.
 */
public final class ScanSphere {

	public final int x;
	public final int y;
	public final int z;
	public final int radius;

	public ScanSphere(final int x, final int y, final int z, final int radius) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.radius = radius;
	}

	@Nonnull
	public static ScanSphere around(@Nonnull final BlockPos center, final int radius) {
		return new ScanSphere(center.getX(), center.getY(), center.getZ(), radius);
	}

	public boolean contains(@Nonnull final BlockPos pos) {
		return contains(pos.getX(), pos.getY(), pos.getZ());
	}

	public boolean contains(final int px, final int py, final int pz) {
		final long dx = px - this.x;
		final long dy = py - this.y;
		final long dz = pz - this.z;
		return dx * dx + dy * dy + dz * dz <= (long) this.radius * this.radius;
	}

	/**
	 * How far the row at {@code (rowY, rowZ)} reaches either side of the center along X: the row holds
	 * {@code x - halfWidth} to {@code x + halfWidth}. -1 if the row misses the sphere.
	 */
	public int halfWidth(final int rowY, final int rowZ) {
		final long dy = rowY - this.y;
		final long dz = rowZ - this.z;
		final long left = (long) this.radius * this.radius - dy * dy - dz * dz;
		// Exact for these magnitudes: a square root is correctly rounded, and no non-square under 2^52 has one
		// close enough to a whole number to round up to it
		return left < 0 ? -1 : (int) Math.sqrt(left);
	}

	/**
	 * True if the two spheres share a position (for the same radius: the centers are at most two radii apart).
	 */
	public boolean overlaps(@Nonnull final ScanSphere other) {
		final long dx = other.x - this.x;
		final long dy = other.y - this.y;
		final long dz = other.z - this.z;
		final long reach = (long) this.radius + other.radius;
		return dx * dx + dy * dy + dz * dz <= reach * reach;
	}

	/**
	 * The number of positions in a sphere of {@code radius}.
	 */
	public static long volumeOf(final int radius) {
		final ScanSphere sphere = new ScanSphere(0, 0, 0, radius);
		long count = 0;
		for (int dy = -radius; dy <= radius; dy++)
			for (int dz = -radius; dz <= radius; dz++) {
				final int w = sphere.halfWidth(dy, dz);
				if (w >= 0)
					count += 2L * w + 1;
			}
		return count;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ScanSphere))
			return false;
		final ScanSphere s = (ScanSphere) o;
		return this.x == s.x && this.y == s.y && this.z == s.z && this.radius == s.radius;
	}

	@Override
	public int hashCode() {
		return ((this.x * 31 + this.y) * 31 + this.z) * 31 + this.radius;
	}

	@Override
	@Nonnull
	public String toString() {
		return "ScanSphere[" + this.x + "," + this.y + "," + this.z + " r" + this.radius + "]";
	}
}
