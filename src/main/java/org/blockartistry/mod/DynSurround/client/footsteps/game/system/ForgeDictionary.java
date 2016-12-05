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

package org.blockartistry.mod.DynSurround.client.footsteps.game.system;

import java.io.File;
import java.util.List;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IBlockMap;
import org.blockartistry.mod.DynSurround.compat.MCHelper;
import org.blockartistry.mod.DynSurround.util.JsonUtils;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;

@SideOnly(Side.CLIENT)
public final class ForgeDictionary {

	public static class Entry {
		@SerializedName("blockClass")
		public String blockClass = null;
		@SerializedName("dictionaryEntries")
		public List<String> dictionaryEntries = ImmutableList.of();
	}

	public List<Entry> entries = ImmutableList.of();

	public static ForgeDictionary load(final File file) throws Exception {
		return JsonUtils.load(file, ForgeDictionary.class);
	}

	public static ForgeDictionary load(final String modId) throws Exception {
		return JsonUtils.load(modId, ForgeDictionary.class);
	}

	private ForgeDictionary() {

	}

	public static void dumpOreNames() {
		ModLog.debug("**** FORGE ORE DICTIONARY NAMES ****");
		for (final String oreName : OreDictionary.getOreNames())
			ModLog.debug(oreName);
		ModLog.debug("************************************");
	}

	public static void initialize(final IBlockMap blockMap) {

		try {
			final ForgeDictionary config = load("forge");

			for (final Entry entry : config.entries) {
				for (final String dictionaryName : entry.dictionaryEntries) {
					final List<ItemStack> stacks = OreDictionary.getOres(dictionaryName, false);
					for (final ItemStack stack : stacks) {
						final Block block = Block.getBlockFromItem(stack.getItem());
						if (block != null) {
							String blockName = MCHelper.nameOf(block);
							if (stack.getHasSubtypes() && stack.getItemDamage() != OreDictionary.WILDCARD_VALUE)
								blockName += "^" + stack.getItemDamage();
							blockMap.register(blockName, entry.blockClass);
						}
					}
				}
			}

		} catch (final Throwable e) {
			ModLog.error("Unable to process Forge dictionary alias JSON", e);
		}
	}

}
