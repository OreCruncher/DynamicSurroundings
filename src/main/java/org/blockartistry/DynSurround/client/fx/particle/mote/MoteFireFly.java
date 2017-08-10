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

package org.blockartistry.DynSurround.client.fx.particle.mote;

import org.blockartistry.lib.Color;

import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.Optional;

@SideOnly(Side.CLIENT)
@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo")
public class MoteFireFly extends MoteAnimatedBase implements ILightProvider {

	private static final int startColorRGB = Color.YELLOW.rgb();
	private static final int fadeColorRGB = Color.LGREEN.rgb();

	private static final float XZ_MOTION_DELTA = 0.2F;
	private static final float Y_MOTION_DELTA = XZ_MOTION_DELTA / 2.0F;
	private static final float ACCELERATION = 0.004F;

	public MoteFireFly(World world, double x, double y, double z) {
		super(160, 8, world, x, y, z, 0, 0, 0);

		this.motionX = RANDOM.nextGaussian() * XZ_MOTION_DELTA;
		this.motionZ = RANDOM.nextGaussian() * XZ_MOTION_DELTA;
		this.motionY = RANDOM.nextGaussian() * Y_MOTION_DELTA;

		this.xAcceleration = RANDOM.nextGaussian() * ACCELERATION;
		this.yAcceleration = RANDOM.nextGaussian() / 2.0D * ACCELERATION;
		this.zAcceleration = RANDOM.nextGaussian() * ACCELERATION;

		this.gravity = 0D;

		this.particleScale *= 0.75F * 0.25F * 0.1F;
		this.maxAge = 48 + RANDOM.nextInt(12);

		this.setColor(startColorRGB);
		this.setColorFade(fadeColorRGB);
	}

	@Override
	public void update() {
		super.update();

		this.doRender = this.age < this.maxAge / 3 || (this.age + this.maxAge) / 3 % 2 == 0;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotX, float rotZ,
			float rotYZ, float rotXY, float rotXZ) {

		if (this.doRender)
			super.renderParticle(buffer, entityIn, partialTicks, rotX, rotZ, rotYZ, rotXY, rotXZ);

	}

	@Optional.Method(modid = "albedo")
	protected double lightedX(final float partialTicks) {
		return (float) (this.prevX + (this.posX - this.prevX) * (double) partialTicks);
	}

	@Optional.Method(modid = "albedo")
	protected double lightedY(final float partialTicks) {
		return (float) (this.prevY + (this.posY - this.prevY) * (double) partialTicks);
	}

	@Optional.Method(modid = "albedo")
	protected double lightedZ(final float partialTicks) {
		return (float) (this.prevZ + (this.posZ - this.prevZ) * (double) partialTicks);
	}

	@Optional.Method(modid = "albedo")
	@Override
	public Light provideLight() {
		final float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
		final double x = lightedX(partialTicks);
		final double y = lightedY(partialTicks);
		final double z = lightedZ(partialTicks);
		final float alpha = 0.5F * this.alpha;
		return Light.builder().pos(x, y, z).color(this.red, this.green, this.blue, alpha).radius(1.0F).build();
	}

}
