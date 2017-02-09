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

package org.blockartistry.mod.DynSurround.client.keyboard;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.hud.LightLevelHUD;
import org.blockartistry.mod.DynSurround.client.hud.LightLevelHUD.Mode;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class KeyHandler {

	private static final KeyBinding SELECTIONBOX_KEY = new KeyBinding("Toggle SelectionBox", Keyboard.KEY_B,
			DSurround.MOD_NAME);
	private static final KeyBinding LIGHTLEVEL_KEY = new KeyBinding("Toggle Light Level", Keyboard.KEY_L,
			DSurround.MOD_NAME);

	static {
		ClientRegistry.registerKeyBinding(SELECTIONBOX_KEY);
		ClientRegistry.registerKeyBinding(LIGHTLEVEL_KEY);
	}

	@SubscribeEvent(receiveCanceled = false)
	public static void onKeyboard(@Nonnull InputEvent.KeyInputEvent event) {

		if (SELECTIONBOX_KEY.isPressed()) {
			final EntityRenderer renderer = Minecraft.getMinecraft().entityRenderer;
			renderer.drawBlockOutline = !renderer.drawBlockOutline;
		}

		if (LIGHTLEVEL_KEY.isPressed()) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				// Only change mode when visible
				if (LightLevelHUD.showHUD) {
					ModOptions.llDisplayMode++;
					if (ModOptions.llDisplayMode >= Mode.values().length)
						ModOptions.llDisplayMode = 0;
					ModLog.info("LightLevel HUD mode: %s", Mode.getMode(ModOptions.llDisplayMode).name());
				}
			} else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				if (LightLevelHUD.showHUD) {
					ModOptions.llHideSafe = !ModOptions.llHideSafe;
					ModLog.info("LightLevel HUD hidesafe: %s", ModOptions.llHideSafe ? "ENABLED" : "DISABLED");
				}
			} else {
				LightLevelHUD.showHUD = !LightLevelHUD.showHUD;
				ModLog.info("LighLevel HUD: %s", LightLevelHUD.showHUD ? "ENABLED" : "DISABLED");
			}
		}

	}

}
