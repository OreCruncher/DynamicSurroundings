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

import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;

/**
 * Holds an RGB triple. See: http://www.rapidtables.com/web/color/RGB_Color.htm
 */
public class Color {

	public static final class ImmutableColor extends Color {

		ImmutableColor(@Nonnull final Color color) {
			super(color);
		}

		ImmutableColor(final int red, final int green, final int blue) {
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
		public Color adjust(@Nonnull final Vec3d adjust, @Nonnull final Color target) {
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
	public static final Color GRAY = new ImmutableColor(128, 128, 128);
	public static final Color LGRAY = new ImmutableColor(192, 192, 192);
	public static final Color SLATEGRAY = new ImmutableColor(112, 128, 144);
	public static final Color DARKSLATEGRAY = new ImmutableColor(47, 79, 79);

	// Minecraft colors mapped to codes
	public static final Color MC_BLACK = new ImmutableColor(0, 0, 0);
	public static final Color MC_DARKBLUE = new ImmutableColor(0, 0, 170);
	public static final Color MC_DARKGREEN = new ImmutableColor(0, 170, 0);
	public static final Color MC_DARKAQUA = new ImmutableColor(0, 170, 170);
	public static final Color MC_DARKRED = new ImmutableColor(170, 0, 0);
	public static final Color MC_DARKPURPLE = new ImmutableColor(170, 0, 170);
	public static final Color MC_GOLD = new ImmutableColor(255, 170, 0);
	public static final Color MC_GRAY = new ImmutableColor(170, 170, 170);
	public static final Color MC_DARKGRAY = new ImmutableColor(85, 85, 85);
	public static final Color MC_BLUE = new ImmutableColor(85, 85, 255);
	public static final Color MC_GREEN = new ImmutableColor(85, 255, 85);
	public static final Color MC_AQUA = new ImmutableColor(85, 255, 255);
	public static final Color MC_RED = new ImmutableColor(255, 85, 85);
	public static final Color MC_LIGHTPURPLE = new ImmutableColor(255, 85, 255);
	public static final Color MC_YELLOW = new ImmutableColor(255, 255, 85);
	public static final Color MC_WHITE = new ImmutableColor(255, 255, 255);

	private static final Map<TextFormatting, Color> colorLookup = new IdentityHashMap<TextFormatting, Color>();
	static {
		colorLookup.put(TextFormatting.BLACK, MC_BLACK);
		colorLookup.put(TextFormatting.DARK_BLUE, MC_DARKBLUE);
		colorLookup.put(TextFormatting.DARK_GREEN, MC_DARKGREEN);
		colorLookup.put(TextFormatting.DARK_AQUA, MC_DARKAQUA);
		colorLookup.put(TextFormatting.DARK_RED, MC_DARKRED);
		colorLookup.put(TextFormatting.DARK_PURPLE, MC_DARKPURPLE);
		colorLookup.put(TextFormatting.GOLD, MC_GOLD);
		colorLookup.put(TextFormatting.GRAY, MC_GRAY);
		colorLookup.put(TextFormatting.DARK_GRAY, MC_DARKGRAY);
		colorLookup.put(TextFormatting.BLUE, MC_BLUE);
		colorLookup.put(TextFormatting.GREEN, MC_GREEN);
		colorLookup.put(TextFormatting.AQUA, MC_AQUA);
		colorLookup.put(TextFormatting.RED, MC_RED);
		colorLookup.put(TextFormatting.LIGHT_PURPLE, MC_LIGHTPURPLE);
		colorLookup.put(TextFormatting.YELLOW, MC_YELLOW);
		colorLookup.put(TextFormatting.WHITE, MC_WHITE);
	}

	public static Color getColor(final TextFormatting format) {
		return colorLookup.get(format);
	}

	public float red;
	public float green;
	public float blue;

	public Color(@Nonnull final Color color) {
		this(color.red, color.green, color.blue);
	}

	public Color(final int red, final int green, final int blue) {
		this(red / 255.0F, green / 255.0F, blue / 255.0F);
	}

	public Color(@Nonnull final Vec3d vec) {
		this((float) vec.xCoord, (float) vec.yCoord, (float) vec.zCoord);
	}

	public Color(final float red, final float green, final float blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	@Nonnull
	public Vec3d toVec3d() {
		return new Vec3d(this.red, this.green, this.blue);
	}

	/*
	 * Calculates the RGB adjustments to make to the color to arrive at the
	 * target color after the specified number of iterations.
	 */
	@Nonnull
	public Vec3d transitionTo(@Nonnull final Color target, final int iterations) {
		final double deltaRed = (target.red - this.red) / iterations;
		final double deltaGreen = (target.green - this.green) / iterations;
		final double deltaBlue = (target.blue - this.blue) / iterations;
		return new Vec3d(deltaRed, deltaGreen, deltaBlue);
	}

	@Nonnull
	public Color scale(final float scaleFactor) {
		this.red *= scaleFactor;
		this.green *= scaleFactor;
		this.blue *= scaleFactor;
		return this;
	}

	@Nonnull
	public static Color scale(@Nonnull final Color color, final float scaleFactor) {
		return new Color(color).scale(scaleFactor);
	}

	@Nonnull
	public Color add(@Nonnull final Color color) {
		this.red += color.red;
		this.green += color.green;
		this.blue += color.blue;
		return this;
	}

	private static float blend(final float c1, final float c2, final float factor) {
		return (float) Math.sqrt((1.0F - factor) * c1 * c1 + factor * c2 * c2);
	}

	@Nonnull
	public Color blend(@Nonnull final Color color, final float factor) {
		this.red = blend(this.red, color.red, factor);
		this.green = blend(this.green, color.green, factor);
		this.blue = blend(this.blue, color.blue, factor);
		return this;
	}

	@Nonnull
	public Color mix(@Nonnull final Color color) {
		return mix(color.red, color.green, color.blue);
	}

	@Nonnull
	public Color mix(final float red, final float green, final float blue) {
		this.red = (this.red + red) / 2.0F;
		this.green = (this.green + green) / 2.0F;
		this.blue = (this.blue + blue) / 2.0F;
		return this;
	}

	@Nonnull
	public Color adjust(@Nonnull final Vec3d adjust, @Nonnull final Color target) {
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

	// Adjust luminance based on the specified percent. > 0 brightens; < 0
	// darkens
	@Nonnull
	public Color luminance(final float percent) {
		final float r = Math.min(Math.max(0, this.red + (this.red * percent)), 1.0F);
		final float g = Math.min(Math.max(0, this.green + (this.green * percent)), 1.0F);
		final float b = Math.min(Math.max(0, this.blue + (this.blue * percent)), 1.0F);
		return new Color(r, g, b);
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

	@Nonnull
	public Color asImmutable() {
		return new ImmutableColor(this);
	}

	@Override
	@Nonnull
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("[r:").append((int) (this.red * 255));
		builder.append(",g:").append((int) (this.green * 255));
		builder.append(",b:").append((int) (this.blue * 255));
		builder.append(']');
		return builder.toString();
	}
}
