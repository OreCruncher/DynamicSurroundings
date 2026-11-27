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

package org.orecruncher.dsurround.registry.acoustics;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.client.footsteps.ConfigOptions;

import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Acoustics played louder or quieter, higher or lower: an accent's volume and pitch as shares of the sound's own, as
 * the 26.x line's step accents have them. Made for the step it is in.
 */
@SideOnly(Side.CLIENT)
public class ScaledAcoustic implements IAcoustic {

	private final IAcoustic[] acoustics;
	private final float volumeScale;
	private final float pitchScale;

	public ScaledAcoustic(@Nonnull final IAcoustic[] acoustics, final float volumeScale, final float pitchScale) {
		this.acoustics = acoustics;
		this.volumeScale = volumeScale;
		this.pitchScale = pitchScale;
	}

	@Nonnull
	@Override
	public String getName() {
		return "Scaled";
	}

	@Override
	public void playSound(@Nonnull final ISoundPlayer player, @Nonnull final Vec3d location,
			@Nullable final EventType event, @Nullable final IOptions inputOptions) {
		final ConfigOptions ops = new ConfigOptions();
		float volume = this.volumeScale;
		float pitch = this.pitchScale;
		if (inputOptions != null) {
			ops.setDelayMin(inputOptions.getDelayMin());
			ops.setDelayMax(inputOptions.getDelayMax());
			ops.setGlidingVolume(inputOptions.getGlidingVolume());
			ops.setGlidingPitch(inputOptions.getGlidingPitch());
			volume *= inputOptions.getVolumeScale();
			pitch *= inputOptions.getPitchScale();
		}
		ops.setVolumeScale(volume);
		ops.setPitchScale(pitch);
		for (final IAcoustic acoustic : this.acoustics)
			acoustic.playSound(player, location, event, ops);
	}

	@Override
	public String toString() {
		return "<< Scaled " + this.volumeScale + "/" + this.pitchScale + " >>";
	}
}
