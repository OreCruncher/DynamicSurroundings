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

package org.orecruncher.dsurround.client.fx.particle;

import javax.annotation.Nonnull;

import org.orecruncher.lib.Color;
import org.orecruncher.lib.gfx.OpenGlState;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleTextPopOff extends ParticleBase {

	protected static final float GRAVITY = 0.8F;
	protected static final float SIZE = 3.0F;
	protected static final int LIFESPAN = 12;
	protected static final double BOUNCE_STRENGTH = 1.5F;
	protected static final int SHADOW_COLOR = Color.BLACK.rgbWithAlpha(1F);

	protected int renderColor = Color.WHITE.rgbWithAlpha(1F);
	protected boolean grow = true;

	protected String text;
	protected float drawX;
	protected float drawY;

	public ParticleTextPopOff(final World world, final String text, final Color color, final double x, final double y,
			final double z) {
		this(world, text, color, x, y, z, 0.001D, 0.05D * BOUNCE_STRENGTH, 0.001D);
	}

	public ParticleTextPopOff(final World world, final String text, final Color color, final double x, final double y,
			final double z, final double dX, final double dY, final double dZ) {
		super(world, x, y, z, dX, dY, dZ);

		this.renderColor = color.rgbWithAlpha(1F);
		this.motionX = dX;
		this.motionY = dY;
		this.motionZ = dZ;
		final float dist = MathHelper
				.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
		this.motionX = (this.motionX / dist * 0.12D);
		this.motionY = (this.motionY / dist * 0.12D);
		this.motionZ = (this.motionZ / dist * 0.12D);
		this.particleTextureJitterX = 1.5F;
		this.particleTextureJitterY = 1.5F;
		this.particleGravity = GRAVITY;
		this.particleScale = SIZE;
		this.particleMaxAge = LIFESPAN;

		setText(text);
	}

	public ParticleTextPopOff setText(@Nonnull final String text) {
		this.text = text;
		this.drawX = -MathHelper.floor(this.font.getStringWidth(this.text) / 2.0F) + 1;
		this.drawY = -MathHelper.floor(this.font.FONT_HEIGHT / 2.0F) + 1;
		return this;
	}

	public ParticleTextPopOff setColor(@Nonnull final Color color) {
		this.renderColor = color.rgbWithAlpha(1F);
		return this;
	}

	@Override
	public void renderParticle(BufferBuilder worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		final float pitch = this.manager.playerViewX * (isThirdPersonView() ? -1 : 1);
		final float yaw = -this.manager.playerViewY;

		final float locX = ((float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpX()));
		final float locY = ((float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpY()));
		final float locZ = ((float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpZ()));

		final OpenGlState glState = OpenGlState.push();
		GlStateManager.translate(locX, locY, locZ);
		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(-1.0F, -1.0F, 1.0F);
		GlStateManager.scale(this.particleScale * 0.008D, this.particleScale * 0.008D, this.particleScale * 0.008D);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 0.003662109F);
		this.font.drawString(this.text, this.drawX, this.drawY, SHADOW_COLOR, false);
		GlStateManager.translate(-0.3F, -0.3F, -0.001F);
		this.font.drawString(this.text, this.drawX, this.drawY, this.renderColor, false);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, OpenGlHelper.lastBrightnessX,
				OpenGlHelper.lastBrightnessY);
		OpenGlState.pop(glState);

		if (this.grow) {
			this.particleScale *= 1.08F;
			if (this.particleScale > SIZE * 3.0D) {
				this.grow = false;
			}
		} else {
			this.particleScale *= 0.96F;
		}
	}

	@Override
	public boolean shouldDisableDepth() {
		return true;
	}

	@Override
	public int getFXLayer() {
		return 3;
	}
}
