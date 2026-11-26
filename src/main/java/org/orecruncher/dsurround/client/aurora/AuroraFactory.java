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

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Makes the aurora for a night, in the style the options say: classic or cinematic, both ported from the 26.x line.
 * When the style is changed, the aurora showing is swapped for the other style's, keeping its fade ({@link #restyle}).
 * Both need shaders; a cinematic aurora whose shaders can't draw is drawn classic.
 */
@SideOnly(Side.CLIENT)
public final class AuroraFactory {

	private AuroraFactory() {
	}

	@Nonnull
	public static IAurora produce(final long seed) {
		if (wantsCinematic())
			return new AuroraCinematic(seed);
		return new AuroraClassic(seed);
	}

	/**
	 * The aurora showing, or the same night's in the style the options say now if that differs, taking over its fade
	 * (so it switches where it is, without fading out and in again)
	 */
	@Nonnull
	public static IAurora restyle(@Nonnull final IAurora current) {
		final boolean cinematic = wantsCinematic();
		if (cinematic == current.isCinematic())
			return current;
		return cinematic ? new AuroraCinematic(current.getSeed(), current.getTracker())
				: new AuroraClassic(current.getSeed(), current.getTracker());
	}

	// Cinematic when the options say so and its shaders can draw
	private static boolean wantsCinematic() {
		return "cinematic".equalsIgnoreCase(ModOptions.aurora.auroraStyle) && CinematicAuroraRenderer.isAvailable();
	}
}
