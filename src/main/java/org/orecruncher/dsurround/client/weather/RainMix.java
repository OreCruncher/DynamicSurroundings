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

package org.orecruncher.dsurround.client.weather;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

/**
 * How one rain sound is played to sound like what it lands on, from the 26.x line's: at the surface's volume and pitch
 * (see {@link RainSurface}), and with the surface's own rain in place of part of it. 26.x plays three rains under a
 * sound (calm, medium and heavy) and the surface's rain takes its share of all three; 1.12.2 plays one, whose volume
 * already follows the storm, so its share is of that one.
 */
public final class RainMix {

	/**
	 * A surface's own rain (leaves, a tin roof) takes this share of the rain where it lands, the rain keeping the rest:
	 * played on top of all of it, it was lost in the wash (26.x's bench, 2026-11-21)
	 */
	static final float LAYER_SHARE = 0.7F;

	/**
	 * The rain's volume and pitch
	 */
	public final float volume;
	public final float pitch;
	/**
	 * The surface's own rain, or null for none, and its volume; it is played as recorded, not pitched (a roof's lower
	 * pitch included)
	 */
	@Nullable
	public final ResourceLocation layer;
	public final float layerVolume;

	private RainMix(final float volume, final float pitch, @Nullable final ResourceLocation layer,
			final float layerVolume) {
		this.volume = volume;
		this.pitch = pitch;
		this.layer = layer;
		this.layerVolume = layerVolume;
	}

	/**
	 * @param surface what the rain lands on, or null with rain on surfaces off
	 * @param volume  the rain sound's volume where it plays
	 * @param pitch   its pitch (lower on a roof above the player)
	 */
	public static RainMix of(@Nullable final RainSurface surface, float volume, float pitch) {
		if (surface == null)
			return new RainMix(volume, pitch, null, 0F);
		volume *= surface.volume();
		pitch *= surface.pitch();
		final ResourceLocation layer = surface.layer();
		if (layer == null)
			return new RainMix(volume, pitch, null, 0F);
		return new RainMix(volume * (1F - LAYER_SHARE), pitch, layer, volume * LAYER_SHARE);
	}
}
