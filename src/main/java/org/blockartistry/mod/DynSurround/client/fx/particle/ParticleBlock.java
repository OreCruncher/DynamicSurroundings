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

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.util.MathStuff;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleBlock extends Particle {

	protected final Block prototype;
	protected final IBlockState state;

	protected float pitchRate;
	protected float yawRate;
	protected float rollRate;

	protected float currentPitch;
	protected float currentYaw;
	protected float currentRoll;

	protected float brightness;
	protected float scale;

	public ParticleBlock(@Nonnull final Block block, @Nonnull final World world, final double x, final double y,
			final double z) {
		this(block, world, x, y, z, 0, 0, 0);
	}

	public ParticleBlock(@Nonnull final Block block, @Nonnull final World world, final double x, final double y,
			final double z, final double dX, final double dY, final double dZ) {
		super(world, x, y, z, dX, dY, dZ);

		this.prototype = block;
		this.state = block.getDefaultState();
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;

		this.scale = 0.25F;
		this.brightness = 1.0F;
	}

	public ParticleBlock setPitchRate(final float rate) {
		this.pitchRate = rate;
		return this;
	}

	public ParticleBlock setYawRate(final float rate) {
		this.yawRate = rate;
		return this;
	}

	public ParticleBlock setRollRate(final float rate) {
		this.rollRate = rate;
		return this;
	}

	public ParticleBlock setMaximumAge(final int age) {
		this.setMaxAge(age);
		return this;
	}

	public ParticleBlock setBrightness(final float bright) {
		this.brightness = bright;
		return this;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (this.pitchRate != 0)
			this.currentPitch = MathStuff.wrapDegrees(this.currentPitch + this.pitchRate);
		if (this.yawRate != 0)
			this.currentYaw = MathStuff.wrapDegrees(this.currentYaw + this.yawRate);
		if (this.rollRate != 0)
			this.currentRoll = MathStuff.wrapDegrees(this.currentRoll + this.rollRate);
	}

	public void renderParticle(VertexBuffer buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		final float x = ((float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX));
		final float y = ((float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY));
		final float z = ((float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ));

		final float pitch = MathStuff.wrapDegrees(this.currentPitch + this.pitchRate * partialTicks);
		final float yaw = MathStuff.wrapDegrees(this.currentYaw + this.yawRate * partialTicks);
		final float roll = MathStuff.wrapDegrees(this.currentRoll + this.rollRate * partialTicks);

		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

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
		GlStateManager.translate(-0.5F, -0.5F, 0.5F);

		final int i = this.getBrightnessForRender(partialTicks);
		final int j = i % 65536;
		final int k = i / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);

		final BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		blockrendererdispatcher.renderBlockBrightness(this.state, this.brightness);

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
