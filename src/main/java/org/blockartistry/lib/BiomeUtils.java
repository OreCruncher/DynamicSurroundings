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

package org.blockartistry.lib;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.lib.collections.IdentityHashSet;

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

public final class BiomeUtils {

	private static final Color NO_COLOR = new Color.ImmutableColor(1F, 1F, 1F);

	private BiomeUtils() {

	}

	public static Set<Type> getBiomeTypes() {
		return new IdentityHashSet<>(BiomeDictionary.Type.values());
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
		return new IdentityHashSet<>(BiomeDictionary.getTypesForBiome(biome));
	}

	public static boolean areBiomesSimilar(@Nonnull final Biome b1, @Nonnull final Biome b2) {
		return BiomeDictionary.areBiomesEquivalent(b1, b2);
	}
}
