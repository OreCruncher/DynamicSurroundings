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
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;

import net.minecraft.util.math.MathHelper;

/**
 * Preset geometry of an Aurora. A preset is selected by the server when an
 * Aurora spawns.
 */
public final class AuroraPreset {

	public final int length;
	public final float nodeLength;
	public final float nodeWidth;
	public final int bandOffset;
	public final int alphaLimit;

	private static final List<AuroraPreset> PRESET = new ArrayList<AuroraPreset>();

	static {
		// 10/5; 90/45
		PRESET.add(new AuroraPreset(128, 30.0F, 2.0F, 45, 48));
		PRESET.add(new AuroraPreset(128, 15.0F, 2.0F, 27, 48));
		PRESET.add(new AuroraPreset(64, 30.0F, 2.0F, 45, 48));
		PRESET.add(new AuroraPreset(64, 15.0F, 2.0F, 27, 48));

		PRESET.add(new AuroraPreset(128, 30.0F, 2.0F, 45, 32));
		PRESET.add(new AuroraPreset(128, 15.0F, 2.0F, 27, 32));
		PRESET.add(new AuroraPreset(64, 30.0F, 2.0F, 45, 32));
		PRESET.add(new AuroraPreset(64, 15.0F, 2.0F, 27, 32));

		PRESET.add(new AuroraPreset(128, 30.0F, 2.0F, 45, 16));
		PRESET.add(new AuroraPreset(128, 15.0F, 2.0F, 27, 16));
		PRESET.add(new AuroraPreset(64, 30.0F, 2.0F, 45, 16));
		PRESET.add(new AuroraPreset(64, 15.0F, 2.0F, 27, 16));
	}

	private AuroraPreset(final int length, final float nodeLength, final float nodeWidth, final int bandOffset,
			final int alphaLimit) {
		this.length = length;
		this.nodeLength = nodeLength;
		this.nodeWidth = nodeWidth;
		this.bandOffset = bandOffset;
		this.alphaLimit = alphaLimit;
	}

	@Nonnull
	public static AuroraPreset get(final int id) {
		return PRESET.get(MathHelper.clamp_int(id, 0, PRESET.size() - 1));
	}

	public static int randomId() {
		return ThreadLocalRandom.current().nextInt(PRESET.size());
	}

	public static int testId() {
		return PRESET.size() - 1;
	}

	@Override
	@Nonnull
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("bandLength:").append(this.length);
		builder.append(";nodeLength:").append(this.nodeLength);
		builder.append(";nodeWidth:").append(this.nodeWidth);
		builder.append(";bandOffset:").append(this.bandOffset);
		builder.append(";alphaLimit:").append(this.alphaLimit);
		return builder.toString();
	}
}
