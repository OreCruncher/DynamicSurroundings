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
package org.orecruncher.dsurround.client.sound;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.mixins.IPositionedSoundMixin;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SoundBuilder {

	private final SoundInstance sound;

	private SoundBuilder(@Nonnull final SoundEvent evt, @Nonnull final SoundCategory cat) {
		this.sound = new SoundInstance(evt, cat);
	}

	public SoundBuilder from(@Nonnull final PositionedSound ps) {
		this.sound.setCategory(ps.getCategory());
		this.sound.setPosition(ps.getXPosF(), ps.getYPosF(), ps.getZPosF());
		this.sound.setAttenuationType(ps.getAttenuationType());
		
		final IPositionedSoundMixin sound = (IPositionedSoundMixin) ps;
		this.sound.setVolume(sound.getVolumeRaw());
		this.sound.setPitch(sound.getPitchRaw());
		return this;
	}

	public SoundBuilder setPosition(final float x, final float y, final float z) {
		this.sound.setPosition(x, y, z);
		return this;
	}

	public SoundBuilder setPosition(@Nonnull final BlockPos pos) {
		this.sound.setPosition(pos);
		return this;
	}

	public SoundBuilder setPosition(@Nonnull final Vec3d pos) {
		this.sound.setPosition(pos);
		return this;
	}

	public SoundBuilder setVolume(final float v) {
		this.sound.setVolume(v);
		return this;
	}

	public SoundBuilder setPitch(final float p) {
		this.sound.setPitch(p);
		return this;
	}

	public SoundInstance build() {
		return this.sound;
	}

	public static SoundBuilder builder(@Nonnull final SoundEvent evt) {
		return builder(evt, SoundCategory.NEUTRAL);
	}

	public static SoundBuilder builder(@Nonnull final SoundEvent evt, @Nonnull final SoundCategory cat) {
		return new SoundBuilder(evt, cat);
	}

	public static SoundInstance create(@Nonnull final SoundEvent evt, @Nonnull final SoundCategory cat) {
		return new SoundInstance(evt, cat);
	}

}
