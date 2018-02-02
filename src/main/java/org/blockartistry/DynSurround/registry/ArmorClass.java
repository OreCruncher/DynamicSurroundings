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

package org.blockartistry.DynSurround.registry;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.ClientRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum ArmorClass {

	//
	NONE("none"),
	//
	LIGHT("light"),
	//
	MEDIUM("medium"),
	//
	CRYSTAL("crystal"),
	//
	HEAVY("heavy");

	private final String className;

	private ArmorClass(@Nonnull final String name) {
		this.className = name;
	}

	@Nonnull
	public String getClassName() {
		return this.className;
	}

	/**
	 * Determines the effective armor class of the Entity. Chest and legs are used
	 * to make the determination.
	 */
	public static ArmorClass effectiveArmorClass(@Nonnull final EntityLivingBase entity) {
		final ArmorClass chest = ClientRegistry.ITEMS
				.getArmorClass(entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST));
		final ArmorClass legs = ClientRegistry.ITEMS
				.getArmorClass(entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS));
		return chest.compareTo(legs) > 0 ? chest : legs;
	}

	/**
	 * Gets the armor class of the entities feet.
	 */
	public static ArmorClass footArmorClass(@Nonnull final EntityLivingBase entity) {
		return ClientRegistry.ITEMS.getArmorClass(entity.getItemStackFromSlot(EntityEquipmentSlot.FEET));
	}

}
