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

import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.random.XorShiftRandom;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleBlockDust;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleDust extends ParticleBlockDust {

	private final float f, f1, f2, f3, f4;
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	private int slX16, blX16;

	public ParticleDust(final World world, final double x, final double y, final double z, final IBlockState block) {
		super(world, x, y, z, 0, 0, 0, block);

		this.canCollide = false;

		this.rand = XorShiftRandom.current();

		multipleParticleScaleBy((float) (0.3F + this.rand.nextGaussian() / 30.0F));
		setPosition(this.posX, this.posY, this.posZ);

		this.f = this.particleTexture.getInterpolatedU(this.particleTextureJitterX / 4.0F * 16.0F);
		this.f1 = this.particleTexture.getInterpolatedU((this.particleTextureJitterX + 1.0F) / 4.0F * 16.0F);
		this.f2 = this.particleTexture.getInterpolatedV(this.particleTextureJitterY / 4.0F * 16.0F);
		this.f3 = this.particleTexture.getInterpolatedV((this.particleTextureJitterY + 1.0F) / 4.0F * 16.0F);
		this.f4 = 0.1F * this.particleScale;
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
		this.motionY -= 0.04D * this.particleGravity;
		moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		this.pos.setPos(this.posX, this.posY, this.posZ);

		if (this.particleMaxAge-- <= 0) {
			setExpired();
		} else if (WorldUtils.isSolidBlock(this.worldObj, this.pos)) {
			setExpired();
		}

		final int combinedLight = getBrightnessForRender(0);
		this.slX16 = combinedLight >> 16 & 65535;
		this.blX16 = combinedLight & 65535;
	}

	@Override
	public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		final float f5 = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
		final float f6 = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
		final float f7 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);
		worldRendererIn
				.pos(f5 - rotationX * this.f4 - rotationXY * this.f4, f6 - rotationZ * this.f4,
						f7 - rotationYZ * this.f4 - rotationXZ * this.f4)
				.tex(this.f, this.f3).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F)
				.lightmap(this.slX16, this.blX16).endVertex();
		worldRendererIn
				.pos(f5 - rotationX * this.f4 + rotationXY * this.f4, f6 + rotationZ * this.f4,
						f7 - rotationYZ * this.f4 + rotationXZ * this.f4)
				.tex(this.f, this.f2).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F)
				.lightmap(this.slX16, this.blX16).endVertex();
		worldRendererIn
				.pos(f5 + rotationX * this.f4 + rotationXY * this.f4, f6 + rotationZ * this.f4,
						f7 + rotationYZ * this.f4 + rotationXZ * this.f4)
				.tex(this.f1, this.f2).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F)
				.lightmap(this.slX16, this.blX16).endVertex();
		worldRendererIn
				.pos(f5 + rotationX * this.f4 - rotationXY * this.f4, f6 - rotationZ * this.f4,
						f7 + rotationYZ * this.f4 - rotationXZ * this.f4)
				.tex(this.f1, this.f3).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F)
				.lightmap(this.slX16, this.blX16).endVertex();
	}

	@Override
	public int getBrightnessForRender(final float partialTicks) {
		return this.worldObj.getCombinedLight(this.pos, 0);
	}

}