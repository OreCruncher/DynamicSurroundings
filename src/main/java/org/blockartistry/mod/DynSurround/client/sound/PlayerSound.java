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
import org.blockartistry.mod.DynSurround.client.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.util.XorShiftRandom;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
class PlayerSound extends MovingSound {

	private static final float DONE_VOLUME_THRESHOLD = 0.001F;
	private static final float FADE_AMOUNT = 0.01F;
	private static final Random RANDOM = new XorShiftRandom();

	private final SoundEffect sound;
	private boolean isFading;

	public PlayerSound(final SoundEffect sound) {
		super(sound.sound, SoundCategory.PLAYERS);

		// Don't set volume to 0; MC will optimize out
		this.sound = sound;
		this.volume = sound.volume;
		this.pitch = sound.getPitch(RANDOM);
		this.repeat = sound.repeatDelay == 0;

		// Repeat delay
		this.repeatDelay = 0;

		final EntityPlayer player = EnvironState.getPlayer();
		// Initial position
		this.xPosF = MathHelper.floor_double(player.posX);
		this.yPosF = MathHelper.floor_double(player.posY + 1);
		this.zPosF = MathHelper.floor_double(player.posZ);
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
		}

		if (this.volume <= DONE_VOLUME_THRESHOLD) {
			this.donePlaying = true;
		} else if (EnvironState.getPlayer() != null) {
			final EntityPlayer player = EnvironState.getPlayer();
			this.xPosF = MathHelper.floor_double(player.posX);
			this.yPosF = MathHelper.floor_double(player.posY + 1);
			this.zPosF = MathHelper.floor_double(player.posZ);
		}
	}

	@Override
	public float getVolume() {
		return this.volume * ModOptions.masterSoundScaleFactor;
	}

	public void setVolume(final float volume) {
		if (volume < this.volume || !this.isFading)
			this.volume = volume;
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
