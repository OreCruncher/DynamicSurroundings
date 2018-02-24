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
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.client.sound.Sounds;
import org.blockartistry.DynSurround.data.xface.ItemConfig;
import org.blockartistry.DynSurround.data.xface.ModConfigurationFile;
import org.blockartistry.lib.ItemStackUtil;
import org.blockartistry.lib.MCHelper;
import org.blockartistry.lib.collections.IdentityHashSet;

import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ItemRegistry extends Registry {

	private static final int SET_CAPACITY = 64;
	private static final int MAP_CAPACITY = 256;

	private static enum ItemType {
		SWORD, AXE, BOW, TOOL, SHIELD, ARMOR, FOOD
	}

	private Set<Class<?>> swordItems = new IdentityHashSet<>(SET_CAPACITY);
	private Set<Class<?>> axeItems = new IdentityHashSet<>(SET_CAPACITY);
	private Set<Class<?>> bowItems = new IdentityHashSet<>(SET_CAPACITY);
	private Set<Class<?>> toolItems = new IdentityHashSet<>(SET_CAPACITY);
	private Set<Class<?>> shieldItems = new IdentityHashSet<>(SET_CAPACITY);
	private Set<Class<?>> crystalItems = new IdentityHashSet<>(SET_CAPACITY);
	private Set<Class<?>> heavyItems = new IdentityHashSet<>(SET_CAPACITY);
	private Set<Class<?>> mediumItems = new IdentityHashSet<>(SET_CAPACITY);
	private Set<Class<?>> lightItems = new IdentityHashSet<>(SET_CAPACITY);

	private final Map<Item, ItemType> items = new IdentityHashMap<>(MAP_CAPACITY);
	private final Map<Item, ArmorClass> armorMap = new IdentityHashMap<>(SET_CAPACITY);
	private final Map<Item, SoundEffect> swings = new IdentityHashMap<>(MAP_CAPACITY);
	private final Map<Item, SoundEffect> uses = new IdentityHashMap<>(MAP_CAPACITY);
	private final Map<Item, SoundEffect> equips = new IdentityHashMap<>(MAP_CAPACITY);

	public ItemRegistry(@Nonnull final Side side) {
		super(side);
	}

	@Override
	public void init() {
		this.swordItems = new IdentityHashSet<>(SET_CAPACITY);
		this.axeItems = new IdentityHashSet<>(SET_CAPACITY);
		this.bowItems = new IdentityHashSet<>(SET_CAPACITY);
		this.toolItems = new IdentityHashSet<>(SET_CAPACITY);
		this.shieldItems = new IdentityHashSet<>(SET_CAPACITY);
		this.crystalItems = new IdentityHashSet<>(SET_CAPACITY);
		this.heavyItems = new IdentityHashSet<>(SET_CAPACITY);
		this.mediumItems = new IdentityHashSet<>(SET_CAPACITY);
		this.lightItems = new IdentityHashSet<>(SET_CAPACITY);

		this.items.clear();
		this.armorMap.clear();
		this.swings.clear();
		this.uses.clear();
		this.equips.clear();
	}

	@Override
	public void configure(@Nonnull final ModConfigurationFile cfg) {
		final ItemConfig config = cfg.itemConfig;
		process(config.axeSound, this.axeItems, ItemType.AXE, null);
		process(config.bowSound, this.bowItems, ItemType.BOW, null);
		process(config.swordSound, this.swordItems, ItemType.SWORD, null);
		process(config.toolSound, this.toolItems, ItemType.TOOL, null);
		process(config.shieldSound, this.shieldItems, ItemType.SHIELD, null);
		process(config.crystalArmor, this.crystalItems, ItemType.ARMOR, ArmorClass.CRYSTAL);
		process(config.heavyArmor, this.heavyItems, ItemType.ARMOR, ArmorClass.HEAVY);
		process(config.mediumArmor, this.mediumItems, ItemType.ARMOR, ArmorClass.MEDIUM);
		process(config.lightArmor, this.lightItems, ItemType.ARMOR, ArmorClass.LIGHT);
	}

	@Override
	public void initComplete() {

		// Iterate through the list of registered Items to see
		// if we know about them, or can infer based on class
		// matching.
		final Iterator<Item> iterator = Item.REGISTRY.iterator();
		while (iterator.hasNext()) {
			final Item item = iterator.next();
			if (!this.items.containsKey(item)) {
				if (doesBelong(this.swordItems, item)) {
					this.items.put(item, ItemType.SWORD);
				} else if (doesBelong(this.axeItems, item)) {
					this.items.put(item, ItemType.AXE);
				} else if (doesBelong(this.toolItems, item)) {
					this.items.put(item, ItemType.TOOL);
				} else if (doesBelong(this.shieldItems, item)) {
					this.items.put(item, ItemType.SHIELD);
				} else if (doesBelong(this.bowItems, item)) {
					this.items.put(item, ItemType.BOW);
				} else if (doesBelong(this.crystalItems, item)) {
					this.items.put(item, ItemType.ARMOR);
					this.armorMap.put(item, ArmorClass.CRYSTAL);
				} else if (doesBelong(this.heavyItems, item)) {
					this.items.put(item, ItemType.ARMOR);
					this.armorMap.put(item, ArmorClass.HEAVY);
				} else if (doesBelong(this.mediumItems, item)) {
					this.items.put(item, ItemType.ARMOR);
					this.armorMap.put(item, ArmorClass.MEDIUM);
				} else if (doesBelong(this.lightItems, item)) {
					this.items.put(item, ItemType.ARMOR);
					this.armorMap.put(item, ArmorClass.LIGHT);
				} else if (item instanceof ItemFood) {
					this.items.put(item, ItemType.FOOD);
				}
			}

			// Process sounds for Items that we are concerned with
			final ItemType t = this.items.get(item);
			if (t != null) {
				SoundEffect se = this.getSwingSound(item, t);
				if (se != null)
					this.swings.put(item, se);

				se = this.getUseSound(item, t);
				if (se != null)
					this.uses.put(item, se);

				se = this.getEquipSound(item, t);
				if (se != null)
					this.equips.put(item, se);
			}
		}

		// Free up resources that are no longer needed
		this.swordItems = null;
		this.axeItems = null;
		this.bowItems = null;
		this.toolItems = null;
		this.shieldItems = null;
		this.crystalItems = null;
		this.heavyItems = null;
		this.mediumItems = null;
		this.lightItems = null;
	}

	@Override
	public void fini() {

	}

	private boolean doesBelong(@Nonnull final Set<Class<?>> itemSet, @Nonnull final Item item) {

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

	private void process(@Nonnull final List<String> items, @Nonnull final Set<Class<?>> theList,
			@Nonnull final ItemType it, @Nullable final ArmorClass armor) {
		for (final String c : items) {
			final boolean likeMatch = c.startsWith("@") || !c.contains(":");
			final Item item = c.contains(":") ? MCHelper.getItemByName(c) : null;

			// If its not a like match it has to be a concrete item
			if (!likeMatch) {
				if (item != null) {
					this.items.put(item, it);
					if (armor != null)
						this.armorMap.put(item, armor);
				} else {
					DSurround.log().warn("Cannot locate item [%s] for ItemRegistry", c);
				}
			} else {
				try {
					// If we don't have an Item assume its a class name. If it is an item
					// we want that class.
					final Class<?> clazz;
					if (item == null)
						clazz = Class.forName(c, false, ItemRegistry.class.getClassLoader());
					else
						clazz = item.getClass();
					theList.add(clazz);
				} catch (@Nonnull final ClassNotFoundException e) {
					DSurround.log().warn("Cannot locate class '%s' for ItemRegistry", c);
				}
			}
		}
	}

	protected ArmorClass getArmorClass(@Nonnull final Item item) {
		final ArmorClass result = this.armorMap.get(item);
		return result != null ? result : ArmorClass.NONE;
	}

	protected SoundEffect getSwingSound(@Nonnull final Item item, @Nonnull final ItemType t) {
		final SoundEffect sound;
		switch (t) {
		case SWORD:
			sound = Sounds.SWORD_SWING;
			break;
		case AXE:
			sound = Sounds.AXE_SWING;
			break;
		case BOW:
		case TOOL:
		case SHIELD:
			sound = Sounds.TOOL_SWING;
			break;
		case ARMOR:
			final ArmorClass armor = this.getArmorClass(item);
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
			break;
		case FOOD:
		default:
			sound = null;
		}

		return sound;
	}

	protected SoundEffect getUseSound(@Nonnull final Item item, @Nonnull final ItemType t) {
		final SoundEffect sound;
		switch (t) {
		case BOW:
			sound = Sounds.BOW_PULL;
			break;
		case SHIELD:
			sound = Sounds.SHIELD_USE;
			break;
		case ARMOR:
			final ArmorClass armor = this.getArmorClass(item);
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
			break;
		default:
			sound = null;
		}

		return sound;
	}

	protected SoundEffect getEquipSound(@Nonnull final Item item, @Nonnull final ItemType t) {
		final SoundEffect sound;
		switch (t) {
		case FOOD:
			sound = Sounds.FOOD_EQUIP;
			break;
		case SWORD:
			sound = ModOptions.sound.swordEquipAsTool ? Sounds.TOOL_EQUIP : Sounds.SWORD_EQUIP;
			break;
		case AXE:
			sound = Sounds.AXE_EQUIP;
			break;
		case TOOL:
			sound = Sounds.TOOL_EQUIP;
			break;
		case BOW:
			sound = Sounds.BOW_EQUIP;
			break;
		case SHIELD:
			sound = Sounds.SHIELD_EQUIP;
			break;
		case ARMOR:
			final ArmorClass armor = this.getArmorClass(item);
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
			break;
		default:
			sound = null;
		}

		return sound;
	}

	@Nullable
	public ItemType getItemType(@Nonnull final ItemStack stack) {
		return ItemStackUtil.isValidItemStack(stack) ? this.items.get(stack.getItem()) : null;
	}

	public boolean isBow(@Nonnull final ItemStack stack) {
		return getItemType(stack) == ItemType.BOW;
	}

	public boolean isShield(@Nonnull final ItemStack stack) {
		return getItemType(stack) == ItemType.SHIELD;
	}

	@Nonnull
	public ArmorClass getArmorClass(@Nonnull final ItemStack stack) {
		return ItemStackUtil.isValidItemStack(stack) ? getArmorClass(stack.getItem()) : ArmorClass.NONE;
	}

	@Nullable
	public SoundEffect getSwingSound(@Nonnull final ItemStack stack) {
		return ItemStackUtil.isValidItemStack(stack) ? this.swings.get(stack.getItem()) : null;
	}

	@Nullable
	public SoundEffect getUseSound(@Nonnull final ItemStack stack) {
		return ItemStackUtil.isValidItemStack(stack) ? this.uses.get(stack.getItem()) : null;
	}

	@Nullable
	public SoundEffect getEquipSound(@Nonnull final ItemStack stack) {
		if (ItemStackUtil.isValidItemStack(stack)) {
			final SoundEffect result = this.equips.get(stack.getItem());
			return result != null ? result : Sounds.UTILITY_EQUIP;
		}
		return null;
	}

}
