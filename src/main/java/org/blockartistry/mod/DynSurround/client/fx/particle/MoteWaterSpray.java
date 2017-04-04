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

import java.util.Random;

import org.blockartistry.mod.DynSurround.util.WorldUtils;
import org.blockartistry.mod.DynSurround.util.random.XorShiftRandom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MoteWaterSpray implements IParticleMote {

	protected final RenderManager manager = Minecraft.getMinecraft().getRenderManager();
	protected final World world;

	protected boolean isAlive = true;
	protected int age;

	protected double posX;
	protected double posY;
	protected double posZ;
	protected final BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();

	protected double motionX;
	protected double motionY;
	protected double motionZ;
	protected double gravity;

	protected double prevX;
	protected double prevY;
	protected double prevZ;

	protected int slX16;
	protected int blX16;

	protected float red = 1F;
	protected float green = 1F;
	protected float blue = 1F;
	protected float alpha = 1F;
	protected float scale;

	protected final float texU1, texU2;
	protected final float texV1, texV2;
	protected final float f4;

	public MoteWaterSpray(final World world, final double x, final double y, final double z, final double dX,
			final double dY, final double dZ) {

		final Random random = XorShiftRandom.current();

		this.world = world;
		this.age = (int) (8.0D / (random.nextDouble() * 0.8D + 0.2D));
		this.prevX = this.posX = x;
		this.prevY = this.posY = y;
		this.prevZ = this.posZ = z;
		this.position.setPos(x, y, z);
		this.motionX = dX;
		this.motionY = dY;
		this.motionZ = dZ;
		this.gravity = 0.06D;

		this.scale = (random.nextFloat() * 0.5F + 0.5F) * 2.0F;

		final int textureIdx = 19 + random.nextInt(4);
		final int texX = textureIdx % 16;
		final int texY = textureIdx / 16;
		this.texU1 = texX / 16F;
		this.texU2 = this.texU1 + 0.0624375F;
		this.texV1 = texY / 16F;
		this.texV2 = this.texV1 + 0.0624375F;

		this.f4 = 0.1F * this.scale;

	}

	@Override
	public boolean isAlive() {
		return this.isAlive;
	}

	@Override
	public void onUpdate() {

		if (this.age-- <= 0) {
			this.isAlive = false;
		} else {
			this.prevX = this.posX;
			this.prevY = this.posY;
			this.prevZ = this.posZ;
			this.motionY -= this.gravity;

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;

			this.position.setPos(this.posX, this.posY, this.posZ);

			if (WorldUtils.isSolidBlock(this.world, this.position)) {
				this.isAlive = false;
			} else {

				this.motionX *= 0.9800000190734863D;
				this.motionY *= 0.9800000190734863D;
				this.motionZ *= 0.9800000190734863D;

				final int combinedLight = this.getBrightnessForRender(0);
				this.slX16 = combinedLight >> 16 & 65535;
				this.blX16 = combinedLight & 65535;
			}
		}
	}

	@Override
	public boolean moveParticleOnExpire() {
		return false;
	}

	@Override
	public Particle getParticle() {
		throw new RuntimeException("Cannot move a mote!");
	}

	@Override
	public int getFXLayer() {
		return 0;
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
		buffer.pos(x, y, z).tex(u, v).color(this.red, this.green, this.blue, this.alpha)
				.lightmap(this.slX16, this.blX16).endVertex();
	}

	@Override
	public void renderParticle(VertexBuffer buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		final double x = (this.prevX + (this.posX - this.prevX) * (double) partialTicks - interpX());
		final double y = (this.prevY + (this.posY - this.prevY) * (double) partialTicks - interpY());
		final double z = (this.prevZ + (this.posZ - this.prevZ) * (double) partialTicks - interpZ());

		drawVertex(buffer, x + (-rotationX * f4 - rotationXY * f4), y + (-rotationZ * f4),
				z + (-rotationYZ * f4 - rotationXZ * f4), this.texU2, this.texV2);
		drawVertex(buffer, x + (-rotationX * f4 + rotationXY * f4), y + (rotationZ * f4),
				z + (-rotationYZ * f4 + rotationXZ * f4), this.texU2, this.texV1);
		drawVertex(buffer, x + (rotationX * f4 + rotationXY * f4), y + (rotationZ * f4),
				z + (rotationYZ * f4 + rotationXZ * f4), this.texU1, this.texV1);
		drawVertex(buffer, x + (rotationX * f4 - rotationXY * f4), y + (-rotationZ * f4),
				z + (rotationYZ * f4 - rotationXZ * f4), this.texU1, this.texV2);
	}

	public int getBrightnessForRender(final float partialTicks) {
		return this.world.getCombinedLight(this.position, 0);
	}

}
