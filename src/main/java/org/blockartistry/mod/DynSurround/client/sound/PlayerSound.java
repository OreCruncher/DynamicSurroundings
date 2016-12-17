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

package org.blockartistry.mod.DynSurround.client.sound;

import java.util.Random;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.util.XorShiftRandom;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
class PlayerSound extends MovingSound {

	private static final float DONE_VOLUME_THRESHOLD = 0.001F;
	private static final float FADE_AMOUNT = 0.015F;
	private static final Random RANDOM = new XorShiftRandom();

	private final SoundEffect sound;
	private boolean isFading;
	private float maxVolume;

	public PlayerSound(final SoundEffect sound) {
		this(sound, false);
	}
	
	public PlayerSound(final SoundEffect sound, final boolean fadeIn) {
		super(sound.sound, SoundCategory.PLAYERS);

		// Don't set volume to 0; MC will optimize out
		this.sound = sound;
		this.maxVolume = sound.volume;
		this.volume = fadeIn ? DONE_VOLUME_THRESHOLD * 2 : sound.volume;
		this.pitch = sound.getPitch(RANDOM);
		this.repeat = sound.repeatDelay == 0;

		// Repeat delay
		this.repeatDelay = 0;

		final BlockPos position = EnvironState.getPlayerPosition();
		this.xPosF = position.getX();
		this.yPosF = position.getY() + 1;
		this.zPosF = position.getZ();
	}

	public void fadeAway() {
		this.isFading = true;
	}

	public boolean sameSound(final SoundEffect snd) {
		return this.sound.equals(snd);
	}

	@Override
	public void update() {
		if (this.donePlaying)
			return;

		if (this.isFading) {
			this.volume -= FADE_AMOUNT;
		} else if(this.volume < this.maxVolume) {
			this.volume += FADE_AMOUNT;
		} else if(this.volume > this.maxVolume) {
			this.volume = this.maxVolume;
		}
		
		if (this.volume <= DONE_VOLUME_THRESHOLD) {
			this.donePlaying = true;
		} else if (EnvironState.getPlayer() != null) {
			final BlockPos position = EnvironState.getPlayerPosition();
			this.xPosF = position.getX();
			this.yPosF = position.getY() + 1;
			this.zPosF = position.getZ();
		}
	}

	@Override
	public float getVolume() {
		return this.volume * ModOptions.masterSoundScaleFactor;
	}

	public void setVolume(final float volume) {
		if (volume < this.maxVolume || !this.isFading)
			this.maxVolume = volume;
	}

	@Override
	public String toString() {
		return this.sound.toString();
	}

	@Override
	public boolean equals(final Object anObj) {
		if (this == anObj)
			return true;
		if (anObj instanceof PlayerSound)
			return this.sameSound(((PlayerSound) anObj).sound);
		if (anObj instanceof SoundEffect)
			return this.sameSound((SoundEffect) anObj);
		return false;
	}
}
