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

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.util.math.MathHelper;

/**
 * Where snowflakes landed, from the 26.x line: each lies flat where it came down, as the flake it fell as, for a
 * moment, then shrinks and fades as it melts into the ground. One blown into the side of something sticks to it, flat
 * on the face it hit. Kept in flat arrays, drawn with the falling snow ({@link DropRenderer#build(SnowFlecks,
 * DropRenderer.Quads, DropRenderer.View, float, java.util.List, int)}); flecks added when it is full are left out.
 * Render thread only.
 */
public final class SnowFlecks {

	/**
	 * Which face of a block a fleck lies on: the top, or a side, named by the way the face looks (west looks toward
	 * negative x)
	 */
	public static final byte FACE_TOP = 0;
	public static final byte FACE_WEST = 1;
	public static final byte FACE_EAST = 2;
	public static final byte FACE_NORTH = 3;
	public static final byte FACE_SOUTH = 4;

	// Ticks a fleck lasts: at least this, then a random number more
	static final int MIN_LIFETIME = 30;
	static final int LIFETIME_RANGE = 40;
	// The share of its life it lies there whole; for the rest it melts: fading, and shrinking to MELTED_SIZE of itself
	static final float WHOLE_SHARE = 0.4F;
	static final float MELTED_SIZE = 0.5F;
	// Just off the surface, and a little apart from each other, so neither the surface nor other flecks show through
	static final float LIFT = 0.006F;
	static final float LIFT_RANGE = 0.004F;

	private static final float TWO_PI = (float) (Math.PI * 2D);

	private final int capacity;
	final double[] x, z;
	final float[] y;
	// The cos and sin of the angle it lies at
	final float[] turnCos, turnSin;
	// How big the flake was, 0 to 1, and which flake it was drawn as, 0 to 1 (see PrecipitationField.pick)
	final float[] size, variant;
	final int[] age, lifetime, light;
	// The face it lies on (see FACE_TOP)
	final byte[] face;
	int count;

	public SnowFlecks(final int capacity) {
		this.capacity = capacity;
		this.x = new double[capacity];
		this.y = new float[capacity];
		this.z = new double[capacity];
		this.turnCos = new float[capacity];
		this.turnSin = new float[capacity];
		this.size = new float[capacity];
		this.variant = new float[capacity];
		this.age = new int[capacity];
		this.lifetime = new int[capacity];
		this.light = new int[capacity];
		this.face = new byte[capacity];
	}

	public int count() {
		return this.count;
	}

	public void clear() {
		this.count = 0;
	}

	/**
	 * Adds a fleck where a snowflake landed on top of something, at a random angle
	 *
	 * @param surfaceY the height of the surface it landed on
	 * @param size     how big the flake was, 0 to 1
	 * @param variant  which flake it was drawn as, 0 to 1
	 * @param light    its packed light
	 */
	public void add(final double x, final double surfaceY, final double z, final float size, final float variant,
			final int light, @Nonnull final Random random) {
		add(x, surfaceY, z, FACE_TOP, size, variant, light, random);
	}

	/**
	 * Adds a fleck where a snowflake landed on a face of a block, at a random angle
	 *
	 * @param x    where on the face: for the top, its height is {@code y}; for a side, the side is at {@code x} (west
	 *             or east) or {@code z} (north or south)
	 * @param face the face (see {@link #FACE_TOP})
	 */
	public void add(final double x, final double y, final double z, final byte face, final float size,
			final float variant, final int light, @Nonnull final Random random) {
		if (this.count == this.capacity)
			return;
		final int i = this.count++;
		final float angle = random.nextFloat() * TWO_PI;
		final float lift = LIFT + random.nextFloat() * LIFT_RANGE;
		this.x[i] = x + normalX(face) * lift;
		this.y[i] = (float) y + (face == FACE_TOP ? lift : 0F);
		this.z[i] = z + normalZ(face) * lift;
		this.face[i] = face;
		this.turnCos[i] = MathHelper.cos(angle);
		this.turnSin[i] = MathHelper.sin(angle);
		this.size[i] = size;
		this.variant[i] = variant;
		this.age[i] = 0;
		this.lifetime[i] = MIN_LIFETIME + random.nextInt(LIFETIME_RANGE);
		this.light[i] = light;
	}

	/**
	 * Ages the flecks, dropping those that have melted
	 */
	public void tick() {
		int i = 0;
		while (i < this.count) {
			if (++this.age[i] >= this.lifetime[i]) {
				final int last = --this.count;
				this.x[i] = this.x[last];
				this.y[i] = this.y[last];
				this.z[i] = this.z[last];
				this.turnCos[i] = this.turnCos[last];
				this.turnSin[i] = this.turnSin[last];
				this.size[i] = this.size[last];
				this.variant[i] = this.variant[last];
				this.age[i] = this.age[last];
				this.lifetime[i] = this.lifetime[last];
				this.light[i] = this.light[last];
				this.face[i] = this.face[last];
			} else {
				i++;
			}
		}
	}

	/**
	 * Which way a face looks along x: -1 for west, 1 for east, otherwise 0
	 */
	static int normalX(final byte face) {
		return face == FACE_WEST ? -1 : face == FACE_EAST ? 1 : 0;
	}

	/**
	 * Which way a face looks along z: -1 for north, 1 for south, otherwise 0
	 */
	static int normalZ(final byte face) {
		return face == FACE_NORTH ? -1 : face == FACE_SOUTH ? 1 : 0;
	}

	/**
	 * How much of a fleck is left, 0 to 1: all of it at first, then less as it melts, none once it has
	 */
	float remaining(final int i, final float partialTick) {
		return remainingForAge(this.age[i] + partialTick, this.lifetime[i]);
	}

	static float remainingForAge(final float age, final int lifetime) {
		if (age >= lifetime)
			return 0F;
		final float melting = (age / lifetime - WHOLE_SHARE) / (1F - WHOLE_SHARE);
		return 1F - MathHelper.clamp(melting, 0F, 1F);
	}

	/**
	 * How big a fleck is drawn, as a share of the flake it fell as, for how much of it is left
	 */
	static float sizeFor(final float remaining) {
		return MELTED_SIZE + (1F - MELTED_SIZE) * remaining;
	}
}
