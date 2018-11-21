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
package org.orecruncher.dsurround.client.footsteps.system.accents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.client.footsteps.interfaces.IFootstepAccentProvider;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.registry.acoustics.IAcoustic;
import org.orecruncher.dsurround.registry.item.IArmorItemData;
import org.orecruncher.dsurround.registry.item.IItemData;
import org.orecruncher.dsurround.registry.item.ItemClass;
import org.orecruncher.dsurround.registry.item.ItemUtils;
import org.orecruncher.lib.collections.ObjectArray;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ArmorAccents implements IFootstepAccentProvider {

	@Override
	@Nonnull
	public String getName() {
		return "Armor Accents";
	}

	@Nullable
	protected IAcoustic resolveArmor(@Nonnull final ItemStack stack) {
		final IItemData id = ItemUtils.getItemData(stack.getItem());
		if (id instanceof IArmorItemData) {
			return ((IArmorItemData)id).getArmorSound(stack);
		}
		return null;
	}
	
	protected IAcoustic resolveFootArmor(@Nonnull final ItemStack stack) {
		final IItemData id = ItemUtils.getItemData(stack.getItem());
		if (id instanceof IArmorItemData) {
			return ((IArmorItemData)id).getFootArmorSound(stack);
		}
		return null;
	}
	
	@Override
	@Nonnull
	public ObjectArray<IAcoustic> provide(@Nonnull final EntityLivingBase entity, @Nullable final BlockPos pos,
			@Nonnull final ObjectArray<IAcoustic> in) {
		final ItemStack armor;
		final ItemStack foot;

		if (EnvironState.isPlayer(entity)) {
			armor = EnvironState.getPlayerItemStack();
			foot = EnvironState.getPlayerFootArmorStack();
		} else {
			armor = ItemClass.effectiveArmorStack(entity);
			foot = ItemClass.footArmorStack(entity);
		}
		
		IItemData id = ItemUtils.getItemData(armor.getItem());
		if (id instanceof IArmorItemData) {
			
		}

		final IAcoustic armorAddon = resolveArmor(armor);
		IAcoustic footAddon = resolveFootArmor(foot);

		if (armorAddon != null) {
			in.add(armorAddon);
			if (armorAddon == footAddon)
				footAddon = null;
		}

		if (footAddon != null)
			in.add(footAddon);

		return in;
	}

}
