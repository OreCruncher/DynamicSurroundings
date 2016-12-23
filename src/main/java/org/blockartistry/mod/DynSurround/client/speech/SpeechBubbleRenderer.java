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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.handlers.SpeechBubbleHandler;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// TODO: Remove once ParticleBillboard is working
@SideOnly(Side.CLIENT)
public final class SpeechBubbleRenderer {
	
	private static SpeechBubbleRenderer INSTANCE = null;

	private static final Color B_COLOR = Color.getColor(TextFormatting.BLACK);
	private static final float B_COLOR_ALPHA = 0.5F; // 0.25F;
	private static final Color F_COLOR = Color.getColor(TextFormatting.GOLD);
	private static final Color F_COLOR_DEPTH = Color.getColor(TextFormatting.GRAY);
	private static final int MIN_TEXT_WIDTH = 60;
	private static final int MAX_TEXT_WIDTH = MIN_TEXT_WIDTH * 4;
	private static final double BUBBLE_MARGIN = 4.0F;

	private SpeechBubbleRenderer() {

	}

	public static void register() {
		INSTANCE = new SpeechBubbleRenderer();
		MinecraftForge.EVENT_BUS.register(INSTANCE);
	}
	
	public static void unregister() {
		MinecraftForge.EVENT_BUS.unregister(INSTANCE);
		INSTANCE = null;
	}

	public static final class RenderingInfo {
		
		private boolean cached;
		private String message;
		private int messageWidth;
		private List<String> text;

		public RenderingInfo(@Nonnull final String message) {
			this.cached = false;
			this.message = message;
		}
		
		private void processText() {
			final FontRenderer font = Minecraft.getMinecraft().getRenderManager().getFontRenderer();
			this.text = font.listFormattedStringToWidth(this.message, MAX_TEXT_WIDTH);

			int maxWidth = MIN_TEXT_WIDTH;

			for (final String s : text) {
				int strWidth = font.getStringWidth(s);
				if (strWidth > maxWidth)
					maxWidth = strWidth;
			}

			this.messageWidth = maxWidth;
			this.message = null;
			this.cached = true;
		}
		
		public List<String> getText() {
			if(!this.cached)
				processText();
			return this.text;
		}
		
		public int getWidth() {
			if(!this.cached)
				processText();
			return this.messageWidth;
		}
	}

	@Nonnull
	public static RenderingInfo generateRenderInfo(@Nonnull final String message) {
		return new RenderingInfo(message.replaceAll("(\\xA7.)", ""));
	}

	// EntityRenderer.drawNameplate()
	private static void drawText(@Nonnull final FontRenderer font, @Nonnull final List<RenderingInfo> input, final float x, final float y,
			final float z, final float viewerYaw, final float viewerPitch, final boolean isThirdPersonFrontal,
			final boolean isSneaking) {

		int maxWidth = MIN_TEXT_WIDTH;
		final List<String> messages = new ArrayList<String>();

		for (final RenderingInfo ri : input) {
			if (ri.getWidth() > maxWidth)
				maxWidth = ri.getWidth();
			messages.addAll(ri.getText());
		}

		// Calculate scale and position
		final int numberOfMessages = messages.size();
		final float scaleBase = 0.8F; // 1.6F;
		final float scale = scaleBase * 0.016666668F;
		final double top = -(numberOfMessages) * 9 - BUBBLE_MARGIN;
		final double bottom = BUBBLE_MARGIN;
		final double left = -(maxWidth / 2.0D + BUBBLE_MARGIN);
		final double right = maxWidth / 2.0D + BUBBLE_MARGIN;

		// GL11Debug.dumpAllIsEnabled();

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
		GlStateManager.translate(x, y, z - 0.05F);

		// Draw the background region
		final float red = B_COLOR.red;
		final float green = B_COLOR.green;
		final float blue = B_COLOR.blue;
		final float alpha = B_COLOR_ALPHA;

		final Tessellator tessellator = Tessellator.getInstance();
		final VertexBuffer buffer = tessellator.getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(left, top, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.pos(left, bottom, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.pos(right, bottom, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.pos(right, top, 0.0D).color(red, green, blue, alpha).endVertex();
		tessellator.draw();

		GlStateManager.enableTexture2D();
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);

		int lines = numberOfMessages;
		for (int t = 0; t < numberOfMessages; t++) {
			final String str = messages.get(t);
			final int offset = -lines * 9;
			final int margin = -font.getStringWidth(str) / 2;
			GlStateManager.disableDepth();
			GlStateManager.depthMask(false);
			font.drawString(str, margin, offset, F_COLOR_DEPTH.rgb());
			GlStateManager.enableDepth();
			GlStateManager.depthMask(true);
			font.drawString(str, margin, offset, F_COLOR.rgb());
			lines--;
		}

		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	//@SubscribeEvent
	public void onEntityRender(@Nonnull final RenderLivingEvent.Post<EntityLivingBase> event) {

		final EntityLivingBase entity = (EntityLivingBase) event.getEntity();
		final EntityPlayer player = EnvironState.getPlayer();

		if (entity.isInvisibleToPlayer(player))
			return;

		final List<RenderingInfo> chatText = SpeechBubbleHandler.INSTANCE.getMessages(entity);

		if (chatText != null && !chatText.isEmpty()) {
			final RenderManager renderManager = event.getRenderer().getRenderManager();
			final boolean flag = entity.isSneaking();
			final float f = renderManager.playerViewY;
			final float f1 = renderManager.playerViewX;
			final boolean flag1 = renderManager.options.thirdPersonView == 2;
			final float f2 = entity.height + 0.5F - (flag ? 0.25F : 0.0F);
			drawText(renderManager.getFontRenderer(), chatText, (float) event.getX(), (float) event.getY() + f2,
					(float) event.getZ(), f, f1, flag1, flag);

		}
	}
}
