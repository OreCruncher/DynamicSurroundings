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
import org.orecruncher.dsurround.client.ClientRegistry;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.client.sound.Sounds;
import org.orecruncher.dsurround.lib.compat.ModEnvironment;

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
	NONE(null, null, Sounds.UTILITY_EQUIP),
	LEATHER(Sounds.LEATHER_ARMOR_EQUIP),
	CHAIN(Sounds.CHAIN_ARMOR_EQUIP),
	CRYSTAL(Sounds.CRYSTAL_ARMOR_EQUIP),
	PLATE(Sounds.PLATE_ARMOR_EQUIP),
	SLIME(null, null, Sounds.UTILITY_EQUIP),
	SHIELD(Sounds.TOOL_SWING, Sounds.SHIELD_USE, Sounds.SHIELD_EQUIP),
	SWORD(Sounds.SWORD_SWING, null, ModOptions.sound.swordEquipAsTool ? Sounds.TOOL_EQUIP : Sounds.SWORD_EQUIP),
	AXE(Sounds.AXE_SWING, null, Sounds.AXE_EQUIP),
	BOW(Sounds.TOOL_SWING, Sounds.BOW_PULL, Sounds.BOW_EQUIP),
	TOOL(Sounds.TOOL_SWING, null, Sounds.TOOL_EQUIP),
	FOOD(null, null, Sounds.FOOD_EQUIP);
	//@formatter:on

	private final SoundEffect swing;
	private final SoundEffect use;
	private final SoundEffect equip;
	
	private ItemClass(@Nullable final SoundEffect sound) {
		this(sound, sound, sound);
	}
	private ItemClass(@Nullable final SoundEffect swing, @Nullable final SoundEffect use, @Nullable final SoundEffect equip) {
		this.swing = swing;
		this.use = use;
		this.equip = equip;
	}
	
	@Nullable
	public SoundEffect getSwingSound() {
		return this.swing;
	}
	
	@Nullable
	public SoundEffect getUseSound() {
		return this.use;
	}
	
	@Nullable
	public SoundEffect getEquipSound() {
		return this.equip;
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
	public static ItemClass effectiveArmorClass(@Nonnull final EntityLivingBase entity) {
		final ItemClass chest = ClientRegistry.ITEMS.getItemClass(resolveSlot(entity, EntityEquipmentSlot.CHEST));
		final ItemClass legs = ClientRegistry.ITEMS.getItemClass(resolveSlot(entity, EntityEquipmentSlot.LEGS));
		return chest.compareTo(legs) > 0 ? chest : legs;
	}

	/**
	 * Gets the armor class of the entities feet.
	 */
	public static ItemClass footArmorClass(@Nonnull final EntityLivingBase entity) {
		return ClientRegistry.ITEMS.getItemClass(resolveSlot(entity, EntityEquipmentSlot.FEET));
	}

}
