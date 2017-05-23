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

package org.blockartistry.lib.font;

import javax.annotation.Nonnull;

import org.blockartistry.lib.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FastFontRenderer {

	public static final FastFontRenderer INSTANCE;

	static {
		// OptiFine demolishes the font renderer so
		// let it try and speed up string rendering
		if (FMLClientHandler.instance().hasOptifine()) {
			INSTANCE = new FastFontRenderer() {
				public void prepare() {

				}

				public void drawString(@Nonnull final String text, final float x, final float y,
						@Nonnull final Color color, final float alpha) {
					font.drawString(text, x, y, color.rgbWithAlpha(alpha), false);
				}
			};
		} else {
			INSTANCE = new FastFontRenderer();
		}
	}

	private static final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

	private FastFontRenderer() {

	}

	public void prepare() {
		GlStateManager.enableAlpha();
		Minecraft.getMinecraft().getTextureManager().bindTexture(font.locationFontTexture);
	}

	public void drawString(@Nonnull final String text, final float x, final float y, @Nonnull final Color color,
			final float alpha) {

		GlStateManager.color(color.red, color.green, color.blue, alpha);

		float xPos = x;

		for (int i = 0; i < text.length(); i++)
			xPos += renderChar(xPos, y, text.charAt(i));
	}

	private static float renderChar(final float x, final float y, final char ch) {
		int i = ch % 16 * 8;
		int j = ch / 16 * 8;
		int l = font.charWidth[ch];
		float f = (float) l - 0.01F;
		GlStateManager.glBegin(5);
		GlStateManager.glTexCoord2f((float) i / 128.0F, (float) j / 128.0F);
		GlStateManager.glVertex3f(x, y, 0.0F);
		GlStateManager.glTexCoord2f((float) i / 128.0F, ((float) j + 7.99F) / 128.0F);
		GlStateManager.glVertex3f(x, y + 7.99F, 0.0F);
		GlStateManager.glTexCoord2f(((float) i + f - 1.0F) / 128.0F, (float) j / 128.0F);
		GlStateManager.glVertex3f(x + f - 1.0F, y, 0.0F);
		GlStateManager.glTexCoord2f(((float) i + f - 1.0F) / 128.0F, ((float) j + 7.99F) / 128.0F);
		GlStateManager.glVertex3f(x + f - 1.0F, y + 7.99F, 0.0F);
		GlStateManager.glEnd();
		return (float) l;
	}
}
