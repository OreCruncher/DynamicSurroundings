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

package org.blockartistry.DynSurround.registry;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModLog;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.client.sound.Sounds;
import org.blockartistry.DynSurround.data.xface.ItemConfig;
import org.blockartistry.lib.MCHelper;

import gnu.trove.set.hash.THashSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRegistry extends Registry {

	private final THashSet<Class<?>> swordItems = new THashSet<Class<?>>();
	private final THashSet<Class<?>> axeItems = new THashSet<Class<?>>();
	private final THashSet<Class<?>> bowItems = new THashSet<Class<?>>();
	private final THashSet<Class<?>> toolItems = new THashSet<Class<?>>();
	private final THashSet<Class<?>> shieldItems = new THashSet<Class<?>>();

	private final Map<Item, ArmorClass> armorMap = new IdentityHashMap<Item, ArmorClass>();

	public ItemRegistry(@Nonnull final Side side) {
		super(side);
	}

	@Override
	public void init() {
		this.swordItems.clear();
		this.axeItems.clear();
		this.bowItems.clear();
		this.toolItems.clear();
		this.shieldItems.clear();
		this.armorMap.clear();
	}

	private boolean postProcess(THashSet<Class<?>> itemSet, final Item item) {
		final Class<?> itemClass = item.getClass();

		// If the item is in the collection already, return
		if (itemSet.contains(itemClass))
			return true;

		// Need to iterate to see if an item is a sub-class of an existing
		// item in the list.
		for (final Class<?> clazz : itemSet) {
			if (clazz.isAssignableFrom(itemClass)) {
				itemSet.add(itemClass);
				return true;
			}
		}

		return false;
	}

	@Override
	public void initComplete() {

		// Post process item list looking for similar items
		final Iterator<Item> iterator = Item.REGISTRY.iterator();
		while (iterator.hasNext()) {
			final Item item = iterator.next();
			if (postProcess(this.swordItems, item))
				continue;
			if (postProcess(this.axeItems, item))
				continue;
			if (postProcess(this.toolItems, item))
				continue;
			if (postProcess(this.shieldItems, item))
				continue;

			postProcess(this.bowItems, item);
		}
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

	private void process(@Nonnull final List<String> itemList, @Nonnull final ArmorClass ac) {
		for (final String i : itemList) {
			final Item item = MCHelper.getItemByName(i);
			if (item != null)
				this.armorMap.put(item, ac);
		}
	}

	public void register(@Nonnull final ItemConfig config) {
		process(config.axeSound, this.axeItems);
		process(config.bowSound, this.bowItems);
		process(config.swordSound, this.swordItems);
		process(config.toolSound, this.toolItems);
		process(config.shieldSound, this.shieldItems);
		process(config.crystalArmor, ArmorClass.CRYSTAL);
		process(config.heavyArmor, ArmorClass.HEAVY);
		process(config.mediumArmor, ArmorClass.MEDIUM);
		process(config.lightArmor, ArmorClass.LIGHT);
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

	public boolean doToolSound(@Nonnull final ItemStack stack) {
		if (stack == null || stack.getItem() == null)
			return false;
		return this.toolItems.contains(stack.getItem().getClass());
	}

	public boolean doShieldSound(@Nonnull final ItemStack stack) {
		if (stack == null || stack.getItem() == null)
			return false;
		return this.shieldItems.contains(stack.getItem().getClass());
	}

	public boolean doFoodSound(@Nonnull final ItemStack stack) {
		if (stack == null || stack.getItem() == null)
			return false;
		return stack.getItem() instanceof ItemFood;
	}

	public ArmorClass getArmorClass(@Nonnull final ItemStack stack) {
		if (stack != null) {
			final Item item = stack.getItem();
			if (item != null) {
				final ArmorClass result = this.armorMap.get(item);
				return result != null ? result : ArmorClass.NONE;
			}
		}
		return ArmorClass.NONE;
	}
	
	@SideOnly(Side.CLIENT)
	public SoundEffect getSwingSound(@Nonnull final ItemStack stack) {
		if (stack != null && stack.getItem() != null) {
			final Class<?> itemClass = stack.getItem().getClass();
			final SoundEffect sound;
			if (this.swordItems.contains(itemClass))
				sound = Sounds.SWORD_SWING;
			else if (this.axeItems.contains(itemClass))
				sound = Sounds.AXE_SWING;
			else if (this.toolItems.contains(itemClass))
				sound = Sounds.TOOL_SWING;
			else if (this.bowItems.contains(itemClass))
				sound = Sounds.TOOL_SWING;
			else if (this.shieldItems.contains(itemClass))
				sound = Sounds.TOOL_SWING;
			else {
				final ArmorClass armor = this.getArmorClass(stack);
				switch (armor) {
				case LIGHT:
					sound = Sounds.LIGHT_ARMOR_EQUIP;
					break;
				case MEDIUM:
					sound = Sounds.MEDIUM_ARMOR_EQUIP;
					break;
				case HEAVY:
					sound = Sounds.HEAVY_ARMOR_EQUIP;
					break;
				case CRYSTAL:
					sound = Sounds.CRYSTAL_ARMOR_EQUIP;
					break;
				default:
					sound = null;
				}
			}

			return sound;
		}

		return null;
	}

	@SideOnly(Side.CLIENT)
	public SoundEffect getUseSound(@Nonnull final ItemStack stack) {
		if (stack != null && stack.getItem() != null) {
			final Class<?> itemClass = stack.getItem().getClass();
			final SoundEffect sound;
			
			if (this.bowItems.contains(itemClass))
				sound = Sounds.BOW_PULL;
			else if (this.shieldItems.contains(itemClass))
				sound = Sounds.TOOL_EQUIP;
			else {
				final ArmorClass armor = this.getArmorClass(stack);
				switch (armor) {
				case LIGHT:
					sound = Sounds.LIGHT_ARMOR_EQUIP;
					break;
				case MEDIUM:
					sound = Sounds.MEDIUM_ARMOR_EQUIP;
					break;
				case HEAVY:
					sound = Sounds.HEAVY_ARMOR_EQUIP;
					break;
				case CRYSTAL:
					sound = Sounds.CRYSTAL_ARMOR_EQUIP;
					break;
				default:
					sound = null;
				}
			}

			return sound;
		}

		return null;
	}

	@SideOnly(Side.CLIENT)
	public SoundEffect getEquipSound(@Nonnull final ItemStack stack) {
		if (stack != null && stack.getItem() != null) {
			final Class<?> itemClass = stack.getItem().getClass();
			final SoundEffect sound;
			if (this.swordItems.contains(itemClass))
				sound = Sounds.SWORD_EQUIP;
			else if (this.axeItems.contains(itemClass))
				sound = Sounds.AXE_EQUIP;
			else if (this.toolItems.contains(itemClass))
				sound = Sounds.TOOL_EQUIP;
			else if (this.bowItems.contains(itemClass))
				sound = Sounds.BOW_EQUIP;
			else if (this.shieldItems.contains(itemClass))
				sound = Sounds.SHIELD_EQUIP;
			else {
				final ArmorClass armor = this.getArmorClass(stack);
				switch (armor) {
				case LIGHT:
					sound = Sounds.LIGHT_ARMOR_EQUIP;
					break;
				case MEDIUM:
					sound = Sounds.MEDIUM_ARMOR_EQUIP;
					break;
				case HEAVY:
					sound = Sounds.HEAVY_ARMOR_EQUIP;
					break;
				case CRYSTAL:
					sound = Sounds.CRYSTAL_ARMOR_EQUIP;
					break;
				default:
					sound = Sounds.UTILITY_EQUIP;
				}
			}

			return sound;
		}

		return null;
	}

}
