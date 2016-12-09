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

import java.util.List;

import org.blockartistry.mod.DynSurround.client.EnvironStateHandler;
import org.blockartistry.mod.DynSurround.client.hud.GuiHUDHandler.IGuiOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DebugHUD extends Gui implements IGuiOverlay {

	private static final float TRANSPARENCY = 1.0F;
	private static final int TEXT_COLOR = (int) (255 * TRANSPARENCY) << 24 | 0xFFFFFF;
	private static final float GUITOP = 178;
	private static final float GUILEFT = 2;

	public void doRender(final RenderGameOverlayEvent.Pre event) {

		if (event.getType() != ElementType.DEBUG) {
			return;
		}
		
		final List<String> output = EnvironStateHandler.getDiagnostics();
		if(output.isEmpty())
			return;

		final Minecraft mc = Minecraft.getMinecraft();
		final FontRenderer font = mc.fontRendererObj;

		GlStateManager.pushMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, TRANSPARENCY);
		GlStateManager.disableLighting();
		GlStateManager.translate(GUILEFT, GUITOP, 0.0F);
		int offset = 0;
		for(final String s: output) {
			font.drawStringWithShadow(s, 0, offset, TEXT_COLOR);
			offset += font.FONT_HEIGHT;
		}
		
		GlStateManager.popMatrix();
	}
}