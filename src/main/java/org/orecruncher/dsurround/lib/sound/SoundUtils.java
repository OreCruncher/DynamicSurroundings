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
package org.orecruncher.dsurround.lib.sound;

import javax.annotation.Nonnull;

import net.minecraft.util.math.BlockPos;
import paulscode.sound.SoundSystemConfig;

public final class SoundUtils {

	private static final float DROPOFF = 16 * 16;

	public static boolean canBeHeard(@Nonnull final BlockPos pos1, @Nonnull final BlockPos pos2, final float volume) {
		if (volume <= 0.0F || SoundSystemConfig.getMasterGain() <= 0F)
			return false;

		// This is from SoundManager. The idea is that if a sound
		// is playing at <= 1F drop off range is about 16 blocks.
		// If the volume is higher the drop off range is further.
		// Note that we are dealing with square distances so the
		// math looks kinda funny.
		float dropoff = DROPOFF;
		if (volume > 1F)
			dropoff *= (volume * volume);

		final double distanceSq = pos1.distanceSq(pos2);
		return distanceSq <= dropoff;
	}
}
