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

package org.blockartistry.mod.DynSurround.util;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.registry.BiomeInfo;
import org.blockartistry.mod.DynSurround.registry.BiomeRegistry;
import org.blockartistry.mod.DynSurround.registry.DimensionRegistry;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

public final class PlayerUtils {

	private static final int INSIDE_Y_ADJUST = 3;

	private static final Pattern REGEX_DEEP_OCEAN = Pattern.compile("(?i).*deep.*ocean.*|.*abyss.*");
	private static final Pattern REGEX_OCEAN = Pattern.compile("(?i)(?!.*deep.*)(.*ocean.*|.*kelp.*|.*coral.*)");
	private static final Pattern REGEX_RIVER = Pattern.compile("(?i).*river.*");

	private PlayerUtils() {
	}

	@Nonnull
	public static BiomeInfo getPlayerBiome(@Nonnull final EntityPlayer player, final boolean getTrue) {

		final int theX = MathStuff.floor_double(player.posX);
		final int theY = MathStuff.floor_double(player.posY);
		final int theZ = MathStuff.floor_double(player.posZ);
		BiomeInfo biome = BiomeRegistry.get(player.worldObj.getBiome(new BlockPos(theX, 0, theZ)));

		if (!getTrue) {
			if (isUnderWater(player)) {
				if (REGEX_DEEP_OCEAN.matcher(biome.getBiomeName()).matches())
					biome = BiomeRegistry.UNDERDEEPOCEAN;
				else if (REGEX_OCEAN.matcher(biome.getBiomeName()).matches())
					biome = BiomeRegistry.UNDEROCEAN;
				else if (REGEX_RIVER.matcher(biome.getBiomeName()).matches())
					biome = BiomeRegistry.UNDERRIVER;
				else
					biome = BiomeRegistry.UNDERWATER;
			} else if (isUnderGround(player, INSIDE_Y_ADJUST))
				biome = BiomeRegistry.UNDERGROUND;
			else if (theY >= DimensionRegistry.getSpaceHeight(player.worldObj))
				biome = BiomeRegistry.OUTERSPACE;
			else if (theY >= DimensionRegistry.getCloudHeight(player.worldObj))
				biome = BiomeRegistry.CLOUDS;
		}
		return biome;
	}

	public static int getPlayerDimension(@Nonnull final EntityPlayer player) {
		if (player == null || player.worldObj == null)
			return -256;
		return player.worldObj.provider.getDimension();
	}

	public static boolean isUnderWater(@Nonnull final EntityPlayer player) {
		final int x = MathStuff.floor_double(player.posX);
		final int y = MathStuff.floor_double(player.posY + player.getEyeHeight());
		final int z = MathStuff.floor_double(player.posZ);
		return player.worldObj.getBlockState(new BlockPos(x, y, z)).getMaterial() == Material.WATER;
	}

	public static boolean isUnderGround(@Nonnull final EntityPlayer player, final int offset) {
		return MathStuff.floor_double(player.posY + offset) < DimensionRegistry.getSeaLevel(player.worldObj);
	}

	@SideOnly(Side.CLIENT)
	public static int getClientPlayerDimension() {
		return getPlayerDimension(FMLClientHandler.instance().getClient().thePlayer);
	}
}
