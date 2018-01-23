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

package org.blockartistry.DynSurround.client.fx.particle;

import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.gfx.OpenGlState;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Supplier;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Billboard is a text rendering that hovers and follows an entity as
// long as there is text to display.
@SideOnly(Side.CLIENT)
public class ParticleBillboard extends ParticleBase {

	private static final Color B_COLOR = Color.getColor(TextFormatting.BLACK);
	private static final float B_COLOR_ALPHA = 0.5F; // 0.25F;
	private static final Color F_COLOR = Color.getColor(TextFormatting.GOLD);
	private static final float F_COLOR_ALPHA = 0.99F;
	private static final Color F_COLOR_DEPTH = Color.getColor(TextFormatting.GRAY);
	private static final int MIN_TEXT_WIDTH = 60;
	private static final double BUBBLE_MARGIN = 4.0F;

	private final float scale;
	private final Entity subject;
	private final Supplier<List<String>> accessor;
	private List<String> text;

	private int textWidth;
	private int numberOfMessages;
	private double top;
	private double bottom;
	private double left;
	private double right;

	private boolean canBeSeen;

	public ParticleBillboard(@Nonnull final Entity entity, @Nonnull final Supplier<List<String>> accessor) {
		super(entity.getEntityWorld(), entity.posX, entity.posY, entity.posZ);

		this.subject = entity;
		this.accessor = accessor;
		this.canCollide = false;

		updatePosition();
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;

		this.scale = 1F;

		this.canBeSeen = canBeSeen();
	}

	private boolean canBeSeen() {
		if (this.subject.isInvisibleToPlayer(EnvironState.getPlayer()))
			return false;
		if (!EnvironState.getPlayer().canEntityBeSeen(this.subject))
			return false;
		final double range = ModOptions.speechBubbleRange * ModOptions.speechBubbleRange;
		return EnvironState.getPlayer().getDistanceSqToEntity(this.subject) <= range;
	}

	public boolean shouldExpire() {
		if (!this.isAlive() || !this.subject.isEntityAlive())
			return true;

		this.text = this.accessor.get();
		return this.text == null || this.text.isEmpty();
	}

	private void updatePosition() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		final AxisAlignedBB box = this.subject.getEntityBoundingBox();
		final double newY = box.maxY + (this.subject.isSneaking() ? 0.25D : 0.5D);
		this.setPosition(this.subject.posX, newY, this.subject.posZ);
	}

	@Override
	public void onUpdate() {
		if (shouldExpire()) {
			this.setExpired();
		} else {

			updatePosition();
			this.canBeSeen = canBeSeen();

			if (this.canBeSeen) {
				this.textWidth = MIN_TEXT_WIDTH;

				for (final String s : this.text)
					this.textWidth = Math.max(this.textWidth, this.font.getStringWidth(s));

				this.numberOfMessages = this.text.size();
				this.top = -(numberOfMessages) * 9 - BUBBLE_MARGIN;
				this.bottom = BUBBLE_MARGIN;
				this.left = -(this.textWidth / 2.0D + BUBBLE_MARGIN);
				this.right = this.textWidth / 2.0D + BUBBLE_MARGIN;
			}

		}
	}

	@Override
	public void renderParticle(final VertexBuffer buffer, final Entity entityIn, final float partialTicks,
			final float rotationX, final float rotationZ, final float rotationYZ, final float rotationXY,
			final float rotationXZ) {

		// Fail safe...
		if (this.text == null || this.text.isEmpty())
			return;

		if (!this.canBeSeen)
			return;

		// Calculate scale and position
		final float pitch = this.manager.playerViewX * (isThirdPersonView() ? -1 : 1);
		final float yaw = -this.manager.playerViewY;

		final float locX = ((float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpX()));
		final float locY = ((float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpY()));
		final float locZ = ((float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpZ()));

		final OpenGlState glState = OpenGlState.push();

		GlStateManager.translate(locX, locY, locZ);
		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(-this.scale * 0.015D, -this.scale * 0.015F, this.scale * 0.015D);

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 0.003662109F);
		final float saveLightX = OpenGlHelper.lastBrightnessX;
		final float saveLightY = OpenGlHelper.lastBrightnessY;

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

		buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(this.left, this.top, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.pos(this.left, this.bottom, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.pos(this.right, this.top, 0.0D).color(red, green, blue, alpha).endVertex();
		buffer.pos(this.right, this.bottom, 0.0D).color(red, green, blue, alpha).endVertex();
		Tessellator.getInstance().draw();

		GlStateManager.enableTexture2D();
		GlStateManager.depthMask(true);
		GlStateManager.translate(0, 0, -0.05F);

		int lines = this.numberOfMessages;
		for (int t = 0; t < this.numberOfMessages; t++) {
			final String str = this.text.get(t);
			final int offset = -lines * 9;
			final int margin = -this.font.getStringWidth(str) / 2;
			GlStateManager.disableDepth();
			GlStateManager.depthMask(false);
			this.font.drawString(str, margin, offset, F_COLOR_DEPTH.rgbWithAlpha(F_COLOR_ALPHA));
			GlStateManager.enableDepth();
			GlStateManager.depthMask(true);
			this.font.drawString(str, margin, offset, F_COLOR.rgbWithAlpha(F_COLOR_ALPHA));
			lines--;
		}

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, saveLightX, saveLightY);
		OpenGlState.pop(glState);
	}

	@Override
	public int getBrightnessForRender(final float partialTick) {
		return this.subject.getBrightnessForRender(partialTick);
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

}
