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

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.util.Color;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Panel {

	public static final Color BACKGROUND_COLOR = Color.DARKSLATEGRAY;
	public static final Color FRAME_COLOR = Color.MC_WHITE;
	public static final Color TEXT_COLOR = Color.MC_YELLOW;

	private static final int DEFAULT_WIDTH = 10;
	private static final int DEFAULT_HEIGHT = 10;

	public static enum Reference {
		/**
		 * Coordinates are relative to the center of the panel
		 */
		CENTER,
		/**
		 * Coordinates are relative to the top center of the panel
		 */
		TOP_CENTER,
		/**
		 * Coordinates are relative to the top left of the panel
		 */
		UPPER_LEFT
	};

	protected final Color foreground;
	protected final Color background;
	protected final Color border;

	protected int width;
	protected int height;
	protected int minWidth;
	protected int minHeight;
	protected float alpha = 1.0F;

	protected boolean drawFrame = true;
	protected boolean drawBackground = true;

	protected ResourceLocation backgroundTexture = null;

	public Panel() {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public Panel(final int width, final int height) {
		this(width, height, TEXT_COLOR, BACKGROUND_COLOR, FRAME_COLOR);
	}

	public Panel(final int width, final int height, final Color fore, final Color back, final Color border) {
		this.foreground = fore;
		this.background = back;
		this.border = border;

		this.minWidth = this.width = width;
		this.minHeight = this.height = height;
	}

	public Panel setAlpha(final float a) {
		this.alpha = a;
		return this;
	}

	@Nonnull
	public Panel setMinimumWidth(final int w) {
		this.minWidth = w;
		this.width = Math.max(this.width, w);
		return this;
	}

	public int getMinimumWidth() {
		return this.minWidth;
	}

	public Panel setWidth(final int w) {
		this.width = Math.max(w, this.minWidth);
		return this;
	}

	public int getWidth() {
		return this.width;
	}

	@Nonnull
	public Panel setMinimumHeight(final int h) {
		this.minHeight = h;
		this.height = Math.max(this.height, h);
		return this;
	}

	public int getMinimumHeight() {
		return this.minHeight;
	}

	public Panel setHeight(final int h) {
		this.height = Math.max(h, this.minHeight);
		return this;
	}

	public int getHeight() {
		return this.height;
	}

	public int getCenterX() {
		return (this.width + 1) / 2;
	}

	public int getCenterY() {
		return (this.height + 1) / 2;
	}

	public Panel drawFrame(final boolean flag) {
		this.drawFrame = flag;
		return this;
	}

	public Panel drawBackground(final boolean flag) {
		this.drawBackground = flag;
		return this;
	}

	public void render(final int locX, final int locY, @Nonnull final Reference ref) {

		if (!(this.drawFrame && this.drawBackground))
			return;

		final int posX;
		final int posY;

		switch (ref) {
		case CENTER:
			posX = locX - getCenterX();
			posY = locY - getCenterY();
			break;
		case TOP_CENTER:
			posX = locX - getCenterX();
			posY = locY;
			break;
		case UPPER_LEFT:
		default:
			posX = locX;
			posY = locY;
		}

		final int backgroundRGB = background.rgbWithAlpha(this.alpha);
		final int frameRGB = border.rgbWithAlpha(this.alpha);

		if (this.drawBackground) {
			if (this.backgroundTexture != null)
				GuiUtils.drawTexturedModalRect(this.backgroundTexture, posX, posY, this.width, this.height);
			else
				GuiUtils.drawRect(posX + 2, posY + 2, posX + this.width - 1, posY + this.height - 1, backgroundRGB);
		}

		if (this.drawFrame)
			GuiUtils.drawTooltipBox(posX, posY, this.width, this.height, frameRGB, frameRGB, frameRGB);

	}

}
