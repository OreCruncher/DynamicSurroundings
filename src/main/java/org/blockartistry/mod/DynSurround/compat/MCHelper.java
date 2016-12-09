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

package org.blockartistry.mod.DynSurround.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.strategy.IdentityHashingStrategy;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class MCHelper {

	private static final String MATERIAL_CUSTOM = "Custom";
	private static final String MATERIAL_NONE = "None";
	private static final Map<Material, String> materialMap = new TCustomHashMap<Material, String>(
			IdentityHashingStrategy.INSTANCE);
	
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
		materialMap.put(Material.FIRE, "Fire");
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
		
	}
	
	protected MCHelper() {
		
	}
	
	
	public static String nameOf(final Block block) {
		return Block.REGISTRY.getNameForObject(block).toString();
	}
	
	public static String nameOf(final Item item) {
		return Item.REGISTRY.getNameForObject(item).toString();
	}

	public static Block getBlockByName(final String blockName) {
		// Yes yes.  I know what I am doing here.  Need to know if the block
		// doesn't exist because of bad data in a config file or some such.
		return Block.REGISTRY.getObjectBypass(new ResourceLocation(blockName));
	}
	
	public static boolean isAirBlock(final IBlockState state, final World world, final BlockPos pos) {
		return state.getMaterial() == Material.AIR;
	}
	
	public static boolean isLiquid(final Block block) {
		return block.getMaterial(null).isLiquid();
	}
	
	public static SoundType getSoundType(final Block block) {
		return block.getSoundType();
	}
	
	public static SoundType getSoundType(final IBlockState state) {
		return getSoundType(state.getBlock());
	}
	
	public static boolean isOpaqueCube(final Block block) {
		return block.isOpaqueCube(null);
	}

	public static boolean hasVariants(final Block block) {
		final Item item = Item.getItemFromBlock(block);
		if(item == null)
			return false;
		
		final List<ItemStack> stacks = new ArrayList<ItemStack>();
		block.getSubBlocks(item, null, stacks);
		return stacks.size() > 1;
	}
	
	public static String getMaterialName(final Material material) {
		if(material == null)
			return MATERIAL_NONE;
		final String materialName = materialMap.get(material);
		return materialName == null ? MATERIAL_CUSTOM : materialName;
	}
}
