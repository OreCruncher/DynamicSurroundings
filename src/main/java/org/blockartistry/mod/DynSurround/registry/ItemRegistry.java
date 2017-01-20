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

package org.blockartistry.mod.DynSurround.registry;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.data.xface.ItemConfig;
import org.blockartistry.mod.DynSurround.util.MCHelper;

import gnu.trove.set.hash.THashSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;

public class ItemRegistry extends Registry {

	public static enum ArmorClass {
		NONE, LIGHT, CRYSTAL, HEAVY;
	}

	private final THashSet<Class<?>> swordItems = new THashSet<Class<?>>();
	private final THashSet<Class<?>> axeItems = new THashSet<Class<?>>();
	private final THashSet<Class<?>> bowItems = new THashSet<Class<?>>();

	private final THashSet<Item> lightArmor = new THashSet<Item>();
	private final THashSet<Item> heavyArmor = new THashSet<Item>();
	private final THashSet<Item> crystalArmor = new THashSet<Item>();

	public ItemRegistry(@Nonnull final Side side) {
		super(side);
	}

	@Override
	public void init() {
		swordItems.clear();
		axeItems.clear();
		bowItems.clear();
		lightArmor.clear();
		heavyArmor.clear();
		crystalArmor.clear();
	}

	@Override
	public void initComplete() {

	}

	@Override
	public void fini() {

	}

	private void process(@Nonnull final List<String> classes, @Nonnull final THashSet<Class<?>> theList) {
		for (final String c : classes) {
			try {
				final Class<?> clazz = Class.forName(c, false, ItemRegistry.class.getClassLoader());
				theList.add(clazz);
			} catch (final ClassNotFoundException e) {
				ModLog.warn("Cannot locate class '%s' for ItemRegistry", c);
			}
		}
	}

	private void process(@Nonnull final List<String> itemList, @Nonnull final Set<Item> items) {
		for (final String i : itemList) {
			final Item item = MCHelper.getItemByName(i);
			if (item != null)
				items.add(item);
		}
	}

	public void register(@Nonnull final ItemConfig config) {
		process(config.axeSound, this.axeItems);
		process(config.bowSound, this.bowItems);
		process(config.swordSound, this.swordItems);
		process(config.crystalArmor, this.crystalArmor);
		process(config.heavyArmor, this.heavyArmor);
		process(config.lightArmor, this.lightArmor);
	}

	public boolean doSwordSound(@Nonnull final ItemStack stack) {
		if (stack == null || stack.getItem() == null)
			return false;
		return this.swordItems.contains(stack.getItem().getClass());
	}

	public boolean doAxeSound(@Nonnull final ItemStack stack) {
		if (stack == null || stack.getItem() == null)
			return false;
		return this.axeItems.contains(stack.getItem().getClass());
	}

	public boolean doBowSound(@Nonnull final ItemStack stack) {
		if (stack == null || stack.getItem() == null)
			return false;
		return this.bowItems.contains(stack.getItem().getClass());
	}

	public ArmorClass getArmorClass(@Nonnull final ItemStack stack) {
		if (stack != null) {
			final Item item = stack.getItem();
			if (item != null) {
				if (this.crystalArmor.contains(item))
					return ArmorClass.CRYSTAL;
				else if (this.heavyArmor.contains(item))
					return ArmorClass.HEAVY;
				else if (this.lightArmor.contains(item))
					return ArmorClass.LIGHT;
			}
		}
		return ArmorClass.NONE;
	}

}
