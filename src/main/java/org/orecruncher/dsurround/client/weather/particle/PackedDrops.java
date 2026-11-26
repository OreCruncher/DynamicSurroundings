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

import net.minecraft.util.math.MathHelper;

/**
 * The drops drawn, packed for building their quads: one record of floats a drop, all a drop's values side by side, in
 * a single array. The field keeps its drops in an array a value ({@link PrecipitationField.Drops}), which suits the
 * step, which works on a few values at a time; the build reads nearly all of them for every drop, every frame, and from
 * a dozen arrays it waited on memory more than it worked. Packed once a tick, by the first build after the drops
 * changed, and read by every frame's build until they change again.
 * <p>
 * Positions are relative to an anchor near the camera as it was packed, as floats: where the drop was last tick, and
 * how far it moved since. Rain and snow come first, then the dust, each in the order the drops are in, so each is
 * built from its own part. What only changes with the drop is worked out here: sizes, which look it takes, the way a
 * rain drop falls.
 * <p>
 * Packed by the first frame's build after the drops change. The 26.x line builds on a thread of its own as well as the
 * render thread, hence the lock; here only the render thread builds.
 */
final class PackedDrops {

	// Where each value is in a drop's record
	static final int PREV_X = 0, PREV_Y = 1, PREV_Z = 2;
	static final int MOVE_X = 3, MOVE_Y = 4, MOVE_Z = 5;
	static final int KIND = 6;
	// Packed light, as a whole number (see whole)
	static final int LIGHT = 7;
	static final int AGE = 8, COVERED = 9, LIFETIME = 10;
	static final int THINNING = 11;
	// Its kind's size, 0 to 1 (see PrecipitationField.rainSize and the others)
	static final int SIZE = 12;
	// Which look it takes, 0 to 1, times the number of looks (see PrecipitationField.variant)
	static final int PICK = 13;
	// Rain: the way it falls, a unit vector. Snow and dust: the cos and sin of its turn; dust: its color, as a whole
	// number (see whole).
	static final int A = 14, B = 15, C = 16;
	// 17 values (the 26.x line pads them to 20 for its GPU drops, which this line doesn't have)
	static final int STRIDE = 17;

	/**
	 * A packed light or an RGB color as a float holding it as a whole number, which a float keeps exactly up to 2^24
	 * (packed light reaches 0xF000F0). The shaders take it back with int(). Not the int's bits as a float: light below
	 * sky 8, or a color with little red, made a denormal of them, which a GPU may read as 0.
	 */
	static float whole(int value) {
		return (float) (value & 0xFFFFFF);
	}

	float[] data = new float[0];
	// Rain and snow are records [0, solid), dust [solid, solid + dust)
	int solid;
	int dust;
	double anchorX, anchorY, anchorZ;
	private boolean stale = true;

	/**
	 * Says the drops changed: the next build packs them again
	 */
	synchronized void invalidate() {
		this.stale = true;
	}

	/**
	 * Packs the drops, if they changed since they were last, around a camera at the given position
	 */
	synchronized void packIfStale(PrecipitationField.Drops d, double cameraX, double cameraY, double cameraZ) {
		if (!this.stale)
			return;
		int needed = d.count * STRIDE;
		if (this.data.length < needed)
			this.data = new float[Math.max(needed, this.data.length + this.data.length / 2)];
		this.anchorX = MathHelper.floor(cameraX);
		this.anchorY = MathHelper.floor(cameraY);
		this.anchorZ = MathHelper.floor(cameraZ);
		int at = 0;
		for (int i = 0; i < d.count; i++) {
			if (d.kind[i] != PrecipitationField.DUST)
				at = this.pack(d, i, at);
		}
		this.solid = at / STRIDE;
		for (int i = 0; i < d.count; i++) {
			if (d.kind[i] == PrecipitationField.DUST)
				at = this.pack(d, i, at);
		}
		this.dust = at / STRIDE - this.solid;
		this.stale = false;
	}

	private int pack(PrecipitationField.Drops d, int i, int at) {
		final float[] r = this.data;
		byte kind = d.kind[i];
		r[at + PREV_X] = (float) (d.prevX[i] - this.anchorX);
		r[at + PREV_Y] = (float) (d.prevY[i] - this.anchorY);
		r[at + PREV_Z] = (float) (d.prevZ[i] - this.anchorZ);
		r[at + MOVE_X] = (float) (d.x[i] - d.prevX[i]);
		r[at + MOVE_Y] = d.y[i] - d.prevY[i];
		r[at + MOVE_Z] = (float) (d.z[i] - d.prevZ[i]);
		r[at + KIND] = kind;
		r[at + LIGHT] = whole(d.light[i]);
		r[at + AGE] = d.age[i];
		r[at + COVERED] = d.covered[i];
		r[at + LIFETIME] = d.lifetime[i];
		r[at + THINNING] = d.thinning[i];
		r[at + PICK] = PrecipitationField.pick(d, i);
		if (kind == PrecipitationField.RAIN) {
			r[at + SIZE] = PrecipitationField.rainSize(d, i);
			final float vx = d.windX[i], vy = -d.speed[i], vz = d.windZ[i];
			final float inverse = (float) (1D / Math.sqrt(vx * vx + vy * vy + vz * vz));
			r[at + A] = vx * inverse;
			r[at + B] = vy * inverse;
			r[at + C] = vz * inverse;
		} else if (kind == PrecipitationField.SNOW) {
			r[at + SIZE] = PrecipitationField.snowSize(d, i);
			r[at + A] = d.spinCos[i];
			r[at + B] = d.spinSin[i];
			r[at + C] = 0F;
		} else {
			r[at + SIZE] = PrecipitationField.dustSize(d, i);
			r[at + A] = d.spinCos[i];
			r[at + B] = d.spinSin[i];
			r[at + C] = whole(d.color[i]);
		}
		return at + STRIDE;
	}

	/**
	 * Which of {@code variants} looks a packed drop takes, as {@link PrecipitationField#variant} picks it
	 */
	static int variant(float pick, int variants) {
		return MathHelper.clamp((int) (pick * variants), 0, variants - 1);
	}
}
