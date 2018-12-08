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

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.client.sound.Sounds;
import org.orecruncher.dsurround.lib.compat.ModEnvironment;
import org.orecruncher.dsurround.registry.RegistryManager;

import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum ItemClass {

	//@formatter:off
	EMPTY(null),	// For things that don't make an equip sound - like AIR
	NONE(null, null, Sounds.UTILITY_EQUIP, false),
	LEATHER(Sounds.LEATHER_ARMOR_EQUIP, true),
	CHAIN(Sounds.CHAIN_ARMOR_EQUIP, true),
	CRYSTAL(Sounds.CRYSTAL_ARMOR_EQUIP, true),
	PLATE(Sounds.PLATE_ARMOR_EQUIP, true),
	SHIELD(Sounds.TOOL_SWING, Sounds.SHIELD_USE, Sounds.SHIELD_EQUIP, false),
	SWORD(Sounds.SWORD_SWING, null, ModOptions.sound.swordEquipAsTool ? Sounds.TOOL_EQUIP : Sounds.SWORD_EQUIP, false),
	AXE(Sounds.AXE_SWING, null, Sounds.AXE_EQUIP, false),
	BOW(Sounds.TOOL_SWING, Sounds.BOW_PULL, Sounds.BOW_EQUIP, false),
	TOOL(Sounds.TOOL_SWING, null, Sounds.TOOL_EQUIP, false),
	FOOD(null, null, Sounds.FOOD_EQUIP, false),
	BOOK(Sounds.BOOK_EQUIP, Sounds.BOOK_EQUIP, Sounds.BOOK_EQUIP, false);
	//@formatter:on

	private final SoundEffect swing;
	private final SoundEffect use;
	private final SoundEffect equip;
	private final boolean isArmor;

	private ItemClass(@Nullable final SoundEffect sound) {
		this(sound, sound, sound, false);
	}

	private ItemClass(@Nullable final SoundEffect sound, final boolean isArmor) {
		this(sound, sound, sound, isArmor);
	}

	private ItemClass(@Nullable final SoundEffect swing, @Nullable final SoundEffect use,
			@Nullable final SoundEffect equip, final boolean isArmor) {
		this.swing = swing;
		this.use = use;
		this.equip = equip;
		this.isArmor = isArmor;
	}

	// Package internal
	@Nullable
	SoundEffect getSwingSound() {
		return this.swing;
	}

	// Package internal
	@Nullable
	SoundEffect getUseSound() {
		return this.use;
	}

	// Package internal
	@Nullable
	SoundEffect getEquipSound() {
		return this.equip;
	}

	public boolean isArmor() {
		return this.isArmor;
	}

	@Nonnull
	private static ItemStack resolveSlot(@Nonnull final EntityLivingBase e, @Nonnull final EntityEquipmentSlot slot) {
		if (ModEnvironment.CosmeticArmorReworked.isLoaded()) {
			final CAStacksBase slots = CosArmorAPI.getCAStacksClient(e.getPersistentID());
			if (slots != null) {
				final ItemStack stack = slots.getStackInSlot(slot.getIndex());
				if (stack != null && !stack.isEmpty())
					return stack;
			}
		}
		return e.getItemStackFromSlot(slot);
	}

	/**
	 * Determines the effective armor class of the Entity. Chest and legs are used
	 * to make the determination.
	 */
	public static ItemStack effectiveArmorStack(@Nonnull final EntityLivingBase entity) {
		final ItemStack chest = resolveSlot(entity, EntityEquipmentSlot.CHEST);
		final ItemStack legs = resolveSlot(entity, EntityEquipmentSlot.LEGS);
		final ItemClass chestic = RegistryManager.ITEMS.getItemClass(chest).getItemClass();
		final ItemClass legsic = RegistryManager.ITEMS.getItemClass(legs).getItemClass();
		return chestic.compareTo(legsic) > 0 ? chest.copy() : legs.copy();
	}

	/**
	 * Gets the armor class of the entities feet.
	 */
	public static ItemStack footArmorStack(@Nonnull final EntityLivingBase entity) {
		return resolveSlot(entity, EntityEquipmentSlot.FEET);
	}

}
