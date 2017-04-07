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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.util.Color;
import org.blockartistry.mod.DynSurround.util.Localization;
import org.blockartistry.mod.DynSurround.util.PlayerUtils;
import org.blockartistry.mod.DynSurround.util.gui.TextPanel;
import org.blockartistry.mod.DynSurround.util.gui.TextPanel.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClockHUD extends GuiOverlay {

	private static final Color BACKGROUND_COLOR = Color.DARKSLATEGRAY;
	private static final Color FRAME_COLOR = Color.MC_WHITE;
	private static final Color TIME_COLOR = Color.MC_YELLOW;

	private static final int TEXT_LINE_START = 6;

	private int elapsedMinutes;
	private int elapsedHours;
	private int elapsedSeconds;

	private final TextPanel textPanel;
	
	public ClockHUD() {
		this.textPanel = new TextPanel(TIME_COLOR, BACKGROUND_COLOR, FRAME_COLOR);
	}

	protected boolean showClock() {
		return ModOptions.enableCompass && PlayerUtils.isHolding(EnvironState.getPlayer(), Items.CLOCK);
	}

	private void updateTime() {

		if (EnvironState.getTickCounter() % 4 != 0)
			return;

		long time = DSurround.proxy().currentSessionDuration();
		this.elapsedHours = (int) (time / 3600000);
		time -= this.elapsedHours * 3600000;
		this.elapsedMinutes = (int) (time / 60000);
		time -= this.elapsedMinutes * 60000;
		this.elapsedSeconds = (int) (time / 1000);

		final List<String> text = new ArrayList<String>();
		text.add(EnvironState.getClock().toString());
		text.add(Localization.format("format.SessionTime", this.elapsedHours, this.elapsedMinutes,
				this.elapsedSeconds));
		
		this.textPanel.setText(text);
		this.textPanel.setAlpha(ModOptions.compassTransparency);
	}

	@Override
	public void doRender(@Nonnull final RenderGameOverlayEvent.Pre event) {

		if (event.getType() != ElementType.CROSSHAIRS || !showClock())
			return;

		updateTime();

		final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

		final ScaledResolution resolution = event.getResolution();
		final int centerX = (resolution.getScaledWidth() + 1) / 2;
		final int centerY = (resolution.getScaledHeight() + 1) / 2 + font.FONT_HEIGHT * TEXT_LINE_START;

		this.textPanel.render(centerX, centerY, Reference.CENTERED);
	}
}
