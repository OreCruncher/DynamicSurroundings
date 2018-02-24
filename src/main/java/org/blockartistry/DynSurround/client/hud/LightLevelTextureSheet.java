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

package org.blockartistry.DynSurround.client.hud;

import org.blockartistry.lib.Color;
import org.blockartistry.lib.gfx.GeneratedTexture;
import org.blockartistry.lib.gfx.OpenGlUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class LightLevelTextureSheet extends GeneratedTexture {

	// Want a good resolution for number rendering if using a decent
	// resource pack. Need to find a programmatic way of
	// determining this value.
	private static final int TEXEL_PER_SIDE = 64;
	private static final float SCALE = TEXEL_PER_SIDE / 16F;
	private static final int SPRITE_COUNT = 16;
	private static final int WIDTH = SPRITE_COUNT * TEXEL_PER_SIDE;
	private static final int HEIGHT = TEXEL_PER_SIDE;
	private static final float U_SIZE = (float) TEXEL_PER_SIDE / (float) WIDTH;
	private static final float V_SIZE = (float) TEXEL_PER_SIDE / (float) HEIGHT;

	private static final Vec2f[] U_COORDS = new Vec2f[SPRITE_COUNT];
	private static final Vec2f V_COORDS = new Vec2f(0, V_SIZE);

	static {
		for (int i = 0; i < SPRITE_COUNT; i++) {
			final float u = (float) i / (float) SPRITE_COUNT;
			U_COORDS[i] = new Vec2f(u, u + U_SIZE);
		}
	}

	public LightLevelTextureSheet() {
		super("DSLightLevelTextures", WIDTH, HEIGHT);
	}

	// Calculate the min/max U for the specified sprite index
	public Vec2f getMinMaxU(final int idx) {
		return U_COORDS[idx];
	}

	// Calculate the min/max V for the specified sprite index
	public Vec2f getMinMaxV(final int idx) {
		return V_COORDS;
	}

	@Override
	public void render() {

		final FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		final int color = Color.WHITE.rgbWithAlpha(0.99F);
		final int shadow = Color.MC_DARKGRAY.rgbWithAlpha(0.99F);

		OpenGlUtil.setStandardBlend();
		for (int i = 0; i < 15; i++) {

			final String str = Integer.toString(i);

			final double mid = TEXEL_PER_SIDE / 2D;
			final double posX = i * TEXEL_PER_SIDE + mid;
			final double posY = mid;

			GlStateManager.pushMatrix();
			GlStateManager.translate(posX, posY, 0);

			// Render the string in the center
			final int margin = -(font.getStringWidth(str) + 1) / 2;
			final int height = -(font.FONT_HEIGHT) / 2;

			GlStateManager.scale(SCALE, SCALE, 0);
			GlStateManager.translate(0.3F, 0.3F, 0F);
			font.drawString(str, margin, height, shadow);
			GlStateManager.translate(-0.3F, -0.3F, -0.001F);
			font.drawString(str, margin, height, color);

			GlStateManager.popMatrix();
		}

	}

}
