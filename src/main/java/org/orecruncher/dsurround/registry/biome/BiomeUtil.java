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

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.lib.ReflectedField;
import org.orecruncher.dsurround.lib.ReflectedField.FloatField;
import org.orecruncher.dsurround.lib.ReflectedField.IntegerField;
import org.orecruncher.dsurround.lib.ReflectedField.ObjectField;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.lib.Color;
import org.orecruncher.lib.WorldUtils;

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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class BiomeUtil {

	private static final Color NO_COLOR = new Color.ImmutableColor(1F, 1F, 1F);

	//@formatter:off
	private static final ObjectField<Biome, BiomeData> biomeInfo =
		new ObjectField<>(
			Biome.class,
			"dsurround_biome_info",
			""
		);
	private static final ObjectField<Biome, String> biomeName =
		new ObjectField<>(
			Biome.class,
			"biomeName",
			"field_76791_y"
		);
	private static final FloatField<Object> bopBiomeFogDensity =
		new FloatField<>(
			"biomesoplenty.common.biome.BOPBiome",
			"fogDensity",
			""
		);
	private static final IntegerField<Object> bopBiomeFogColor =
		new IntegerField<>(
			"biomesoplenty.common.biome.BOPBiome",
			"fogColor",
			""
		);
	//@formatter:on

	private static final Class<?> bopBiome = ReflectedField.resolveClass("biomesoplenty.common.biome.BOPBiome");

	@SuppressWarnings("unchecked")
	@Nonnull
	public static <T extends BiomeData> T getBiomeData(@Nonnull final Biome biome) {
		T result = null;
		if (biome != null) {
			result = (T) biomeInfo.get(biome);
			if (result == null) {
				RegistryManager.BIOME.reload();
				result = (T) biomeInfo.get(biome);
			}
		}

		if (result == null) {
			ModBase.log().warn("Unable to find configuration for biome [%s] (hc=%d)", biome.getRegistryName(),
					System.identityHashCode(biome));
			result = (T) RegistryManager.BIOME.WTF_INFO;
			setBiomeData(biome, result);
		}
		return result;
	}

	public static <T extends BiomeData> void setBiomeData(@Nonnull final Biome biome, @Nullable final T data) {
		biomeInfo.set(biome, data);
	}

	@Nonnull
	public static String getBiomeName(@Nonnull final Biome biome) {
		final String result = biomeName.get(biome);
		return StringUtils.isEmpty(result) ? "UNKNOWN" : result;
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
		return bopBiomeFogColor.isAvailable() ? bopBiomeFogColor.get(biome) : 0;
	}

	public static float getBoPBiomeFogDensity(@Nonnull final Biome biome) {
		return bopBiomeFogDensity.isAvailable() ? bopBiomeFogDensity.get(biome) : 0F;
	}

	// ===================================
	//
	// Miscellaneous Support Functions
	//
	// ===================================
	@Nonnull
	public static Set<Type> getBiomeTypes() {
		//@formatter:off
		final ObjectField<BiomeDictionary.Type, Map<String, BiomeDictionary.Type>> accessor =
			new ObjectField<>(
				BiomeDictionary.Type.class,
				"byName", "");
		//@formatter:on

		if (accessor.isAvailable()) {
			return new ReferenceOpenHashSet<>(accessor.get(null).values());
		}

		throw new IllegalStateException("Cannot locate BiomeDictionary.Type table!");
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
