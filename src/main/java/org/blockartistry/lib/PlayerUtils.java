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

package org.blockartistry.lib;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.registry.BiomeInfo;
import org.blockartistry.DynSurround.registry.BiomeRegistry;
import org.blockartistry.DynSurround.registry.DimensionInfo;
import org.blockartistry.lib.math.MathStuff;
import org.blockartistry.lib.random.XorShiftRandom;

import com.google.common.base.Predicates;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class PlayerUtils {

	private static final int INSIDE_Y_ADJUST = 3;

	private static final Pattern REGEX_DEEP_OCEAN = Pattern.compile("(?i).*deep.*ocean.*|.*abyss.*");
	private static final Pattern REGEX_OCEAN = Pattern.compile("(?i)(?!.*deep.*)(.*ocean.*|.*kelp.*|.*coral.*)");
	private static final Pattern REGEX_RIVER = Pattern.compile("(?i).*river.*");

	private PlayerUtils() {
	}

	@Nonnull
	public static BiomeInfo getPlayerBiome(@Nonnull final EntityPlayer player, final boolean getTrue) {
		Biome biome = player.world.getBiome(new BlockPos(player.posX, 0, player.posZ));

		if (!getTrue) {
			if (player.isInsideOfMaterial(Material.WATER)) {
				if (REGEX_RIVER.matcher(biome.getBiomeName()).matches())
					biome = BiomeRegistry.UNDERRIVER;
				else if (REGEX_OCEAN.matcher(biome.getBiomeName()).matches())
					biome = BiomeRegistry.UNDEROCEAN;
				else if (REGEX_DEEP_OCEAN.matcher(biome.getBiomeName()).matches())
					biome = BiomeRegistry.UNDERDEEPOCEAN;
				else
					biome = BiomeRegistry.UNDERWATER;
			} else {
				final DimensionInfo dimInfo = ClientRegistry.DIMENSION.getData(player.world);
				final int theY = MathStuff.floor(player.posY);
				if ((theY + INSIDE_Y_ADJUST) <= dimInfo.getSeaLevel())
					biome = BiomeRegistry.UNDERGROUND;
				else if (theY >= dimInfo.getSpaceHeight())
					biome = BiomeRegistry.OUTERSPACE;
				else if (theY >= dimInfo.getCloudHeight())
					biome = BiomeRegistry.CLOUDS;
			}
		}

		return ClientRegistry.BIOME.get(biome);
	}

	@Nullable
	public static EntityPlayer getRandomPlayer(@Nonnull final World world) {
		final List<EntityPlayer> players = world.getPlayers(EntityPlayer.class, Predicates.<EntityPlayer>alwaysTrue());
		if (players.size() == 1) {
			return players.get(0);
		} else if (players.size() > 0) {
			return players.get(XorShiftRandom.current().nextInt(players.size()));
		}
		return null;
	}

	public static boolean isHolding(@Nonnull final EntityPlayer player, @Nonnull final Item item,
			@Nonnull final EnumHand hand) {
		final ItemStack stack = player.getHeldItem(hand);
		if (stack != null && !stack.isEmpty()) {
			if (stack.getItem() == item)
				return true;
		}
		return false;
	}

	public static boolean isHolding(@Nonnull final EntityPlayer player, @Nonnull final Item item) {
		return isHolding(player, item, EnumHand.MAIN_HAND) || isHolding(player, item, EnumHand.OFF_HAND);
	}

	@Nullable
	public static Entity entityImLookingAt(@Nonnull final EntityPlayer player) {
		final RayTraceResult result = Minecraft.getMinecraft().objectMouseOver;
		if (result != null && result.typeOfHit == RayTraceResult.Type.ENTITY)
			return result.entityHit;
		return null;
	}
}
