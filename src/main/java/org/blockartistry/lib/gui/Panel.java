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

import javax.annotation.Nonnull;

import org.blockartistry.lib.Color;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class Panel<T extends Panel<?>> {

	public static final Color BACKGROUND_COLOR = new Color(33, 33, 33);
	public static final Color BORDER_COLOR = Color.MC_DARKGRAY;
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

	protected Color foreground;
	protected Color background;
	protected Color border;

	protected int width;
	protected int height;
	protected int minWidth;
	protected int minHeight;
	protected float alpha = 1.0F;

	protected boolean drawFrame = true;
	protected boolean drawBackground = true;

	protected ResourceLocation backgroundTexture = null;
	protected int textureWidth;
	protected int textureHeight;
	protected Vec2f U = new Vec2f(0, 0);
	protected Vec2f V = new Vec2f(1, 1);

	public Panel() {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public Panel(final int width, final int height) {
		this(width, height, TEXT_COLOR, BACKGROUND_COLOR, BORDER_COLOR);
	}

	public Panel(final int width, final int height, final Color fore, final Color back, final Color border) {
		this.foreground = fore != null ? fore : TEXT_COLOR;
		this.background = back != null ? back : BACKGROUND_COLOR;
		this.border = border != null ? border : BORDER_COLOR;

		this.minWidth = this.width = width;
		this.minHeight = this.height = height;
	}

	@SuppressWarnings("unchecked")
	public T setBackgroundTexture(@Nonnull final ResourceLocation texture) {
		this.backgroundTexture = texture;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setTextureDimensions(final int width, final int height) {
		this.textureHeight = height;
		this.textureWidth = width;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setTextureU(final int start, final int end) {
		this.U = GuiUtils.calculateSpan(this.textureWidth, start, end);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setTextureV(final int start, final int end) {
		this.V = GuiUtils.calculateSpan(this.textureHeight, start, end);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setTextureCoords(@Nonnull final Vec2f u, @Nonnull final Vec2f v) {
		this.U = u;
		this.V = v;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setForegroundColor(@Nonnull final Color c) {
		this.foreground = c;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setBackgroundColor(@Nonnull final Color c) {
		this.background = c;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setBorderColor(@Nonnull final Color c) {
		this.border = c;
		return (T) this;
	}

	public ResourceLocation getBackgroundTexture() {
		return this.backgroundTexture;
	}

	@Nonnull
	public Color getForegroundColor() {
		return this.foreground;
	}

	@Nonnull
	public Color getBackgroundColor() {
		return this.background;
	}

	@Nonnull
	Color getFrameColor() {
		return this.border;
	}

	@SuppressWarnings("unchecked")
	public T setAlpha(final float a) {
		this.alpha = a;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public T setMinimumWidth(final int w) {
		this.minWidth = w;
		this.width = Math.max(this.width, w);
		return (T) this;
	}

	public int getMinimumWidth() {
		return this.minWidth;
	}

	@SuppressWarnings("unchecked")
	public T setWidth(final int w) {
		this.width = Math.max(w, this.minWidth);
		return (T) this;
	}

	public int getWidth() {
		return this.width;
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public T setMinimumHeight(final int h) {
		this.minHeight = h;
		this.height = Math.max(this.height, h);
		return (T) this;
	}

	public int getMinimumHeight() {
		return this.minHeight;
	}

	@SuppressWarnings("unchecked")
	public T setHeight(final int h) {
		this.height = Math.max(h, this.minHeight);
		return (T) this;
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

	@SuppressWarnings("unchecked")
	public T drawFrame(final boolean flag) {
		this.drawFrame = flag;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T drawBackground(final boolean flag) {
		this.drawBackground = flag;
		return (T) this;
	}

	public void render(final int locX, final int locY, @Nonnull final Reference ref) {

		if (!(this.drawFrame || this.drawBackground))
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

		if (this.drawBackground) {
			if (this.backgroundTexture != null) {
				GuiUtils.drawTexturedModalRect(this.backgroundTexture, posX, posY, this.width, this.height, this.U,
						this.V);
			} else {
				final int backgroundRGB = this.background.rgbWithAlpha(this.alpha);
				GuiUtils.drawRect(posX + 2, posY + 2, posX + this.width - 1, posY + this.height - 1, backgroundRGB);
			}
		}

		if (this.drawFrame) {
			final int frameRGB = this.border.rgbWithAlpha(this.alpha);
			GuiUtils.drawTooltipBox(posX, posY, this.width, this.height, frameRGB, frameRGB, frameRGB);
		}

	}

}
