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

import org.blockartistry.lib.Color;
import org.blockartistry.lib.random.XorShiftRandom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSimpleAnimated;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Started with ParticleFireWork.Spark as a basis.
@SideOnly(Side.CLIENT)
public class ParticleFireFly extends ParticleSimpleAnimated {

	private static final int startColorRGB = Color.YELLOW.rgb();
	private static final int fadeColorRGB = Color.LGREEN.rgb();

	private static final float XZ_MOTION_DELTA = 0.2F;
	private static final float Y_MOTION_DELTA = XZ_MOTION_DELTA / 2.0F;
	private static final float ACCELERATION = 0.004F;

	protected final RenderManager manager = Minecraft.getMinecraft().getRenderManager();

	private double xAcceleration;
	private double yAcceleration;
	private double zAcceleration;

	private boolean doRender;

	protected int slX16;
	protected int blX16;

	protected float texU1, texU2;
	protected float texV1, texV2;
	protected float f4;

	public ParticleFireFly(final World world, double xCoord, double yCoord, double zCoord) {
		super(world, xCoord, yCoord, zCoord, 160, 8, 0.0F);

		this.rand = XorShiftRandom.current();

		this.motionX = this.rand.nextGaussian() * XZ_MOTION_DELTA;
		this.motionZ = this.rand.nextGaussian() * XZ_MOTION_DELTA;
		this.motionY = this.rand.nextGaussian() * Y_MOTION_DELTA;

		this.xAcceleration = this.rand.nextGaussian() * ACCELERATION;
		this.yAcceleration = this.rand.nextGaussian() / 2.0D * ACCELERATION;
		this.zAcceleration = this.rand.nextGaussian() * ACCELERATION;

		this.particleScale *= 0.75F * 0.25F;
		this.particleMaxAge = 48 + this.rand.nextInt(12);

		this.setColor(startColorRGB);
		this.setColorFade(fadeColorRGB);
		
		this.f4 = 0.1F * this.particleScale;
	}

	protected double interpX() {
		return this.manager.viewerPosX;
	}

	protected double interpY() {
		return this.manager.viewerPosY;
	}

	protected double interpZ() {
		return this.manager.viewerPosZ;
	}

	protected void drawVertex(final VertexBuffer buffer, final double x, final double y, final double z, final double u,
			final double v) {
		buffer.pos(x, y, z).tex(u, v).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
				.lightmap(this.slX16, this.blX16).endVertex();
	}

	/**
	 * Renders the particle
	 */
	public void renderParticle(VertexBuffer buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if (this.doRender) {
			final double x = (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpX());
			final double y = (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpY());
			final double z = (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpZ());

			drawVertex(buffer, x + (-rotationX * f4 - rotationXY * f4), y + (-rotationZ * f4),
					z + (-rotationYZ * f4 - rotationXZ * f4), this.texU2, this.texV2);
			drawVertex(buffer, x + (-rotationX * f4 + rotationXY * f4), y + (rotationZ * f4),
					z + (-rotationYZ * f4 + rotationXZ * f4), this.texU2, this.texV1);
			drawVertex(buffer, x + (rotationX * f4 + rotationXY * f4), y + (rotationZ * f4),
					z + (rotationYZ * f4 + rotationXZ * f4), this.texU1, this.texV1);
			drawVertex(buffer, x + (rotationX * f4 - rotationXY * f4), y + (-rotationZ * f4),
					z + (rotationYZ * f4 - rotationXZ * f4), this.texU1, this.texV2);
		}
	}

	public void onUpdate() {

		this.motionX += this.xAcceleration;
		this.motionY += this.yAcceleration;
		this.motionZ += this.zAcceleration;

		super.onUpdate();

		this.doRender = this.particleAge < this.particleMaxAge / 3
				|| (this.particleAge + this.particleMaxAge) / 3 % 2 == 0;

		if (this.doRender) {
			final int combinedLight = this.getBrightnessForRender(0);
			this.slX16 = combinedLight >> 16 & 65535;
			this.blX16 = combinedLight & 65535;
		}
	}

	@Override
	public void setParticleTextureIndex(final int particleTextureIndex) {
		this.particleTextureIndexX = particleTextureIndex % 16;
		this.particleTextureIndexY = particleTextureIndex / 16;

		this.texU1 = this.particleTextureIndexX / 16F;
		this.texU2 = this.texU1 + 0.0624375F;
		this.texV1 = this.particleTextureIndexY / 16F;
		this.texV2 = this.texV1 + 0.0624375F;
	}
}
