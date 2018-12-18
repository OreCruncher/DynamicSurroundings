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

package org.orecruncher.dsurround.client.hud;

import java.util.ArrayList;
import java.util.List;

import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class GuiHUDHandler {

	private static GuiHUDHandler INSTANCE;

	private GuiHUDHandler() {
		register(new InspectionHUD());
	}

	private final List<GuiOverlay> overlays = new ArrayList<>();

	public void register(final GuiOverlay overlay) {
		this.overlays.add(overlay);
	}

	public static void register() {
		INSTANCE = new GuiHUDHandler();
		MinecraftForge.EVENT_BUS.register(INSTANCE);
	}

	public static void unregister() {
		if (INSTANCE != null) {
			MinecraftForge.EVENT_BUS.unregister(INSTANCE);
			INSTANCE = null;
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onRenderGameOverlayEvent(final RenderGameOverlayEvent.Pre event) {
		for (int i = 0; i < this.overlays.size(); i++)
			this.overlays.get(i).doRender(event);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onRenderGameOverlayEvent(final RenderGameOverlayEvent.Post event) {
		for (int i = 0; i < this.overlays.size(); i++)
			this.overlays.get(i).doRender(event);
	}

	@SubscribeEvent
	public void playerTick(final TickEvent.PlayerTickEvent event) {
		if (event.side == Side.SERVER || event.phase == Phase.END || Minecraft.getMinecraft().isGamePaused())
			return;

		if (event.player == null || event.player.world == null)
			return;

		if (event.player != Minecraft.getMinecraft().player)
			return;

		final int tickRef = EnvironState.getTickCounter();
		for (int i = 0; i < this.overlays.size(); i++)
			this.overlays.get(i).doTick(tickRef);

	}
}
