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

package org.blockartistry.mod.DynSurround.client.weather;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;

import org.blockartistry.lib.Color;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.registry.DimensionRegistry;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public final class AuroraRenderer {

	private final DimensionRegistry dimensions = RegistryManager.get(RegistryType.DIMENSION);

	public void renderAurora(final float partialTick, @Nonnull final Aurora aurora) {

		final float alpha = aurora.getAlphaf();
		if (alpha <= 0.0F)
			return;

		final Minecraft mc = Minecraft.getMinecraft();
		final Tessellator tess = Tessellator.getInstance();
		final VertexBuffer renderer = tess.getBuffer();
		final float tranY;
		if (ModOptions.auroraHeightPlayerRelative) {
			// Fix height above player
			tranY = ModOptions.playerFixedHeight;
		} else {
			// Adjust to keep aurora at the same altitude
			tranY = this.dimensions.getCloudHeight(mc.world) + 5 - (float) (mc.player.lastTickPosY
					+ (mc.player.posY - mc.player.lastTickPosY) * partialTick);
		}

		final double tranX = aurora.posX
				- (mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTick);

		final double tranZ = aurora.posZ
				- (mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTick);

		if (ModOptions.auroraAnimate)
			aurora.translate(partialTick);

		final Color base = aurora.getBaseColor();
		final Color fade = aurora.getFadeColor();
		final double zero = 0.0D;

		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();

		GlStateManager.translate((float) tranX, tranY, (float) tranZ);
		GlStateManager.scale(0.5D, 8.0D, 0.5D);
		GlStateManager.disableTexture2D();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableAlpha();
		GlStateManager.disableCull();
		GlStateManager.depthMask(false);

		for (final Node[] array : aurora.getNodeList()) {
			for (int i = 0; i < array.length - 1; i++) {

				final Node node = array[i];

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
					final Node nodePlus = array[i + 1];
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
				renderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
				renderer.pos(posX, zero, posZ).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(posX, posY, posZ).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(posX2, posY2, posZ2).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(posX2, zero, posZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				tess.draw();

				// Bottom
				renderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
				renderer.pos(posX, zero, posZ).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(posX2, zero, posZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX2, zero, tetZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX, zero, tetZ).color(base.red, base.green, base.blue, alpha).endVertex();
				tess.draw();

				// Back
				renderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
				renderer.pos(tetX, zero, tetZ).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX, posY, tetZ).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(tetX2, posY2, tetZ2).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(tetX2, zero, tetZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				tess.draw();
			}
		}

		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.depthMask(true);
		GlStateManager.enableCull();
		GlStateManager.enableTexture2D();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.disableBlend();
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}
}
