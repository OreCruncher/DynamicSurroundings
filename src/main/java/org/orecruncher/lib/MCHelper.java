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

package org.orecruncher.lib;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class MCHelper {

	private static final String MATERIAL_CUSTOM = "Custom";
	private static final String MATERIAL_NONE = "None";
	private static final Map<Material, String> materialMap = new Reference2ObjectOpenHashMap<>();
	private static final Set<Block> hasVariants = new ReferenceOpenHashSet<>();

	private static boolean variantCheck(@Nonnull final Block block) {
		final Item item = Item.getItemFromBlock(block);
		if (item == null)
			return false;

		if (item.getHasSubtypes())
			return true;

		final NonNullList<ItemStack> stacks = NonNullList.create();
		try {
			block.getSubBlocks(block.getCreativeTab(), stacks);
		} catch (final Throwable t) {
			return false;
		}
		return stacks.size() > 1;
	}

	static {
		materialMap.put(Material.AIR, "Air");
		materialMap.put(Material.ANVIL, "Anvil");
		materialMap.put(Material.BARRIER, "Barrier");
		materialMap.put(Material.CACTUS, "Cactus");
		materialMap.put(Material.CAKE, "Cake");
		materialMap.put(Material.CARPET, "Carpet");
		materialMap.put(Material.CIRCUITS, "Circuits");
		materialMap.put(Material.CLAY, "Clay");
		materialMap.put(Material.CLOTH, "Cloth");
		materialMap.put(Material.CORAL, "Coral");
		materialMap.put(Material.CRAFTED_SNOW, "Crafted Snow");
		materialMap.put(Material.DRAGON_EGG, "Dragon Egg");
		materialMap.put(Material.FIRE, "FireJetEffect");
		materialMap.put(Material.GLASS, "Glass");
		materialMap.put(Material.GOURD, "Gourd");
		materialMap.put(Material.GRASS, "Grass");
		materialMap.put(Material.GROUND, "Ground");
		materialMap.put(Material.ICE, "Ice");
		materialMap.put(Material.IRON, "Iron");
		materialMap.put(Material.LAVA, "Lava");
		materialMap.put(Material.LEAVES, "Leaves");
		materialMap.put(Material.PACKED_ICE, "Packed Ice");
		materialMap.put(Material.PISTON, "Piston");
		materialMap.put(Material.PLANTS, "Plants");
		materialMap.put(Material.PORTAL, "Portal");
		materialMap.put(Material.REDSTONE_LIGHT, "Redstone Light");
		materialMap.put(Material.ROCK, "Rock");
		materialMap.put(Material.SAND, "Sand");
		materialMap.put(Material.SNOW, "Snow");
		materialMap.put(Material.SPONGE, "Sponge");
		materialMap.put(Material.STRUCTURE_VOID, "Structure Void");
		materialMap.put(Material.TNT, "TNT");
		materialMap.put(Material.VINE, "Vine");
		materialMap.put(Material.WATER, "Water");
		materialMap.put(Material.WEB, "Web");
		materialMap.put(Material.WOOD, "Wood");

		// Scan the block registry looking for blocks that have subtypes
		// and add them to the subtype list.
		final Set<Block> blocks = StreamSupport.stream(Block.REGISTRY.spliterator(), false)
				.filter(MCHelper::variantCheck).collect(Collectors.toSet());
		hasVariants.addAll(blocks);
	}

	protected MCHelper() {

	}

	@Nonnull
	public static String nameOf(@Nonnull final Block block) {
		return Block.REGISTRY.getNameForObject(block).toString();
	}

	@Nonnull
	public static String nameOf(@Nonnull final Item item) {
		return Item.REGISTRY.getNameForObject(item).toString();
	}

	@Nullable
	public static Item getItemByName(@Nonnull final String itemName) {
		return Item.getByNameOrId(itemName);
	}

	@Nonnull
	public static Block getBlockByName(@Nonnull final String blockName) {
		return Block.getBlockFromName(blockName);
	}

	@Nullable
	public static SoundType getSoundType(@Nonnull final IBlockState state) {
		return getSoundType(state, null);
	}

	@SuppressWarnings("deprecation")
	@Nullable
	public static SoundType getSoundType(@Nonnull final IBlockState state, @Nullable final World world) {
		try {
			return state.getBlock().getSoundType(state, world, null, null);
		} catch (@Nonnull final Throwable t) {
			// getSoundType() allows pos and entity to be null - nothing said about
			// world. Fallback to the classic deprecated way if there is an
			// error of some sort.
			return state.getBlock().getSoundType();
		}
	}

	public static boolean hasVariants(@Nonnull final Block block) {
		return hasVariants.contains(block);
	}

	public static boolean hasSpecialMeta(@Nonnull final Block block) {
		return block instanceof BlockCrops;
	}

	@Nonnull
	public static String getMaterialName(@Nullable final Material material) {
		if (material == null)
			return MATERIAL_NONE;
		final String materialName = materialMap.get(material);
		return materialName == null ? MATERIAL_CUSTOM : materialName;
	}
}
