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

package org.blockartistry.lib.gui;

import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.lib.Localization;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTooltip {
	
	public static interface ITooltipRenderer {
		void drawTooltip(final int x, final int y, @Nonnull final List<String> text);
	}
	
	protected final ITooltipRenderer renderer;
	protected final HoverChecker checker;
	protected final List<String> tooltip;
	
	public GuiTooltip(@Nonnull final ITooltipRenderer renderer, @Nonnull GuiButton button, @Nonnull final String tipText) {
		this(renderer, button, tipText, 200);
	}

	public GuiTooltip(@Nonnull final ITooltipRenderer renderer, @Nonnull GuiButton button, @Nonnull final String tipText, final int width) {
		final FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		this.renderer = renderer;
		this.checker = new HoverChecker(button, 800);
		this.tooltip = generateTooltip(font, tipText, width);
	}

	private List<String> generateTooltip(@Nonnull FontRenderer font, @Nonnull final String langKey, final int width) {
		final String t = Localization.format(langKey);
		return font.listFormattedStringToWidth(t, width);
	}

	public boolean handle(final int mouseX, final int mouseY) {
		if(this.checker.checkHover(mouseX, mouseY)) {
			this.renderer.drawTooltip(mouseX, mouseY, this.tooltip);
			return true;
		}
		return false;
	}
}
