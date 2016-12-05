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
import org.blockartistry.mod.DynSurround.client.fx.JetEffect;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect.SoundType;
import org.blockartistry.mod.DynSurround.compat.MCHelper;
import org.blockartistry.mod.DynSurround.data.config.BlockConfig;
import org.blockartistry.mod.DynSurround.data.config.BlockConfig.Effect;

import org.blockartistry.mod.DynSurround.data.config.SoundConfig;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public final class BlockRegistry {

	private static final Map<Block, Entry> registry = new IdentityHashMap<Block, Entry>();

	private static final class Entry {
		public final Block block;
		public int chance = 100;
		public int stepChance = 100;
		public final List<SoundEffect> sounds = new ArrayList<SoundEffect>();
		public final List<SoundEffect> stepSounds = new ArrayList<SoundEffect>();
		public final List<BlockEffect> effects = new ArrayList<BlockEffect>();

		public Entry(final Block block) {
			this.block = block;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append(String.format("Block [%s]:", this.block.getUnlocalizedName()));

			if (!this.sounds.isEmpty()) {
				builder.append(" chance:").append(this.chance);
				builder.append("; sounds [");
				for (final SoundEffect sound : this.sounds)
					builder.append(sound.toString()).append(',');
				builder.append(']');
			}

			if (!this.stepSounds.isEmpty()) {
				builder.append(" chance:").append(this.stepChance);
				builder.append("; step sounds [");
				for (final SoundEffect sound : this.stepSounds)
					builder.append(sound.toString()).append(',');
				builder.append(']');
			}

			if (!this.effects.isEmpty()) {
				builder.append("; effects [");
				for (final BlockEffect effect : this.effects)
					builder.append(effect.toString()).append(',');
				builder.append(']');
			}

			return builder.toString();
		}
	}

	public static void initialize() {

		registry.clear();
		processConfig();

		if (ModOptions.enableDebugLogging) {
			ModLog.info("*** BLOCK REGISTRY ***");
			for (final Entry entry : registry.values())
				ModLog.info(entry.toString());
		}
	}

	public static List<BlockEffect> getEffects(final Block block) {
		final Entry entry = registry.get(block);
		return entry != null ? entry.effects : null;
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

	public static SoundEffect getSound(final Block block, final Random random, final String conditions) {
		final Entry entry = registry.get(block);
		if (entry == null || entry.sounds.isEmpty() || random.nextInt(entry.chance) != 0)
			return null;
		return getRandomSound(entry.sounds, random, conditions);
	}

	public static SoundEffect getStepSound(final Block block, final Random random, final String conditions) {
		final Entry entry = registry.get(block);
		if (entry == null || entry.stepSounds.isEmpty() || random.nextInt(entry.stepChance) != 0)
			return null;
		return getRandomSound(entry.stepSounds, random, conditions);
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
				final Block block = MCHelper.getBlockByName(blockName);
				if (block == null || block == Blocks.AIR) {
					ModLog.warn("Unknown block [%s] in block config file", blockName);
					continue;
				}

				Entry blockData = registry.get(block);
				if (blockData == null) {
					blockData = new Entry(block);
					registry.put(block, blockData);
				}

				// Reset of a block clears all registry
				if (entry.soundReset != null && entry.soundReset.booleanValue())
					blockData.sounds.clear();
				if (entry.stepSoundReset != null && entry.stepSoundReset.booleanValue())
					blockData.stepSounds.clear();
				if (entry.effectReset != null && entry.effectReset.booleanValue())
					blockData.effects.clear();

				if (entry.chance != null)
					blockData.chance = entry.chance.intValue();
				if (entry.stepChance != null)
					blockData.stepChance = entry.stepChance.intValue();

				for (final SoundConfig sr : entry.sounds) {
					if (sr.sound != null && !SoundRegistry.isSoundBlocked(sr.sound)) {
						final SoundEffect eff = new SoundEffect(sr);
						if (eff.type == SoundType.STEP)
							blockData.stepSounds.add(eff);
						else
							blockData.sounds.add(eff);
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
					else {
						ModLog.warn("Unknown effect type in config: '%s'", e.effect);
						continue;
					}

					blockData.effects.add(blockEffect);
				}
			}
		}
	}

}
