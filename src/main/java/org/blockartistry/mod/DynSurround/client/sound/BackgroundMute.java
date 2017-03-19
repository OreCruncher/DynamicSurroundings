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

package org.blockartistry.mod.DynSurround.client.sound;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class BackgroundMute {

	private static boolean isMuted = false;
	private static float savedVolume = 1.0F;

	@SubscribeEvent
	public static void clientTick(final TickEvent.ClientTickEvent event) {
		if (ModOptions.muteWhenBackground) {
			final GameSettings settings = Minecraft.getMinecraft().gameSettings;
			if (isMuted && Display.isActive()) {
				isMuted = false;
				settings.setSoundLevel(SoundCategory.MASTER, savedVolume);
				ModLog.info("Sound unmuted; volume %f", savedVolume);
			} else if (!isMuted && !Display.isActive()) {
				isMuted = true;
				savedVolume = settings.getSoundLevel(SoundCategory.MASTER);
				settings.setSoundLevel(SoundCategory.MASTER, 0.0001F);
				ModLog.info("Muting sounds");
			}
		}
	}
}
