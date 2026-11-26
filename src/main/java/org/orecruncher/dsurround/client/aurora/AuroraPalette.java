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

package org.orecruncher.dsurround.client.aurora;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

/**
 * The colors of an aurora, from its lower edge (the brightest part) up to where it fades out at the top. These are the
 * color sets of the original Dynamic Surroundings auroras, including its warmer and cooler versions of some. Ported
 * from the 26.x line.
 */
public final class AuroraPalette {

	/**
	 * A color, each channel 0 to 1
	 */
	public static final class Rgb {
		public final float red;
		public final float green;
		public final float blue;

		public Rgb(final float red, final float green, final float blue) {
			this.red = red;
			this.green = green;
			this.blue = blue;
		}

		@Nonnull
		static Rgb of(final int rgb) {
			return new Rgb(((rgb >> 16) & 0xFF) / 255F, ((rgb >> 8) & 0xFF) / 255F, (rgb & 0xFF) / 255F);
		}

		/**
		 * Brighter (positive) or darker (negative): each channel changed by the fraction {@code percent} of itself
		 */
		@Nonnull
		Rgb luminance(final float percent) {
			return new Rgb(scale(this.red, percent), scale(this.green, percent), scale(this.blue, percent));
		}

		private static float scale(final float channel, final float percent) {
			return Math.max(0F, Math.min(1F, channel + channel * percent));
		}

		@Override
		public boolean equals(final Object o) {
			if (!(o instanceof Rgb))
				return false;
			final Rgb c = (Rgb) o;
			return this.red == c.red && this.green == c.green && this.blue == c.blue;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.red, this.green, this.blue);
		}
	}

	// The colors the original palettes were made from
	private static final Rgb RED = Rgb.of(0xFF0000);
	private static final Rgb YELLOW = Rgb.of(0xFFFF00);
	private static final Rgb LIGHT_GREEN = Rgb.of(0x7FFF00);
	private static final Rgb GREEN = Rgb.of(0x00FF00);
	private static final Rgb TURQUOISE = Rgb.of(0x00FF7F);
	private static final Rgb CYAN = Rgb.of(0x00FFFF);
	private static final Rgb BLUE = Rgb.of(0x0000FF);
	private static final Rgb MAGENTA = Rgb.of(0xFF00FF);
	private static final Rgb INDIGO = Rgb.of(0x4B0082);
	private static final Rgb NAVY = Rgb.of(0x000080);
	private static final Rgb AURORA_RED = new Rgb(1F, 0F, 0F);
	private static final Rgb AURORA_GREEN = new Rgb(0.5F, 1F, 0F);
	private static final Rgb AURORA_BLUE = new Rgb(0F, 0.8F, 1F);
	private static final float WARMER = 0.3F;
	private static final float COOLER = -0.3F;

	public static final List<AuroraPalette> PALETTES;

	static {
		final List<AuroraPalette> palettes = new ArrayList<>();
		palettes.add(of(Rgb.of(0x00FF99), Rgb.of(0x33FF00)));
		palettes.add(of(BLUE, GREEN));
		palettes.add(of(MAGENTA, GREEN));
		palettes.add(of(INDIGO, GREEN));
		palettes.add(of(TURQUOISE, LIGHT_GREEN));
		palettes.add(of(YELLOW, RED));
		palettes.add(of(GREEN, RED));
		palettes.add(of(GREEN, YELLOW));
		palettes.add(of(RED, YELLOW));
		palettes.add(of(NAVY, INDIGO));
		palettes.add(of(CYAN, MAGENTA));
		palettes.add(new AuroraPalette(AURORA_GREEN, AURORA_BLUE, AURORA_RED));
		// Warmer and cooler versions of some of them
		final List<AuroraPalette> varied = ImmutableList.of(of(YELLOW, RED), of(GREEN, RED), of(GREEN, YELLOW),
				of(BLUE, GREEN), of(INDIGO, GREEN), new AuroraPalette(AURORA_GREEN, AURORA_BLUE, AURORA_RED));
		for (final AuroraPalette palette : varied)
			palettes.add(palette.luminance(WARMER));
		for (final AuroraPalette palette : varied)
			palettes.add(palette.luminance(COOLER));
		// Brightening an already saturated color set changes nothing, so a few of the warmer versions are the same as
		// the originals; drop those, rather than have them make their originals twice as likely
		final Set<AuroraPalette> distinct = new LinkedHashSet<>(palettes);
		PALETTES = ImmutableList.copyOf(distinct);
	}

	public final Rgb bottom;
	public final Rgb middle;
	public final Rgb top;

	public AuroraPalette(@Nonnull final Rgb bottom, @Nonnull final Rgb middle, @Nonnull final Rgb top) {
		this.bottom = bottom;
		this.middle = middle;
		this.top = top;
	}

	/**
	 * A palette that blends from {@code base} at the bottom to {@code fade} at the top; the middle is the base color,
	 * as in the original
	 */
	@Nonnull
	private static AuroraPalette of(@Nonnull final Rgb base, @Nonnull final Rgb fade) {
		return new AuroraPalette(base, base, fade);
	}

	@Nonnull
	AuroraPalette luminance(final float percent) {
		return new AuroraPalette(this.bottom.luminance(percent), this.middle.luminance(percent),
				this.top.luminance(percent));
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof AuroraPalette))
			return false;
		final AuroraPalette p = (AuroraPalette) o;
		return this.bottom.equals(p.bottom) && this.middle.equals(p.middle) && this.top.equals(p.top);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.bottom, this.middle, this.top);
	}
}
