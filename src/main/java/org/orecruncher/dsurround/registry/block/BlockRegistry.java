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

package org.orecruncher.dsurround.registry.block;

import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.client.ClientRegistry;
import org.orecruncher.dsurround.client.fx.BlockEffect;
import org.orecruncher.dsurround.client.fx.BlockEffectType;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.config.BlockConfig;
import org.orecruncher.dsurround.registry.config.EffectConfig;
import org.orecruncher.dsurround.registry.config.ModConfiguration;
import org.orecruncher.dsurround.registry.config.SoundConfig;
import org.orecruncher.dsurround.registry.config.SoundType;
import org.orecruncher.dsurround.registry.sound.SoundRegistry;

import com.google.common.collect.ImmutableMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class BlockRegistry extends Registry {

	private static final BlockProfile NO_PROFILE = BlockProfile.createProfile(BlockMatcher.AIR).setChance(0)
			.setStepChance(0);

	private Map<BlockMatcher, BlockProfile> registry;
	private Map<IBlockState, BlockProfile> cache;

	public BlockRegistry(@Nonnull final Side side) {
		super(side);
	}

	@Override
	public void init() {
		this.registry = new Object2ObjectOpenHashMap<>();
		this.cache = new Reference2ObjectOpenHashMap<>();
	}

	@Override
	public void configure(@Nonnull final ModConfiguration cfg) {
		for (final BlockConfig block : cfg.blocks)
			register(block);
	}

	@Override
	public void initComplete() {
		this.registry = ImmutableMap.copyOf(this.registry);
	}

	@Override
	public void fini() {

	}

	@Nonnull
	public BlockProfile findProfile(@Nonnull final IBlockState state) {
		BlockProfile profile = this.cache.get(state);
		if (profile == null) {
			profile = this.registry.get(BlockMatcher.asGeneric(state));
			if (profile == null)
				profile = NO_PROFILE;
			this.cache.put(state, profile);
		}
		return profile;
	}

	@Nonnull
	public BlockEffect[] getEffects(@Nonnull final IBlockState state) {
		return findProfile(state).getEffects();
	}

	@Nonnull
	public BlockEffect[] getAlwaysOnEffects(@Nonnull final IBlockState state) {
		return findProfile(state).getAlwaysOnEffects();
	}

	@Nonnull
	public SoundEffect[] getAllSounds(@Nonnull final IBlockState state) {
		return findProfile(state).getSounds();
	}

	@Nonnull
	public SoundEffect[] getAllStepSounds(@Nonnull final IBlockState state) {
		return findProfile(state).getStepSounds();
	}

	public int getStepSoundChance(@Nonnull final IBlockState state) {
		return findProfile(state).getStepChance();
	}

	public int getSoundChance(@Nonnull final IBlockState state) {
		return findProfile(state).getChance();
	}

	@Nullable
	public SoundEffect getStepSoundToPlay(@Nonnull final IBlockState state, @Nonnull final Random rand) {
		return findProfile(state).getStepSoundToPlay(rand);
	}

	@Nullable
	public SoundEffect getSoundToPlay(@Nonnull final IBlockState state, @Nonnull final Random rand) {
		return findProfile(state).getSoundToPlay(rand);
	}

	@Nullable
	protected BlockProfile getOrCreateProfile(@Nonnull final BlockMatcher info) {
		if (info.getBlock() == Blocks.AIR)
			return null;

		// If the BlockMatch is a generic it will be found in the registry, else
		// it will be found in the cache.
		BlockProfile profile = info.isGeneric() ? this.registry.get(info) : this.cache.get(info.getBlockstate());
		if (profile == null) {
			profile = BlockProfile.createProfile(info);
			if (info.isGeneric())
				this.registry.put(info, profile);
			else
				this.cache.put(info.getBlockstate(), profile);
		}

		return profile;
	}

	public void register(@Nonnull final BlockConfig entry) {
		if (entry.blocks.isEmpty())
			return;

		final SoundRegistry soundRegistry = ClientRegistry.SOUND;

		for (final String blockName : entry.blocks) {
			final BlockMatcher blockInfo = BlockMatcher.create(blockName);
			if (blockInfo == null) {
				ModBase.log().warn("Unknown block [%s] in block config file", blockName);
				continue;
			}

			final BlockProfile blockData = getOrCreateProfile(blockInfo);
			if (blockData == null) {
				ModBase.log().warn("Unknown block [%s] in block config file", blockName);
				continue;
			}

			// Reset of a block clears all registry
			if (entry.soundReset != null && entry.soundReset.booleanValue())
				blockData.clearSounds();
			if (entry.stepSoundReset != null && entry.stepSoundReset.booleanValue())
				blockData.clearStepSounds();
			if (entry.effectReset != null && entry.effectReset.booleanValue())
				blockData.clearEffects();

			if (entry.chance != null)
				blockData.setChance(entry.chance.intValue());
			if (entry.stepChance != null)
				blockData.setStepChance(entry.stepChance.intValue());

			for (final SoundConfig sr : entry.sounds) {
				if (sr.sound != null && !soundRegistry.isSoundBlocked(sr.sound)) {
					final SoundEffect.Builder b = new SoundEffect.Builder(sr);
					if (sr.soundCategory == null)
						b.setSoundCategory(SoundCategory.BLOCKS);
					final SoundEffect eff = b.build();
					if (eff.getSoundType() == SoundType.STEP)
						blockData.addStepSound(eff);
					else
						blockData.addSound(eff);
				}
			}

			for (final EffectConfig e : entry.effects) {

				if (StringUtils.isEmpty(e.effect))
					continue;

				final BlockEffectType type = BlockEffectType.get(e.effect);
				if (type == BlockEffectType.UNKNOWN) {
					ModBase.log().warn("Unknown block effect type in configuration: [%s]", e.effect);
				} else if (type.isEnabled()) {
					final int chance = e.chance != null ? e.chance.intValue() : 100;
					final BlockEffect blockEffect = type.getInstance(chance);
					if (blockEffect != null) {
						if (e.conditions != null)
							blockEffect.setConditions(e.conditions);
						blockData.addEffect(blockEffect);
					}
				}
			}
		}
	}
}
