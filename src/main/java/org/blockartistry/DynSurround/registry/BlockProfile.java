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

package org.blockartistry.DynSurround.registry;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.client.fx.BlockEffect;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.lib.MyUtils;
import org.blockartistry.lib.WeightTable;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockProfile {

	public final static BlockEffect[] NO_EFFECTS = {};
	public final static SoundEffect[] NO_SOUNDS = {};

	public static BlockProfile createProfile(@Nonnull final BlockInfo blockInfo) {
		return new BlockProfile(blockInfo);
	}

	protected final BlockInfo info;
	protected int chance = 100;
	protected int stepChance = 100;
	protected SoundEffect[] sounds = NO_SOUNDS;
	protected SoundEffect[] stepSounds = NO_SOUNDS;
	protected BlockEffect[] effects = NO_EFFECTS;
	protected BlockEffect[] alwaysOn = NO_EFFECTS;

	public BlockProfile(@Nonnull final BlockInfo blockInfo) {
		this.info = blockInfo;
	}

	public BlockProfile setChance(final int chance) {
		this.chance = chance;
		return this;
	}

	public int getChance() {
		return this.chance;
	}

	public BlockProfile setStepChance(final int chance) {
		this.stepChance = chance;
		return this;
	}

	public int getStepChance() {
		return this.stepChance;
	}

	public BlockProfile addSound(@Nonnull final SoundEffect sound) {
		this.sounds = MyUtils.append(this.sounds, sound);
		return this;
	}

	public BlockProfile clearSounds() {
		this.sounds = NO_SOUNDS;
		return this;
	}

	@Nonnull
	public SoundEffect[] getSounds() {
		return this.sounds;
	}

	public BlockProfile addStepSound(@Nonnull final SoundEffect sound) {
		this.stepSounds = MyUtils.append(this.stepSounds, sound);
		return this;
	}

	public BlockProfile clearStepSounds() {
		this.stepSounds = NO_SOUNDS;
		return this;
	}

	@Nonnull
	public SoundEffect[] getStepSounds() {
		return this.stepSounds;
	}

	public BlockProfile addEffect(@Nonnull final BlockEffect effect) {
		if (effect.getChance() > 0)
			this.effects = MyUtils.append(this.effects, effect);
		else
			this.alwaysOn = MyUtils.append(this.alwaysOn, effect);
		return this;
	}

	public BlockProfile clearEffects() {
		this.effects = NO_EFFECTS;
		this.alwaysOn = NO_EFFECTS;
		return this;
	}

	@Nonnull
	public BlockEffect[] getEffects() {
		return this.effects;
	}

	@Nonnull
	public BlockEffect[] getAlwaysOnEffects() {
		return this.alwaysOn;
	}

	@Nullable
	public SoundEffect getSoundToPlay(@Nonnull final Random random) {
		return this.sounds != NO_SOUNDS && random.nextInt(getChance()) == 0
				? new WeightTable<>(this.sounds).next()
				: null;
	}

	@Nullable
	public SoundEffect getStepSoundToPlay(@Nonnull final Random random) {
		return this.stepSounds != NO_SOUNDS && random.nextInt(getStepChance()) == 0
				? new WeightTable<>(this.stepSounds).next()
				: null;
	}

	public boolean hasSoundsOrEffects() {
		return this.sounds.length > 0 || this.effects.length > 0;
	}

	public boolean hasAlwaysOnEffects() {
		return this.alwaysOn.length > 0;
	}

	@Override
	@Nonnull
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Block [").append(this.info.toString()).append("]");

		if (this.sounds != NO_SOUNDS) {
			boolean commaFlag = false;
			builder.append(" chance:").append(this.chance);
			builder.append("; sounds [");
			for (final SoundEffect sound : this.sounds) {
				if (commaFlag)
					builder.append(",");
				else
					commaFlag = true;
				builder.append(sound.toString());
			}
			builder.append(']');
		} else {
			builder.append("NO SOUNDS");
		}

		if (this.stepSounds != NO_SOUNDS) {
			boolean commaFlag = false;
			builder.append(" chance:").append(this.stepChance);
			builder.append("; step sounds [");
			for (final SoundEffect sound : this.stepSounds) {
				if (commaFlag)
					builder.append(",");
				else
					commaFlag = true;
				builder.append(sound.toString());
			}
			builder.append(']');
		} else {
			builder.append("; NO STEP SOUNDS");
		}

		if (this.effects != this.alwaysOn) {
			boolean commaFlag = false;
			builder.append("; effects [");
			for (final BlockEffect effect : this.effects) {
				if (commaFlag)
					builder.append(",");
				else
					commaFlag = true;
				builder.append(effect.toString());
			}
			for (final BlockEffect effect : this.alwaysOn) {
				if (commaFlag)
					builder.append(",");
				else
					commaFlag = true;
				builder.append(effect.toString());
			}
			builder.append(']');
		} else {
			builder.append("; NO EFFECTS");
		}

		return builder.toString();
	}
}
