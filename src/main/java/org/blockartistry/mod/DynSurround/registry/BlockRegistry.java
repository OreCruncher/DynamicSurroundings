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
import org.blockartistry.mod.DynSurround.client.fx.FireFlyEffect;
import org.blockartistry.mod.DynSurround.client.fx.JetEffect;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.data.xface.BlockConfig;
import org.blockartistry.mod.DynSurround.data.xface.EffectConfig;
import org.blockartistry.mod.DynSurround.data.xface.SoundConfig;
import org.blockartistry.mod.DynSurround.data.xface.SoundType;
import org.blockartistry.mod.DynSurround.registry.DataScripts.IDependent;
import org.blockartistry.mod.DynSurround.util.MCHelper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.oredict.OreDictionary;

public final class BlockRegistry implements IDependent {

	private final static BlockRegistry INSTANCE = new BlockRegistry();

	private BlockRegistry() {
		DataScripts.registerDependent(this);
	}

	@Override
	public void preInit() {
		registry.clear();
	}

	@Override
	public void postInit() {
		if (ModOptions.enableDebugLogging) {
			ModLog.info("*** BLOCK REGISTRY ***");
			for (final BlockProfile entry : registry.values())
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

	private static final Map<Block, BlockProfile> registry = new IdentityHashMap<Block, BlockProfile>();

	public static void initialize() {
		INSTANCE.preInit();
	}

	@Nullable
	public static List<BlockEffect> getEffects(@Nonnull final IBlockState state) {
		final BlockProfile entry = registry.get(state.getBlock());
		return entry != null ? entry.getEffects(state) : null;
	}

	@Nonnull
	private static SoundEffect getRandomSound(@Nonnull final List<SoundEffect> list, @Nonnull final Random random,
			@Nonnull final String conditions) {
		int totalWeight = 0;
		final List<SoundEffect> candidates = new ArrayList<SoundEffect>();
		for (final SoundEffect s : list)
			if (s.matches(conditions)) {
				candidates.add(s);
				totalWeight += s.weight;
			}
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

	@Nullable
	public static SoundEffect getSound(@Nonnull final IBlockState state, @Nonnull final Random random,
			@Nonnull final String conditions) {
		final BlockProfile entry = registry.get(state.getBlock());
		if (entry == null)
			return null;

		final List<SoundEffect> sounds = entry.getSounds(state);
		if (sounds.isEmpty())
			return null;

		if (random.nextInt(entry.getChance(state)) != 0)
			return null;

		return getRandomSound(sounds, random, conditions);
	}

	@Nullable
	public static SoundEffect getStepSound(@Nonnull final IBlockState state, @Nonnull final Random random,
			@Nonnull final String conditions) {
		final BlockProfile entry = registry.get(state.getBlock());
		if (entry == null)
			return null;

		final List<SoundEffect> sounds = entry.getStepSounds(state);
		if (sounds.isEmpty())
			return null;

		if (random.nextInt(entry.getStepChance(state)) != 0)
			return null;

		return getRandomSound(sounds, random, conditions);
	}

	public static void register(@Nonnull final BlockConfig entry) {
		if (entry.blocks.isEmpty())
			return;

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

			BlockProfile blockData = registry.get(block);
			if (blockData == null) {
				blockData = BlockProfile.createProfile(blockInfo);
				registry.put(block, blockData);
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
				if (sr.sound != null && !SoundRegistry.isSoundBlocked(sr.sound)) {
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
						blockEffect = new JetEffect.Steam(chance);
				} else if (StringUtils.equalsIgnoreCase("fire", e.effect)) {
					if (ModOptions.enableFireJets)
						blockEffect = new JetEffect.Fire(chance);
				} else if (StringUtils.equalsIgnoreCase("bubble", e.effect)) {
					if (ModOptions.enableBubbleJets)
						blockEffect = new JetEffect.Bubble(chance);
				} else if (StringUtils.equalsIgnoreCase("dust", e.effect)) {
					if (ModOptions.enableDustJets)
						blockEffect = new JetEffect.Dust(chance);
				} else if (StringUtils.equalsIgnoreCase("fountain", e.effect)) {
					if (ModOptions.enableFountainJets)
						blockEffect = new JetEffect.Fountain(chance);
				} else if (StringUtils.equalsIgnoreCase("firefly", e.effect)) {
					if (ModOptions.enableFireflies)
						blockEffect = new FireFlyEffect(chance);
				} else {
					ModLog.warn("Unknown effect type in config: '%s'", e.effect);
					continue;
				}

				blockData.addEffect(blockInfo, blockEffect);
			}
		}
	}
}
