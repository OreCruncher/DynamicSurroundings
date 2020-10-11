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

package org.orecruncher.dsurround.client.fx.particle.mote;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class MoteAnimatedBase extends MoteMotionBase {

	/**
	 * The base texture index. The texture index starts at this + (numAgingFrames -
	 * 1), and works its way down to this number as the particle decays.
	 */
	protected int textureIdx;
	/**
	 * How many different textures there are to progress through as the particle
	 * decays
	 */
	protected final int numAgingFrames;
	protected float baseAirFriction = 0.91F;
	/** The red value to drift toward */
	protected float fadeTargetRed;
	/** The green value to drift toward */
	protected float fadeTargetGreen;
	/** The blue value to drift toward */
	protected float fadeTargetBlue;
	/** True if setColorFade has been called */
	protected boolean fadingColor;

	protected double xAcceleration;
	protected double yAcceleration;
	protected double zAcceleration;

	protected boolean doRender;
	protected int particleTextureIndexX;
	protected int particleTextureIndexY;
	protected float texU1, texU2;
	protected float texV1, texV2;
	protected float particleScale;

	public MoteAnimatedBase(final int textureIdx, final int numFrames, @Nonnull final World world, double x, double y,
			double z, double dX, double dY, double dZ) {
		super(world, x, y, z, dX, dY, dZ);

		this.textureIdx = textureIdx;
		this.numAgingFrames = numFrames;
		this.particleScale = (RANDOM.nextFloat() * 0.5F + 0.5F) * 2.0F;

	}

	public void setColor(final int rgb) {
		this.red = ((rgb & 16711680) >> 16);
		this.green = ((rgb & 65280) >> 8);
		this.blue = ((rgb & 255) >> 0);
		this.alpha = 255;
	}

	/**
	 * sets a color for the particle to drift toward (20% closer each tick, never
	 * actually getting very close)
	 */
	public void setColorFade(final int rgb) {
		this.fadeTargetRed = ((rgb & 16711680) >> 16) / 255.0F;
		this.fadeTargetGreen = ((rgb & 65280) >> 8) / 255.0F;
		this.fadeTargetBlue = ((rgb & 255) >> 0) / 255.0F;
		this.fadingColor = true;
	}

	@Override
	public void handleCollision() {
		this.motionX *= 0.699999988079071D;
		this.motionZ *= 0.699999988079071D;
	}

	@Override
	public void update() {

		this.motionY += this.yAcceleration;
		this.motionX += this.xAcceleration;
		this.motionZ += this.zAcceleration;
		this.motionX *= this.baseAirFriction;
		this.motionY *= this.baseAirFriction;
		this.motionZ *= this.baseAirFriction;

		super.update();

		if (!isAlive()) {

			if (this.age > this.maxAge / 2) {
				this.alpha = (int) ((1.0F - ((float) this.age - (float) (this.maxAge / 2)) / this.maxAge) * 255);

				if (this.fadingColor) {
					this.red += (this.fadeTargetRed - this.red) * 0.2F;
					this.green += (this.fadeTargetGreen - this.green) * 0.2F;
					this.blue += (this.fadeTargetBlue - this.blue) * 0.2F;
				}
			}

			setParticleTextureIndex(
					this.textureIdx + (this.numAgingFrames - 1 - this.age * this.numAgingFrames / this.maxAge));
		}
	}

	@Override
	public int getBrightnessForRender(final float partialTick) {
		return 15728880;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		final double x = renderX(partialTicks);
		final double y = renderY(partialTicks);
		final double z = renderZ(partialTicks);

		drawVertex(buffer, x + (-rotationX * this.particleScale - rotationXY * this.particleScale),
				y + (-rotationZ * this.particleScale),
				z + (-rotationYZ * this.particleScale - rotationXZ * this.particleScale), this.texU2, this.texV2);
		drawVertex(buffer, x + (-rotationX * this.particleScale + rotationXY * this.particleScale),
				y + (rotationZ * this.particleScale),
				z + (-rotationYZ * this.particleScale + rotationXZ * this.particleScale), this.texU2, this.texV1);
		drawVertex(buffer, x + (rotationX * this.particleScale + rotationXY * this.particleScale),
				y + (rotationZ * this.particleScale),
				z + (rotationYZ * this.particleScale + rotationXZ * this.particleScale), this.texU1, this.texV1);
		drawVertex(buffer, x + (rotationX * this.particleScale - rotationXY * this.particleScale),
				y + (-rotationZ * this.particleScale),
				z + (rotationYZ * this.particleScale - rotationXZ * this.particleScale), this.texU1, this.texV2);
	}

	public void setParticleTextureIndex(final int particleTextureIndex) {
		this.particleTextureIndexX = particleTextureIndex % 16;
		this.particleTextureIndexY = particleTextureIndex / 16;

		this.texU1 = this.particleTextureIndexX / 16F;
		this.texU2 = this.texU1 + 0.0624375F;
		this.texV1 = this.particleTextureIndexY / 16F;
		this.texV2 = this.texV1 + 0.0624375F;
	}

}
