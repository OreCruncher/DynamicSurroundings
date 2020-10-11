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
import org.orecruncher.dsurround.registry.IDataAccessor;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.lib.Color;
import org.orecruncher.lib.ReflectedField;
import org.orecruncher.lib.ReflectedField.FloatField;
import org.orecruncher.lib.ReflectedField.IntegerField;
import org.orecruncher.lib.ReflectedField.ObjectField;
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
			null
		);
	private static final IntegerField<Object> bopBiomeFogColor =
		new IntegerField<>(
			"biomesoplenty.common.biome.BOPBiome",
			"fogColor",
			null
		);
	private static final ObjectField<BiomeDictionary.Type, Map<String, BiomeDictionary.Type>> biomeType =
		new ObjectField<>(
			BiomeDictionary.Type.class,
			"byName",
			null
		);
	//@formatter:on

	private static final Class<?> bopBiome = ReflectedField.resolveClass("biomesoplenty.common.biome.BOPBiome");

	@SuppressWarnings("unchecked")
	@Nonnull
	public static BiomeInfo getBiomeData(@Nonnull final Biome biome) {
		final IDataAccessor<BiomeInfo> accessor = (IDataAccessor<BiomeInfo>) biome;
		BiomeInfo result;
		result = accessor.getData();
		if (result == null) {
			RegistryManager.BIOME.reload();
			result = accessor.getData();
		}

		if (result == null) {
			ModBase.log().warn("Unable to find configuration for biome [%s] (hc=%d)", biome.getRegistryName(),
					System.identityHashCode(biome));
			result = RegistryManager.BIOME.WTF_INFO;
			accessor.setData(result);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static void setBiomeData(@Nonnull final Biome biome, @Nullable final BiomeInfo data) {
		((IDataAccessor<BiomeInfo>) biome).setData(data);
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
		if (biomeType.isAvailable()) {
			return new ReferenceOpenHashSet<>(biomeType.get(null).values());
		}

		throw new IllegalStateException("Cannot locate BiomeDictionary.Type table!");
	}

	@Nonnull
	public static Color getBiomeWaterColor(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return new Color(BiomeColorHelper.getWaterColorAtPos(world, pos));
	}

	@Nonnull
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
		// It's possible to have a biome that is not registered come through here
		// There is an internal check that will throw an exception if that is the
		// case. Seen this with OTG installed.
		try {
			return new ReferenceOpenHashSet<>(BiomeDictionary.getTypes(biome));
		} catch (@Nonnull final Throwable t) {
			final String name = biomeName.get(biome);
			ModBase.log().warn("Unable to get biome type data for biome '%s'", name);
		}
		return new ReferenceOpenHashSet<>();
	}

	public static boolean areBiomesSimilar(@Nonnull final Biome b1, @Nonnull final Biome b2) {
		return BiomeDictionary.areSimilar(b1, b2);
	}

}
