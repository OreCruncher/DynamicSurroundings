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

package org.blockartistry.mod.DynSurround.client.hud;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.hud.GuiHUDHandler.GuiOverlay;
import org.blockartistry.mod.DynSurround.util.Color;
import org.blockartistry.mod.DynSurround.util.PlayerUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClockHUD extends GuiOverlay {

	private static final Color TIME_COLOR = Color.MC_YELLOW;

	private int elapsedMinutes;
	private int elapsedHours;
	private int elapsedSeconds;

	private String time;
	private String elapsed;

	protected boolean showClock() {
		return ModOptions.enableCompass && PlayerUtils.isHolding(EnvironState.getPlayer(), Items.CLOCK);
	}

	private void updateTime() {

		if (this.time != null && EnvironState.getTickCounter() % 4 != 0)
			return;

		long time = DSurround.proxy().currentSessionDuration();
		this.elapsedHours = (int) (time / 3600000);
		time -= this.elapsedHours * 3600000;
		this.elapsedMinutes = (int) (time / 60000);
		time -= this.elapsedMinutes * 60000;
		this.elapsedSeconds = (int) (time / 1000);

		this.time = EnvironState.getClock().toString();
		this.elapsed = String.format("Session Time %d:%02d:%02d", this.elapsedHours, this.elapsedMinutes,
				this.elapsedSeconds);

	}

	@Override
	public void doRender(@Nonnull final RenderGameOverlayEvent.Pre event) {

		if (event.getType() != ElementType.CROSSHAIRS || !showClock())
			return;

		updateTime();

		final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

		final ScaledResolution resolution = event.getResolution();
		final int centerX = (resolution.getScaledWidth() + 1) / 2;
		final int centerY = (resolution.getScaledHeight() + 1) / 2 + 50;

		GlStateManager.color(1F, 1F, 1F, ModOptions.compassTransparency);
		GlStateManager.enableBlend();

		drawCenteredString(font, time, centerX, (int) (centerY),
				TIME_COLOR.rgbWithAlpha(ModOptions.compassTransparency));
		drawCenteredString(font, elapsed, centerX, (int) (centerY + 15),
				TIME_COLOR.rgbWithAlpha(ModOptions.compassTransparency));

		GlStateManager.color(1F, 1F, 1F, 1F);

	}
}
