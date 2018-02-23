/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher, Abastro
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

package org.blockartistry.DynSurround.client.sound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.lib.sound.BasicSound;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SoundEngine {

	private static SoundEngine instance = null;

	public static SoundEngine instance() {
		if (instance == null)
			instance = new SoundEngine();

		return instance;
	}

	private SoundEngine() {

	}

	private static SoundManagerReplacement getManager() {
		return SoundManagerReplacement.getSoundManager();
	}

	public boolean isSoundPlaying(@Nonnull final BasicSound<?> sound) {
		return getManager().isSoundPlaying(sound);
	}

	public boolean isSoundPlaying(@Nonnull final String soundId) {
		return getManager().isSoundPlaying(soundId);
	}

	public void stopSound(@Nonnull final BasicSound<?> sound) {
		getManager().stopSound(sound);
	}

	public void stopAllSounds() {
		getManager().stopAllSounds();
	}

	@Nullable
	public String playSound(@Nonnull final BasicSound<?> sound) {
		getManager().playSound(sound);
		return sound.getId();
	}

}
