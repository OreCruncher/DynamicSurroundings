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

import net.minecraft.block.material.Material;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleBubble;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleWaterSplash extends ParticleJet {

	protected static final class ParticleWaterBubbles extends ParticleBubble {

		protected ParticleWaterBubbles(final World worldIn, final double x, final double y, final double z,
				final double dX, final double dY, final double dZ) {
			super(worldIn, x, y, z, dX, dY, dZ);
		}

		@Override
		public void onUpdate() {
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			this.motionY += 0.002D;
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.8500000238418579D;
			this.motionY *= 0.8500000238418579D;
			this.motionZ *= 0.8500000238418579D;

			if (this.worldObj.getBlockState(new BlockPos(this.posX, this.posY, this.posZ))
					.getMaterial() != Material.WATER) {
				this.setExpired();
			}

			if (this.particleMaxAge-- <= 0) {
				this.setExpired();
			}
		}

	}

	public ParticleWaterSplash(final int strength, final World world, final double x, final double y, final double z) {
		super(strength, world, x, y, z, 6 - (strength / 2));
	}

	@Override
	protected void spawnJetParticle() {
		final double motionX = RANDOM.nextGaussian() * 0.02D;
		final double motionZ = RANDOM.nextGaussian() * 0.02D;
		final Particle particle = new ParticleWaterBubbles(this.worldObj, this.posX, this.posY, this.posZ, motionX,
				0.1F, motionZ);
		ParticleHelper.addParticle(particle);

	}

}
