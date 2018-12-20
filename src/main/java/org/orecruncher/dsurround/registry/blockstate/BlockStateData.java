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

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Base class for the data being assigned into the IBlockState implementation.
 */
@SideOnly(Side.CLIENT)
public class BlockStateData {

	public final static BlockEffect[] NO_EFFECTS = {};
	public final static SoundEffect[] NO_SOUNDS = {};
	
	protected static final BlockStateData DEFAULT = new BlockStateData();
	
	@Nonnull
	public BlockStateData setChance(final int chance) {
		return this;
	}

	public int getChance() {
		return 0;
	}

	@Nonnull
	public BlockStateData addSound(@Nonnull final SoundEffect sound) {
		return this;
	}

	@Nonnull
	public BlockStateData clearSounds() {
		return this;
	}

	@Nonnull
	public SoundEffect[] getSounds() {
		return NO_SOUNDS;
	}

	@Nonnull
	public BlockStateData addEffect(@Nonnull final BlockEffect effect) {
		return this;
	}

	@Nonnull
	public BlockStateData clearEffects() {
		return this;
	}

	@Nonnull
	public BlockEffect[] getEffects() {
		return NO_EFFECTS;
	}

	@Nonnull
	public BlockEffect[] getAlwaysOnEffects() {
		return NO_EFFECTS;
	}

	@Nullable
	public SoundEffect getSoundToPlay(@Nonnull final Random random) {
		return null;
	}

	public boolean hasSoundsOrEffects() {
		return false;
	}

	public boolean hasAlwaysOnEffects() {
		return false;
	}

	@Override
	@Nonnull
	public String toString() {
		return "<Default BlockStateData>";
	}
}
