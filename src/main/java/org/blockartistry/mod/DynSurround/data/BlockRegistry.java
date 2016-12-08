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

package org.blockartistry.mod.DynSurround.data;

import java.io.File;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.Module;
import org.blockartistry.mod.DynSurround.client.fx.BlockEffect;
import org.blockartistry.mod.DynSurround.client.fx.FireFlyEffect;
import org.blockartistry.mod.DynSurround.client.fx.JetEffect;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect.SoundType;
import org.blockartistry.mod.DynSurround.data.config.BlockConfig;
import org.blockartistry.mod.DynSurround.data.config.BlockConfig.Effect;

import org.blockartistry.mod.DynSurround.data.config.SoundConfig;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public final class BlockRegistry {

	private static final Map<Block, BlockProfile> registry = new IdentityHashMap<Block, BlockProfile>();

	public static void initialize() {

		registry.clear();
		processConfig();

		// TODO: Implement toString()...
		if (ModOptions.enableDebugLogging) {
			ModLog.info("*** BLOCK REGISTRY ***");
			for (final BlockProfile entry : registry.values())
				ModLog.info(entry.toString());
		}
	}

	public static List<BlockEffect> getEffects(final IBlockState state) {
		final BlockProfile entry = registry.get(state.getBlock());
		return entry != null ? entry.getEffects(state) : null;
	}

	private static SoundEffect getRandomSound(final List<SoundEffect> list, final Random random,
			final String conditions) {
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

	public static SoundEffect getSound(final IBlockState state, final Random random, final String conditions) {
		final BlockProfile entry = registry.get(state.getBlock());
		if (entry == null || entry.sounds.isEmpty() || random.nextInt(entry.chance) != 0)
			return null;
		return getRandomSound(entry.getSounds(state), random, conditions);
	}

	public static SoundEffect getStepSound(final IBlockState state, final Random random, final String conditions) {
		final BlockProfile entry = registry.get(state.getBlock());
		if (entry == null || entry.stepSounds.isEmpty() || random.nextInt(entry.stepChance) != 0)
			return null;
		return getRandomSound(entry.getStepSounds(state), random, conditions);
	}

	private static void processConfig() {

		// Load block config for Dynamic Surroundings
		try {
			process(BlockConfig.load("blocks"));
		} catch (final Exception e) {
			e.printStackTrace();
		}

		// Check for each of the loaded mods to see if there is
		// a config file embedded.
		for (final ModContainer mod : Loader.instance().getActiveModList()) {
			try {
				process(BlockConfig.load(mod.getModId() + "_blocks"));
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		final String[] configFiles = ModOptions.blockConfigFiles;
		for (final String file : configFiles) {
			final File theFile = new File(Module.dataDirectory(), file);
			if (theFile.exists()) {
				try {
					final BlockConfig config = BlockConfig.load(theFile);
					if (config != null)
						process(config);
					else
						ModLog.warn("Unable to process block config file " + file);
				} catch (final Exception ex) {
					ModLog.error("Unable to process block config file " + file, ex);
				}
			} else {
				ModLog.warn("Could not locate block config file [%s]", file);
			}
		}
	}

	private static void process(final BlockConfig config) {
		for (final BlockConfig.Entry entry : config.entries) {
			if (entry.blocks.isEmpty())
				continue;

			for (final String blockName : entry.blocks) {
				final BlockInfo blockInfo = BlockInfo.create(blockName);
				if(blockInfo == null) {
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

				for (final Effect e : entry.effects) {
					if (StringUtils.isEmpty(e.effect))
						continue;
					BlockEffect blockEffect = null;
					final int chance = e.chance != null ? e.chance.intValue() : 100;
					if (StringUtils.equalsIgnoreCase("steam", e.effect))
						blockEffect = new JetEffect.Steam(chance);
					else if (StringUtils.equalsIgnoreCase("fire", e.effect))
						blockEffect = new JetEffect.Fire(chance);
					else if (StringUtils.equalsIgnoreCase("bubble", e.effect))
						blockEffect = new JetEffect.Bubble(chance);
					else if (StringUtils.equalsIgnoreCase("dust", e.effect))
						blockEffect = new JetEffect.Dust(chance);
					else if (StringUtils.equalsIgnoreCase("fountain", e.effect))
						blockEffect = new JetEffect.Fountain(chance);
					else if(StringUtils.equalsIgnoreCase("firefly", e.effect))
						blockEffect = new FireFlyEffect(chance);
					else {
						ModLog.warn("Unknown effect type in config: '%s'", e.effect);
						continue;
					}

					blockData.addEffect(blockInfo, blockEffect);
				}
			}
		}
	}

}
