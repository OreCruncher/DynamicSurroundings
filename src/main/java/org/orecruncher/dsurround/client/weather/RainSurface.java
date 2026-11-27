/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.orecruncher.dsurround.client.weather;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.tags.BlockTags;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * What the rain is falling on, from the 26.x line: the rain sounds like what it lands on (see {@link RainMix}), and wet
 * steps use it (hard ground slaps, soft ground takes the rain in).
 * <p>
 * Looked up in order: something hot or water, then the {@code rain_on_*} tags (for packs, and blocks whose sound type
 * says the wrong thing), then {@code rain_absorbing}, then the block's sound type, so a mod's blocks mostly sort
 * themselves. Anything else is hard ground: stone, brick. 1.12.2 has few sound types: leaves step as plants, as grass
 * does, so leaves are by tag ({@code rain_on_leaves}, with the ore dictionary's {@code treeLeaves}), and plants are
 * otherwise soft.
 * <p>
 * Each surface has the rain sound's volume and pitch on it, as shares of what they would be: 26.x's numbers without
 * enhanced sound processing (set by ear on its rain-surfaces bench, 2026-11-21), which are all 1.12.2 can use, having
 * no sound filters. A volume over 1 still makes the rain louder, as the rain plays well under the sound engine's limit
 * of 1. Leaves, metal, glass and water also have rain of their own ({@link #layer}), recordings played in place of most
 * of the rain there (see {@link RainMix}), as pitch can't add a ring, a tick or a patter. The inspection overlay gives a
 * block's surface and what sorted it ({@link #describe}), and the diagnostics overlay what the last rain sounds fell on
 * ({@link RainSurfaceTally}).
 */
@SideOnly(Side.CLIENT)
public enum RainSurface {
	// Stone, brick, concrete: the rain sounds as it always has
	HARD(1F, 1F),
	// Grass, dirt, sand, snow, wool: duller and quieter, the ground takes the drops in
	SOFT(0.4F, 0.75F),
	// A canopy or leaf litter: a busier, lighter patter
	LEAVES(1.6F, 1.3F),
	// Planks, logs, a wooden roof: boxy, a little lower
	WOOD(1F, 0.8F),
	// Iron, an anvil: thin and ringing, louder
	METAL(1.6F, 1.5F),
	// Glass, ice: bright ticks
	GLASS(1.3F, 1.6F),
	// Open water: a softer hiss
	WATER(0.7F, 1.3F),
	// Lava, magma: a thin sizzle
	HOT(0.6F, 1.7F);

	private final float volume;
	private final float pitch;

	RainSurface(final float volume, final float pitch) {
		this.volume = volume;
		this.pitch = pitch;
	}

	/**
	 * The rain sound's volume on this surface, as a share of what it would be
	 */
	public float volume() {
		return this.volume;
	}

	/**
	 * The rain sound's pitch on this surface, as a share of what it would be
	 */
	public float pitch() {
		return this.pitch;
	}

	/**
	 * The surface's own rain, played in place of most of the rain where a rain sound lands on it, or null for none: what
	 * pitch can't give (a tin roof's ring, drops ticking on glass, pattering on leaves, plinking on water)
	 */
	@Nullable
	public ResourceLocation layer() {
		switch (this) {
		case LEAVES:
			return Layers.LEAVES;
		case METAL:
			return Layers.METAL;
		case GLASS:
			return Layers.GLASS;
		case WATER:
			return Layers.WATER;
		default:
			return null;
		}
	}

	/**
	 * What the rain falls on at a block
	 */
	@Nonnull
	public static RainSurface of(@Nonnull final World world, @Nonnull final BlockPos pos) {
		final IBlockState state = world.getBlockState(pos);
		if (isHot(state))
			return HOT;
		if (isWater(state))
			return WATER;
		return Sorting.sort(state, world, pos).surface;
	}

	/**
	 * What the block at a place is to the rain and why, for the inspection overlay: "metal (sound type METAL)",
	 * "leaves (tag dsurround:effects/rain_on_leaves)"
	 */
	@Nonnull
	public static String describe(@Nonnull final World world, @Nonnull final BlockPos pos) {
		final IBlockState state = world.getBlockState(pos);
		if (isHot(state))
			return "hot (lava or magma)";
		if (isWater(state))
			return "water";
		final Sorted sorted = Sorting.sort(state, world, pos);
		final String name = sorted.surface.name().toLowerCase(Locale.ROOT);
		if (sorted.tag != null)
			return name + " (tag " + sorted.tag + ")";
		if (sorted.surface != HARD)
			return name + " (sound type " + Sorting.soundTypeName(Sorting.soundOf(state, world, pos)) + ")";
		return name + " (no tag or sound type says otherwise)";
	}

	private static boolean isHot(@Nonnull final IBlockState state) {
		return state.getMaterial() == Material.LAVA || state.getBlock() == Blocks.MAGMA;
	}

	private static boolean isWater(@Nonnull final IBlockState state) {
		return state.getMaterial() == Material.WATER;
	}

	// The surfaces' own rain (sounds.json)
	private static final class Layers {
		static final ResourceLocation LEAVES = new ResourceLocation("dsurround", "rain.leaves");
		static final ResourceLocation METAL = new ResourceLocation("dsurround", "rain.metal");
		static final ResourceLocation GLASS = new ResourceLocation("dsurround", "rain.glass");
		static final ResourceLocation WATER = new ResourceLocation("dsurround", "rain.water");

		private Layers() {
		}
	}

	/**
	 * A surface and the tag that gave it, null when the sound type (or nothing) did
	 */
	private static final class Sorted {
		final RainSurface surface;
		@Nullable
		final String tag;

		Sorted(@Nonnull final RainSurface surface, @Nullable final String tag) {
			this.surface = surface;
			this.tag = tag;
		}
	}

	// Apart from the enum and made on first use, so that the enum can be used where the game's sounds aren't (tests)
	private static final class Sorting {

		private static final Set<SoundType> METAL_SOUNDS = set(SoundType.METAL, SoundType.ANVIL);
		private static final Set<SoundType> GLASS_SOUNDS = set(SoundType.GLASS);
		private static final Set<SoundType> WOOD_SOUNDS = set(SoundType.WOOD, SoundType.LADDER);
		private static final Set<SoundType> SOFT_SOUNDS = set(SoundType.GROUND, SoundType.PLANT, SoundType.CLOTH,
				SoundType.SAND, SoundType.SNOW, SoundType.SLIME);

		// In the order they are looked up: a tag wins over the sound type
		private static final List<Sorted> TAGS = Arrays.asList(new Sorted(METAL, BlockTags.RAIN_ON_METAL),
				new Sorted(GLASS, BlockTags.RAIN_ON_GLASS), new Sorted(LEAVES, BlockTags.RAIN_ON_LEAVES),
				new Sorted(WOOD, BlockTags.RAIN_ON_WOOD), new Sorted(SOFT, BlockTags.RAIN_ABSORBING));
		private static final Sorted BY_SOUND_METAL = new Sorted(METAL, null);
		private static final Sorted BY_SOUND_GLASS = new Sorted(GLASS, null);
		private static final Sorted BY_SOUND_WOOD = new Sorted(WOOD, null);
		private static final Sorted BY_SOUND_SOFT = new Sorted(SOFT, null);
		private static final Sorted BY_DEFAULT = new Sorted(HARD, null);

		// The game's sound types: each one's field name, and what each step sound is to the rain where every game
		// sound type stepping with it agrees
		private static final Map<SoundType, String> NAMES = new IdentityHashMap<>();
		private static final Map<ResourceLocation, Sorted> BY_STEP = new HashMap<>();

		static {
			final Set<ResourceLocation> disagreeing = new HashSet<>();
			for (final Field field : SoundType.class.getFields()) {
				if (field.getType() != SoundType.class || !Modifier.isStatic(field.getModifiers()))
					continue;
				try {
					final SoundType sound = (SoundType) field.get(null);
					NAMES.putIfAbsent(sound, field.getName());
					final Sorted sorted = bySoundType(sound);
					if (sorted == BY_DEFAULT)
						continue;
					final ResourceLocation step = sound.getStepSound().getSoundName();
					final Sorted before = BY_STEP.putIfAbsent(step, sorted);
					if (before != null && before.surface != sorted.surface)
						disagreeing.add(step);
				} catch (final IllegalAccessException ignored) {
					// Public fields only; none is refused
				}
			}
			disagreeing.forEach(BY_STEP::remove);
		}

		private Sorting() {
		}

		private static Set<SoundType> set(final SoundType... sounds) {
			final Set<SoundType> result = Collections.newSetFromMap(new IdentityHashMap<>());
			result.addAll(Arrays.asList(sounds));
			return result;
		}

		@Nonnull
		static Sorted sort(@Nonnull final IBlockState state, @Nonnull final World world, @Nonnull final BlockPos pos) {
			for (final Sorted rule : TAGS)
				if (RegistryManager.TAGS.is(rule.tag, state))
					return rule;
			return bySound(soundOf(state, world, pos));
		}

		@Nonnull
		static SoundType soundOf(@Nonnull final IBlockState state, @Nonnull final World world,
				@Nonnull final BlockPos pos) {
			try {
				final SoundType sound = state.getBlock().getSoundType(state, world, pos, null);
				return sound != null ? sound : SoundType.STONE;
			} catch (final Throwable t) {
				// A mod's block that wants more than a place to say
				return SoundType.STONE;
			}
		}

		@Nonnull
		private static Sorted bySound(@Nonnull final SoundType sound) {
			final Sorted sorted = bySoundType(sound);
			if (sorted != BY_DEFAULT || NAMES.containsKey(sound))
				return sorted;
			// A mod's own sound type that steps as one of the game's does is what that one is to the rain
			final Sorted byStep = BY_STEP.get(sound.getStepSound().getSoundName());
			return byStep != null ? byStep : BY_DEFAULT;
		}

		@Nonnull
		private static Sorted bySoundType(@Nonnull final SoundType sound) {
			if (METAL_SOUNDS.contains(sound))
				return BY_SOUND_METAL;
			if (GLASS_SOUNDS.contains(sound))
				return BY_SOUND_GLASS;
			if (WOOD_SOUNDS.contains(sound))
				return BY_SOUND_WOOD;
			if (SOFT_SOUNDS.contains(sound))
				return BY_SOUND_SOFT;
			return BY_DEFAULT;
		}

		@Nonnull
		static String soundTypeName(@Nonnull final SoundType sound) {
			final String name = NAMES.get(sound);
			return name != null ? name : "mod's own, steps " + sound.getStepSound().getSoundName();
		}
	}
}
