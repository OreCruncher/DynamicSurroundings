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

import javax.annotation.Nonnull;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.util.random.XorShiftRandom;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class TrackingSound extends BasicSound<TrackingSound> implements ITickableSound, IMySound<TrackingSound> {

	private static final float DONE_VOLUME_THRESHOLD = 0.001F;
	private static final float FADE_AMOUNT = 0.015F;
	
	private final Random RANDOM = XorShiftRandom.current();
	private final EntityLivingBase attachedTo;
	private final SoundEffect sound;
	private boolean isFading;
	private float maxVolume;
	private boolean isDonePlaying;
	
	private long lastTick;

	TrackingSound(@Nonnull final EntityLivingBase attachedTo, @Nonnull final SoundEffect sound, final boolean fadeIn) {
		super(sound.sound, SoundCategory.PLAYERS);

		this.attachedTo = attachedTo;

		// Don't set volume to 0; MC will optimize out
		this.sound = sound;
		this.maxVolume = sound.getVolume();
		this.volume = fadeIn ? DONE_VOLUME_THRESHOLD * 2 : this.maxVolume;
		this.pitch = sound.getPitch(RANDOM);

		this.lastTick = EnvironState.getTickCounter() - 1;
		
		super.sound = SoundHandler.MISSING_SOUND;
		
		updateLocation();
	}

	@Override
	public boolean canRepeat() {
		return !this.isDonePlaying() && this.sound.isRepeatable();
	}

	@Override
	public int getRepeatDelay() {
		return this.sound.getRepeat(RANDOM);
	}

	public void fade() {
		this.isFading = true;
	}

	public boolean isFading() {
		return this.isFading;
	}

	public boolean isDonePlaying() {
		return this.isDonePlaying;
	}

	public boolean sameSound(final SoundEffect snd) {
		return this.sound.equals(snd);
	}

	public void updateLocation() {
		final AxisAlignedBB box = this.attachedTo.getEntityBoundingBox();
		final Vec3d point = box.getCenter();
		this.setPosition((float) point.xCoord, (float) box.minY, (float) point.zCoord);
	}
	
	public boolean isEntityAlive() {
		return this.attachedTo.isEntityAlive();
	}

	@Override
	public void update() {
		if (this.isDonePlaying())
			return;

		if (!isEntityAlive()) {
			this.isDonePlaying = true;
			return;
		}

		final long tickDelta = EnvironState.getTickCounter() - this.lastTick;
		if(tickDelta == 0)
			return;
		
		this.lastTick = EnvironState.getTickCounter();
		
		if (this.isFading()) {
			this.volume -= FADE_AMOUNT * tickDelta;
		} else if (this.volume < this.maxVolume) {
			this.volume += FADE_AMOUNT * tickDelta;
		}
		
		if (this.volume > this.maxVolume) {
			this.volume = this.maxVolume;
		}

		if (this.volume <= DONE_VOLUME_THRESHOLD) {
			// Make sure the volume is 0 so a repeating
			// sound won't make a last gasp in the sound
			// engine.
			this.isDonePlaying = true;
			this.volume = 0.0F;
		} else {
			updateLocation();
		}
	}

	@Override
	public float getVolume() {
		return super.getVolume() * ModOptions.masterSoundScaleFactor;
	}

	@Override
	public TrackingSound setVolume(final float volume) {
		if (volume < this.maxVolume || !this.isFading)
			this.maxVolume = volume;
		return this;
	}
	
	@Override
	public int getTickAge() {
		return 0;
	}

	@Override
	public boolean equals(final Object anObj) {
		if (this == anObj)
			return true;
		if (anObj instanceof TrackingSound)
			return this.sameSound(((TrackingSound) anObj).sound);
		if (anObj instanceof SoundEffect)
			return this.sameSound((SoundEffect) anObj);
		return false;
	}
}
