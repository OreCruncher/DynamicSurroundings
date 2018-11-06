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

package org.orecruncher.dsurround.entity.speech;

import javax.annotation.Nonnull;

import org.orecruncher.lib.collections.ObjectArray;

import net.minecraft.client.Minecraft;

public final class RenderContext {

	private static final int MIN_TEXT_WIDTH = 60;
	private static final double BUBBLE_MARGIN = 4.0F;

	public final int textWidth;
	public final int numberOfMessages;
	public final double top;
	public final double bottom;
	public final double left;
	public final double right;

	RenderContext(@Nonnull final ObjectArray<String> messages) {
		int theWidth = MIN_TEXT_WIDTH;

		for (final String s : messages)
			theWidth = Math.max(theWidth, Minecraft.getMinecraft().fontRenderer.getStringWidth(s));

		this.textWidth = theWidth;
		this.numberOfMessages = messages.size();
		this.top = -(this.numberOfMessages) * 9 - BUBBLE_MARGIN;
		this.bottom = BUBBLE_MARGIN;
		this.left = -(this.textWidth / 2.0D + BUBBLE_MARGIN);
		this.right = this.textWidth / 2.0D + BUBBLE_MARGIN;
	}
}
