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

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.lib.sound.BasicSound;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpotSound extends BasicSound<SpotSound> {

	private static final int SPOT_SOUND_RANGE = 8;
	public static final ISoundScale BIOME_EFFECT = () -> {
		return ModOptions.sound.masterSoundScaleFactor;
	};

	public SpotSound() {
		super((ResourceLocation) null, SoundCategory.PLAYERS);
		this.setVolumeScale(BIOME_EFFECT);
	}

	SpotSound(@Nonnull final BlockPos pos, @Nonnull final SoundEffect sound) {
		super(sound.getSound(), sound.getCategory());

		this.volume = sound.getVolume();
		this.pitch = sound.getPitch(this.RANDOM);
		this.repeat = false;
		this.repeatDelay = 0;

		this.setPosition(pos);
		this.setVolumeScale(BIOME_EFFECT);
	}

	private float randomRange(final int range) {
		return this.RANDOM.nextInt(range) - this.RANDOM.nextInt(range);
	}

	SpotSound(@Nonnull final Entity player, @Nonnull final SoundEffect sound) {
		super(sound.getSound(), sound.getCategory());

		this.volume = sound.getVolume();
		this.pitch = sound.getPitch(this.RANDOM);
		this.repeat = false;
		this.repeatDelay = 0;

		this.setPosition(player);

		// If it is not a player sound randomize the location around the player
		if (sound.getCategory() != SoundCategory.PLAYERS) {
			this.xPosF += randomRange(SPOT_SOUND_RANGE);
			this.yPosF += randomRange(SPOT_SOUND_RANGE);
			this.zPosF += randomRange(SPOT_SOUND_RANGE);
		}

		this.setVolumeScale(BIOME_EFFECT);
	}

}