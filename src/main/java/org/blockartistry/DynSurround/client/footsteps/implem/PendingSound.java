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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.client.footsteps.interfaces.IOptions;
import org.blockartistry.DynSurround.client.footsteps.interfaces.ISoundPlayer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PendingSound {

	private static final float LATENESS_THRESHOLD_DIVIDER = 1.2f;

	private final EntityLivingBase location;
	private final SoundEvent sound;
	private final float volume;
	private final float pitch;
	private final IOptions options;
	private final long timeToPlay;
	private final long maximum;
	private final float lateTolerance;

	public PendingSound(@Nonnull final EntityLivingBase location, @Nonnull final SoundEvent sound, final float volume, final float pitch,
			@Nullable final IOptions options, final long timeToPlay, final long maximum) {
		this.location = location;
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.options = options;

		this.timeToPlay = timeToPlay;
		this.maximum = maximum;
		this.lateTolerance = maximum / LATENESS_THRESHOLD_DIVIDER;
	}

	/**
	 * Play the sound stored in this pending sound.
	 * 
	 * @param player
	 */
	public void playSound(@Nonnull final ISoundPlayer player, @Nonnull final Variator var) {
		player.playSound(this.location, this.sound, this.volume, this.pitch, var, this.options);
	}

	/**
	 * Returns the time after which this sound plays.
	 * 
	 * @return
	 */
	public long getTimeToPlay() {
		return this.timeToPlay;
	}

	/**
	 * Get the maximum delay of this sound, for threshold purposes. If the value
	 * is negative, the sound will not be skippable.
	 * 
	 * @return
	 */
	public long getMaximumBase() {
		return this.maximum;
	}

	public float getLateTolerance() {
		return this.lateTolerance;
	}

	public boolean isLate(final long time) {
		if (this.maximum < 0)
			return false;

		return (time - this.timeToPlay) > this.lateTolerance;
	}

	public long howLate(final long time) {
		return time - this.timeToPlay;
	}

}