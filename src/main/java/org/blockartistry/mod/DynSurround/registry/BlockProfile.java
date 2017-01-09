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

package org.blockartistry.mod.DynSurround.registry;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.fx.BlockEffect;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.util.MCHelper;

import net.minecraft.block.state.IBlockState;

public class BlockProfile {

	public static BlockProfile createProfile(@Nonnull final BlockInfo blockInfo) {
		return new BlockProfile(blockInfo);
	}

	protected final BlockInfo info;
	protected final boolean hasVariants;
	protected int chance = 100;
	protected int stepChance = 100;
	protected final List<SoundEffect> sounds = new ArrayList<SoundEffect>();
	protected final List<SoundEffect> stepSounds = new ArrayList<SoundEffect>();
	protected final List<BlockEffect> effects = new ArrayList<BlockEffect>();
	protected final List<BlockEffect> alwaysOn = new ArrayList<BlockEffect>();

	public BlockProfile(@Nonnull final BlockInfo blockInfo) {
		this.info = blockInfo;
		this.hasVariants = MCHelper.hasVariants(this.info.block);
	}

	public void setChance(@Nonnull final BlockInfo blockInfo, final int chance) {
		this.chance = chance;
	}

	public int getChance(@Nonnull final IBlockState blockInfo) {
		return this.chance;
	}

	public void setStepChance(@Nonnull final BlockInfo blockInfo, final int chance) {
		this.stepChance = chance;
	}

	public int getStepChance(@Nonnull final IBlockState blockInfo) {
		return this.stepChance;
	}

	public void addSound(@Nonnull final BlockInfo blockInfo, @Nonnull final SoundEffect sound) {
		this.sounds.add(sound);
	}

	public void clearSounds(@Nonnull final BlockInfo blockInfo) {
		this.sounds.clear();
	}

	@Nonnull
	public List<SoundEffect> getSounds(@Nonnull final IBlockState state) {
		return this.sounds;
	}

	public void addStepSound(@Nonnull final BlockInfo blockInfo, @Nonnull final SoundEffect sound) {
		this.stepSounds.add(sound);
	}

	public void clearStepSounds(@Nonnull final BlockInfo blockInfo) {
		this.stepSounds.clear();
	}

	@Nonnull
	public List<SoundEffect> getStepSounds(@Nonnull final IBlockState state) {
		return this.stepSounds;
	}

	public void addEffect(@Nonnull final BlockInfo blockInfo, @Nonnull final BlockEffect effect) {
		if (effect.getChance() > 0)
			this.effects.add(effect);
		else
			this.alwaysOn.add(effect);
	}

	public void clearEffects(@Nonnull final BlockInfo blockInfo) {
		this.effects.clear();
		this.alwaysOn.clear();
	}

	@Nonnull
	public List<BlockEffect> getEffects(@Nonnull final IBlockState state) {
		return this.effects;
	}

	@Nonnull
	List<BlockEffect> getAlwaysOnEffects(@Nonnull final IBlockState state) {
		return this.alwaysOn;
	}

	@Override
	@Nonnull
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Block [").append(this.info.toString()).append("]");

		if (!this.sounds.isEmpty()) {
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

		if (!this.stepSounds.isEmpty()) {
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

		if (!this.effects.isEmpty() || !this.alwaysOn.isEmpty()) {
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
