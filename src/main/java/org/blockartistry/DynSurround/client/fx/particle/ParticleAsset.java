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

import javax.annotation.Nonnull;

import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.math.MathStuff;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class ParticleAsset extends ParticleBase {

	protected float pitchRate;
	protected float yawRate;
	protected float rollRate;

	protected float currentPitch;
	protected float currentYaw;
	protected float currentRoll;

	protected float scale;

	protected boolean dieOnGround;

	public ParticleAsset(@Nonnull final World world, final double x, final double y, final double z) {
		this(world, x, y, z, 0, 0, 0);
	}

	public ParticleAsset(@Nonnull final World world, final double x, final double y, final double z, final double dX,
			final double dY, final double dZ) {
		super(world, x, y, z, dX, dY, dZ);

		this.scale = 0.25F;
		this.setSize(0.25F, 0.25F);

		this.particleGravity = 0.12F;
		this.dieOnGround = true;

		this.currentPitch = 360.0F * this.rand.nextFloat();
		this.currentYaw = 360.0F * this.rand.nextFloat();
		this.currentRoll = 360.0F * this.rand.nextFloat();
	}

	public void setDieOnGround(final boolean flag) {
		this.dieOnGround = flag;
	}

	public void setScale(final float scale) {
		this.scale = scale;
		this.setSize(this.scale, this.scale);
	}

	public void setPitchRate(final float rate) {
		this.pitchRate = rate;
	}

	public void setYawRate(final float rate) {
		this.yawRate = rate;
	}

	public void setRollRate(final float rate) {
		this.rollRate = rate;
	}

	public void setGravity(final float grav) {
		this.particleGravity = grav;
	}

	public void setMotion(final float dX, final float dY, final float dZ) {
		this.motionX = dX;
		this.motionY = dY;
		this.motionZ = dZ;
	}

	protected boolean isOnGround() {
		if (this.motionX == 0 && this.motionY == 0 && this.motionZ == 0)
			return true;

		return !WorldUtils.isAirBlock(this.world, new BlockPos(this.posX, this.posY - 1D, this.posZ));
	}

	@Override
	public void onUpdate() {
		if (this.isExpired)
			return;

		super.onUpdate();

		if (this.dieOnGround && isOnGround()) {
			this.setExpired();
		}

		if (this.pitchRate != 0)
			this.currentPitch = MathStuff.wrapDegrees(this.currentPitch + this.pitchRate);
		if (this.yawRate != 0)
			this.currentYaw = MathStuff.wrapDegrees(this.currentYaw + this.yawRate);
		if (this.rollRate != 0)
			this.currentRoll = MathStuff.wrapDegrees(this.currentRoll + this.rollRate);
	}

	/**
	 * Override to provide the necessary rendering for the asset in question.
	 * Render position and rotations have already been set.
	 */
	protected abstract void handleRender(final float partialTicks);

	/**
	 * Override to provide a translation different than the default. This
	 * translation occurs after rotation. Purpose is to "center" the rendering
	 * on the rotation point.
	 */
	protected void doModelTranslate() {
		GlStateManager.translate(-0.5F, -0.5F, 0.5F);
	}

	/**
	 * Override to provide a different texture
	 */
	protected ResourceLocation getTexture() {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

	@Override
	public final void renderParticle(VertexBuffer buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		final float x = ((float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpX()));
		final float y = ((float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpY()));
		final float z = ((float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpZ()));

		final float pitch = MathStuff.wrapDegrees(this.currentPitch + this.pitchRate * partialTicks);
		final float yaw = MathStuff.wrapDegrees(this.currentYaw + this.yawRate * partialTicks);
		final float roll = MathStuff.wrapDegrees(this.currentRoll + this.rollRate * partialTicks);

		bindTexture(getTexture());

		GlStateManager.enableRescaleNormal();
		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();

		GlStateManager.translate(x, y, z);
		GlStateManager.scale(this.scale, this.scale, this.scale);

		GlStateManager.rotate(roll, 0, 0, 1.0F);
		GlStateManager.rotate(pitch, 1.0F, 0, 0);
		GlStateManager.rotate(yaw, 0, 1.0F, 0);

		final int i = this.getBrightnessForRender(partialTicks);
		final int j = i % 65536;
		final int k = i / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);

		doModelTranslate();
		handleRender(partialTicks);

		GlStateManager.disableBlend();
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
		GlStateManager.disableRescaleNormal();
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

}
