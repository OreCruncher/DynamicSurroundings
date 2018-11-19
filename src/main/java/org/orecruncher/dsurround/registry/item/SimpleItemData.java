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

package org.orecruncher.dsurround.registry.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.client.sound.SoundEffect;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * Simple default item handler that uses the standard settings in the
 * ItemClass.  Reusable singletons can be found in the CACHE.
 */
@SideOnly(Side.CLIENT)
public class SimpleItemData implements IItemData {

	public static final Reference2ObjectOpenHashMap<ItemClass, SimpleItemData> CACHE = new Reference2ObjectOpenHashMap<>();

	static {
		for (final ItemClass ic : ItemClass.values())
			if (ic.isArmor())
				CACHE.put(ic, new SimpleArmorItemData(ic));
			else
				CACHE.put(ic, new SimpleItemData(ic));
	}

	protected final ItemClass itemClass;

	public SimpleItemData(@Nonnull final ItemClass ic) {
		this.itemClass = ic;
	}

	@Override
	@Nonnull
	public ItemClass getItemClass() {
		return this.itemClass;
	}

	@Override
	@Nullable
	public SoundEffect getEquipSound(@Nonnull final ItemStack stack) {
		return this.itemClass.getEquipSound();
	}

	@Override
	@Nullable
	public SoundEffect getSwingSound(@Nonnull final ItemStack stack) {
		return this.itemClass.getSwingSound();
	}

	@Override
	@Nullable
	public SoundEffect getUseSound(@Nonnull final ItemStack stack) {
		return this.itemClass.getUseSound();
	}

}
