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

package org.orecruncher.dsurround.registry.biome;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.lib.compat.ModEnvironment;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.lib.Color;
import org.orecruncher.lib.WorldUtils;

import com.google.common.collect.ImmutableSet;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * Helper class used to access and manipulate the reference to our data we have
 * referenced in the Biome implementation class. Goal is to avoid all the
 * dictionary lookups and things.
 */
public final class BiomeUtil {

	private static final Color NO_COLOR = new Color.ImmutableColor(1F, 1F, 1F);

	// This field was added by the core mod for our use
	private static Field biomeInfo = ReflectionHelper.findField(Biome.class, "dsurround_biome_info");

	// Regular Minecraft fields
	private static Field biomeName = ReflectionHelper.findField(Biome.class, "biomeName", "field_76791_y");

	// BoP support for fog
	private static Class<?> bopBiome = null;
	private static Field bopBiomeFogDensity = null;
	private static Field bopBiomeFogColor = null;

	static {

		if (ModEnvironment.BiomesOPlenty.isLoaded())
			try {
				bopBiome = Class.forName("biomesoplenty.common.biome.BOPBiome");
				bopBiomeFogDensity = ReflectionHelper.findField(bopBiome, "fogDensity");
				bopBiomeFogColor = ReflectionHelper.findField(bopBiome, "fogColor");
			} catch (final Throwable t) {
				bopBiome = null;
				bopBiomeFogDensity = null;
				bopBiomeFogColor = null;
			}
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends BiomeData> T getBiomeData(@Nonnull final Biome biome) {
		T result = null;
		try {
			result = (T) biomeInfo.get(biome);
			if (result == null) {
				RegistryManager.BIOME.reload();
				result = (T) biomeInfo.get(biome);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			ModBase.log().error("Unable to get hold of private field on Biome!", e);
		}
		return result == null ? (T) RegistryManager.BIOME.WTF_INFO : result;
	}

	public static <T extends BiomeData> void setBiomeData(@Nonnull final Biome biome, @Nullable final T data) {
		try {
			biomeInfo.set(biome, data);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			ModBase.log().error("Unable to set private field on Biome!", e);
		}
	}

	@Nonnull
	public static String getBiomeName(@Nonnull final Biome biome) {
		try {
			return (String) biomeName.get(biome);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// Intentionally left blank
		}
		return "UNKNOWN";
	}

	// ==================
	//
	// BoP Support
	//
	// ==================

	public static boolean isBoPBiome(@Nonnull final Biome biome) {
		return bopBiome != null && bopBiome.isInstance(biome);
	}

	public static int getBoPBiomeFogColor(@Nonnull final Biome biome) {
		try {
			return bopBiomeFogColor != null ? bopBiomeFogColor.getInt(biome) : 0;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// Intentionally left blank
		}
		return 0;
	}

	public static float getBoPBiomeFogDensity(@Nonnull final Biome biome) {
		try {
			return bopBiomeFogDensity != null ? bopBiomeFogDensity.getFloat(biome) : 0F;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// Intentionally left blank
		}
		return 0;
	}

	// ===================================
	//
	// Miscellaneous Support Functions
	//
	// ===================================
	public static Set<Type> getBiomeTypes() {
		try {
			final Field accessor = ReflectionHelper.findField(BiomeDictionary.Type.class, "byName");
			if (accessor != null) {
				@SuppressWarnings("unchecked")
				final Map<String, BiomeDictionary.Type> stuff = (Map<String, BiomeDictionary.Type>) accessor.get(null);
				return new ReferenceOpenHashSet<>(stuff.values());
			}

			return ImmutableSet.of();

		} catch (final Throwable t) {
			throw new RuntimeException("Cannot locate BiomeDictionary.Type table!");
		}

	}

	@Nonnull
	public static Color getBiomeWaterColor(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return new Color(BiomeColorHelper.getWaterColorAtPos(world, pos));
	}

	@Nullable
	public static Color getColorForLiquid(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return getColorForLiquid(world, WorldUtils.getBlockState(world, pos), pos);
	}

	@Nonnull
	public static Color getColorForLiquid(@Nonnull final World world, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos) {
		final Block liquid = state.getBlock();
		if (liquid == Blocks.WATER) {
			return getBiomeWaterColor(world, pos);
		} else {
			// Lookup in fluid registry
			final Fluid fluid = FluidRegistry.lookupFluidForBlock(liquid);
			if (fluid != null) {
				return new Color(fluid.getColor());
			}
		}
		return NO_COLOR;
	}

	@Nonnull
	public static Set<Type> getBiomeTypes(@Nonnull final Biome biome) {
		return new ReferenceOpenHashSet<>(BiomeDictionary.getTypes(biome));
	}

	public static boolean areBiomesSimilar(@Nonnull final Biome b1, @Nonnull final Biome b2) {
		return BiomeDictionary.areSimilar(b1, b2);
	}

}
