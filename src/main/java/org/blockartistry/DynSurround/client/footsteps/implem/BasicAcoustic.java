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

package org.blockartistry.DynSurround.client.footsteps.implem;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.client.footsteps.interfaces.EventType;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IOptions;
import org.blockartistry.DynSurround.client.footsteps.interfaces.ISoundPlayer;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IOptions.Option;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BasicAcoustic implements IAcoustic {

	protected String acousticName;
	protected SoundEvent sound;
	protected float volMin = 1f;
	protected float volMax = 1f;
	protected float pitchMin = 1f;
	protected float pitchMax = 1f;

	protected IOptions outputOptions;

	public BasicAcoustic() {
		this("Unnamed");
	}

	public BasicAcoustic(@Nonnull final String name) {
		this.acousticName = name;
	}

	@Override
	@Nonnull
	public String getAcousticName() {
		return this.acousticName;
	}

	@Override
	public void playSound(@Nonnull final ISoundPlayer player, @Nonnull final Object location,
			@Nonnull final EventType event, @Nullable final IOptions inputOptions) {
		// Special case for intentionally empty sounds (as opposed to fall back
		// sounds)
		if (this.sound == null)
			return;

		float volume = generateVolume(player.getRNG());
		float pitch = generatePitch(player.getRNG());
		if (inputOptions != null) {
			if (inputOptions.hasOption(Option.GLIDING_VOLUME)) {
				volume = this.volMin + (this.volMax - this.volMin) * inputOptions.asFloat(Option.GLIDING_VOLUME);
			}
			if (inputOptions.hasOption(Option.GLIDING_PITCH)) {
				pitch = this.pitchMin + (this.pitchMax - this.pitchMin) * inputOptions.asFloat(Option.GLIDING_PITCH);
			}
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

	public void setSound(@Nonnull final SoundEvent sound) {
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
		builder.append(this.getAcousticName()).append('[');
		if (this.sound != null)
			builder.append(this.sound.getSoundName());
		else
			builder.append("<NO SOUND>");
		builder.append(']');
		return builder.toString();
	}

}