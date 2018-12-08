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

package org.orecruncher.dsurround.registry.item;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.registry.RegistryManager;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public final class ItemUtils {

	// This field was added by the core mod for our use
	private static Field itemInfo = ReflectionHelper.findField(Item.class, "dsurround_item_info");

	@Nullable
	public static IItemData getItemData(@Nonnull final Item item) {
		IItemData result = null;
		
		try {
			result = (IItemData) itemInfo.get(item);
			if (result == null) {
				RegistryManager.ITEMS.reload();
				result = (IItemData) itemInfo.get(item);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			ModBase.log().error("Unable to get hold of private field on Item!", e);
		}
		
		if (result == null) {
			ModBase.log().warn("Unable to find IItemData for item [%s]", item.toString());
			result = SimpleItemData.CACHE.get(ItemClass.NONE);
			setItemData(item, result);
		}
		
		return result; 
	}

	public static void setItemData(@Nonnull final Item item, @Nonnull final IItemData data) {
		try {
			itemInfo.set(item, data);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			ModBase.log().error("Unable to set private field on Item!", e);
		}
	}

}
