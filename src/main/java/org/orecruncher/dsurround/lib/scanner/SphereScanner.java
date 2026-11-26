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

import java.util.ArrayDeque;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.event.BlockUpdateEvent;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Scans the sphere around the scan center, reporting blocks of interest as they come into range
 * ({@link #blockScan}). A sphere rather than a cube: about half the blocks for the same reach.
 * <p>
 * All work goes through one queue of {@link ScanJob}s processed in order, for up to {@link #TIME_BUDGET_NANOS}
 * each tick:
 * <ul>
 * <li>a full scan of the sphere when scanning starts, the world or the range changes, or the player jumps somewhere
 * new</li>
 * <li>when the player moves, a scan job for the shell that entered range</li>
 * <li>when a chunk loads, a scan job for the part of the sphere it holds</li>
 * </ul>
 * Jobs walk the world one chunk section at a time, nearest first, and skip sections that are unloaded or hold only
 * air, so a chunk that isn't loaded yet no longer holds the scan up: it is scanned when it arrives. Fast movement
 * spreads its cost over several ticks instead of landing in one. Ported from the 26.x line.
 * <p>
 * Register an instance on the Forge event bus while connected, for chunk loads and block changes.
 */
public abstract class SphereScanner extends Scanner {

	/**
	 * How long scanning may run each tick. Scanning runs on the client thread, so this is time taken from the
	 * frame. This is the real limit; the block count below is a backstop.
	 */
	protected static final long TIME_BUDGET_NANOS = 1_000_000L;
	/**
	 * Upper bound on blocks read per tick regardless of time, in case the clock misbehaves.
	 */
	protected static final int MAX_BLOCKS_PER_TICK = 65_536;
	/**
	 * How many blocks are read between checks of the clock; the budget can be overrun by at most this many.
	 */
	protected static final int CLOCK_CHECK_INTERVAL = 256;
	// After this many failures from blockScan nothing more is logged, so a broken effect can't flood the log
	private static final int MAX_LOGGED_ERRORS = 10;

	private final ArrayDeque<ScanJob> jobs = new ArrayDeque<>();

	// This tick's budget, set up by processJobs()
	private long deadline;
	private int blocksLeft;
	private int untilClockCheck;

	private int radius;
	private long sphereVolume;
	private int errorsLogged;

	// The sphere currently in range. Null until the first scan, and while the sphere lies entirely outside the
	// world (flying far above the build limit, say).
	@Nullable
	protected ScanSphere activeSphere;
	// The client makes a new World on every dimension change, so comparing the instance detects those (and a
	// reconnect)
	@Nullable
	protected World lastWorld;

	protected SphereScanner(@Nonnull final ScanLocus locus, @Nonnull final String name, final int radius) {
		super(locus, name, radius);
		this.radius = radius;
		this.sphereVolume = ScanSphere.volumeOf(radius);
	}

	private static int worldMinY(@Nonnull final World world) {
		return 0;
	}

	private static int worldMaxY(@Nonnull final World world) {
		return world.getHeight() - 1;
	}

	/**
	 * The sphere centered on {@code pos}, or null if none of it is inside the world's height.
	 */
	@Nullable
	protected ScanSphere getSphereFor(@Nonnull final World world, @Nonnull final BlockPos pos) {
		if (pos.getY() - this.radius > worldMaxY(world) || pos.getY() + this.radius < worldMinY(world))
			return null;
		return ScanSphere.around(pos, this.radius);
	}

	/**
	 * Changes the radius; a change starts over with a full scan.
	 */
	public void setRange(final int radius) {
		if (this.radius != radius) {
			this.radius = radius;
			this.sphereVolume = ScanSphere.volumeOf(radius);
			this.activeSphere = null;
			this.jobs.clear();
		}
	}

	public int getRange() {
		return this.radius;
	}

	/**
	 * True if {@code pos} is in the sphere currently in range.
	 */
	public boolean isInRange(@Nonnull final BlockPos pos) {
		final ScanSphere sphere = this.activeSphere;
		return sphere != null && sphere.contains(pos);
	}

	/**
	 * Drops all queued work and starts a full scan of the sphere around the scan center. Blocks already reported may
	 * be reported again, so {@link #blockScan} must tolerate repeats.
	 */
	protected void startFullScan(@Nonnull final World world, @Nonnull final BlockPos center) {
		this.lastWorld = world;
		this.activeSphere = getSphereFor(world, center);
		this.jobs.clear();
		if (this.activeSphere != null)
			queue(ScanJob.full(this.activeSphere, worldMinY(world), worldMaxY(world)));
	}

	private void queue(@Nonnull final ScanJob job) {
		if (!job.isDone())
			this.jobs.add(job);
	}

	private long getPendingBlocks() {
		long pending = 0;
		for (final ScanJob job : this.jobs)
			pending += job.remaining();
		return pending;
	}

	@Override
	public void update() {
		final BlockPos center = this.locus.getCenter();
		final World world = this.locus.getWorld().getWorld();
		if (center == null || world == null) {
			this.activeSphere = null;
			this.jobs.clear();
			return;
		}

		final ScanSphere newSphere = getSphereFor(world, center);
		if (newSphere == null) {
			// The whole sphere is above or below the world; start over once some of it is back inside
			this.activeSphere = null;
			this.jobs.clear();
			return;
		}

		final ScanSphere oldSphere = this.activeSphere;
		if (oldSphere == null || world != this.lastWorld) {
			startFullScan(world, center);
		} else if (!newSphere.equals(oldSphere)) {
			moveTo(world, center, oldSphere, newSphere);
		}

		processJobs(world);
	}

	/**
	 * Queues the work for the sphere in range moving from {@code oldSphere} to {@code newSphere}.
	 */
	private void moveTo(@Nonnull final World world, @Nonnull final BlockPos center,
			@Nonnull final ScanSphere oldSphere, @Nonnull final ScanSphere newSphere) {
		// No overlap means the player jumped somewhere new (teleport, respawn), so scan the new sphere from scratch
		if (!oldSphere.overlaps(newSphere)) {
			startFullScan(world, center);
			return;
		}

		queue(ScanJob.difference(newSphere, oldSphere, worldMinY(world), worldMaxY(world)));
		this.activeSphere = newSphere;

		// Moving faster than the budget keeps up with (flying, riding) would let the queue grow without limit. Once
		// it holds more than two spheres, the movement work alone exceeds a full scan, so starting over is cheaper.
		if (getPendingBlocks() > 2L * this.sphereVolume)
			startFullScan(world, center);
	}

	private void processJobs(@Nonnull final World world) {
		this.deadline = System.nanoTime() + TIME_BUDGET_NANOS;
		this.blocksLeft = MAX_BLOCKS_PER_TICK;
		this.untilClockCheck = CLOCK_CHECK_INTERVAL;

		while (this.blocksLeft > 0 && !this.jobs.isEmpty()) {
			final ScanJob job = this.jobs.peekFirst();
			runJob(job, world);
			if (job.isDone())
				this.jobs.pollFirst();
		}
	}

	/**
	 * Uses one unit of this tick's budget. Every {@link #CLOCK_CHECK_INTERVAL} units the clock is checked, and once
	 * the time is up the budget is emptied.
	 */
	private void spend() {
		this.blocksLeft--;
		if (--this.untilClockCheck == 0) {
			this.untilClockCheck = CLOCK_CHECK_INTERVAL;
			if (System.nanoTime() >= this.deadline)
				this.blocksLeft = 0;
		}
	}

	/**
	 * Works on {@code job} until it is done or this tick's budget is spent. Each block read costs one unit; skipping
	 * a whole section costs one.
	 */
	private void runJob(@Nonnull final ScanJob job, @Nonnull final World world) {
		final BlockPos.MutableBlockPos pos = this.workingPos;
		final ScanSphere inRange = this.activeSphere;
		while (this.blocksLeft > 0 && !job.isDone()) {
			final ExtendedBlockStorage section = getSection(world, job.currentSection());
			if (section == null) {
				// Unloaded, or nothing but air: no block in it can be of interest
				job.finishSection();
				spend();
				continue;
			}

			while (this.blocksLeft > 0) {
				if (!job.next(pos)) {
					job.finishSection();
					break;
				}
				spend();

				// A block that has left range since this job was queued is skipped
				if (inRange == null || !inRange.contains(pos))
					continue;

				scanBlock(section.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15), pos);
			}
		}
	}

	/**
	 * The chunk section at {@code packed} ({@link ScanJob#section}), or null if its chunk isn't loaded or it holds
	 * only air.
	 */
	@Nullable
	private static ExtendedBlockStorage getSection(@Nonnull final World world, final long packed) {
		final int sy = ScanJob.sectionY(packed);
		if (sy < 0 || sy > 15)
			return null;
		final Chunk chunk = world.getChunkProvider().getLoadedChunk(ScanJob.sectionX(packed),
				ScanJob.sectionZ(packed));
		if (chunk == null || chunk.isEmpty())
			return null;
		final ExtendedBlockStorage section = chunk.getBlockStorageArray()[sy];
		return section == Chunk.NULL_BLOCK_STORAGE || section.isEmpty() ? null : section;
	}

	/**
	 * Passes a block of interest to {@link #blockScan}. An exception from one block is logged (the first few) instead
	 * of abandoning the rest of the scan.
	 */
	protected final void scanBlock(@Nonnull final IBlockState state, @Nonnull final BlockPos pos) {
		if (!interestingBlock(state))
			return;
		try {
			blockScan(state, pos, this.random);
		} catch (final VirtualMachineError fatal) {
			throw fatal;
		} catch (final Throwable t) {
			if (this.errorsLogged < MAX_LOGGED_ERRORS) {
				this.errorsLogged++;
				this.log.error(String.format("[%s] blockScan failed at %s for %s", this.name, pos, state), t);
			}
		}
	}

	@Override
	@Nullable
	protected BlockPos nextPos(@Nonnull final BlockPos.MutableBlockPos pos, @Nonnull final Random rand) {
		// Not used: update() walks the queued jobs
		return null;
	}

	/**
	 * Queues a scan of the part of the sphere in a chunk that has just loaded. Chunks keep arriving after a scan
	 * starts (joining, respawning, a range beyond the server's view distance), and a section that wasn't loaded when
	 * the scan reached it was skipped. The event comes before the chunk's data is read; the job runs on a later
	 * tick, once it has been.
	 */
	@SubscribeEvent
	public void onChunkLoad(@Nonnull final ChunkEvent.Load event) {
		final ScanSphere sphere = this.activeSphere;
		final World world = event.getWorld();
		if (sphere == null || world == null || world != this.lastWorld)
			return;

		// One block wider than the chunk on each side: a block along the border of an already loaded neighbor may
		// depend on blocks in this chunk (steam is found from the lava next to the water), and that neighbor was
		// scanned while this chunk was missing
		final Chunk chunk = event.getChunk();
		final int minX = chunk.x << 4;
		final int minZ = chunk.z << 4;
		queue(ScanJob.column(sphere, worldMinY(world), worldMaxY(world), minX - 1, minX + 16, minZ - 1, minZ + 16));
	}

	/**
	 * Reports a block the client was told changed, if it is in range. Not queued: block changes are few and should
	 * take effect at once.
	 */
	@SubscribeEvent
	public void onBlockUpdate(@Nonnull final BlockUpdateEvent event) {
		final ScanSphere sphere = this.activeSphere;
		if (sphere == null || event.world != this.lastWorld || event.oldState == event.newState)
			return;
		if (sphere.contains(event.pos))
			scanBlock(event.newState, event.pos);
	}
}
