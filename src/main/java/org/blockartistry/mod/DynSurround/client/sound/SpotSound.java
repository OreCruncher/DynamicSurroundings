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

package org.blockartistry.mod.DynSurround.client.sound;

import java.util.Random;

import javax.annotation.Nonnull;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.util.random.XorShiftRandom;

import net.minecraft.client.audio.PositionedSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpotSound extends PositionedSound implements IMySound {

	private static final int SPOT_SOUND_RANGE = 8;

	private final Random RANDOM = XorShiftRandom.current();
	private final SoundEffect sound;
	private final int timeMark;

	SpotSound(@Nonnull final BlockPos pos, @Nonnull final SoundEffect sound, final int delay) {
		super(sound.sound, sound.category);

		this.sound = sound;
		this.volume = sound.getVolume();
		this.pitch = sound.getPitch(RANDOM);
		this.repeat = false;
		this.repeatDelay = 0;

		this.xPosF = (float) pos.getX() + 0.5F;
		this.yPosF = (float) pos.getY() + 0.5F;
		this.zPosF = (float) pos.getZ() + 0.5F;

		this.timeMark = EnvironState.getTickCounter() + delay;
	}

	private float randomRange(final int range) {
		return RANDOM.nextInt(range) - RANDOM.nextInt(range);
	}

	SpotSound(@Nonnull final EntityPlayer player, @Nonnull final SoundEffect sound) {
		super(sound.sound, sound.category);

		this.sound = sound;
		this.volume = sound.getVolume();
		this.pitch = sound.getPitch(RANDOM);
		this.repeat = false;
		this.repeatDelay = 0;

		final Vec3d point = player.getEntityBoundingBox().getCenter();
		this.xPosF = (float) point.xCoord + randomRange(SPOT_SOUND_RANGE);
		this.yPosF = (float) point.yCoord + randomRange(SPOT_SOUND_RANGE);
		this.zPosF = (float) point.zCoord + randomRange(SPOT_SOUND_RANGE);

		this.timeMark = EnvironState.getTickCounter();
	}

	@Override
	public float getVolume() {
		return super.getVolume() * ModOptions.masterSoundScaleFactor;
	}

	@Override
	public void setVolume(final float volume) {
		this.volume = volume;
	}
	
	@Override
	public void setPitch(final float pitch) {
		this.pitch = pitch;
	}

	public int getTickAge() {
		return EnvironState.getTickCounter() - this.timeMark;
	}
	
	public void fade() {
		
	}

	public SoundEffect getSoundEffect() {
		return this.sound;
	}

	@Override
	public String toString() {
		return this.sound.toString();
	}

}