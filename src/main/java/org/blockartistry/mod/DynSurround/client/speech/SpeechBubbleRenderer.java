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

package org.blockartistry.mod.DynSurround.client.speech;

import java.util.List;

import org.blockartistry.mod.DynSurround.client.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.server.SpeechBubbleService;
import org.blockartistry.mod.DynSurround.util.Color;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpeechBubbleRenderer {

	private static final Color B_COLOR = Color.SLATEGRAY;
	private static final float B_COLOR_ALPHA = 0.5F; // 0.25F;
	private static final Color F_COLOR = Color.GOLD;
	private static final int MIN_TEXT_WIDTH = 60;
	private static final int MAX_TEXT_WIDTH = MIN_TEXT_WIDTH * 4;
	private static final double BUBBLE_MARGIN = 4.0F;

	private static final double RENDER_RANGE = SpeechBubbleService.SPEECH_BUBBLE_RANGE
			* SpeechBubbleService.SPEECH_BUBBLE_RANGE;

	protected SpeechBubbleRenderer() {

	}

	public static void initialize() {
		MinecraftForge.EVENT_BUS.register(new SpeechBubbleRenderer());
	}

	public static List<String> scrub(final String message) {
		final FontRenderer font = Minecraft.getMinecraft().getRenderManager().getFontRenderer();
		return font.listFormattedStringToWidth(message.replaceAll("(\\xA7.)", ""), MAX_TEXT_WIDTH);
	}

	// EntityRenderer.drawNameplate()
	private static void drawText(final FontRenderer font, final List<String> input, final float x, final float y,
			final float z, final float viewerYaw, final float viewerPitch, final boolean isThirdPersonFrontal,
			final boolean isSneaking) {

		final int numberOfMessages = input.size();
		int maxWidth = MIN_TEXT_WIDTH;

		for (final String s : input) {
			int strWidth = font.getStringWidth(s);
			if (strWidth > maxWidth)
				maxWidth = strWidth;
		}

		// Calculate scale and position
		final float scaleBase = 0.8F; // 1.6F;
		final float scale = scaleBase * 0.016666668F;
		final double top = -(numberOfMessages) * 9 - BUBBLE_MARGIN;
		final double bottom = BUBBLE_MARGIN;
		final double left = -(maxWidth / 2.0D + BUBBLE_MARGIN);
		final double right = maxWidth / 2.0D + BUBBLE_MARGIN;

		final Tessellator tessellator = Tessellator.getInstance();
		final VertexBuffer buffer = tessellator.getBuffer();

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate((float) (isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(-scale, -scale, scale);
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);
		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.disableTexture2D();

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

		// Draw the background region
		final float red = B_COLOR.red;
		final float green = B_COLOR.green;
		final float blue = B_COLOR.blue;
		final float alpha = B_COLOR_ALPHA;

		buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(left, top, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.pos(left, bottom, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.pos(right, bottom, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.pos(right, top, 0.0D).color(red, green, blue, alpha).endVertex();
		tessellator.draw();
		
		GlStateManager.enableTexture2D();
		GlStateManager.translate(0, 0, -0.01F); // no z-fighting!
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		
		int lines = numberOfMessages;
		for (int t = 0; t < numberOfMessages; t++) {
			final String str = input.get(t);
			final int offset = -lines * 9;
			final int margin = -font.getStringWidth(str) / 2;
			font.drawString(str, margin, offset, F_COLOR.rgb());
			lines--;
		}

		//GlStateManager.enableDepth();
		//GlStateManager.depthMask(true);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	@SubscribeEvent
	public void onEntityRender(final RenderLivingEvent.Post<AbstractClientPlayer> event) {

		final Entity entity = event.getEntity();
		if (!(entity instanceof EntityPlayerSP))
			return;

		final EntityPlayer player = EnvironState.getPlayer();
		if (player.getDistanceSqToEntity(entity) > RENDER_RANGE)
			return;

		final List<String> chatText = SpeechBubbleHandler.getMessagesForPlayer(player);
		if (chatText != null) {
			final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
			final boolean flag = player.isSneaking();
			final float f = renderManager.playerViewY;
			final float f1 = renderManager.playerViewX;
			final boolean flag1 = renderManager.options.thirdPersonView == 2;
			final float f2 = player.height + 0.5F - (flag ? 0.25F : 0.0F);
			drawText(renderManager.getFontRenderer(), chatText, (float) event.getX(), (float) event.getY() + f2,
					(float) event.getZ(), f, f1, flag1, flag);

		}
	}
}
