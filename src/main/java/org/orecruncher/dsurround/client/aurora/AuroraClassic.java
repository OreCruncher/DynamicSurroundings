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

package org.orecruncher.dsurround.client.aurora;

import org.lwjgl.opengl.GL11;
import org.orecruncher.lib.Color;
import org.orecruncher.lib.gfx.OpenGlState;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class AuroraClassic extends AuroraBase {

	public AuroraClassic(final long seed) {
		super(seed);
	}

	@Override
	public void update() {
		super.update();
		this.band.update();
	}

	@Override
	public void render(final float partialTick) {

		final float alpha = getAlpha();
		final Tessellator tess = Tessellator.getInstance();
		final BufferBuilder renderer = tess.getBuffer();

		final double tranY = getTranslationY(partialTick);
		final double tranX = getTranslationX(partialTick);
		final double tranZ = getTranslationZ(partialTick);

		final Color base = getBaseColor();
		final Color fade = getFadeColor();
		final double zero = 0.0D;

		final OpenGlState glState = OpenGlState.push();

		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableTexture2D();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableAlpha();
		GlStateManager.disableCull();
		GlStateManager.depthMask(false);

		this.band.translate(partialTick);
		final Panel[] array = this.band.getNodeList();

		for (int b = 0; b < this.bandCount; b++) {

			GlStateManager.pushMatrix();
			GlStateManager.translate(tranX, tranY, tranZ + this.offset * b);
			GlStateManager.scale(0.5D, 8.0D, 0.5D);

			renderer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
			for (int i = 0; i < array.length - 1; i++) {

				final Panel node = array[i];

				final double posY = node.getModdedY();
				final double posX = node.tetX;
				final double posZ = node.tetZ;
				final double tetX = node.tetX2;
				final double tetZ = node.tetZ2;

				final double posX2;
				final double posZ2;
				final double tetX2;
				final double tetZ2;
				final double posY2;

				if (i < array.length - 2) {
					final Panel nodePlus = array[i + 1];
					posX2 = nodePlus.tetX;
					posZ2 = nodePlus.tetZ;
					tetX2 = nodePlus.tetX2;
					tetZ2 = nodePlus.tetZ2;
					posY2 = nodePlus.getModdedY();
				} else {
					posX2 = tetX2 = node.posX;
					posZ2 = tetZ2 = node.getModdedZ();
					posY2 = 0.0D;
				}

				// Front
				renderer.pos(posX, zero, posZ).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(posX, posY, posZ).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(posX2, posY2, posZ2).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(posX2, posY2, posZ2).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(posX2, zero, posZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(posX, zero, posZ).color(base.red, base.green, base.blue, alpha).endVertex();

				// Bottom
				renderer.pos(posX, zero, posZ).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(posX2, zero, posZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX2, zero, tetZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX2, zero, tetZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX, zero, tetZ).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(posX, zero, posZ).color(base.red, base.green, base.blue, alpha).endVertex();

				// Back
				renderer.pos(tetX, zero, tetZ).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX, posY, tetZ).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(tetX2, posY2, tetZ2).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(tetX2, posY2, tetZ2).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(tetX2, zero, tetZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX, zero, tetZ).color(base.red, base.green, base.blue, alpha).endVertex();
			}
			tess.draw();
			GlStateManager.popMatrix();
		}

		OpenGlState.pop(glState);
	}

	@Override
	public String toString() {
		return "<CLASSIC> " + super.toString();
	}
}
