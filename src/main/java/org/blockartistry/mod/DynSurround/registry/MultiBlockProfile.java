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

import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.fx.BlockEffect;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.block.state.IBlockState;

public class MultiBlockProfile extends BlockProfile {

	protected final TIntObjectHashMap<BlockProfile> blockData = new TIntObjectHashMap<BlockProfile>();

	public MultiBlockProfile(@Nonnull final BlockInfo blockInfo) {
		super(blockInfo);
	}

	@Nonnull
	protected BlockProfile getProfileWithCreate(@Nonnull final BlockInfo blockInfo) {
		BlockProfile profile = blockData.get(blockInfo.meta);
		if (profile == null) {
			profile = new BlockProfile(blockInfo);
			blockData.put(blockInfo.meta, profile);
		}
		return profile;
	}

	@Override
	public void setChance(@Nonnull final BlockInfo blockInfo, final int chance) {
		if (blockInfo.isGeneric()) {
			super.setChance(blockInfo, chance);
			return;
		}
		getProfileWithCreate(blockInfo).setChance(blockInfo, chance);
	}

	@Override
	public int getChance(@Nonnull final IBlockState state) {
		final BlockProfile profile = blockData.get(state.getBlock().getMetaFromState(state));
		return profile != null ? profile.getChance(state) : super.getChance(state);
	}

	@Override
	public void setStepChance(@Nonnull final BlockInfo blockInfo, final int chance) {
		if (blockInfo.isGeneric()) {
			super.setStepChance(blockInfo, chance);
			return;
		}
		getProfileWithCreate(blockInfo).setStepChance(blockInfo, chance);
	}

	@Override
	public int getStepChance(@Nonnull final IBlockState state) {
		final BlockProfile profile = blockData.get(state.getBlock().getMetaFromState(state));
		return profile != null ? profile.getStepChance(state) : super.getStepChance(state);
	}

	@Override
	public void addSound(@Nonnull final BlockInfo blockInfo, @Nonnull final SoundEffect sound) {
		if (blockInfo.meta == BlockInfo.GENERIC) {
			super.addSound(blockInfo, sound);
			return;
		}
		getProfileWithCreate(blockInfo).addSound(blockInfo, sound);
	}

	@Override
	public void clearSounds(@Nonnull final BlockInfo blockInfo) {
		if (blockInfo.meta == BlockInfo.GENERIC) {
			super.clearSounds(blockInfo);
			return;
		}
		final BlockProfile profile = blockData.get(blockInfo.meta);
		if (profile != null)
			profile.clearSounds(blockInfo);
	}

	@Override
	@Nonnull
	public List<SoundEffect> getSounds(@Nonnull final IBlockState state) {
		final BlockProfile profile = blockData.get(state.getBlock().getMetaFromState(state));
		return profile != null ? profile.getSounds(state) : super.getSounds(state);
	}

	@Override
	public void addStepSound(@Nonnull final BlockInfo blockInfo, @Nonnull final SoundEffect sound) {
		if (blockInfo.meta == BlockInfo.GENERIC) {
			super.addStepSound(blockInfo, sound);
			return;
		}
		getProfileWithCreate(blockInfo).addStepSound(blockInfo, sound);
	}

	@Override
	public void clearStepSounds(@Nonnull final BlockInfo blockInfo) {
		if (blockInfo.meta == BlockInfo.GENERIC) {
			super.clearStepSounds(blockInfo);
			return;
		}
		final BlockProfile profile = blockData.get(blockInfo.meta);
		if (profile != null)
			profile.clearStepSounds(blockInfo);
	}

	@Override
	@Nonnull
	public List<SoundEffect> getStepSounds(@Nonnull final IBlockState state) {
		final BlockProfile profile = blockData.get(state.getBlock().getMetaFromState(state));
		return profile != null ? profile.getStepSounds(state) : super.getStepSounds(state);
	}

	@Override
	public void addEffect(@Nonnull final BlockInfo blockInfo, @Nonnull final BlockEffect effect) {
		if (blockInfo.meta == BlockInfo.GENERIC) {
			super.addEffect(blockInfo, effect);
			return;
		}
		getProfileWithCreate(blockInfo).addEffect(blockInfo, effect);
	}

	@Override
	public void clearEffects(@Nonnull final BlockInfo blockInfo) {
		if (blockInfo.meta == BlockInfo.GENERIC) {
			super.clearEffects(blockInfo);
			return;
		}
		final BlockProfile profile = blockData.get(blockInfo.meta);
		if (profile != null)
			profile.clearEffects(blockInfo);
	}

	@Override
	@Nonnull
	public List<BlockEffect> getEffects(@Nonnull final IBlockState state) {
		final BlockProfile profile = blockData.get(state.getBlock().getMetaFromState(state));
		return profile != null ? profile.getEffects(state) : super.getEffects(state);
	}

	@Override
	@Nonnull
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DEFAULT ").append(super.toString());
		for (final TIntObjectIterator<BlockProfile> itr = blockData.iterator(); itr.hasNext();) {
			itr.advance();
			builder.append("\nMETA ").append(itr.key()).append(" ").append(itr.value().toString());
		}
		return builder.toString();
	}

}
