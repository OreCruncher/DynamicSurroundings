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

import org.blockartistry.mod.DynSurround.util.WorldUtils;

import net.minecraft.client.particle.ParticleRain;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleWaterSpray extends ParticleRain {

	private final float f, f1, f2, f3, f4;
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	private int slX16, blX16;

	protected ParticleWaterSpray(final World world, final double x, final double y, final double z, double speedX,
			final double speedY, final double speedZ) {
		super(world, x, y, z);

		this.motionX = speedX;
		this.motionY = speedY;
		this.motionZ = speedZ;

		this.canCollide = false;

		f = (float) this.particleTextureIndexX / 16.0F;
		f1 = f + 0.0624375F;
		f2 = (float) this.particleTextureIndexY / 16.0F;
		f3 = f2 + 0.0624375F;
		f4 = 0.1F * this.particleScale;
	}

	@Override
	public void moveEntity(final double dX, final double dY, final double dZ) {
		this.posX += dX;
		this.posY += dY;
		this.posZ += dZ;
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY -= (double) this.particleGravity;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		this.pos.setPos(this.posX, this.posY, this.posZ);

		if (this.particleMaxAge-- <= 0) {
			this.setExpired();
		} else if (WorldUtils.isSolidBlock(this.worldObj, this.pos)) {
			this.setExpired();
		}

		final int combinedLight = this.getBrightnessForRender(0);
		this.slX16 = combinedLight >> 16 & 65535;
		this.blX16 = combinedLight & 65535;

	}

	@Override
	public void renderParticle(VertexBuffer buffer, Entity player, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		
		final float x = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
		final float y = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
		final float z = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);

		buffer
				.pos((double) x + (double) (-rotationX * f4 - rotationXY * f4),
						(double) y + (double) (-rotationZ * f4),
						(double) z + (double) (-rotationYZ * f4 - rotationXZ * f4))
				.tex((double) f1, (double) f3)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(slX16, blX16)
				.endVertex();
		buffer
				.pos((double) x + (double) (-rotationX * f4 + rotationXY * f4),
						(double) y + (double) (rotationZ * f4),
						(double) z + (double) (-rotationYZ * f4 + rotationXZ * f4))
				.tex((double) f1, (double) f2)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(slX16, blX16)
				.endVertex();
		buffer
				.pos((double) x + (double) (rotationX * f4 + rotationXY * f4),
						(double) y + (double) (rotationZ * f4),
						(double) z + (double) (rotationYZ * f4 + rotationXZ * f4))
				.tex((double) f, (double) f2)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(slX16, blX16)
				.endVertex();
		buffer
				.pos((double) x + (double) (rotationX * f4 - rotationXY * f4),
						(double) y + (double) (-rotationZ * f4),
						(double) z + (double) (rotationYZ * f4 - rotationXZ * f4))
				.tex((double) f, (double) f3)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(slX16, blX16)
				.endVertex();
	}

	@Override
	public int getBrightnessForRender(final float partialTicks) {
		return this.worldObj.getCombinedLight(this.pos, 0);
	}

}
