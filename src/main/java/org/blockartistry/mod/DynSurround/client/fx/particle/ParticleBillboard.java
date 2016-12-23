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

package org.blockartistry.mod.DynSurround.client.fx.particle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.blockartistry.mod.DynSurround.client.handlers.SpeechBubbleHandler;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.speech.SpeechBubbleRenderer.RenderingInfo;
import org.blockartistry.mod.DynSurround.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;

// Billboard is a text rendering that hovers and follows an entity as
// long as there is text to display.
public class ParticleBillboard extends Particle {

	private static final Color B_COLOR = Color.getColor(TextFormatting.BLACK);
	private static final float B_COLOR_ALPHA = 0.5F; // 0.25F;
	private static final Color F_COLOR = Color.getColor(TextFormatting.GOLD);
	private static final Color F_COLOR_DEPTH = Color.getColor(TextFormatting.GRAY);
	private static final int MIN_TEXT_WIDTH = 60;
	private static final double BUBBLE_MARGIN = 4.0F;

	private final FontRenderer font;
	private final float scale;
	private final WeakReference<Entity> subject;
	private List<RenderingInfo> renderInfo;
	private List<String> text;
	private int textWidth;
	
	private boolean thirdPerson;
	private float pitch;
	private float yaw;

	public ParticleBillboard(final Entity entity) {
		super(entity.getEntityWorld(), entity.posX, entity.posY, entity.posZ);

		final double newY = entity.posY + entity.height + 0.5D - (entity.isSneaking() ? 0.25D : 0);
		this.setPosition(entity.posX, newY, entity.posZ);
		this.prevPosX = entity.posX;
		this.prevPosY = newY;
		this.prevPosZ = entity.posZ;

		this.canCollide = false;
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.subject = new WeakReference<Entity>(entity);

		this.font = Minecraft.getMinecraft().fontRendererObj;
		this.scale = 1F; // 0.8F * 0.016666668F;
	}

	protected boolean shouldExpire() {
		if (this.subject.isEnqueued())
			return true;

		final Entity entity = this.subject.get();

		if (entity.isInvisibleToPlayer(EnvironState.getPlayer()))
			return true;

		this.renderInfo = SpeechBubbleHandler.INSTANCE.getMessages(entity);
		return this.renderInfo == null;
	}

	@Override
	public void onUpdate() {
		if (shouldExpire()) {
			this.setExpired();
			return;
		}

		final Entity entity = this.subject.get();

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		final double newY = entity.posY + entity.height + 0.5D - (entity.isSneaking() ? 0.25D : 0);
		this.setPosition(entity.posX, newY, entity.posZ);

		this.textWidth = MIN_TEXT_WIDTH;
		this.text = new ArrayList<String>();

		for (final RenderingInfo ri : this.renderInfo) {
			if (ri.getWidth() > this.textWidth)
				this.textWidth = ri.getWidth();
			this.text.addAll(ri.getText());
		}

		final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		this.thirdPerson = renderManager.options.thirdPersonView == 2;
		this.pitch = renderManager.playerViewX * (this.thirdPerson ? -1 : 1);
		this.yaw = -renderManager.playerViewY;
	}

	@Override
	public void renderParticle(final VertexBuffer buffer, final Entity entityIn, final float partialTicks,
			final float rotationX, final float rotationZ, final float rotationYZ, final float rotationXY,
			final float rotationXZ) {

		if (this.text == null || this.text.isEmpty())
			return;

		// Calculate scale and position
		final int numberOfMessages = this.text.size();
		final double top = -(numberOfMessages) * 9 - BUBBLE_MARGIN;
		final double bottom = BUBBLE_MARGIN;
		final double left = -(this.textWidth / 2.0D + BUBBLE_MARGIN);
		final double right = this.textWidth / 2.0D + BUBBLE_MARGIN;

		final float locX = ((float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX));
		final float locY = ((float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY));
		final float locZ = ((float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ));

		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();

		GlStateManager.translate(locX, locY, locZ);
		GlStateManager.rotate(this.yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(this.pitch, 1.0F, 0.0F, 0.0F);

		GlStateManager.scale(-1.0F, -1.0F, 1.0F);
		GlStateManager.scale(this.particleScale * 0.008D, this.particleScale * 0.008D, this.particleScale * 0.008D);
		GlStateManager.scale(this.scale, this.scale, this.scale);

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 0.003662109F);

		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);
		GlStateManager.disableDepth();

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		// // Draw the background region
		final float red = B_COLOR.red;
		final float green = B_COLOR.green;
		final float blue = B_COLOR.blue;
		final float alpha = B_COLOR_ALPHA;

		buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(left, top, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.pos(left, bottom, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.pos(right, bottom, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.pos(right, top, 0.0D).color(red, green, blue, alpha).endVertex();
		Tessellator.getInstance().draw();

		GlStateManager.enableTexture2D();
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		GlStateManager.translate(0, 0, -0.05F);

		int lines = numberOfMessages;
		for (int t = 0; t < numberOfMessages; t++) {
			final String str = this.text.get(t);
			final int offset = -lines * 9;
			final int margin = -font.getStringWidth(str) / 2;
			GlStateManager.disableDepth();
			GlStateManager.depthMask(false);
			this.font.drawString(str, margin, offset, F_COLOR_DEPTH.rgb());
			GlStateManager.enableDepth();
			GlStateManager.depthMask(true);
			this.font.drawString(str, margin, offset, F_COLOR.rgb());
			lines--;
		}

		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

}
