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

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;

/**
 * A queued piece of scanner work: the positions inside one sphere and not inside another (or all of the first),
 * within the world's height and an optional column, whose blocks are to be scanned (they came into range).
 * <p>
 * Every row of a sphere along X is one span, so a row of the region is at most two spans: the inner sphere's minus
 * the outer's. That keeps a move cheap however the player moves; a step forward touches only a thin shell.
 * <p>
 * The work is grouped by chunk section so the scanner can skip a whole section at once when it is empty or not
 * loaded. Sections are visited nearest the inner sphere's center first, so what is close shows up first. Within a
 * section, X changes fastest, then Z, then Y. A job can be paused partway and resumed on a later tick. Ported from
 * the 26.x line, without unscans (nothing here uses them).
 */
final class ScanJob {

	private static final int NO_LIMIT_LOW = Integer.MIN_VALUE;
	private static final int NO_LIMIT_HIGH = Integer.MAX_VALUE;

	private final ScanSphere inside;
	@Nullable
	private final ScanSphere outside;
	// The region's bounds: the inner sphere's, trimmed to the world's height and the column
	private final int minX, minY, minZ, maxX, maxY, maxZ;

	// The sections holding some of the region, in visiting order, and how many of its blocks each holds
	private final long[] sections;
	private final long[] counts;
	private int index = 0;
	private long remaining;

	// Walking the current section
	private final int[] spans = new int[4];
	private int spanCount;
	private int spanIndex;
	private int x, y, z;
	private boolean rowStarted;

	// Section coordinates packed into a long as later versions' SectionPos does: 22 bits X, 20 bits Y, 22 bits Z
	static long section(final int sx, final int sy, final int sz) {
		return (((long) sx & 0x3FFFFFL) << 42) | ((long) sy & 0xFFFFFL) | (((long) sz & 0x3FFFFFL) << 20);
	}

	static int sectionX(final long packed) {
		return (int) (packed >> 42);
	}

	static int sectionY(final long packed) {
		return (int) (packed << 44 >> 44);
	}

	static int sectionZ(final long packed) {
		return (int) (packed << 22 >> 42);
	}

	private static int toSection(final int block) {
		return block >> 4;
	}

	private static int sectionStart(final int section) {
		return section << 4;
	}

	private static int sectionEnd(final int section) {
		return (section << 4) + 15;
	}

	private ScanJob(@Nonnull final ScanSphere inside, @Nullable final ScanSphere outside, final int worldMinY,
			final int worldMaxY, final int columnMinX, final int columnMaxX, final int columnMinZ,
			final int columnMaxZ) {
		this.inside = inside;
		this.outside = outside;

		final int r = inside.radius;
		this.minX = Math.max(inside.x - r, columnMinX);
		this.maxX = Math.min(inside.x + r, columnMaxX);
		this.minY = Math.max(inside.y - r, worldMinY);
		this.maxY = Math.min(inside.y + r, worldMaxY);
		this.minZ = Math.max(inside.z - r, columnMinZ);
		this.maxZ = Math.min(inside.z + r, columnMaxZ);

		if (this.minX > this.maxX || this.minY > this.maxY || this.minZ > this.maxZ) {
			this.sections = new long[0];
			this.counts = new long[0];
			return;
		}

		// Count the region's blocks in each section it touches, a row at a time
		final int sx0 = toSection(this.minX);
		final int sy0 = toSection(this.minY);
		final int sz0 = toSection(this.minZ);
		final int nx = toSection(this.maxX) - sx0 + 1;
		final int ny = toSection(this.maxY) - sy0 + 1;
		final int nz = toSection(this.maxZ) - sz0 + 1;
		final long[] grid = new long[nx * ny * nz];
		final int[] rowSpans = new int[4];
		for (int ry = this.minY; ry <= this.maxY; ry++) {
			final int gy = (toSection(ry) - sy0) * nz;
			for (int rz = this.minZ; rz <= this.maxZ; rz++) {
				final int gyz = (gy + toSection(rz) - sz0) * nx;
				final int n = rowSpans(ry, rz, this.minX, this.maxX, rowSpans);
				for (int s = 0; s < n; s++) {
					// Split the span at section boundaries
					int from = rowSpans[2 * s];
					final int to = rowSpans[2 * s + 1];
					while (from <= to) {
						final int sectionX = toSection(from);
						final int end = Math.min(to, sectionEnd(sectionX));
						grid[gyz + sectionX - sx0] += end - from + 1;
						from = end + 1;
					}
				}
			}
		}

		int used = 0;
		for (final long count : grid)
			if (count > 0)
				used++;

		final long[] found = new long[used];
		final long[] foundCounts = new long[used];
		final long[] distances = new long[used];
		int i = 0;
		for (int gy = 0; gy < ny; gy++)
			for (int gz = 0; gz < nz; gz++)
				for (int gx = 0; gx < nx; gx++) {
					final long count = grid[(gy * nz + gz) * nx + gx];
					if (count == 0)
						continue;
					final int sx = sx0 + gx;
					final int sy = sy0 + gy;
					final int sz = sz0 + gz;
					found[i] = section(sx, sy, sz);
					foundCounts[i] = count;
					// From the sphere's center to the section's, doubled to stay whole
					final long dx = 2L * sectionStart(sx) + 15 - 2L * inside.x;
					final long dy = 2L * sectionStart(sy) + 15 - 2L * inside.y;
					final long dz = 2L * sectionStart(sz) + 15 - 2L * inside.z;
					distances[i] = dx * dx + dy * dy + dz * dz;
					this.remaining += count;
					i++;
				}

		// A few hundred sections at most; 1.12.2's fastutil has no lambda-friendly int sort
		final Integer[] order = new Integer[used];
		for (int k = 0; k < used; k++)
			order[k] = k;
		Arrays.sort(order, (a, b) -> Long.compare(distances[a], distances[b]));

		this.sections = new long[used];
		this.counts = new long[used];
		for (int k = 0; k < used; k++) {
			this.sections[k] = found[order[k]];
			this.counts[k] = foundCounts[order[k]];
		}
	}

	/**
	 * Everything in {@code sphere} within the world's height.
	 */
	@Nonnull
	static ScanJob full(@Nonnull final ScanSphere sphere, final int worldMinY, final int worldMaxY) {
		return new ScanJob(sphere, null, worldMinY, worldMaxY, NO_LIMIT_LOW, NO_LIMIT_HIGH, NO_LIMIT_LOW,
				NO_LIMIT_HIGH);
	}

	/**
	 * What is in {@code inside} but not {@code outside}, within the world's height: for a move, the new sphere
	 * against the old one, the blocks entering range.
	 */
	@Nonnull
	static ScanJob difference(@Nonnull final ScanSphere inside, @Nonnull final ScanSphere outside,
			final int worldMinY, final int worldMaxY) {
		return new ScanJob(inside, outside, worldMinY, worldMaxY, NO_LIMIT_LOW, NO_LIMIT_HIGH, NO_LIMIT_LOW,
				NO_LIMIT_HIGH);
	}

	/**
	 * What of {@code sphere} lies in the given column (bounds inclusive) within the world's height.
	 */
	@Nonnull
	static ScanJob column(@Nonnull final ScanSphere sphere, final int worldMinY, final int worldMaxY,
			final int minX, final int maxX, final int minZ, final int maxZ) {
		return new ScanJob(sphere, null, worldMinY, worldMaxY, minX, maxX, minZ, maxZ);
	}

	/**
	 * Writes the spans of the row at {@code (rowY, rowZ)} that are in the region and between {@code low} and
	 * {@code high} into {@code out} as (first, last) pairs, in order along X.
	 *
	 * @return the number of spans: 0, 1 or 2
	 */
	private int rowSpans(final int rowY, final int rowZ, final int low, final int high, final int[] out) {
		final int w = this.inside.halfWidth(rowY, rowZ);
		if (w < 0)
			return 0;
		final int a = Math.max(this.inside.x - w, low);
		final int b = Math.min(this.inside.x + w, high);
		if (a > b)
			return 0;

		final ScanSphere cutter = this.outside;
		final int cut = cutter == null ? -1 : cutter.halfWidth(rowY, rowZ);
		if (cut < 0) {
			out[0] = a;
			out[1] = b;
			return 1;
		}

		// [a, b] minus [c, d]: what lies before c and what lies after d
		final int c = cutter.x - cut;
		final int d = cutter.x + cut;
		int n = 0;
		if (c > a) {
			out[0] = a;
			out[1] = Math.min(b, c - 1);
			n = 1;
		}
		if (d < b) {
			out[2 * n] = Math.max(a, d + 1);
			out[2 * n + 1] = b;
			n++;
		}
		return n;
	}

	boolean isDone() {
		return this.index >= this.sections.length;
	}

	/**
	 * The section being worked on, packed with {@link #section}. Only while not done.
	 */
	long currentSection() {
		return this.sections[this.index];
	}

	/**
	 * Writes the next position of the region in the current section into {@code out}, resuming where the last call
	 * left off.
	 *
	 * @return false once the section has no more ({@code out} is then unchanged)
	 */
	boolean next(@Nonnull final BlockPos.MutableBlockPos out) {
		while (true) {
			if (this.spanIndex < this.spanCount) {
				if (this.x <= this.spans[2 * this.spanIndex + 1]) {
					out.setPos(this.x++, this.y, this.z);
					return true;
				}
				if (++this.spanIndex < this.spanCount)
					this.x = this.spans[2 * this.spanIndex];
				continue;
			}
			if (!nextRow())
				return false;
		}
	}

	/**
	 * Moves to the next row of the current section and works out its spans.
	 */
	private boolean nextRow() {
		final long sec = this.sections[this.index];
		final int sy = sectionY(sec);
		final int sz = sectionZ(sec);
		final int lowZ = Math.max(this.minZ, sectionStart(sz));
		if (!this.rowStarted) {
			this.rowStarted = true;
			this.y = Math.max(this.minY, sectionStart(sy));
			this.z = lowZ;
		} else if (++this.z > Math.min(this.maxZ, sectionEnd(sz))) {
			this.z = lowZ;
			this.y++;
		}
		if (this.y > Math.min(this.maxY, sectionEnd(sy))) {
			this.spanCount = 0;
			return false;
		}

		final int sx = sectionX(sec);
		this.spanCount = rowSpans(this.y, this.z, Math.max(this.minX, sectionStart(sx)),
				Math.min(this.maxX, sectionEnd(sx)), this.spans);
		this.spanIndex = 0;
		if (this.spanCount > 0)
			this.x = this.spans[0];
		return true;
	}

	/**
	 * Moves on to the next section, whether the current one was walked to the end or skipped.
	 */
	void finishSection() {
		this.remaining -= this.counts[this.index];
		this.index++;
		this.rowStarted = false;
		this.spanCount = 0;
		this.spanIndex = 0;
	}

	/**
	 * Blocks not yet processed. Counts whole sections, so a partly walked section still counts in full.
	 */
	long remaining() {
		return this.remaining;
	}
}
