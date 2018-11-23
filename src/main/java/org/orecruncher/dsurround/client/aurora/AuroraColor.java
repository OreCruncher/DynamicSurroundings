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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.lib.Color;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class AuroraColor {

	/**
	 * Color that forms the base of the aurora and is the brightest.
	 */
	public final Color baseColor;

	/**
	 * Color that forms the top of the aurora and usually fades to black.
	 */
	public final Color fadeColor;

	/**
	 * Mid-band color for aurora styles that use it.
	 */
	public final Color middleColor;

	private static final List<AuroraColor> COLOR_SETS = new ArrayList<>();

	private static final float WARMER = 0.3F;
	private static final float COOLER = -0.3F;

	static {
		COLOR_SETS.add(new AuroraColor(new Color(0x0, 0xff, 0x99), new Color(0x33, 0xff, 0x00)));
		COLOR_SETS.add(new AuroraColor(Color.BLUE, Color.GREEN));
		COLOR_SETS.add(new AuroraColor(Color.MAGENTA, Color.GREEN));
		COLOR_SETS.add(new AuroraColor(Color.INDIGO, Color.GREEN));
		COLOR_SETS.add(new AuroraColor(Color.TURQOISE, Color.LGREEN));
		COLOR_SETS.add(new AuroraColor(Color.YELLOW, Color.RED));
		COLOR_SETS.add(new AuroraColor(Color.GREEN, Color.RED));
		COLOR_SETS.add(new AuroraColor(Color.GREEN, Color.YELLOW));
		COLOR_SETS.add(new AuroraColor(Color.RED, Color.YELLOW));
		COLOR_SETS.add(new AuroraColor(Color.NAVY, Color.INDIGO));
		COLOR_SETS.add(new AuroraColor(Color.CYAN, Color.MAGENTA));
		COLOR_SETS.add(new AuroraColor(Color.AURORA_GREEN, Color.AURORA_RED, Color.AURORA_BLUE));

		// Warmer versions
		COLOR_SETS.add(new AuroraColor(Color.YELLOW.luminance(WARMER).asImmutable(),
				Color.RED.luminance(WARMER).asImmutable()));
		COLOR_SETS.add(new AuroraColor(Color.GREEN.luminance(WARMER).asImmutable(),
				Color.RED.luminance(WARMER).asImmutable()));
		COLOR_SETS.add(new AuroraColor(Color.GREEN.luminance(WARMER).asImmutable(),
				Color.YELLOW.luminance(WARMER).asImmutable()));
		COLOR_SETS.add(new AuroraColor(Color.BLUE.luminance(WARMER).asImmutable(),
				Color.GREEN.luminance(WARMER).asImmutable()));
		COLOR_SETS.add(new AuroraColor(Color.INDIGO.luminance(WARMER).asImmutable(),
				Color.GREEN.luminance(WARMER).asImmutable()));
		COLOR_SETS.add(new AuroraColor(Color.AURORA_GREEN.luminance(WARMER).asImmutable(),
				Color.AURORA_RED.luminance(WARMER).asImmutable(), Color.AURORA_BLUE.luminance(WARMER).asImmutable()));

		// Cooler versions
		COLOR_SETS.add(new AuroraColor(Color.YELLOW.luminance(COOLER).asImmutable(),
				Color.RED.luminance(COOLER).asImmutable()));
		COLOR_SETS.add(new AuroraColor(Color.GREEN.luminance(COOLER).asImmutable(),
				Color.RED.luminance(COOLER).asImmutable()));
		COLOR_SETS.add(new AuroraColor(Color.GREEN.luminance(COOLER).asImmutable(),
				Color.YELLOW.luminance(COOLER).asImmutable()));
		COLOR_SETS.add(new AuroraColor(Color.BLUE.luminance(COOLER).asImmutable(),
				Color.GREEN.luminance(COOLER).asImmutable()));
		COLOR_SETS.add(new AuroraColor(Color.INDIGO.luminance(COOLER).asImmutable(),
				Color.GREEN.luminance(COOLER).asImmutable()));
		COLOR_SETS.add(new AuroraColor(Color.AURORA_GREEN.luminance(COOLER).asImmutable(),
				Color.AURORA_RED.luminance(COOLER).asImmutable(), Color.AURORA_BLUE.luminance(COOLER).asImmutable()));

	}

	private AuroraColor(@Nonnull final Color base, @Nonnull final Color fade) {
		this(base, fade, base);
	}

	private AuroraColor(@Nonnull final Color base, @Nonnull final Color fade, @Nonnull final Color mid) {
		this.baseColor = base;
		this.fadeColor = fade;
		this.middleColor = mid;
	}

	@Nonnull
	public static AuroraColor get(@Nonnull final Random random) {
		final int idx = random.nextInt(COLOR_SETS.size());
		return COLOR_SETS.get(idx);
	}

	public static int testId() {
		return COLOR_SETS.size() - 1;
	}
}
