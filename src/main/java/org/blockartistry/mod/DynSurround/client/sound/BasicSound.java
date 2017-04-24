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

package org.blockartistry.mod.DynSurround.client.sound;

import javax.annotation.Nonnull;

import com.google.common.base.Objects;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BasicSound<T extends BasicSound<?>> extends PositionedSound {

	public BasicSound(@Nonnull final SoundEvent event, @Nonnull final SoundCategory cat) {
		this(event.getSoundName(), cat);
	}

	public BasicSound(@Nonnull final ResourceLocation soundResource, @Nonnull final SoundCategory cat) {
		super(soundResource, cat);

		this.volume = 1F;
		this.pitch = 1F;
		this.xPosF = this.yPosF = this.zPosF = 0F;
		this.repeat = false;
		this.repeatDelay = 0;
		this.attenuationType = ISound.AttenuationType.LINEAR;
	}

	@SuppressWarnings("unchecked")
	public T setVolume(final float v) {
		this.volume = v;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setPitch(final float p) {
		this.pitch = p;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setPosition(final float x, final float y, final float z) {
		this.xPosF = x;
		this.yPosF = y;
		this.zPosF = z;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setAttenuationType(@Nonnull final ISound.AttenuationType type) {
		this.attenuationType = type;
		return (T) this;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(this.positionedSoundLocation.toString())
				.addValue(this.category.toString()).add("volume", this.volume).add("pitch", this.pitch)
				.add("attenuation", this.attenuationType).add("x", this.xPosF).add("y", this.yPosF).add("z", this.zPosF)
				.toString();
	}
}
