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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.util.math.BlockPos;

public class ScanJobTests {

	private static final int MIN_Y = 0;
	private static final int MAX_Y = 255;

	// Every position a job hands out, checking none comes twice and that sections come nearest first
	private static Set<BlockPos> walk(final ScanJob job, final ScanSphere center) {
		final Set<BlockPos> seen = new HashSet<>();
		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		long lastDistance = -1;
		final long expected = job.remaining();
		while (!job.isDone()) {
			final long sec = job.currentSection();
			final long dx = 2L * (ScanJob.sectionX(sec) << 4) + 15 - 2L * center.x;
			final long dy = 2L * (ScanJob.sectionY(sec) << 4) + 15 - 2L * center.y;
			final long dz = 2L * (ScanJob.sectionZ(sec) << 4) + 15 - 2L * center.z;
			final long distance = dx * dx + dy * dy + dz * dz;
			assertTrue(distance >= lastDistance, "sections out of order");
			lastDistance = distance;
			while (job.next(pos)) {
				assertEquals(ScanJob.sectionX(sec), pos.getX() >> 4);
				assertEquals(ScanJob.sectionY(sec), pos.getY() >> 4);
				assertEquals(ScanJob.sectionZ(sec), pos.getZ() >> 4);
				assertTrue(seen.add(pos.toImmutable()), "visited twice: " + pos);
			}
			job.finishSection();
		}
		assertEquals(expected, seen.size(), "remaining() didn't match what was walked");
		assertEquals(0, job.remaining());
		return seen;
	}

	private static Set<BlockPos> sphere(final ScanSphere s, final ScanSphere minus, final int minX, final int maxX,
			final int minZ, final int maxZ) {
		final Set<BlockPos> result = new HashSet<>();
		for (int x = s.x - s.radius; x <= s.x + s.radius; x++)
			for (int y = Math.max(s.y - s.radius, MIN_Y); y <= Math.min(s.y + s.radius, MAX_Y); y++)
				for (int z = s.z - s.radius; z <= s.z + s.radius; z++)
					if (x >= minX && x <= maxX && z >= minZ && z <= maxZ && s.contains(x, y, z)
							&& (minus == null || !minus.contains(x, y, z)))
						result.add(new BlockPos(x, y, z));
		return result;
	}

	@Test
	public void packedSectionsRoundTrip() {
		final int[] values = { 0, 1, -1, 15, -16, 1_875_000, -1_875_000 };
		for (final int x : values)
			for (final int y : new int[] { 0, 15, -4, 20 })
				for (final int z : values) {
					final long packed = ScanJob.section(x, y, z);
					assertEquals(x, ScanJob.sectionX(packed));
					assertEquals(y, ScanJob.sectionY(packed));
					assertEquals(z, ScanJob.sectionZ(packed));
				}
	}

	@Test
	public void fullScanVisitsTheSphereOnce() {
		// Negative coordinates and a center near the bottom of the world, which trims the sphere
		for (final ScanSphere s : new ScanSphere[] { new ScanSphere(-37, 70, 1203, 24), new ScanSphere(5, 6, -9, 24),
				new ScanSphere(100, 250, 100, 16) }) {
			final Set<BlockPos> walked = walk(ScanJob.full(s, MIN_Y, MAX_Y), s);
			assertEquals(sphere(s, null, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE),
					walked);
		}
	}

	@Test
	public void volumeMatchesAnUntrimmedSphere() {
		final ScanSphere s = new ScanSphere(0, 100, 0, 24);
		assertEquals(ScanSphere.volumeOf(24), walk(ScanJob.full(s, MIN_Y, MAX_Y), s).size());
	}

	@Test
	public void aMoveScansOnlyTheShellEnteringRange() {
		final ScanSphere old = new ScanSphere(-3, 64, -3, 24);
		for (final ScanSphere moved : new ScanSphere[] { new ScanSphere(-2, 64, -3, 24),
				new ScanSphere(4, 60, -10, 24), new ScanSphere(-3, 64, 30, 24) }) {
			final Set<BlockPos> walked = walk(ScanJob.difference(moved, old, MIN_Y, MAX_Y), moved);
			assertEquals(sphere(moved, old, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE,
					Integer.MAX_VALUE), walked);
		}
	}

	@Test
	public void aLoadedChunkScansItsColumnOnly() {
		final ScanSphere s = new ScanSphere(-20, 64, 7, 24);
		// The chunk at (-2, 0), one block wider each side
		final int minX = -32 - 1, maxX = -17 + 1, minZ = -1, maxZ = 16;
		final Set<BlockPos> walked = walk(ScanJob.column(s, MIN_Y, MAX_Y, minX, maxX, minZ, maxZ), s);
		assertEquals(sphere(s, null, minX, maxX, minZ, maxZ), walked);
	}

	@Test
	public void aSphereOutsideTheWorldIsDoneAtOnce() {
		final ScanJob job = ScanJob.full(new ScanSphere(0, 400, 0, 24), MIN_Y, MAX_Y);
		assertTrue(job.isDone());
		assertEquals(0, job.remaining());
	}
}
