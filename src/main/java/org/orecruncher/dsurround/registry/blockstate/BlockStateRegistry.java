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

import java.util.Map;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.client.fx.BlockEffect;
import org.orecruncher.dsurround.client.fx.BlockEffectType;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.config.BlockConfig;
import org.orecruncher.dsurround.registry.config.EffectConfig;
import org.orecruncher.dsurround.registry.config.ModConfiguration;
import org.orecruncher.dsurround.registry.config.SoundConfig;
import org.orecruncher.dsurround.registry.sound.SoundRegistry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class BlockStateRegistry extends Registry {

	private static final BlockStateProfile NO_PROFILE = new BlockStateProfile().setChance(0);

	private Map<BlockStateMatcher, BlockStateProfile> registry;

	public BlockStateRegistry() {
		super("BlockState Data");
	}

	@Override
	public void init() {
		this.registry = new Object2ObjectOpenHashMap<>();

		// Wipe out any cached data
		StreamSupport.stream(ForgeRegistries.BLOCKS.spliterator(), false)
				.map(block -> block.getBlockState().getValidStates()).flatMap(l -> l.stream())
				.forEach(state -> BlockStateUtil.setStateData(state, null));
	}

	@Override
	public void configure(@Nonnull final ModConfiguration cfg) {
		for (final BlockConfig block : cfg.blocks)
			register(block);
	}

	@Nonnull
	public BlockStateProfile get(@Nonnull final IBlockState state) {
		BlockStateProfile profile = BlockStateUtil.getStateData(state);
		if (profile == null) {
			profile = this.registry.get(BlockStateMatcher.asGeneric(state));
			if (profile == null)
				profile = NO_PROFILE;
			BlockStateUtil.setStateData(state, profile);
		}
		return profile;
	}

	@Nullable
	private BlockStateProfile getOrCreateProfile(@Nonnull final BlockStateMatcher info) {
		if (info.getBlock() == Blocks.AIR)
			return null;

		BlockStateProfile profile = this.registry.get(info);
		if (profile == null) {
			profile = new BlockStateProfile();
			this.registry.put(info, profile);
		}

		return profile;
	}

	private void register(@Nonnull final BlockConfig entry) {
		if (entry.blocks.isEmpty())
			return;

		final SoundRegistry soundRegistry = RegistryManager.SOUND;

		for (final String blockName : entry.blocks) {
			final BlockStateMatcher blockInfo = BlockStateMatcher.create(blockName);
			if (blockInfo == null) {
				ModBase.log().warn("Unknown block [%s] in block config file", blockName);
				continue;
			}

			final BlockStateProfile blockData = getOrCreateProfile(blockInfo);
			if (blockData == null) {
				ModBase.log().warn("Unknown block [%s] in block config file", blockName);
				continue;
			}

			// Reset of a block clears all registry
			if (entry.soundReset != null && entry.soundReset.booleanValue())
				blockData.clearSounds();
			if (entry.effectReset != null && entry.effectReset.booleanValue())
				blockData.clearEffects();

			if (entry.chance != null)
				blockData.setChance(entry.chance.intValue());

			for (final SoundConfig sr : entry.sounds) {
				if (sr.sound != null && !soundRegistry.isSoundBlocked(sr.sound)) {
					final SoundEffect.Builder b = new SoundEffect.Builder(sr);
					if (sr.soundCategory == null)
						b.setSoundCategory(SoundCategory.BLOCKS);
					final SoundEffect eff = b.build();
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
