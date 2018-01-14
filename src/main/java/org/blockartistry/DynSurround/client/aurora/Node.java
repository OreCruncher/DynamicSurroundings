/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.blockartistry.DynSurround.client.aurora;

import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

import org.blockartistry.lib.MathStuff;

import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
final class Node {

	private static final float COS_DEG90_FACTOR = MathStuff.cos(MathStuff.PI_F / 2.0F);
	private static final float COS_DEG270_FACTOR = MathStuff.cos(MathStuff.PI_F / 2.0F + MathStuff.PI_F);
	private static final float SIN_DEG90_FACTOR = MathStuff.sin(MathStuff.PI_F / 2.0F);
	private static final float SIN_DEG270_FACTOR = MathStuff.sin(MathStuff.PI_F / 2.0F + MathStuff.PI_F);

	private float dZ = 0.0F;
	private float dY = 0.0F;

	private float cosDeg90 = 0.0F;
	private float cosDeg270 = 0.0F;
	private float sinDeg90 = 0.0F;
	private float sinDeg270 = 0.0F;

	public float angle;
	public float posX;
	public float posY;
	public float posZ;

	public float tetX = 0.0F;
	public float tetX2 = 0.0F;
	public float tetZ = 0.0F;
	public float tetZ2 = 0.0F;

	public Node(@Nonnull final Node template, final int offset) {
		final float rads = MathStuff.toRadians(90.0F + template.angle);
		this.posX = template.posX + MathStuff.cos(rads) * offset;
		this.posY = template.posY - 2.0F;
		this.posZ = template.posZ + MathStuff.sin(rads) * offset;
		this.angle = template.angle;
	}

	public Node(final float x, final float y, final float z, final float theta) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.angle = theta;
	}

	// -----------------------------------------------
	// Fast atan2:
	// http://dspguru.com/dsp/tricks/fixed-point-atan2-with-self-normalization
	private static final float COEFF_1 = (float) (Math.PI / 4.0F);
	private static final float COEFF_2 = COEFF_1 * 3.0F;
	private static final float CONST = 1e-10F;

	private static final float atan2_fast(final float y, final float x) {
		final float abs_y = MathStuff.abs(y) + CONST;
		final float angle;

		if (x >= 0.0F)
			angle = COEFF_1 - COEFF_1 * (x - abs_y) / (x + abs_y);
		else
			angle = COEFF_2 - COEFF_2 * (x + abs_y) / (abs_y - x);

		return y < 0.0 ? -angle : angle;
	}

	public void setDeltaZ(final float f) {
		this.dZ = f;
	}

	public void setDeltaY(final float f) {
		this.dY = f;
	}

	public float getModdedZ() {
		return this.posZ + this.dZ;
	}

	public float getModdedY() {
		final float y = this.posY + this.dY;
		return y < 0.0F ? 0.0F : y;
	}

	public void setWidth(final float w) {
		this.cosDeg270 = COS_DEG270_FACTOR * w;
		this.cosDeg90 = COS_DEG90_FACTOR * w;
		this.sinDeg270 = SIN_DEG270_FACTOR * w;
		this.sinDeg90 = SIN_DEG90_FACTOR * w;
	}

	public void findAngles(final Node next) {
		this.tetX = this.tetX2 = this.posX;
		this.tetZ = this.tetZ2 = this.getModdedZ();
		this.angle = 0.0F;
		if (next != null) {
			this.angle = atan2_fast(this.getModdedZ() - next.getModdedZ(), this.posX - next.posX);
			this.tetX += this.cosDeg90;
			this.tetX2 += this.cosDeg270;
			this.tetZ += this.sinDeg90;
			this.tetZ2 += this.sinDeg270;
		}
	}
}
