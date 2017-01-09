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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.mod.DynSurround.util.MCHelper;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;

public final class BlockRegistry extends Registry {

	protected final static List<BlockEffect> NO_EFFECTS = ImmutableList.of();
	protected final static List<SoundEffect> NO_SOUNDS = ImmutableList.of();

	BlockRegistry(@Nonnull final Side side) {
		super(side);
	}

	@Override
	public void init() {
		registry.clear();
	}

	@Override
	public void initComplete() {
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
	}

	@Override
	public void fini() {

	}

	private final Map<Block, BlockProfile> registry = new IdentityHashMap<Block, BlockProfile>();

	@Nonnull
	public List<BlockEffect> getEffects(@Nonnull final IBlockState state) {
		final BlockProfile entry = this.registry.get(state.getBlock());
		return entry != null ? entry.getEffects(state) : NO_EFFECTS;
	}

	@Nonnull
	public List<BlockEffect> getAlwaysOnEffects(@Nonnull final IBlockState state) {
		final BlockProfile entry = this.registry.get(state.getBlock());
		return entry != null ? entry.getAlwaysOnEffects(state) : NO_EFFECTS;
	}

	@Nonnull
	private SoundEffect getRandomSound(@Nonnull final List<SoundEffect> list, @Nonnull final Random random) {

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

		if (candidates.size() == 1)
			return candidates.get(0);

		int targetWeight = random.nextInt(totalWeight);
		int i = 0;
		for (i = candidates.size(); (targetWeight -= candidates.get(i - 1).weight) >= 0; i--)
			;

		return candidates.get(i - 1);
	}

	@Nonnull
	public List<SoundEffect> getAllSounds(@Nonnull final IBlockState state) {
		final BlockProfile entry = this.registry.get(state.getBlock());
		if (entry == null)
			return NO_SOUNDS;

		final List<SoundEffect> sounds = entry.getSounds(state);
		if (sounds.isEmpty())
			return NO_SOUNDS;

		return sounds;
	}

	@Nonnull
	public List<SoundEffect> getAllStepSounds(@Nonnull final IBlockState state) {
		// Air and liquid have no step sounds so optimize that out
		if (state.getMaterial() == Material.AIR || state.getMaterial().isLiquid())
			return NO_SOUNDS;

		final BlockProfile entry = this.registry.get(state.getBlock());
		if (entry == null)
			return NO_SOUNDS;

		final List<SoundEffect> sounds = entry.getStepSounds(state);
		if (sounds.isEmpty())
			return NO_SOUNDS;

		return sounds;
	}

	@Nullable
	public SoundEffect getSound(@Nonnull final IBlockState state, @Nonnull final Random random) {
		final BlockProfile entry = this.registry.get(state.getBlock());
		if (entry == null)
			return null;

		final List<SoundEffect> sounds = entry.getSounds(state);
		if (sounds.isEmpty())
			return null;

		if (random.nextInt(entry.getChance(state)) != 0)
			return null;

		return getRandomSound(sounds, random);
	}

	@Nullable
	public SoundEffect getStepSound(@Nonnull final IBlockState state, @Nonnull final Random random) {

		// Air and liquid have no step sounds so optimize that out
		if (state.getMaterial() == Material.AIR || state.getMaterial().isLiquid())
			return null;

		final BlockProfile entry = this.registry.get(state.getBlock());
		if (entry == null)
			return null;

		final List<SoundEffect> sounds = entry.getStepSounds(state);
		if (sounds.isEmpty())
			return null;

		if (random.nextInt(entry.getStepChance(state)) != 0)
			return null;

		return getRandomSound(sounds, random);
	}

	public boolean hasEffectsOrSounds(@Nonnull final IBlockState state) {
		final BlockProfile entry = this.registry.get(state.getBlock());
		return entry != null && !(entry.getEffects(state).isEmpty() && entry.getSounds(state).isEmpty());
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

			final Block block = blockInfo.getBlock();
			if (block == null || block == Blocks.AIR) {
				ModLog.warn("Unknown block [%s] in block config file", blockName);
				continue;
			}

			BlockProfile blockData = this.registry.get(block);
			if (blockData == null) {
				blockData = BlockProfile.createProfile(blockInfo);
				this.registry.put(block, blockData);
			}

			// Reset of a block clears all registry
			if (entry.soundReset != null && entry.soundReset.booleanValue())
				blockData.clearSounds(blockInfo);
			if (entry.stepSoundReset != null && entry.stepSoundReset.booleanValue())
				blockData.clearStepSounds(blockInfo);
			if (entry.effectReset != null && entry.effectReset.booleanValue())
				blockData.clearEffects(blockInfo);

			if (entry.chance != null)
				blockData.setChance(blockInfo, entry.chance.intValue());
			if (entry.stepChance != null)
				blockData.setStepChance(blockInfo, entry.stepChance.intValue());

			for (final SoundConfig sr : entry.sounds) {
				if (sr.sound != null && !soundRegistry.isSoundBlocked(sr.sound)) {
					final SoundEffect eff = new SoundEffect(sr);
					if (eff.type == SoundType.STEP)
						blockData.addStepSound(blockInfo, eff);
					else
						blockData.addSound(blockInfo, eff);
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

				if (e.conditions != null)
					blockEffect.setConditions(e.conditions);

				blockData.addEffect(blockInfo, blockEffect);
			}
		}
	}
}
