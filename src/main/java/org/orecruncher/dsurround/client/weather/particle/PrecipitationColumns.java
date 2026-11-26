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

package org.orecruncher.dsurround.client.weather.particle;

import java.util.Arrays;

import net.minecraft.util.math.MathHelper;

/**
 * The columns around the camera, looked up once a tick: where precipitation stops, what falls, and how it is lit.
 * Drops read their column here, so the cost of the lookups follows the area covered rather than the number of drops.
 * Ported from the 26.x line's particle weather.
 * <p>
 * Dust is the region's: the color of the loose ground across the area, averaged, whatever the dust is blowing over.
 * <p>
 * Looking a column up takes several reads of the world (its height, biome, temperature, light), so each is kept in a
 * slot of its own, found from where it is in the world, and not looked up again while it stays in the area: moving
 * into another column looks up only the columns that came into the area. A few rows are looked at again each tick,
 * every row once in {@link #REFRESH_TICKS} ticks, to see blocks placed or broken, light changing and the camera's
 * height (which what falls is taken at), spreading the work rather than doing it all at once; and all of them when
 * {@link #invalidate} says what was found is out of date. What falls in a column is asked again only when the height
 * it is taken at or the ground's has changed, or on every {@link #WEATHER_LOOKS}th look, for the season turning. The
 * shares of the area that rain, snow and dust fall in, and the dust's color across it, are kept as running totals.
 * <p>
 * The drops keep to a cylinder around the camera, so the area is the disc of columns they can reach, not the square
 * around it. The slots still cover the square; those outside the disc are left empty.
 */
final class PrecipitationColumns {

	static final int REFRESH_TICKS = 10;
	/**
	 * A column's looks, at the most, between asking what falls in it: every 5 seconds standing still
	 */
	static final int WEATHER_LOOKS = 10;

	private final int radius;
	private final int size;
	// Per row of the area, how far the disc reaches either side of the camera's column, in columns
	private final int[] rowReach;
	// How many columns the disc has
	private final int columns;
	// Each slot's column in the world, or Integer.MIN_VALUE for none
	private final int[] slotX;
	private final int[] slotZ;
	private final int[] ground;
	private final int[] light;
	private final int[] dustColor;
	private final float[] snowShare;
	private final PrecipitationKind[] kind;
	// The height what falls was asked at, and the looks left before it is asked again
	private final int[] weatherY;
	private final byte[] weatherLooksLeft;
	private int originX;
	private int originZ;
	private int regionalDustColor;
	// Running totals over the area: columns rain or snow falls in, columns dust blows in, and the columns dust is
	// blown from with their colors added up
	private int falling;
	private int dusty;
	private int sources;
	private long red, green, blue;
	// The first row of the area looked at again next tick
	private int refreshRow;
	private boolean stale = true;

	PrecipitationColumns(final int radius) {
		this.radius = radius;
		this.size = radius * 2 + 1;
		final int cells = this.size * this.size;
		this.slotX = new int[cells];
		this.slotZ = new int[cells];
		this.ground = new int[cells];
		this.light = new int[cells];
		this.dustColor = new int[cells];
		this.snowShare = new float[cells];
		this.kind = new PrecipitationKind[cells];
		this.weatherY = new int[cells];
		this.weatherLooksLeft = new byte[cells];
		this.rowReach = new int[this.size];
		int count = 0;
		for (int dz = 0; dz < this.size; dz++) {
			int reach = 0;
			while (reach < radius && inDisc(reach + 1, dz - radius, radius))
				reach++;
			this.rowReach[dz] = reach;
			count += reach * 2 + 1;
		}
		this.columns = count;
		forget();
	}

	/**
	 * Whether a drop within {@code radius} of the camera can be in the column {@code dx}, {@code dz} columns from the
	 * camera's: the camera is anywhere in its own column, so the gap between the two columns is what counts
	 */
	static boolean inDisc(final int dx, final int dz, final int radius) {
		final int gapX = Math.max(0, Math.abs(dx) - 1);
		final int gapZ = Math.max(0, Math.abs(dz) - 1);
		return gapX * gapX + gapZ * gapZ < radius * radius;
	}

	/**
	 * How many columns are looked up
	 */
	int columnCount() {
		return this.columns;
	}

	private boolean inArea(final int i, final int j) {
		return Math.abs(i - this.radius) <= this.rowReach[j];
	}

	/**
	 * Brings the columns up to date for where the camera is: all of them if they were {@link #invalidate invalidated}
	 * (or never looked up); otherwise those that came into the area, and the rows due a fresh look. Columns that left
	 * the disc are taken out. Called once a tick.
	 *
	 * @return true if any were looked up
	 */
	boolean refresh(final IColumnSource source, final int cameraX, final int cameraY, final int cameraZ) {
		if (this.stale) {
			update(source, cameraX, cameraY, cameraZ);
			return true;
		}
		this.originX = cameraX - this.radius;
		this.originZ = cameraZ - this.radius;

		final int rowsPerTick = rowsPerTick(this.size);
		final int refreshFrom = this.refreshRow;
		this.refreshRow = (this.refreshRow + rowsPerTick) % this.size;
		boolean looked = false;
		boolean dropped = false;
		for (int j = 0; j < this.size; j++) {
			final int z = this.originZ + j;
			final boolean refresh = Math.floorMod(j - refreshFrom, this.size) < rowsPerTick;
			for (int i = 0; i < this.size; i++) {
				final int x = this.originX + i;
				final int slot = slot(x, z);
				if (!inArea(i, j)) {
					if (this.slotX[slot] != Integer.MIN_VALUE) {
						empty(slot);
						dropped = true;
					}
				} else if (refresh || this.slotX[slot] != x || this.slotZ[slot] != z) {
					look(source, slot, x, z, cameraY);
					looked = true;
				}
			}
		}
		if (looked || dropped)
			updateRegionalDustColor(source, cameraX, cameraY, cameraZ);
		return looked;
	}

	/**
	 * How many of the area's rows are looked at again each tick: enough for all of them in {@link #REFRESH_TICKS}
	 */
	static int rowsPerTick(final int size) {
		return Math.max(1, MathHelper.ceil(size / (float) REFRESH_TICKS));
	}

	/**
	 * Marks what was found as out of date, so the next {@link #refresh} looks at every column again
	 */
	void invalidate() {
		this.stale = true;
	}

	/**
	 * Looks up every column around the camera afresh. What falls, and its light, are taken where the drops are seen:
	 * at the camera's height, or the ground's if that is higher.
	 */
	void update(final IColumnSource source, final int cameraX, final int cameraY, final int cameraZ) {
		forget();
		this.stale = false;
		this.refreshRow = 0;
		this.originX = cameraX - this.radius;
		this.originZ = cameraZ - this.radius;
		for (int j = 0; j < this.size; j++) {
			final int reach = this.rowReach[j];
			for (int i = this.radius - reach; i <= this.radius + reach; i++) {
				final int x = this.originX + i, z = this.originZ + j;
				look(source, slot(x, z), x, z, cameraY);
			}
		}
		updateRegionalDustColor(source, cameraX, cameraY, cameraZ);
	}

	/**
	 * Looks a column up into its slot, in place of what the slot held, keeping the running totals
	 */
	private void look(final IColumnSource source, final int slot, final int x, final int z, final int cameraY) {
		if (this.slotX[slot] != Integer.MIN_VALUE)
			count(slot, -1);
		final int g = source.groundHeight(x, z);
		final int y = Math.max(g, cameraY);
		// What falls is kept while the column, the heights and the looks left allow (see WEATHER_LOOKS)
		final PrecipitationKind what;
		if (this.slotX[slot] == x && this.slotZ[slot] == z && this.weatherY[slot] == y && this.ground[slot] == g
				&& this.weatherLooksLeft[slot] > 0) {
			what = this.kind[slot];
			this.weatherLooksLeft[slot]--;
		} else {
			what = source.precipitation(x, y, z);
			this.kind[slot] = what;
			this.snowShare[slot] = what == PrecipitationKind.RAIN ? source.snowShare(x, y, z) : 0F;
			this.weatherY[slot] = y;
			this.weatherLooksLeft[slot] = WEATHER_LOOKS - 1;
		}
		this.slotX[slot] = x;
		this.slotZ[slot] = z;
		this.ground[slot] = g;
		this.light[slot] = source.light(x, y, z);
		// From the top block of the column, if dust is blown from it
		this.dustColor[slot] = what == PrecipitationKind.DUST ? source.dustColor(x, g - 1, z)
				: IColumnSource.NOT_A_DUST_SOURCE;
		count(slot, 1);
	}

	private void empty(final int slot) {
		count(slot, -1);
		this.slotX[slot] = Integer.MIN_VALUE;
		this.slotZ[slot] = Integer.MIN_VALUE;
		this.kind[slot] = PrecipitationKind.NONE;
		this.dustColor[slot] = IColumnSource.NOT_A_DUST_SOURCE;
	}

	/**
	 * Adds a slot's column to the running totals, or takes it out
	 *
	 * @param sign 1 to add, -1 to take out
	 */
	private void count(final int slot, final int sign) {
		final PrecipitationKind what = this.kind[slot];
		if (what == PrecipitationKind.RAIN || what == PrecipitationKind.SNOW)
			this.falling += sign;
		if (what == PrecipitationKind.DUST) {
			this.dusty += sign;
			final int color = this.dustColor[slot];
			if (color != IColumnSource.NOT_A_DUST_SOURCE) {
				this.sources += sign;
				this.red += sign * ((color >> 16) & 0xFF);
				this.green += sign * ((color >> 8) & 0xFF);
				this.blue += sign * (color & 0xFF);
			}
		}
	}

	private void updateRegionalDustColor(final IColumnSource source, final int cameraX, final int cameraY,
			final int cameraZ) {
		if (this.sources > 0)
			this.regionalDustColor = (int) (this.red / this.sources) << 16 | (int) (this.green / this.sources) << 8
					| (int) (this.blue / this.sources);
		else if (this.dusty > 0)
			this.regionalDustColor = source.fallbackDustColor(cameraX, cameraY, cameraZ);
	}

	private int slot(final int x, final int z) {
		return Math.floorMod(z, this.size) * this.size + Math.floorMod(x, this.size);
	}

	private void forget() {
		Arrays.fill(this.slotX, Integer.MIN_VALUE);
		Arrays.fill(this.slotZ, Integer.MIN_VALUE);
		Arrays.fill(this.kind, PrecipitationKind.NONE);
		Arrays.fill(this.dustColor, IColumnSource.NOT_A_DUST_SOURCE);
		this.falling = 0;
		this.dusty = 0;
		this.sources = 0;
		this.red = this.green = this.blue = 0;
	}

	/**
	 * The column a position is in, or -1 if it is outside the area looked up
	 */
	int index(final double x, final double z) {
		final int bx = MathHelper.floor(x), bz = MathHelper.floor(z);
		final int dx = bx - this.originX;
		final int dz = bz - this.originZ;
		if (dz < 0 || dz >= this.size || Math.abs(dx - this.radius) > this.rowReach[dz])
			return -1;
		return slot(bx, bz);
	}

	/**
	 * The share of the columns rain or snow falls in, 0 to 1
	 */
	float fallingShare() {
		return this.falling / (float) this.columns;
	}

	/**
	 * The share of the columns dust blows in, 0 to 1
	 */
	float dustShare() {
		return this.dusty / (float) this.columns;
	}

	float snowShare(final int index) {
		return this.snowShare[index];
	}

	int ground(final int index) {
		return this.ground[index];
	}

	int light(final int index) {
		return this.light[index];
	}

	int dustColor(final int index) {
		return this.dustColor[index];
	}

	int regionalDustColor() {
		return this.regionalDustColor;
	}

	/**
	 * The color a puff of dust takes where it rises: the region's, or, over a block dust is blown from, half the
	 * region's and half the block's own
	 */
	int dustColorAt(final int index) {
		final int own = this.dustColor[index];
		final int regional = this.regionalDustColor;
		if (own == IColumnSource.NOT_A_DUST_SOURCE)
			return regional;
		return mix(own, regional);
	}

	private static int mix(final int a, final int b) {
		final int r = (((a >> 16) & 0xFF) + ((b >> 16) & 0xFF)) / 2;
		final int g = (((a >> 8) & 0xFF) + ((b >> 8) & 0xFF)) / 2;
		final int bl = ((a & 0xFF) + (b & 0xFF)) / 2;
		return r << 16 | g << 8 | bl;
	}

	PrecipitationKind kind(final int index) {
		return this.kind[index];
	}
}
