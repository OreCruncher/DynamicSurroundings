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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.registry.item.IItemData;
import org.orecruncher.dsurround.registry.item.ItemClass;
import org.orecruncher.dsurround.registry.item.SimpleItemData;

import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConstructArmoryProducer implements IItemDataProducer {

	private Class<?> helmet;
	private Class<?> chestplate;
	private Class<?> leggings;
	private Class<?> boots;

	public ConstructArmoryProducer() {
		try {
			this.helmet = Class.forName("c4.conarm.common.items.armor.Helmet");
			this.chestplate = Class.forName("c4.conarm.common.items.armor.Chestplate");
			this.leggings = Class.forName("c4.conarm.common.items.armor.Leggings");
			this.boots = Class.forName("c4.conarm.common.items.armor.Boots");
		} catch (@Nonnull final Throwable t) {
			ModBase.log().error("Unable to initialize Construct Armory producer!", t);
			this.helmet = null;
			this.chestplate = null;
			this.leggings = null;
			this.boots = null;
		}

	}

	protected boolean isValid() {
		return this.helmet != null;
	}

	@Override
	@Nullable
	public IItemData create(@Nonnull final Item item, @Nonnull final ItemClass ic) {
		if (isValid()) {
			// For helmets let the SimpleItemData handle
			if (this.helmet.isInstance(item)) {
				return SimpleItemData.CACHE.get(ic);
			} else if (this.chestplate.isInstance(item)) {
				return new ConstructArmoryItemData(ic);
			} else if (this.leggings.isInstance(item)) {
				return new ConstructArmoryItemData(ic);
			} else if (this.boots.isInstance(item)) {
				return new ConstructArmoryItemData(ic);
			}
		}
		// Let the default handler sort things
		return null;
	}

}
