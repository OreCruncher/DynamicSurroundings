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

import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.client.fx.JetEffect;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.client.sound.SoundManager;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRain;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleWaterSplash extends ParticleJet {

	private static final ResourceLocation splashSound = new ResourceLocation(DSurround.RESOURCE_ID, "rain");

	private static class ParticleWaterSpray extends ParticleRain {

		protected ParticleWaterSpray(final World world, final double x, final double y, final double z, double speedX,
				final double speedY, final double speedZ) {
			super(world, x, y, z);

			this.motionX = speedX;
			this.motionY = speedY;
			this.motionZ = speedZ;

			this.canCollide = false;
		}

		public void onUpdate() {
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			this.motionY -= (double) this.particleGravity;
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.9800000190734863D;
			this.motionY *= 0.9800000190734863D;
			this.motionZ *= 0.9800000190734863D;

			if (this.particleMaxAge-- <= 0) {
				this.setExpired();
			}
		}

	}

	private int soundCount = -1;

	public ParticleWaterSplash(final int strength, final World world, final double x, final double y, final double z) {
		super(strength, world, x, y, z);
	}

	@Override
	public boolean shouldDie() {
		return !JetEffect.WaterSplash.isValidSpawnBlock(this.worldObj, this.getPos());
	}

	// Entity.resetHeight()
	@Override
	protected void spawnJetParticle() {

		if (++this.soundCount % 20 == 0) {
			final float volume = this.jetStrength / 3.0F;
			if (SoundManager.canSoundBeHeard(this.getPos(), volume)) {
				final float pitch = 1.0F - 0.7F * (volume / 3.0F) + (RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.2F;
				final SoundEffect effect = new SoundEffect(splashSound, volume, pitch);
				SoundManager.playSoundAt(this.getPos(), effect, 0, SoundCategory.BLOCKS);
			}
		}

		int splashCount = this.particleLimit - this.myParticles.size();

		for (int j = 0; (float) j < splashCount; ++j) {
			final double xOffset = (RANDOM.nextFloat() * 2.0F - 1.0F);
			final double zOffset = (RANDOM.nextFloat() * 2.0F - 1.0F);
			final double motionX = xOffset * (this.jetStrength / 40.0D);
			final double motionY = 0.1D + RANDOM.nextFloat() * this.jetStrength / 20.0D;
			final double motionZ = zOffset * (this.jetStrength / 40.D);
			final Particle particle = new ParticleWaterSpray(this.worldObj, this.posX + xOffset, (double) (this.posY),
					this.posZ + zOffset, motionX, motionY, motionZ);
			addParticle(particle);
		}
	}

}
