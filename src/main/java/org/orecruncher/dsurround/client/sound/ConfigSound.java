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

package org.orecruncher.dsurround.client.sound;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.lib.sound.SoundState;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.sound.SoundMetadata;

import com.google.common.base.MoreObjects;

import net.minecraft.client.audio.ISound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Special sound class for playing sounds during configuration. The sound engine
 * will not block or scale volumes so that the player can hear the sound
 * regardless of how the config is set up.
 */
@SideOnly(Side.CLIENT)
public class ConfigSound extends BasicSound<ConfigSound> {

	public ConfigSound(@Nonnull final String soundResource, final float volume) {
		super(new ResourceLocation(soundResource), SoundCategory.MASTER);

		this.volume = volume;
		this.pitch = 1F;
		this.xPosF = this.yPosF = this.zPosF = 0F;
		this.repeat = false;
		this.repeatDelay = 0;
		this.attenuationType = ISound.AttenuationType.NONE;

		final SoundMetadata data = RegistryManager.SOUND.getSoundMetadata(this.positionedSoundLocation);
		if (data != null)
			this.category = data.getCategory();
	}

	@Override
	public boolean isDonePlaying() {
		return this.getState() == SoundState.DONE;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).addValue(this.positionedSoundLocation.toString())
				.add("volume", this.volume).add("pitch", this.pitch).add("attenuation", this.attenuationType)
				.toString();
	}
}
