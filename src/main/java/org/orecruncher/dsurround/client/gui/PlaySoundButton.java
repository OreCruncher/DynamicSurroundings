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

package org.orecruncher.dsurround.client.gui;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.client.sound.ConfigSoundInstance;
import org.orecruncher.dsurround.client.sound.MusicFader;
import org.orecruncher.dsurround.client.sound.SoundEngine;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PlaySoundButton extends GuiButtonExt {

	private final String soundResource;
	private ConfigSoundInstance playingSound;

	public PlaySoundButton(final int id, @Nonnull final String sound) {
		super(id, 0, 0, 68, 18, GuiConstants.TEXT_PLAY);

		this.soundResource = sound;
	}

	public String getSoundResource() {
		return this.soundResource;
	}

	protected void updateDisplayText() {
		this.displayString = this.playingSound != null ? GuiConstants.TEXT_STOP : GuiConstants.TEXT_PLAY;
	}

	@Override
	public void drawButton(@Nonnull final Minecraft mc, final int x, final int y, final float partial) {
		if (this.playingSound != null) {
			if (!SoundEngine.instance().isSoundPlaying(this.playingSound)) {
				this.playingSound = null;
				updateDisplayText();
			}
		}
		super.drawButton(mc, x, y, partial);
	}

	private void doPlay(@Nonnull final ConfigSoundInstance sound) {
		this.playingSound = sound;
		MusicFader.playConfigSound(sound);
	}

	public void playSound(@Nonnull final Minecraft mc, final float volume) {
		if (this.playingSound != null) {
			MusicFader.stopConfigSound(this.playingSound);
			this.playingSound = null;
		} else {
			doPlay(new ConfigSoundInstance(this.soundResource, volume));
		}

		updateDisplayText();
	}

	public void stopSound() {
		if (this.playingSound != null)
			playSound(null, 0F);
	}

}
