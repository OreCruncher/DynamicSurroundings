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

package org.blockartistry.mod.DynSurround.util;

import net.minecraft.util.math.Vec3d;

/**
 * Holds an RGB triple. See: http://www.rapidtables.com/web/color/RGB_Color.htm
 */
public class Color {

	private static class ImmutableColor extends Color {

		public ImmutableColor(final int red, final int green, final int blue) {
			super(red, green, blue);
		}

		@Override
		public Color scale(final float scaleFactor) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Color mix(final float red, final float green, final float blue) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Color adjust(final Vec3d adjust, final Color target) {
			throw new UnsupportedOperationException();
		}
	}

	public static final Color RED = new ImmutableColor(255, 0, 0);
	public static final Color ORANGE = new ImmutableColor(255, 127, 0);
	public static final Color YELLOW = new ImmutableColor(255, 255, 0);
	public static final Color LGREEN = new ImmutableColor(127, 255, 0);
	public static final Color GREEN = new ImmutableColor(0, 255, 0);
	public static final Color TURQOISE = new ImmutableColor(0, 255, 127);
	public static final Color CYAN = new ImmutableColor(0, 255, 255);
	public static final Color AUQUAMARINE = new ImmutableColor(0, 127, 255);
	public static final Color BLUE = new ImmutableColor(0, 0, 255);
	public static final Color VIOLET = new ImmutableColor(127, 0, 255);
	public static final Color MAGENTA = new ImmutableColor(255, 0, 255);
	public static final Color RASPBERRY = new ImmutableColor(255, 0, 127);
	public static final Color BLACK = new ImmutableColor(0, 0, 0);
	public static final Color WHITE = new ImmutableColor(255, 255, 255);
	public static final Color PURPLE = new ImmutableColor(80, 0, 80);
	public static final Color INDIGO = new ImmutableColor(75, 0, 130);
	public static final Color NAVY = new ImmutableColor(0, 0, 128);
	public static final Color TAN = new ImmutableColor(210, 180, 140);
	public static final Color GOLD = new ImmutableColor(255, 215, 0);

	public float red;
	public float green;
	public float blue;

	public Color(final Color color) {
		this(color.red, color.green, color.blue);
	}

	public Color(final int red, final int green, final int blue) {
		this(red / 255.0F, green / 255.0F, blue / 255.0F);
	}

	public Color(final Vec3d vec) {
		this((float) vec.xCoord, (float) vec.yCoord, (float) vec.zCoord);
	}

	public Color(final float red, final float green, final float blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public Vec3d toVec3d() {
		return new Vec3d(this.red, this.green, this.blue);
	}

	/*
	 * Calculates the RGB adjustments to make to the color to arrive at the
	 * target color after the specified number of iterations.
	 */
	public Vec3d transitionTo(final Color target, final int iterations) {
		final double deltaRed = (target.red - this.red) / iterations;
		final double deltaGreen = (target.green - this.green) / iterations;
		final double deltaBlue = (target.blue - this.blue) / iterations;
		return new Vec3d(deltaRed, deltaGreen, deltaBlue);
	}

	public Color scale(final float scaleFactor) {
		this.red *= scaleFactor;
		this.green *= scaleFactor;
		this.blue *= scaleFactor;
		return this;
	}

	public static Color scale(final Color color, final float scaleFactor) {
		return new Color(color).scale(scaleFactor);
	}

	public Color anaglyph() {
		final float r = (this.red * 30.0F + this.green * 59.0F + this.blue * 11.0F) / 100.0F;
		final float g = (this.red * 30.0F + this.green * 70.0F) / 100.0F;
		final float b = (this.red * 30.0F + this.blue * 70.0F) / 100.0F;
		this.red = r;
		this.green = g;
		this.blue = b;
		return this;
	}

	public static Color anaglyph(final Color color) {
		return new Color(color).anaglyph();
	}

	public Color add(final Color color) {
		this.red += color.red;
		this.green += color.green;
		this.blue += color.blue;
		return this;
	}

	private static float blend(final float c1, final float c2, final float factor) {
		return (float) Math.sqrt((1.0F - factor) * c1 * c1 + factor * c2 * c2);
	}

	public Color blend(final Color color, final float factor) {
		this.red = blend(this.red, color.red, factor);
		this.green = blend(this.green, color.green, factor);
		this.blue = blend(this.blue, color.blue, factor);
		return this;
	}

	public Color mix(final Color color) {
		return mix(color.red, color.green, color.blue);
	}

	public Color mix(final float red, final float green, final float blue) {
		this.red = (this.red + red) / 2.0F;
		this.green = (this.green + green) / 2.0F;
		this.blue = (this.blue + blue) / 2.0F;
		return this;
	}

	public Color adjust(final Vec3d adjust, final Color target) {
		this.red += adjust.xCoord;
		if ((adjust.xCoord < 0.0F && this.red < target.red) || (adjust.xCoord > 0.0F && this.red > target.red)) {
			this.red = target.red;
		}

		this.green += adjust.yCoord;
		if ((adjust.yCoord < 0.0F && this.green < target.green)
				|| (adjust.yCoord > 0.0F && this.green > target.green)) {
			this.green = target.green;
		}

		this.blue += adjust.zCoord;
		if ((adjust.zCoord < 0.0F && this.blue < target.blue) || (adjust.zCoord > 0.0F && this.blue > target.blue)) {
			this.blue = target.blue;
		}
		return this;
	}

	public int rgb() {
		final int iRed = (int) (this.red * 255);
		final int iGreen = (int) (this.green * 255);
		final int iBlue = (int) (this.blue * 255);
		return iRed << 16 | iGreen << 8 | iBlue;
	}

	public int rgbWithAlpha(final float alpha) {
		final int iAlpha = (int) (alpha * 255);
		return rgb() | (iAlpha << 24);
	}

	@Override
	public boolean equals(final Object anObject) {
		if (anObject == null || !(anObject instanceof Color))
			return false;
		final Color color = (Color) anObject;
		return this.red == color.red && this.green == color.green && this.blue == color.blue;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("[r:").append((int) (this.red * 255));
		builder.append(",g:").append((int) (this.green * 255));
		builder.append(",b:").append((int) (this.blue * 255));
		builder.append(']');
		return builder.toString();
	}
}
