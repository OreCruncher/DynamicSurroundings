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

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.util.random.XorShiftRandom;

import com.google.common.base.Objects;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BasicSound<T extends BasicSound<?>> extends PositionedSound {

	public static interface ISoundScale {
		float getScale();
	}

	public static final ISoundScale DEFAULT_SCALE = new ISoundScale() {
		@Override
		public float getScale() {
			return 1.0F;
		}
	};

	protected final Random RANDOM = XorShiftRandom.current();
	protected ISoundScale volumeScale;

	public BasicSound(@Nonnull final SoundEvent event, @Nonnull final SoundCategory cat) {
		this(event.getSoundName(), cat);
	}

	public BasicSound(@Nonnull final ResourceLocation soundResource, @Nonnull final SoundCategory cat) {
		super(soundResource, cat);

		this.volumeScale = DEFAULT_SCALE;

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
	
	public T setPosition(@Nonnull final Entity entity) {
		final Vec3d point = entity.getEntityBoundingBox().getCenter();
		return this.setPosition(point);
	}

	public T setPosition(@Nonnull final Vec3i pos) {
		return this.setPosition(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
	}

	public T setPosition(@Nonnull final Vec3d pos) {
		return this.setPosition((float) pos.xCoord, (float) pos.yCoord, (float) pos.zCoord);
	}

	@SuppressWarnings("unchecked")
	public T setAttenuationType(@Nonnull final ISound.AttenuationType type) {
		this.attenuationType = type;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setRepeat(final boolean flag) {
		this.repeat = flag;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setRepeatDelay(final int delay) {
		this.repeatDelay = delay;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setVolumeScale(@Nonnull final ISoundScale scale) {
		this.volumeScale = scale;
		return (T) this;
	}

	@Override
	public float getVolume() {
		return super.getVolume() * this.volumeScale.getScale();
	}

	public void fade() {

	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(this.positionedSoundLocation.toString())
				.addValue(this.category.toString()).add("volume", this.getVolume()).add("pitch", this.getPitch())
				.add("attenuation", this.getAttenuationType()).add("x", this.getXPosF()).add("y", this.getYPosF())
				.add("z", this.getZPosF()).toString();
	}
}
