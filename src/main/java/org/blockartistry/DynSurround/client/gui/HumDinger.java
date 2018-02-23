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

package org.blockartistry.DynSurround.client.gui;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.sound.AdhocSound;
import org.blockartistry.DynSurround.client.sound.SoundEngine;
import org.blockartistry.lib.random.XorShiftRandom;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class HumDinger {

	private static boolean hasPlayed = false;

	@SubscribeEvent
	public static void onGuiOpen(@Nonnull final GuiOpenEvent event) {
		if (!hasPlayed && event.getGui() instanceof GuiMainMenu) {
			hasPlayed = true;
			final String[] possibles = ModOptions.general.startupSoundList;
			if (possibles == null || possibles.length == 0)
				return;
			final String res = possibles[XorShiftRandom.current().nextInt(possibles.length)];
			if (!StringUtils.isEmpty(res)) {
				final SoundEvent se = SoundEvent.REGISTRY.getObject(new ResourceLocation(res));
				if (se != null)
					SoundEngine.playSound(new AdhocSound(se, SoundCategory.MASTER));
				else
					DSurround.log().warn("Unable to locate startup sound [%s]", res);
			} else {
				DSurround.log().warn("Improperly formatted startup sound list!");
			}
		}
	}
}
