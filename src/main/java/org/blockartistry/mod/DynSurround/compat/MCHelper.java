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
public class MCHelper {

	public static String nameOf(final Block block) {
		return Block.REGISTRY.getNameForObject(block).toString();
	}
	
	public static String nameOf(final Item item) {
		return Item.REGISTRY.getNameForObject(item).toString();
	}

	public static Block getBlockByName(final String blockName) {
		return Block.REGISTRY.getObject(new ResourceLocation(blockName));
	}
	
	public static boolean isAirBlock(final IBlockState state, final World world, final BlockPos pos) {
		return state.getMaterial() == Material.AIR;
	}
	
	public static boolean isLiquid(final Block block) {
		// TODO: Look for replacement!
		return block.getMaterial(null).isLiquid();
	}
	
	public static SoundType getSoundType(final Block block) {
		// TODO: Look for replacement!
		return block.getSoundType();
	}
	
	public static boolean isOpaqueCube(final Block block) {
		// TODO: Look for replacement!
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
}
