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

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect.SoundType;
import org.blockartistry.mod.DynSurround.util.XorShiftRandom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystemConfig;
import net.minecraftforge.fml.relauncher.Side;

/*
 * Emitters are used to produce sounds that are continuous
 * or on repeat. They ensure that the sound is always queue
 * in the sound system even if the underlying sound system
 * cancels the sound.
 */
@SideOnly(Side.CLIENT)
class Emitter {

	protected static final Random RANDOM = new XorShiftRandom();

	protected final SoundEffect effect;
	protected PlayerSound activeSound;

	protected int repeatDelay = 0;

	public Emitter(final SoundEffect sound) {
		this.effect = sound;
	}

	public void update() {
		final SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		if (this.activeSound != null) {
			if (handler.isSoundPlaying(this.activeSound))
				return;
			ModLog.debug("FADE: " + this.activeSound.toString());
			this.activeSound.fadeAway();
			this.activeSound = null;
			this.repeatDelay = this.effect.getRepeat(RANDOM);
			if (this.repeatDelay > 0)
				return;
		} else if (this.repeatDelay > 0) {
			if (--this.repeatDelay > 0)
				return;
		}

		// If the volume is turned off don't send
		// down a sound.
		if (SoundSystemConfig.getMasterGain() <= 0)
			return;

		final PlayerSound theSound = new PlayerSound(effect);
		if (this.effect.type == SoundType.PERIODIC) {
			this.repeatDelay = this.effect.getRepeat(RANDOM);
		} else {
			this.activeSound = theSound;
		}

		try {
			SoundManager.playSound(theSound);
		} catch (final Throwable t) {
			;
		}
	}

	public void setVolume(final float volume) {
		if (this.activeSound != null)
			this.activeSound.setVolume(volume);
	}

	public float getVolume() {
		return this.activeSound != null ? this.activeSound.getVolume() : 0.0F;
	}

	public void fade() {
		if (this.activeSound != null)
			this.activeSound.fadeAway();
	}
}
