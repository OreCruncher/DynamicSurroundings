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

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiOverlay extends Gui {

	/*
	 * Override to provide pre render logic.
	 */
	public void doRender(final RenderGameOverlayEvent.Pre event) {
	}

	/*
	 * Override to provide post render logic
	 */
	public void doRender(final RenderGameOverlayEvent.Post event) {
	}

	/*
	 * Override if the overlay needs to be ticked during the client tick phase.
	 */
	public void doTick(final int tickRef) {

	}

	@Override
	protected void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
		final float zLevel = 0.0F;

		final float f = (startColor >> 24 & 255) / 255.0F;
		final float f1 = (startColor >> 16 & 255) / 255.0F;
		final float f2 = (startColor >> 8 & 255) / 255.0F;
		final float f3 = (startColor & 255) / 255.0F;
		final float f4 = (endColor >> 24 & 255) / 255.0F;
		final float f5 = (endColor >> 16 & 255) / 255.0F;
		final float f6 = (endColor >> 8 & 255) / 255.0F;
		final float f7 = (endColor & 255) / 255.0F;
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(7425);
		final Tessellator tessellator = Tessellator.getInstance();
		final VertexBuffer t = tessellator.getBuffer();
		t.begin(7, DefaultVertexFormats.POSITION_COLOR);
		t.pos(left + right, top, zLevel).color(f1, f2, f3, f).endVertex();
		t.pos(left, top, zLevel).color(f1, f2, f3, f).endVertex();
		t.pos(left, top + bottom, zLevel).color(f5, f6, f7, f4).endVertex();
		t.pos(left + right, top + bottom, zLevel).color(f5, f6, f7, f4).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	protected void drawTooltipBox(int x, int y, int w, int h, int bg, int grad1, int grad2) {
		drawGradientRect(x + 1, y, w - 1, 1, bg, bg);
		drawGradientRect(x + 1, y + h, w - 1, 1, bg, bg);
		drawGradientRect(x, y + 1, 1, h - 1, bg, bg);
		drawGradientRect(x + w, y + 1, 1, h - 1, bg, bg);
		drawGradientRect(x + 1, y + 2, 1, h - 3, grad1, grad2);
		drawGradientRect(x + w - 1, y + 2, 1, h - 3, grad1, grad2);
		drawGradientRect(x + 1, y + 1, w - 1, 1, grad1, grad1);
		drawGradientRect(x + 1, y + h - 1, w - 1, 1, grad2, grad2);
	}
}