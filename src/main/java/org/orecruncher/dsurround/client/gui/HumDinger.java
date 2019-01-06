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

package org.orecruncher.dsurround.client.gui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.sound.SoundBuilder;
import org.orecruncher.dsurround.client.sound.SoundEngine;
import org.orecruncher.dsurround.client.sound.SoundInstance;
import org.orecruncher.lib.random.XorShiftRandom;
import org.orecruncher.lib.task.Scheduler;

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
			//@formatter:off
			final List<String> possibles = Arrays.stream(ModOptions.general.startupSoundList)
				.map(s -> StringUtils.trim(s))
				.filter(s -> s.length() > 0)
				.collect(Collectors.toList());
			//@formatter:on
			if (possibles.size() == 0)
				return;
			final String res = possibles.get(XorShiftRandom.current().nextInt(possibles.size()));
			if (!StringUtils.isEmpty(res)) {
				final SoundEvent se = SoundEvent.REGISTRY.getObject(new ResourceLocation(res));
				if (se != null) {
					Scheduler.scheduleDeferred(Side.CLIENT, () -> {
						try {
							final SoundInstance snd = SoundBuilder.create(se, SoundCategory.MASTER);
							SoundEngine.instance().playSound(snd);
						} catch (@Nonnull final Throwable t) {
							ModBase.log().error("Error executing ding", t);
						}
					});
				} else {
					ModBase.log().warn("Unable to locate startup sound [%s]", res);
				}
			} else {
				ModBase.log().warn("Improperly formatted startup sound list!");
			}
		}
	}
}
