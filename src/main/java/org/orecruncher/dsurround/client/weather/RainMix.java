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
import net.minecraft.util.math.MathHelper;

/**
 * How one rain sound is played, from the 26.x line's (its RainMix and StormSounds): the mod's calm rain, louder as the
 * storm strengthens, a medium rain through middling storms and vanilla's heavier rain from a moderate storm up, each
 * at vanilla's volume and pitch for where it plays, scaled, so a drizzle patters, a steady rain hisses and a downpour
 * roars (vanilla's recordings are some 19 dB louder than the calm and medium rain, which make the bed under it). With
 * rain on surfaces it sounds like what it lands on: at the surface's volume and pitch (see {@link RainSurface}), with
 * the surface's own rain in place of part of all three. A single sound (vanilla's rain by option, or dust) is played
 * as it is handed in, the surface's rain in place of part of it.
 */
public final class RainMix {

	/**
	 * Vanilla's volume for its rain sound: in the open, and on a roof above the player (with half the pitch)
	 */
	public static final float VANILLA_VOLUME = 0.2F;
	public static final float VANILLA_VOLUME_ABOVE = 0.1F;
	/**
	 * The calm rain's volume in the lightest drizzle, as a share of vanilla's; it rises to all of it at full strength
	 */
	static final float QUIETEST = 0.5F;
	/**
	 * Vanilla's heavier rain comes in from this strength, and is at its own volume from {@link #HEAVY_FULL}
	 */
	static final float HEAVY_FROM = 0.5F;
	static final float HEAVY_FULL = 0.9F;
	/**
	 * The medium rain comes in from {@link #MEDIUM_FROM}, is at vanilla's volume from {@link #MEDIUM_FULL}, and gives
	 * way to vanilla's heavier rain from {@link #MEDIUM_FADE}, gone at full strength
	 */
	static final float MEDIUM_FROM = 0.2F;
	static final float MEDIUM_FULL = 0.5F;
	static final float MEDIUM_FADE = 0.7F;
	/**
	 * A surface's own rain (leaves, a tin roof) takes this share of the rain where it lands, the rain keeping the rest:
	 * played on top of all of it, it was lost in the wash (26.x's bench, 2026-11-21)
	 */
	static final float LAYER_SHARE = 0.7F;

	/**
	 * The sound handed in (the calm rain, in a blend), the medium rain and vanilla's heavier rain (0 for none), and
	 * their pitch
	 */
	public final float volume;
	public final float mediumVolume;
	public final float heavyVolume;
	public final float pitch;
	/**
	 * The surface's own rain, or null for none, and its volume; it is played as recorded, not pitched (a roof's lower
	 * pitch included)
	 */
	@Nullable
	public final ResourceLocation layer;
	public final float layerVolume;

	private RainMix(final float volume, final float mediumVolume, final float heavyVolume, final float pitch,
			@Nullable final ResourceLocation layer, final float layerVolume) {
		this.volume = volume;
		this.mediumVolume = mediumVolume;
		this.heavyVolume = heavyVolume;
		this.pitch = pitch;
		this.layer = layer;
		this.layerVolume = layerVolume;
	}

	/**
	 * The three rains for a sound
	 *
	 * @param surface   what the rain lands on, or null with rain on surfaces off
	 * @param volume    vanilla's volume where it plays ({@link #VANILLA_VOLUME}, {@link #VANILLA_VOLUME_ABOVE})
	 * @param pitch     its pitch (lower on a roof above the player)
	 * @param rainLevel the storm's strength, 0 to 1
	 */
	public static RainMix of(@Nullable final RainSurface surface, float volume, float pitch, final float rainLevel) {
		ResourceLocation layer = null;
		float layerVolume = 0F;
		if (surface != null) {
			volume *= surface.volume();
			pitch *= surface.pitch();
			layer = surface.layer();
			if (layer != null) {
				// Its share of all three, so it rises with the storm as they do
				layerVolume = volume * layerShare(rainLevel);
				volume *= 1F - LAYER_SHARE;
			}
		}
		return new RainMix(volume * calmShare(rainLevel), volume * mediumShare(rainLevel),
				volume * heavyShare(rainLevel), pitch, layer, layerVolume);
	}

	/**
	 * A single sound, played as it is handed in
	 */
	public static RainMix single(@Nullable final RainSurface surface, float volume, float pitch) {
		if (surface == null)
			return new RainMix(volume, 0F, 0F, pitch, null, 0F);
		volume *= surface.volume();
		pitch *= surface.pitch();
		final ResourceLocation layer = surface.layer();
		if (layer == null)
			return new RainMix(volume, 0F, 0F, pitch, null, 0F);
		return new RainMix(volume * (1F - LAYER_SHARE), 0F, 0F, pitch, layer, volume * LAYER_SHARE);
	}

	/**
	 * The calm rain's volume at a storm's strength, as a share of vanilla's
	 */
	static float calmShare(final float rainLevel) {
		final float t = MathHelper.clamp(rainLevel, 0F, 1F);
		return QUIETEST + (1F - QUIETEST) * t;
	}

	/**
	 * The medium rain, as a share of vanilla's volume: none in a drizzle, rising smoothly, then giving way as the
	 * downpour comes in
	 */
	static float mediumShare(final float rainLevel) {
		return smoothStep(MEDIUM_FROM, MEDIUM_FULL, rainLevel) * (1F - smoothStep(MEDIUM_FADE, 1F, rainLevel));
	}

	/**
	 * Vanilla's heavier rain, as a share of its own volume: none in light rain, rising smoothly
	 */
	static float heavyShare(final float rainLevel) {
		return smoothStep(HEAVY_FROM, HEAVY_FULL, rainLevel);
	}

	/**
	 * A surface's own rain, as a share of the rain's volume where it plays: its share of all three
	 */
	static float layerShare(final float rainLevel) {
		return LAYER_SHARE * (calmShare(rainLevel) + mediumShare(rainLevel) + heavyShare(rainLevel));
	}

	private static float smoothStep(final float from, final float to, final float x) {
		final float t = MathHelper.clamp((x - from) / (to - from), 0F, 1F);
		return t * t * (3F - 2F * t);
	}
}
