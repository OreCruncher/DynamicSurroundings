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

import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.lib.compat.ModEnvironment;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystemConfig;

@SideOnly(Side.CLIENT)
public class SoundInstance extends PositionedSound implements ISoundInstance {

	protected static final float ATTENUATION_OFFSET = 32F;
	protected static final Random RANDOM = XorShiftRandom.current();

	protected final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	protected float volumeThrottle = 1.0F;
	protected SoundState state = SoundState.NONE;

	SoundInstance(@Nonnull final SoundEvent event, @Nonnull final SoundCategory cat) {
		this(event.getSoundName(), cat);
	}

	SoundInstance(@Nonnull final ResourceLocation soundResource, @Nonnull final SoundCategory cat) {
		super(soundResource, cat);

		this.volume = 1F;
		this.pitch = 1F;
		this.setPosition(0, 0, 0);
		this.repeat = false;
		this.repeatDelay = 0;
		this.attenuationType = ISound.AttenuationType.LINEAR;

		super.sound = SoundHandler.MISSING_SOUND;
	}

	@Override
	public SoundState getState() {
		return this.state;
	}

	@Override
	public void setState(@Nonnull final SoundState state) {
		this.state = state;
	}

	public SoundInstance setCategory(@Nonnull final SoundCategory cat) {
		this.category = cat;
		return this;
	}

	public SoundInstance setVolume(final float v) {
		this.volume = v;
		return this;
	}

	public SoundInstance setPitch(final float p) {
		this.pitch = p;
		return this;
	}

	public SoundInstance setPosition(final float x, final float y, final float z) {
		this.xPosF = x;
		this.yPosF = y;
		this.zPosF = z;
		this.pos.setPos(x, y, z);
		return this;
	}

	public SoundInstance setPosition(@Nonnull final Vec3i pos) {
		return this.setPosition(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
	}

	public SoundInstance setPosition(@Nonnull final Vec3d pos) {
		return this.setPosition((float) pos.x, (float) pos.y, (float) pos.z);
	}

	@Override
	public float getYPosF() {
		final float y = super.getYPosF();
		return getAttenuationType() == AttenuationType.NONE ? y + ATTENUATION_OFFSET : y;
	}

	public SoundInstance setAttenuationType(@Nonnull final ISound.AttenuationType type) {
		this.attenuationType = type;
		return this;
	}

	public SoundInstance setRepeat(final boolean flag) {
		this.repeat = flag;
		return this;
	}

	public SoundInstance setRepeatDelay(final int delay) {
		this.repeatDelay = delay;
		return this;
	}

	public SoundInstance setVolumeThrottle(final float throttle) {
		this.volumeThrottle = throttle;
		return this;
	}

	@Override
	public float getVolume() {
		return super.getVolume() * this.volumeThrottle;
	}

	public void fade() {

	}

	public void unfade() {

	}

	public boolean isFading() {
		return false;
	}

	public boolean isDonePlaying() {
		return false;
	}

	public boolean canSoundBeHeard() {
		return this.volume > 0
				&& (getAttenuationType() == AttenuationType.NONE || SoundSystemConfig.getMasterGain() > 0);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Sound{");
		builder.append(getSoundLocation().toString());
		builder.append(", ").append(getCategory().toString());
		builder.append(", ").append(getState().toString());
		builder.append(", v:").append(getVolume());
		builder.append(", p:").append(getPitch());
		builder.append("}");
		return builder.toString();
	}

	public static AttenuationType noAttenuation() {
		return ModEnvironment.SoundPhysics.isLoaded() ? AttenuationType.LINEAR : AttenuationType.NONE;
	}

}
