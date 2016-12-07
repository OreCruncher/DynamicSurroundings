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

import net.minecraft.client.particle.ParticleSimpleAnimated;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.blockartistry.mod.DynSurround.util.Color;
import org.blockartistry.mod.DynSurround.util.XorShiftRandom;

// Started with ParticleFireWork.Spark as a basis.
@SideOnly(Side.CLIENT)
public class ParticleFireFly extends ParticleSimpleAnimated {

	private static final XorShiftRandom RANDOM = new XorShiftRandom();
	private static final int startColorRGB = Color.YELLOW.rgb();
	private static final int fadeColorRGB = Color.LGREEN.rgb();

	private static final float XZ_MOTION_DELTA = 0.2F;
	private static final float Y_MOTION_DELTA = XZ_MOTION_DELTA / 2.0F;
	private static final float ACCELERATION = 0.004F;

	private double xAcceleration;
	private double yAcceleration;
	private double zAcceleration;

	public ParticleFireFly(final World world, double xCoord, double yCoord, double zCoord) {
		super(world, xCoord, yCoord, zCoord, 160, 8, 0.0F);

		this.motionX = RANDOM.nextGaussian() * XZ_MOTION_DELTA;
		this.motionZ = RANDOM.nextGaussian() * XZ_MOTION_DELTA;
		this.motionY = RANDOM.nextGaussian() * Y_MOTION_DELTA;

		this.xAcceleration = RANDOM.nextGaussian() * ACCELERATION;
		this.yAcceleration = RANDOM.nextGaussian() * ACCELERATION;
		this.zAcceleration = RANDOM.nextGaussian() * ACCELERATION;

		this.particleScale *= 0.75F * 0.25F;
		this.particleMaxAge = 48 + this.rand.nextInt(12);

		this.setColor(startColorRGB);
		this.setColorFade(fadeColorRGB);
	}

	public boolean isTransparent() {
		return true;
	}

	/**
	 * Renders the particle
	 */
	public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if (this.particleAge < this.particleMaxAge / 3
				|| (this.particleAge + this.particleMaxAge) / 3 % 2 == 0) {
			super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY,
					rotationXZ);
		}
	}

	public void onUpdate() {

		this.motionX += this.xAcceleration;
		this.motionY += this.yAcceleration;
		this.motionZ += this.zAcceleration;

		super.onUpdate();
	}
}
