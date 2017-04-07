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

package org.blockartistry.mod.DynSurround.util.gui;

import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.util.Color;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TextPanel {
	
	public static final Color BACKGROUND_COLOR = Color.DARKSLATEGRAY;
	public static final Color FRAME_COLOR = Color.MC_WHITE;
	public static final Color TEXT_COLOR = Color.MC_YELLOW;

	public static enum Reference {
		CENTERED,
		UPPER_LEFT
	};

	private final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

	private final Color foreground;
	private final Color background;
	private final Color border;

	private float alpha = 1.0F;

	private List<String> text = ImmutableList.of();
	private int width;
	private int height;
	
	private int minWidth;

	private int centerX = 0;
	private int centerY = 0;
	
	public TextPanel() {
		this(TEXT_COLOR, BACKGROUND_COLOR, FRAME_COLOR);
	}
	
	public TextPanel(final Color fore, final Color back, final Color border) {
		this.foreground = fore;
		this.background = back;
		this.border = border;
	}

	public TextPanel setAlpha(final float a) {
		this.alpha = a;
		return this;
	}

	@Nonnull
	public TextPanel setMinimumWidth(final int w) {
		this.minWidth = w;
		return this;
	}
	
	@Nonnull
	public TextPanel setText(@Nonnull final List<String> text) {
		this.text = text;
		this.height = (text.size() + 1) * font.FONT_HEIGHT;

		this.width = this.minWidth;
		for (final String s : text)
			this.width = Math.max(font.getStringWidth(s), this.width);
		this.width += font.FONT_HEIGHT;

		this.centerX = (this.width + 1) / 2;
		this.centerY = (this.height + 1) / 2;

		return this;
	}

	public void render(final int locX, final int locY, @Nonnull final Reference ref) {
		
		if(this.text.isEmpty())
			return;

		final int posX;
		final int posY;
		
		if(ref == Reference.CENTERED) {
			posX = locX - this.centerX;
			posY = locY - this.centerY;
		} else {
			posX = locX;
			posY = locY;
		}

		final int backgroundRGB = background.rgbWithAlpha(this.alpha);
		final int textRGB = foreground.rgbWithAlpha(this.alpha);
		final int frameRGB = border.rgbWithAlpha(this.alpha);

		GuiUtils.drawRect(posX + 2, posY + 2, posX + this.width - 1, posY + this.height - 1, backgroundRGB);
		GuiUtils.drawTooltipBox(posX, posY, this.width, this.height, frameRGB, frameRGB, frameRGB);

		GlStateManager.color(1F, 1F, 1F, this.alpha);
		GlStateManager.enableBlend();
		
		final int drawX;
		
		if(ref == Reference.CENTERED)
			drawX = locX;
		else
			drawX = locX + this.centerX;
		
		int drawY = posY + (this.font.FONT_HEIGHT + 1) / 2;
		
		for(final String s: this.text) {
			GuiUtils.drawCenteredString(this.font, s, drawX, drawY, textRGB);
			drawY += font.FONT_HEIGHT;
		}

		GlStateManager.color(1F, 1F, 1F, 1F);

	}

}
