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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.fx.BlockEffect;
import org.blockartistry.mod.DynSurround.client.fx.BubbleJetEffect;
import org.blockartistry.mod.DynSurround.client.fx.DustJetEffect;
import org.blockartistry.mod.DynSurround.client.fx.FireFlyEffect;
import org.blockartistry.mod.DynSurround.client.fx.FireJetEffect;
import org.blockartistry.mod.DynSurround.client.fx.FountainJetEffect;
import org.blockartistry.mod.DynSurround.client.fx.SteamJetEffect;
import org.blockartistry.mod.DynSurround.client.fx.WaterSplashJetEffect;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.data.xface.BlockConfig;
import org.blockartistry.mod.DynSurround.data.xface.EffectConfig;
import org.blockartistry.mod.DynSurround.data.xface.SoundConfig;
import org.blockartistry.mod.DynSurround.data.xface.SoundType;
import org.blockartistry.mod.DynSurround.registry.BlockInfo.BlockInfoMutable;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.mod.DynSurround.util.MCHelper;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;

public final class BlockRegistry extends Registry {

	public final static BlockEffect[] NO_EFFECTS = {};
	public final static SoundEffect[] NO_SOUNDS = {};

	BlockRegistry(@Nonnull final Side side) {
		super(side);
	}

	@Override
	public void init() {
		this.registry = new HashMap<BlockInfo, BlockProfile>();
		this.alwaysOnEffects = new HashSet<BlockInfo>();
		this.hasSoundsAndEffects = new HashSet<BlockInfo>();
	}

	@Override
	public void initComplete() {

		// Scan the registry looking for profiles that match what we want.
		for (final BlockProfile profile : this.registry.values()) {
			if (profile.getAlwaysOnEffects().length > 0)
				this.alwaysOnEffects.add(profile.info);
			if (profile.getEffects().length > 0 || profile.getSounds().length > 0)
				this.hasSoundsAndEffects.add(profile.info);
		}

		if (ModOptions.enableDebugLogging) {
			ModLog.info("*** BLOCK REGISTRY ***");
			for (final BlockProfile entry : this.registry.values())
				ModLog.info(entry.toString());

			ModLog.info("**** FORGE ORE DICTIONARY NAMES ****");
			for (final String oreName : OreDictionary.getOreNames())
				ModLog.info(oreName);

			ModLog.info("**** BLOCKS REGISTERED WITH FORGE ****");
			final Iterator<Block> itr = Block.REGISTRY.iterator();
			while (itr.hasNext())
				ModLog.info(MCHelper.nameOf(itr.next()));
		}
		
		this.registry = ImmutableMap.copyOf(this.registry);
		this.alwaysOnEffects = ImmutableSet.copyOf(this.alwaysOnEffects);
		this.hasSoundsAndEffects = ImmutableSet.copyOf(this.hasSoundsAndEffects);
		
	}

	@Override
	public void fini() {

	}

	private Map<BlockInfo, BlockProfile> registry = new HashMap<BlockInfo, BlockProfile>();
	private Set<BlockInfo> alwaysOnEffects = new HashSet<BlockInfo>();
	private Set<BlockInfo> hasSoundsAndEffects = new HashSet<BlockInfo>();

	private final BlockInfoMutable key = new BlockInfoMutable();

	private BlockProfile findProfile(@Nonnull final IBlockState state) {
		BlockProfile profile = this.registry.get(this.key.set(state));
		if (profile == null && this.key.hasSubTypes()) {
			profile = this.registry.get(this.key.asGeneric());
		}
		return profile;
	}

	@Nonnull
	public BlockEffect[] getEffects(@Nonnull final IBlockState state) {
		final BlockProfile entry = findProfile(state);
		return entry != null ? entry.getEffects() : NO_EFFECTS;
	}

	@Nonnull
	public BlockEffect[] getAlwaysOnEffects(@Nonnull final IBlockState state) {
		final BlockProfile entry = findProfile(state);
		return entry != null ? entry.getAlwaysOnEffects() : NO_EFFECTS;
	}

	@Nonnull
	private SoundEffect getRandomSound(@Nonnull final SoundEffect[] list, @Nonnull final Random random) {

		// Degenerative case of a single sound
		if (list.length == 1) {
			return list[0].matches() ? list[0] : null;
		}

		// Build a weight table on the fly
		int totalWeight = 0;
		final List<SoundEffect> candidates = new ArrayList<SoundEffect>();
		for (final SoundEffect s : list)
			if (s.weight > 0 && s.matches()) {
				candidates.add(s);
				totalWeight += s.weight;
			}

		// It's possible all the sounds got filtered out
		if (totalWeight <= 0)
			return null;

		// Possible that there is a single candidate
		if (candidates.size() == 1)
			return candidates.get(0);

		int targetWeight = random.nextInt(totalWeight);
		int i = 0;
		for (i = candidates.size(); (targetWeight -= candidates.get(i - 1).weight) >= 0; i--)
			;

		return candidates.get(i - 1);
	}

	@Nonnull
	public SoundEffect[] getAllSounds(@Nonnull final IBlockState state) {
		final BlockProfile entry = findProfile(state);
		return entry != null ? entry.getSounds() : NO_SOUNDS;
	}

	@Nonnull
	public SoundEffect[] getAllStepSounds(@Nonnull final IBlockState state) {
		final BlockProfile entry = findProfile(state);
		return entry != null ? entry.getStepSounds() : NO_SOUNDS;
	}

	@Nullable
	public SoundEffect getSound(@Nonnull final IBlockState state, @Nonnull final Random random) {
		final BlockProfile entry = findProfile(state);
		final SoundEffect[] sounds;
		if (entry == null || (sounds = entry.getSounds()) == NO_SOUNDS || random.nextInt(entry.getChance()) != 0)
			return null;

		return getRandomSound(sounds, random);
	}

	public int getStepSoundChance(@Nonnull final IBlockState state) {
		final BlockProfile entry = findProfile(state);
		return entry != null ? entry.getStepChance() : 0;
	}

	public int getSoundChance(@Nonnull final IBlockState state) {
		final BlockProfile entry = findProfile(state);
		return entry != null ? entry.getChance() : 0;
	}

	@Nullable
	public SoundEffect getStepSound(@Nonnull final IBlockState state, @Nonnull final Random random) {
		final BlockProfile entry = findProfile(state);
		final SoundEffect[] sounds;
		if (entry == null || (sounds = entry.getSounds()) == NO_SOUNDS || random.nextInt(entry.getStepChance()) != 0)
			return null;

		return getRandomSound(sounds, random);
	}

	private boolean isInteresting(@Nonnull final Set<BlockInfo> data, @Nonnull final BlockInfo info) {
		if (info.getBlock() == Blocks.AIR)
			return false;

		if (data.contains(info))
			return true;

		if (!info.hasSubTypes())
			return false;

		return data.contains(this.key.set(info).asGeneric());
	}

	public boolean hasAlwaysOnEffects(@Nonnull final BlockInfo info) {
		return isInteresting(this.alwaysOnEffects, info);
	}

	public boolean hasEffectsOrSounds(@Nonnull final BlockInfo info) {
		return isInteresting(this.hasSoundsAndEffects, info);
	}

	@Nullable
	protected BlockProfile getOrCreateProfile(@Nonnull final BlockInfo info) {
		if (info.getBlock() == Blocks.AIR)
			return null;

		BlockProfile profile = this.registry.get(info);
		if (profile == null) {
			profile = BlockProfile.createProfile(info);
			this.registry.put(info, profile);
		}
		return profile;
	}

	public void register(@Nonnull final BlockConfig entry) {
		if (entry.blocks.isEmpty())
			return;

		final SoundRegistry soundRegistry = RegistryManager.getManager().getRegistry(RegistryType.SOUND);

		for (final String blockName : entry.blocks) {
			final BlockInfo blockInfo = BlockInfo.create(blockName);
			if (blockInfo == null) {
				ModLog.warn("Unknown block [%s] in block config file", blockName);
				continue;
			}

			final BlockProfile blockData = getOrCreateProfile(blockInfo);
			if (blockData == null) {
				ModLog.warn("Unknown block [%s] in block config file", blockName);
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
					final SoundEffect eff = new SoundEffect(sr);
					if (eff.type == SoundType.STEP)
						blockData.addStepSound(eff);
					else
						blockData.addSound(eff);
				}
			}

			for (final EffectConfig e : entry.effects) {
				if (StringUtils.isEmpty(e.effect))
					continue;
				BlockEffect blockEffect = null;
				final int chance = e.chance != null ? e.chance.intValue() : 100;
				if (StringUtils.equalsIgnoreCase("steam", e.effect)) {
					if (ModOptions.enableSteamJets)
						blockEffect = new SteamJetEffect(chance);
				} else if (StringUtils.equalsIgnoreCase("fire", e.effect)) {
					if (ModOptions.enableFireJets)
						blockEffect = new FireJetEffect(chance);
				} else if (StringUtils.equalsIgnoreCase("bubble", e.effect)) {
					if (ModOptions.enableBubbleJets)
						blockEffect = new BubbleJetEffect(chance);
				} else if (StringUtils.equalsIgnoreCase("dust", e.effect)) {
					if (ModOptions.enableDustJets)
						blockEffect = new DustJetEffect(chance);
				} else if (StringUtils.equalsIgnoreCase("fountain", e.effect)) {
					if (ModOptions.enableFountainJets)
						blockEffect = new FountainJetEffect(chance);
				} else if (StringUtils.equalsIgnoreCase("firefly", e.effect)) {
					if (ModOptions.enableFireflies)
						blockEffect = new FireFlyEffect(chance);
				} else if (StringUtils.equalsIgnoreCase("splash", e.effect)) {
					if (ModOptions.enableWaterSplash)
						blockEffect = new WaterSplashJetEffect(chance);
				} else {
					ModLog.warn("Unknown effect type in config: '%s'", e.effect);
					continue;
				}

				if (blockEffect != null) {
					if (e.conditions != null)
						blockEffect.setConditions(e.conditions);
					blockData.addEffect(blockEffect);
				}
			}
		}
	}
}
