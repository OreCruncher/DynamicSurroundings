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

package org.orecruncher.dsurround.client.weather.frost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/**
 * The sides of windows around the player that frost ({@link FrostFaces}), kept a section (16 cubed) at a time.
 * Ported from the 26.x line. A section is looked at again when something in or near it changes (a block, its chunk
 * loading), and in turn every few seconds, for the temperature, which a season changes, and the light, which a roof
 * put up changes. What each face has frosted carries over a fresh look; a face new to the field starts with the frost
 * it should have, so windows come into range already frosted rather than frosting as the player arrives.
 * <p>
 * Client thread only: looking at a section reads blocks and their shapes.
 */
public final class FrostField {

	// Full looks at sections a tick. A section without frosting glass costs a pass over its blocks.
	static final int SCANS_PER_TICK = 8;
	// How far around the player windows frost, across and up or down, in blocks
	public static final int RANGE = 32;
	static final int RANGE_VERTICAL = 16;
	// A block change this close to a section's edge can change the faces in the next (see FrostFaces.EDGE_REACH)
	static final int CHANGE_REACH = 3;
	// The world's sections, bottom to top
	static final int MIN_SECTION_Y = 0;
	static final int MAX_SECTION_Y = 15;

	static final class Section {
		final long key;
		final int x, y, z;
		List<FrostFace> faces = Collections.emptyList();
		boolean dirty = true;

		Section(final int x, final int y, final int z) {
			this.key = key(x, y, z);
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	private final Long2ObjectOpenHashMap<Section> sections = new Long2ObjectOpenHashMap<>();
	private final List<Section> pending = new ArrayList<>();
	private final List<Section> inTurn = new ArrayList<>();
	private final Long2FloatOpenHashMap carried = new Long2FloatOpenHashMap();
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	private int turn;
	private int faceCount;

	// The sections in range, inclusive; an empty range (min above max) until the first update
	private int minX = 1, maxX = 0, minY = 1, maxY = 0, minZ = 1, maxZ = 0;

	static long key(final int sx, final int sy, final int sz) {
		return ((sx & 0x1FFFFFL) << 43) | ((sz & 0x1FFFFFL) << 22) | (sy & 0x3FFFFFL);
	}

	public void clear() {
		this.sections.clear();
		this.pending.clear();
		this.inTurn.clear();
		this.faceCount = 0;
		this.minX = this.minY = this.minZ = 1;
		this.maxX = this.maxY = this.maxZ = 0;
	}

	public int sectionCount() {
		return this.sections.size();
	}

	public int faceCount() {
		return this.faceCount;
	}

	public void forEachFace(@Nonnull final Consumer<FrostFace> action) {
		// Called every frame: by index, rather than an iterator for each section's list
		for (final Section section : this.sections.values()) {
			final List<FrostFace> faces = section.faces;
			for (int i = 0, n = faces.size(); i < n; i++)
				action.accept(faces.get(i));
		}
	}

	/**
	 * A block changed: the sections it could change faces in look again
	 */
	public void blockChanged(final int x, final int y, final int z) {
		for (int sx = (x - CHANGE_REACH) >> 4; sx <= (x + CHANGE_REACH) >> 4; sx++)
			for (int sy = (y - CHANGE_REACH) >> 4; sy <= (y + CHANGE_REACH) >> 4; sy++)
				for (int sz = (z - CHANGE_REACH) >> 4; sz <= (z + CHANGE_REACH) >> 4; sz++)
					this.markDirty(sx, sy, sz);
	}

	/**
	 * A chunk arrived: every section of it in range looks again
	 */
	public void chunkChanged(final int cx, final int cz) {
		if (cx < this.minX || cx > this.maxX || cz < this.minZ || cz > this.maxZ)
			return;
		for (int sy = this.minY; sy <= this.maxY; sy++)
			this.markDirty(cx, sy, cz);
	}

	private void markDirty(final int sx, final int sy, final int sz) {
		final Section section = this.sections.get(key(sx, sy, sz));
		if (section != null)
			section.dirty = true;
	}

	/**
	 * Brings the sections in range up to date for a player at a block position, and the frost on every face a tick on
	 *
	 * @param target how much frost a face should have, worked out for it
	 */
	public void update(@Nonnull final World world, @Nonnull final IFrostWorld frostWorld, final int px, final int py,
			final int pz, @Nonnull final FrostTarget target) {
		this.setRange(MIN_SECTION_Y, MAX_SECTION_Y, px, py, pz);

		// One section in turn looks again, for the temperature and the light
		if (!this.inTurn.isEmpty()) {
			this.turn = (this.turn + 1) % this.inTurn.size();
			this.inTurn.get(this.turn).dirty = true;
		}

		this.pending.clear();
		for (final Section section : this.sections.values())
			if (section.dirty)
				this.pending.add(section);
		if (this.pending.size() > SCANS_PER_TICK) {
			final int cx = px >> 4, cy = py >> 4, cz = pz >> 4;
			this.pending.sort(Comparator.comparingInt(s -> distanceSq(s, cx, cy, cz)));
		}
		final int scans = Math.min(this.pending.size(), SCANS_PER_TICK);
		for (int i = 0; i < scans; i++)
			this.scan(world, frostWorld, this.pending.get(i), target);
		this.pending.clear();

		int count = 0;
		for (final Section section : this.sections.values()) {
			final List<FrostFace> faces = section.faces;
			for (int i = 0, n = faces.size(); i < n; i++) {
				final FrostFace face = faces.get(i);
				face.target = target.of(face);
				face.shown = FrostConditions.ease(face.shown, face.target);
			}
			count += faces.size();
		}
		this.faceCount = count;
	}

	/**
	 * Sets the sections in range for a player at a block position: those out of it are dropped, new ones are added to
	 * be looked at. Nothing changes while the player stays within the same sections.
	 */
	void setRange(final int worldMinY, final int worldMaxY, final int px, final int py, final int pz) {
		final int minX = (px - RANGE) >> 4, maxX = (px + RANGE) >> 4;
		final int minZ = (pz - RANGE) >> 4, maxZ = (pz + RANGE) >> 4;
		final int minY = Math.max(worldMinY, (py - RANGE_VERTICAL) >> 4);
		final int maxY = Math.min(worldMaxY, (py + RANGE_VERTICAL) >> 4);
		if (minX == this.minX && maxX == this.maxX && minY == this.minY && maxY == this.maxY && minZ == this.minZ
				&& maxZ == this.maxZ)
			return;

		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.minZ = minZ;
		this.maxZ = maxZ;

		this.sections.values().removeIf(
				s -> s.x < minX || s.x > maxX || s.y < minY || s.y > maxY || s.z < minZ || s.z > maxZ);
		for (int sx = minX; sx <= maxX; sx++)
			for (int sy = minY; sy <= maxY; sy++)
				for (int sz = minZ; sz <= maxZ; sz++) {
					final long key = key(sx, sy, sz);
					if (!this.sections.containsKey(key))
						this.sections.put(key, new Section(sx, sy, sz));
				}
		this.inTurn.clear();
		this.inTurn.addAll(this.sections.values());
		this.turn = 0;
	}

	private static int distanceSq(@Nonnull final Section s, final int cx, final int cy, final int cz) {
		final int dx = s.x - cx, dy = s.y - cy, dz = s.z - cz;
		return dx * dx + dy * dy + dz * dz;
	}

	private void scan(@Nonnull final World world, @Nonnull final IFrostWorld frostWorld,
			@Nonnull final Section section, @Nonnull final FrostTarget target) {
		section.dirty = false;
		final List<FrostFace> old = section.faces;
		section.faces = Collections.emptyList();

		// Not loaded yet: the chunk arriving marks it again
		final Chunk chunk = world.getChunkProvider().getLoadedChunk(section.x, section.z);
		if (chunk == null || chunk.isEmpty())
			return;
		final ExtendedBlockStorage blocks = chunk.getBlockStorageArray()[section.y];
		if (blocks == Chunk.NULL_BLOCK_STORAGE || blocks.isEmpty())
			return;

		this.carried.clear();
		for (final FrostFace face : old)
			this.carried.put(face.key(), face.shown);

		final List<FrostFace> found = new ArrayList<>();
		final int baseX = section.x << 4, baseY = section.y << 4, baseZ = section.z << 4;
		for (int ly = 0; ly < 16; ly++)
			for (int lz = 0; lz < 16; lz++)
				for (int lx = 0; lx < 16; lx++) {
					final IBlockState state = blocks.get(lx, ly, lz);
					if (!frostWorld.frosts(state))
						continue;
					this.pos.setPos(baseX + lx, baseY + ly, baseZ + lz);
					FrostFaces.facesOf(frostWorld, this.pos.toImmutable(), state, face -> {
						face.target = target.of(face);
						final long key = face.key();
						face.shown = this.carried.containsKey(key) ? this.carried.get(key) : face.target;
						found.add(face);
					});
				}
		section.faces = found;
	}

	/**
	 * How much frost a face should have, 0 to 1
	 */
	@FunctionalInterface
	public interface FrostTarget {
		float of(@Nonnull FrostFace face);
	}
}
