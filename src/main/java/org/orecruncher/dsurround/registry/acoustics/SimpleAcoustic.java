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

package org.orecruncher.dsurround.registry.acoustics;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * A simple acoustic is one that is backed by a singular sound event.  Most sounds
 * that are played in Minecraft can be considered a simple acoustic.
 */
@SideOnly(Side.CLIENT)
public class SimpleAcoustic implements IAcoustic {

	@Nullable
	protected SoundEvent sound;
	protected float volMin = 1f;
	protected float volMax = 1f;
	protected float pitchMin = 1f;
	protected float pitchMax = 1f;

	@Nullable
	protected IOptions outputOptions;

	public SimpleAcoustic() {
		// Used in various builders
	}

	public SimpleAcoustic(@Nonnull final SoundEvent evt) {
		this.sound = evt;
	}

	@Override
	@Nonnull
	public String getName() {
		return this.sound != null ? this.sound.getSoundName().toString() : "<UNKNOWN>";
	}

	@Override
	public void playSound(@Nonnull final ISoundPlayer player, @Nonnull final Vec3d location,
			@Nonnull final EventType event, @Nullable final IOptions inputOptions) {
		// Special case for intentionally empty sounds (as opposed to fall back
		// sounds)
		if (this.sound == null)
			return;

		float volume = generateVolume(player.getRNG());
		float pitch = generatePitch(player.getRNG());
		if (inputOptions != null) {
			if (inputOptions.getGlidingVolume() > 0) {
				volume = this.volMin + (this.volMax - this.volMin) * inputOptions.getGlidingVolume();
			}
			if (inputOptions.getGlidingPitch() > 0) {
				pitch = this.pitchMin + (this.pitchMax - this.pitchMin) * inputOptions.getGlidingPitch();
			}
			volume *= inputOptions.getVolumeScale();
			pitch *= inputOptions.getPitchScale();
		}

		player.playSound(location, this.sound, volume, pitch, this.outputOptions);
	}

	private float generateVolume(@Nonnull final Random rng) {
		return randAB(rng, this.volMin, this.volMax);
	}

	private float generatePitch(@Nonnull final Random rng) {
		return randAB(rng, this.pitchMin, this.pitchMax);
	}

	private float randAB(@Nonnull final Random rng, final float a, final float b) {
		if (a >= b)
			return a;

		return a + rng.nextFloat() * (b - a);
	}

	public void setSound(@Nullable final SoundEvent sound) {
		this.sound = sound;
	}

	public void setVolMin(final float volMin) {
		this.volMin = volMin;
	}

	public void setVolMax(final float volMax) {
		this.volMax = volMax;
	}

	public void setPitchMin(final float pitchMin) {
		this.pitchMin = pitchMin;
	}

	public void setPitchMax(final float pitchMax) {
		this.pitchMax = pitchMax;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(getName()).append('[');
		builder.append("vMin:").append(this.volMin).append(',');
		builder.append("vMax:").append(this.volMax).append(',');
		builder.append("pMin:").append(this.pitchMax).append(',');
		builder.append("pMax:").append(this.pitchMax);
		builder.append(']');
		return builder.toString();
	}

}