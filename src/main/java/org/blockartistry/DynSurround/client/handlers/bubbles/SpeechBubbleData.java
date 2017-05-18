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

package org.blockartistry.DynSurround.client.handlers.bubbles;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpeechBubbleData {
	
	private static final int MIN_TEXT_WIDTH = 60;
	private static final int MAX_TEXT_WIDTH = MIN_TEXT_WIDTH * 3;

	private final int expires;
	private String incomingText;
	private List<String> messages;

	public SpeechBubbleData(@Nonnull final String message, final int expiry) {
		this.incomingText = message.replaceAll("(\\xA7.)", "");
		this.expires = expiry;
	}

	// Need to do lazy formatting of the text. Reason is that events
	// can be fired before the client is fully constructed meaning that
	// the font renderer would be non-existent.
	@Nonnull
	public List<String> getText() {
		if (this.messages == null) {
			final FontRenderer font = Minecraft.getMinecraft().getRenderManager().getFontRenderer();
			if (font == null)
				return ImmutableList.of();
			this.messages = font.listFormattedStringToWidth(this.incomingText, MAX_TEXT_WIDTH);
			this.incomingText = null;
		}
		return this.messages;
	}

	public boolean isExpired(final int currentTick) {
		return currentTick > this.expires;
	}
}
