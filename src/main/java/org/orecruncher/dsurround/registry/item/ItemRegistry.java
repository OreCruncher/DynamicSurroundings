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

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.config.ModConfiguration;
import org.orecruncher.dsurround.registry.item.compat.ItemDataProducer;
import org.orecruncher.lib.ItemStackUtil;
import org.orecruncher.lib.MCHelper;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ItemRegistry extends Registry {

	// https://www.regexplanet.com/advanced/java/index.html

	// Pattern for matching Java class names
	private static final String ID_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
	private static final Pattern FQCN = Pattern.compile(ID_PATTERN + "(\\." + ID_PATTERN + ")*");

	// Pattern for matching ItemStack names
	private static final Pattern ITEM_PATTERN = Pattern.compile("([\\w\\-]+:[\\w\\.\\-/]+)[:]?(\\d+|\\*)?(\\{.*\\})?");

	private static final int SET_CAPACITY = 64;
	private static final int MAP_CAPACITY = 256;

	private SimpleItemData NONE_DATA;

	private EnumMap<ItemClass, Set<Class<?>>> classMap;
	private Map<Item, IItemData> items;

	public ItemRegistry() {
		super("Item Data");
	}

	@Override
	public void init() {
		this.classMap = new EnumMap<>(ItemClass.class);
		this.items = new IdentityHashMap<>(MAP_CAPACITY);
		this.NONE_DATA = SimpleItemData.CACHE.get(ItemClass.NONE);
		
		Item.REGISTRY.iterator().forEachRemaining(item -> ItemUtils.setItemData(item, NONE_DATA));
		ItemUtils.setItemData(Items.AIR, SimpleItemData.CACHE.get(ItemClass.EMPTY));

		for (final ItemClass ic : ItemClass.values())
			this.classMap.put(ic, new ReferenceOpenHashSet<>(SET_CAPACITY));
	}

	@Override
	public void configure(@Nonnull final ModConfiguration cfg) {
		for (final Entry<String, List<String>> entry : cfg.items.entrySet()) {
			process(entry.getValue(), entry.getKey());
		}
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
				final ItemClass ic = resolveClass(item);
				if (ic != ItemClass.NONE) {
					this.items.put(item, ItemDataProducer.create(item, ic));
				}
			}
		}

		// Set the cached field on the Item object with the data
		this.items.entrySet().forEach(entry -> ItemUtils.setItemData(entry.getKey(), entry.getValue()));

		// Free up resources that are no longer needed
		this.items = null;
		this.classMap = null;
	}

	private ItemClass resolveClass(@Nonnull final Item item) {
		for (final ItemClass ic : ItemClass.values()) {
			final Set<Class<?>> itemSet = this.classMap.get(ic);
			if (doesBelong(itemSet, item))
				return ic;
		}
		return ItemClass.NONE;
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

	private void process(@Nullable final List<String> items, @Nonnull final String itemClass) {
		if (items == null || items.isEmpty())
			return;

		final ItemClass ic = ItemClass.valueOf(itemClass);
		if (ic == null) {
			ModBase.log().warn("Unknown ItemClass %s", itemClass);
			return;
		}

		final Set<Class<?>> theList = this.classMap.get(ic);

		for (final String c : items) {
			// If its not a like match it has to be a concrete item
			Matcher match = ITEM_PATTERN.matcher(c);
			if (match.matches()) {
				final String itemName = match.group(1);
				final Item item = MCHelper.getItemByName(itemName);
				if (item != null) {
					this.items.put(item, ItemDataProducer.create(item, ic));
				} else {
					ModBase.log().warn("Cannot locate item [%s] for ItemRegistry", c);
				}
			} else {
				match = FQCN.matcher(c);
				if (match.matches()) {
					try {
						// If we don't have an Item assume its a class name. If it is an item
						// we want that class.
						final Class<?> clazz = Class.forName(c, false, ItemRegistry.class.getClassLoader());
						theList.add(clazz);
					} catch (@Nonnull final ClassNotFoundException e) {
						ModBase.log().warn("Cannot locate class '%s' for ItemRegistry", c);
					}
				} else {
					ModBase.log().warn("Unrecognized pattern '%s' for ItemRegistry", c);
				}
			}
		}
	}

	@Nonnull
	public IItemData getItemClass(@Nonnull final ItemStack stack) {
		return ItemStackUtil.isValidItemStack(stack) ? ItemUtils.getItemData(stack.getItem()) : NONE_DATA;
	}

}
