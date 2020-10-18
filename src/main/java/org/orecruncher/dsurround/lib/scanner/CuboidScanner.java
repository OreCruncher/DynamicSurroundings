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

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.event.BlockUpdateEvent;
import org.orecruncher.lib.chunk.IBlockAccessEx;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Scans the area around the player in a continuous pattern.
 */
public abstract class CuboidScanner extends Scanner {

	// Iteration variables
	protected boolean scanFinished = false;
	protected Cuboid activeCuboid;
	protected CuboidPointIterator fullRange;

	// State of last tick
	protected BlockPos lastPos;
	protected int lastReference = 0;

	protected CuboidScanner(@Nonnull final ScanLocus locus, @Nonnull final String name, final int range,
			final int blocksPerTick) {
		super(locus, name, range, blocksPerTick);
	}

	public boolean isScanFinished() {
		return this.scanFinished;
	}

	protected BlockPos[] getMinMaxPointsForVolume(@Nonnull final BlockPos pos) {
		BlockPos min = pos.add(-this.xRange, -this.yRange, -this.zRange);
		final BlockPos max = pos.add(this.xRange, this.yRange, this.zRange);

		if (min.getY() < 0)
			min = new BlockPos(min.getX(), 0, min.getZ());

		return new BlockPos[] { min, max };
	}

	protected Cuboid getVolumeFor(final BlockPos pos) {
		final BlockPos[] points = getMinMaxPointsForVolume(pos);
		return new Cuboid(points);
	}

	protected void resetFullScan() {
		this.lastPos = this.locus.getCenter();
		this.lastReference = this.locus.getReference();
		this.scanFinished = false;

		final BlockPos[] points = getMinMaxPointsForVolume(this.lastPos);
		this.activeCuboid = new Cuboid(points);
		this.fullRange = new CuboidPointIterator(points);
	}

	@Override
	public void update() {

		// If there is no player position or it's bogus just return
		final BlockPos playerPos = this.locus.getCenter();
		if (playerPos == null || playerPos.getY() < 0) {
			this.fullRange = null;
		} else {
			// If the full range was reset, or the player dimension changed,
			// dump
			// everything and restart.
			if (this.fullRange == null || this.locus.getReference() != this.lastReference) {
				resetFullScan();
				super.update();
			} else if (this.lastPos.equals(playerPos)) {
				// The player didn't move. If a scan is in progress
				// continue.
				if (!this.scanFinished)
					super.update();
			} else {
				// The player moved.
				final Cuboid oldVolume = this.activeCuboid != null ? this.activeCuboid : getVolumeFor(this.lastPos);
				final Cuboid newVolume = getVolumeFor(playerPos);
				final Cuboid intersect = oldVolume.intersection(newVolume);

				// If there is no intersect it means the player moved
				// enough of a distance in the last tick to make it a new
				// area. Otherwise, if there is a sufficiently large
				// change to the scan area dump and restart.
				if (intersect == null || oldVolume.volume() < (oldVolume.volume() - intersect.volume()) * 2) {
					resetFullScan();
					super.update();
				} else {

					// Looks to be a small update, like a player walking around.
					// If the scan has already completed we do an update.
					if (this.scanFinished) {
						this.lastPos = playerPos;
						this.activeCuboid = newVolume;
						updateScan(newVolume, intersect);
					} else {
						// The existing scan hasn't completed but now we
						// have a delta set. Finish out scanning the
						// old volume and once that is locked then an
						// subsequent tick will do a delta update to get
						// the new blocks.
						super.update();
					}
				}
			}
		}
	}

	protected void updateScan(@Nonnull final Cuboid newVolume, @Nonnull final Cuboid intersect) {

		final IBlockAccessEx provider = this.locus.getWorld();

		// Notify on blocks coming into range
		final ComplementsPointIterator newInRange = new ComplementsPointIterator(newVolume, intersect);
		for (BlockPos point = newInRange.next(); point != null; point = newInRange.next()) {
			if (point.getY() > 0) {
				final IBlockState state = provider.getBlockState(point);
				if (interestingBlock(state))
					blockScan(state, point, this.random);
			}
		}

		this.scanFinished = true;
	}

	@Override
	@Nullable
	protected BlockPos nextPos(@Nonnull final BlockPos.MutableBlockPos workingPos, @Nonnull final Random rand) {

		if (this.scanFinished)
			return null;

		final IBlockAccessEx provider = this.locus.getWorld();

		int checked = 0;

		BlockPos point;
		while ((point = this.fullRange.peek()) != null) {

			// Chunk not loaded we need to skip this tick
			if (!provider.isAvailable(point))
				return null;

			// Consume the point
			this.fullRange.next();

			// Has to be in valid space for it to
			// be returned.
			if (point.getY() > 0) {
				return point;
			}

			// Advance our check counter and loop back
			// to examine the next point.
			if (++checked >= this.blocksPerTick)
				return null;
		}

		this.scanFinished = true;
		return null;
	}

	protected boolean isInteresting(@Nonnull final BlockUpdateEvent event) {
		if (this.activeCuboid == null || event.oldState == event.newState)
			return false;

		if (!this.activeCuboid.contains(event.pos))
			return false;

		if (!interestingBlock(event.newState))
			return false;

		return this.locus.getWorld().isAvailable(event.pos);
	}

	@SubscribeEvent()
	public void onBlockUpdate(@Nonnull final BlockUpdateEvent event) {
		try {
			if (isInteresting(event)) {
				blockScan(event.newState, event.pos, this.random);
			}
		} catch (final Throwable t) {
			this.log.error("onBlockUpdate() error", t);
		}
	}

}