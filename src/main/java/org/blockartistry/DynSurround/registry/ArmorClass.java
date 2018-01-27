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
	NONE("none", "NOT_EMITTER", "NOT_EMITTER"),
	//
	LIGHT("light", "armor_light", "NOT_EMITTER"),
	//
	MEDIUM("medium", "armor_medium", "medium_foot"),
	//
	CRYSTAL("crystal", "armor_crystal", "crystal_foot"),
	//
	HEAVY("heavy", "armor_heavy", "heavy_foot");

	private final String className;
	private final String acoustic;
	private final String footAcoustic;

	private ArmorClass(@Nonnull final String name, @Nonnull final String acoustic, @Nonnull final String footAcoustic) {
		this.className = name;
		this.acoustic = acoustic;
		this.footAcoustic = footAcoustic;
	}

	@Nonnull
	public String getClassName() {
		return this.className;
	}

	@Nonnull
	public String getAcoustic() {
		return this.acoustic;
	}

	@Nonnull
	public String getFootAcoustic() {
		return this.footAcoustic;
	}

	/**
	 * Determines the effective armor class of the Entity. Used to determine the
	 * sound overlay to add. The chest and leg slots are used.
	 */
	public static ArmorClass effectiveArmorClass(@Nonnull final EntityLivingBase entity) {
		final ArmorClass chest = ClientRegistry.ITEMS
				.getArmorClass(entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST));
		final ArmorClass legs = ClientRegistry.ITEMS
				.getArmorClass(entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS));
		return chest.compareTo(legs) > 0 ? chest : legs;
	}

	/**
	 * Gets the armor class of the entities feet in order to apply additional sound
	 * accents when moving.
	 */
	public static ArmorClass footArmorClass(@Nonnull final EntityLivingBase entity) {
		return ClientRegistry.ITEMS.getArmorClass(entity.getItemStackFromSlot(EntityEquipmentSlot.FEET));
	}

}
