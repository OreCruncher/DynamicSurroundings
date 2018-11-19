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

package org.orecruncher.dsurround.registry.item.compat;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.lib.compat.ModEnvironment;
import org.orecruncher.dsurround.registry.item.IItemData;
import org.orecruncher.dsurround.registry.item.ItemClass;
import org.orecruncher.dsurround.registry.item.SimpleItemData;

import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ItemDataProducer {

	private static final List<IItemDataProducer> PRODUCERS = new ArrayList<>();

	static {
		if (ModEnvironment.ConstructArmory.isLoaded())
			PRODUCERS.add(new ConstructArmoryProducer());

		// This goes last. This generates a simple IItemData that defaults
		// to settings in the ItemClass.
		PRODUCERS.add((item, ic) -> SimpleItemData.CACHE.get(ic));
	}

	@Nonnull
	public static IItemData create(@Nonnull final Item item, @Nonnull final ItemClass ic) {
		for (final IItemDataProducer producer : PRODUCERS) {
			final IItemData data = producer.create(item, ic);
			if (data != null)
				return data;
		}
		return SimpleItemData.CACHE.get(ItemClass.NONE);
	}

}
