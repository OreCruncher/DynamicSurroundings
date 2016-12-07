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

package org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.implem;

import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.IOptions;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.ISoundPlayer;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PendingSound {
	private final Object location;
	private final SoundEvent sound;
	private final float volume;
	private final float pitch;
	private final IOptions options;
	private final long timeToPlay;
	private final long maximum;

	public PendingSound(final Object location, final SoundEvent sound, final float volume, final float pitch,
			final IOptions options, final long timeToPlay, final long maximum) {
		this.location = location;
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.options = options;

		this.timeToPlay = timeToPlay;
		this.maximum = maximum;
	}

	/**
	 * Play the sound stored in this pending sound.
	 * 
	 * @param player
	 */
	public void playSound(final ISoundPlayer player) {
		player.playSound(this.location, this.sound, this.volume, this.pitch, this.options);
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
}