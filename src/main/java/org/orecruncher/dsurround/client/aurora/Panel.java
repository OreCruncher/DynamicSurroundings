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

package org.orecruncher.dsurround.client.aurora;

import javax.annotation.Nonnull;

import org.orecruncher.lib.math.MathStuff;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
final class Panel {

	private static final float COS_DEG90_FACTOR = MathStuff.cos(MathStuff.PI_F / 2.0F);
	private static final float COS_DEG270_FACTOR = MathStuff.cos(MathStuff.PI_F / 2.0F + MathStuff.PI_F);
	private static final float SIN_DEG90_FACTOR = MathStuff.sin(MathStuff.PI_F / 2.0F);
	private static final float SIN_DEG270_FACTOR = MathStuff.sin(MathStuff.PI_F / 2.0F + MathStuff.PI_F);

	public float dZ = 0.0F;
	public float dY = 0.0F;

	public float cosDeg90 = 0.0F;
	public float cosDeg270 = 0.0F;
	public float sinDeg90 = 0.0F;
	public float sinDeg270 = 0.0F;

	public float angle;
	public float posX;
	public float posY;
	public float posZ;

	public float tetX = 0.0F;
	public float tetX2 = 0.0F;
	public float tetZ = 0.0F;
	public float tetZ2 = 0.0F;

	public Panel(@Nonnull final Panel template, final int offset) {
		final float rads = MathStuff.toRadians(90.0F + template.angle);
		this.posX = template.posX + MathStuff.cos(rads) * offset;
		this.posY = template.posY - 2.0F;
		this.posZ = template.posZ + MathStuff.sin(rads) * offset;
		this.angle = template.angle;
	}

	public Panel(final float x, final float y, final float z, final float theta) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.angle = theta;
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

}
