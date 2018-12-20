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

package org.orecruncher.dsurround.registry.blockstate;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.client.fx.BlockEffect;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.lib.MyUtils;
import org.orecruncher.lib.WeightTable;

import com.google.common.base.Joiner;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockStateProfile extends BlockStateData {

	protected int chance = 100;
	protected SoundEffect[] sounds = NO_SOUNDS;
	protected BlockEffect[] effects = NO_EFFECTS;
	protected BlockEffect[] alwaysOn = NO_EFFECTS;

	@Nonnull
	public BlockStateProfile setChance(final int chance) {
		this.chance = chance;
		return this;
	}

	public int getChance() {
		return this.chance;
	}

	@Nonnull
	public BlockStateProfile addSound(@Nonnull final SoundEffect sound) {
		this.sounds = MyUtils.append(this.sounds, sound);
		return this;
	}

	@Nonnull
	public BlockStateProfile clearSounds() {
		this.sounds = NO_SOUNDS;
		return this;
	}

	@Override
	@Nonnull
	public SoundEffect[] getSounds() {
		return this.sounds;
	}

	@Nonnull
	public BlockStateProfile addEffect(@Nonnull final BlockEffect effect) {
		if (effect.getChance() > 0)
			this.effects = MyUtils.append(this.effects, effect);
		else
			this.alwaysOn = MyUtils.append(this.alwaysOn, effect);
		return this;
	}

	@Nonnull
	public BlockStateProfile clearEffects() {
		this.effects = NO_EFFECTS;
		this.alwaysOn = NO_EFFECTS;
		return this;
	}

	@Override
	@Nonnull
	public BlockEffect[] getEffects() {
		return this.effects;
	}

	@Override
	@Nonnull
	public BlockEffect[] getAlwaysOnEffects() {
		return this.alwaysOn;
	}

	@Override
	@Nullable
	public SoundEffect getSoundToPlay(@Nonnull final Random random) {
		return this.sounds != NO_SOUNDS && random.nextInt(getChance()) == 0 ? new WeightTable<>(this.sounds).next()
				: null;
	}

	@Override
	public boolean hasSoundsOrEffects() {
		return this.sounds.length > 0 || this.effects.length > 0;
	}

	@Override
	public boolean hasAlwaysOnEffects() {
		return this.alwaysOn.length > 0;
	}

	@Override
	@Nonnull
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		if (this.sounds != NO_SOUNDS) {
			builder.append(" chance:").append(this.chance);
			builder.append("; sounds [");
			builder.append(Joiner.on(',').join(this.sounds));
			builder.append(']');
		} else {
			builder.append("NO SOUNDS");
		}

		if (this.effects != this.alwaysOn) {
			builder.append("; effects [");
			builder.append(Joiner.on(',').join(this.effects));
			builder.append(',');
			builder.append(Joiner.on(',').join(this.alwaysOn));
			builder.append(']');
		} else {
			builder.append("; NO EFFECTS");
		}

		return builder.toString();
	}
}
