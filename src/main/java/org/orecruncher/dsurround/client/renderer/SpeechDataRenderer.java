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

package org.orecruncher.dsurround.client.renderer;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.capabilities.CapabilitySpeechData;
import org.orecruncher.dsurround.capabilities.speech.ISpeechData;
import org.orecruncher.dsurround.capabilities.speech.RenderContext;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.lib.Color;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.gfx.OpenGlState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(value = Side.CLIENT, modid = ModBase.MOD_ID)
public class SpeechDataRenderer {

	private static final Color B_COLOR = Color.getColor(TextFormatting.BLACK);
	private static final float B_COLOR_ALPHA = 0.5F; // 0.25F;
	private static final Color F_COLOR = Color.getColor(TextFormatting.GOLD);
	private static final float F_COLOR_ALPHA = 0.99F;
	private static final Color F_COLOR_DEPTH = Color.getColor(TextFormatting.GRAY);

	private static RenderManager getRenderManager() {
		return Minecraft.getMinecraft().getRenderManager();
	}

	private static double interpX() {
		return getRenderManager().viewerPosX;
	}

	private static double interpY() {
		return getRenderManager().viewerPosY;
	}

	private static double interpZ() {
		return getRenderManager().viewerPosZ;
	}

	private static boolean isThirdPersonView() {
		final GameSettings settings = getRenderManager().options;
		return settings == null ? false : settings.thirdPersonView == 2;
	}

	private static void doRender(@Nonnull final Entity entity, @Nonnull final ISpeechData data,
			final float partialTicks) {
		final RenderContext ctx = data.getRenderContext();
		if (ctx == null || ctx.numberOfMessages == 0)
			return;

		final FontRenderer font = Minecraft.getMinecraft().fontRenderer;

		// Calculate scale and position
		final float pitch = getRenderManager().playerViewX * (isThirdPersonView() ? -1 : 1);
		final float yaw = -getRenderManager().playerViewY;
		final float locX = ((float) (entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks - interpX()));
		final float locY = ((float) (entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks - interpY()));
		final float locZ = ((float) (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks - interpZ()));

		final OpenGlState glState = OpenGlState.push();

		GlStateManager.translate(locX, locY + entity.height + 0.25F, locZ);
		GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
		final float scale = -1F;
		GlStateManager.scale(scale * 0.015D, scale * 0.015F, scale * 0.015D);

		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();

		// // Draw the background region
		final float red = B_COLOR.red;
		final float green = B_COLOR.green;
		final float blue = B_COLOR.blue;
		final float alpha = B_COLOR_ALPHA;

		final BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(ctx.left, ctx.top, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.pos(ctx.left, ctx.bottom, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.pos(ctx.right, ctx.top, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.pos(ctx.right, ctx.bottom, 0.0D).color(red, green, blue, alpha).endVertex();
		Tessellator.getInstance().draw();

		GlStateManager.enableTexture2D();
		GlStateManager.depthMask(true);
		GlStateManager.translate(0, 0, -0.05F);

		final ObjectArray<String> text = data.getText();
		int lines = ctx.numberOfMessages;
		for (int t = 0; t < ctx.numberOfMessages; t++) {
			final String str = text.get(t);
			final int offset = -lines * 9;
			final int margin = -font.getStringWidth(str) / 2;
			GlStateManager.disableDepth();
			GlStateManager.depthMask(false);
			font.drawString(str, margin, offset, F_COLOR_DEPTH.rgbWithAlpha(F_COLOR_ALPHA));
			GlStateManager.enableDepth();
			GlStateManager.depthMask(true);
			font.drawString(str, margin, offset, F_COLOR.rgbWithAlpha(F_COLOR_ALPHA));
			lines--;
		}

		OpenGlState.pop(glState);
	}

	private static boolean canBeSeen(@Nonnull EntityPlayer player, @Nonnull Entity subject) {
		if (subject.isInvisibleToPlayer(player))
			return false;
		if (!player.canEntityBeSeen(subject))
			return false;
		return true;
	}

	@SubscribeEvent
	public static void onRenderWorldLast(@Nonnull final RenderWorldLastEvent event) {
		final EntityPlayer player = EnvironState.getPlayer();
		if (player != null) {
			// 16 block range
			final int range = 16 * 16;
			final ObjectArray<Entity> entities = WorldUtils.gatherEntitiesInView(player, range,
					event.getPartialTicks());
			for (final Entity e : entities) {
				final ISpeechData data = CapabilitySpeechData.getCapability(e);
				if (data != null && canBeSeen(player, e)) {
					doRender(e, data, event.getPartialTicks());
				}
			}
		}
	}
}
