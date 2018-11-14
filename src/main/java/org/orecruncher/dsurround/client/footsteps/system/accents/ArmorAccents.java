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

import org.orecruncher.dsurround.client.ClientRegistry;
import org.orecruncher.dsurround.client.footsteps.interfaces.IAcoustic;
import org.orecruncher.dsurround.client.footsteps.interfaces.IFootstepAccentProvider;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.registry.item.ItemClass;
import org.orecruncher.lib.collections.ObjectArray;

import net.minecraft.entity.EntityLivingBase;
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

	@Override
	@Nonnull
	public ObjectArray<IAcoustic> provide(@Nonnull final EntityLivingBase entity, @Nullable final BlockPos pos,
			@Nonnull final ObjectArray<IAcoustic> in) {
		final ItemClass armor;
		final ItemClass foot;

		if (EnvironState.isPlayer(entity)) {
			armor = EnvironState.getPlayerArmorClass();
			foot = EnvironState.getPlayerFootArmorClass();
		} else {
			armor = ItemClass.effectiveArmorClass(entity);
			foot = ItemClass.footArmorClass(entity);
		}

		final IAcoustic armorAddon = ClientRegistry.FOOTSTEPS.getArmorAcoustic(armor);
		IAcoustic footAddon = ClientRegistry.FOOTSTEPS.getFootArmorAcoustic(foot);

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
