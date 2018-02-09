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
package org.blockartistry.DynSurround.client.footsteps.system.accents;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IFootstepAccentProvider;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.registry.ArmorClass;
import org.blockartistry.lib.collections.ObjectArray;

import net.minecraft.entity.EntityLivingBase;
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
	public ObjectArray<IAcoustic> provide(@Nonnull final EntityLivingBase entity,
			@Nonnull final ObjectArray<IAcoustic> in) {
		final ArmorClass armor;
		final ArmorClass foot;

		if (EnvironState.isPlayer(entity)) {
			armor = EnvironState.getPlayerArmorClass();
			foot = EnvironState.getPlayerFootArmorClass();
		} else {
			armor = ArmorClass.effectiveArmorClass(entity);
			foot = ArmorClass.footArmorClass(entity);
		}

		final IAcoustic armorAddon = ClientRegistry.FOOTSTEPS.getArmorAcoustic(armor);
		IAcoustic footAddon = ClientRegistry.FOOTSTEPS.getFootArmorAcoustic(foot);

		if (armorAddon != null || footAddon != null) {
			// Eliminate duplicates
			if (armorAddon == footAddon)
				footAddon = null;

			in.add(armorAddon);
			in.add(footAddon);
		}
		return in;
	}

}
