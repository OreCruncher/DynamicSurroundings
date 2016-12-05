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

package org.blockartistry.mod.DynSurround.data;

import java.util.ArrayList;
import java.util.List;

import org.blockartistry.mod.DynSurround.util.Color;
import org.blockartistry.mod.DynSurround.util.XorShiftRandom;

import net.minecraft.util.math.MathHelper;

/**
 * Preset color pairs for generating aurora colors.
 */
public final class ColorPair {

	/**
	 * Color that forms the base of the aurora and is the
	 * brightest.
	 */
	public final Color baseColor;
	
	/**
	 * Color that forms the top of the aurora and usually
	 * fades to black.
	 */
	public final Color fadeColor;

	private static final List<ColorPair> PAIRS = new ArrayList<ColorPair>();

	static {
		PAIRS.add(new ColorPair(new Color(0x0, 0xff, 0x99), new Color(0x33, 0xff, 0x00)));
		PAIRS.add(new ColorPair(Color.BLUE, Color.GREEN));
		PAIRS.add(new ColorPair(Color.TURQOISE, Color.LGREEN));
		PAIRS.add(new ColorPair(Color.YELLOW, Color.RED));
		PAIRS.add(new ColorPair(Color.NAVY, Color.INDIGO));
		PAIRS.add(new ColorPair(Color.GREEN, Color.YELLOW));
		PAIRS.add(new ColorPair(Color.MAGENTA, Color.GREEN));
		PAIRS.add(new ColorPair(Color.INDIGO, Color.GREEN));
		PAIRS.add(new ColorPair(Color.CYAN, Color.MAGENTA));
	}

	private ColorPair(final Color base, final Color fade) {
		this.baseColor = base;
		this.fadeColor = fade;
	}

	public static ColorPair get(final int id) {
		return PAIRS.get(MathHelper.clamp_int(id, 0, PAIRS.size() - 1));
	}

	public static int randomId() {
		return XorShiftRandom.shared.nextInt(PAIRS.size());
	}

	public static int testId() {
		return PAIRS.size() - 1;
	}
}
