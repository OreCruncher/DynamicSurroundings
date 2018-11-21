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

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.registry.acoustics.IAcoustic;
import org.orecruncher.dsurround.registry.item.ItemClass;
import org.orecruncher.dsurround.registry.item.SimpleArmorItemData;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConstructArmoryItemData extends SimpleArmorItemData {

	public ConstructArmoryItemData(@Nonnull final ItemClass ic) {
		super(ic);
	}

	protected String getIdentifier(@Nonnull final ItemStack stack) {
		final NBTTagCompound nbt = stack.getTagCompound();
		if (nbt != null && nbt.hasKey("Modifiers")) {
			final NBTTagList modifiers = nbt.getTagList("Modifiers", 10);
			if (modifiers != null && !modifiers.isEmpty()) {
				final NBTTagCompound piece = modifiers.getCompoundTagAt(0);
				if (piece != null && piece.hasKey("identifier")) {
					final String id = piece.getString("identifier");
					if (!StringUtils.isEmpty(id))
						return id;
				}
			}
		}
		return null;
	}

	@Override
	@Nullable
	public SoundEffect getEquipSound(@Nonnull final ItemStack stack) {
		return super.getEquipSound(stack);
	}

	@Override
	@Nullable
	public SoundEffect getSwingSound(@Nonnull final ItemStack stack) {
		return super.getSwingSound(stack);
	}

	@Override
	@Nullable
	public SoundEffect getUseSound(@Nonnull final ItemStack stack) {
		return super.getUseSound(stack);
	}

	@Override
	@Nullable
	public IAcoustic getArmorSound(@Nonnull final ItemStack stack) {
		final String id = getIdentifier(stack);
		if (id != null) {
			final IAcoustic a = ConstructArmoryProducer.ARMOR.get(id);
			if (a != null)
				return a;
		}
		return super.getArmorSound(stack);
	}

	@Override
	@Nullable
	public IAcoustic getFootArmorSound(@Nonnull final ItemStack stack) {
		final String id = getIdentifier(stack);
		if (id != null) {
			final IAcoustic a = ConstructArmoryProducer.FOOT.get(id);
			if (a != null)
				return a;
		}
		return super.getFootArmorSound(stack);
	}

}
