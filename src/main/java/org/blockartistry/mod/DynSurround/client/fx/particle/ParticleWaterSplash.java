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
import org.blockartistry.mod.DynSurround.util.SoundUtils;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleSplash;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleWaterSplash extends ParticleJet {

	private static final SoundEvent splashSound = SoundUtils
			.getOrRegisterSound(new ResourceLocation(DSurround.RESOURCE_ID, "rain"));

	private static class ParticleWaterSpray extends ParticleSplash {

		protected ParticleWaterSpray(final World world, final double x, final double y, final double z, double speedX,
				final double speedY, final double speedZ) {
			super(world, x, y, z, speedX, speedY, speedZ);

			this.motionX = speedX;
			this.motionY = speedY;
			this.motionZ = speedZ;
		}

	}

	private int soundCount = -1;

	public ParticleWaterSplash(final int strength, final World world, final double x, final double y, final double z) {
		super(strength, world, x, y, z);
	}

	/**
	 * Water splash systems stay around indefinitely doing their thing.
	 */
	@Override
	public boolean shouldDie() {
		return false;
	}
	
	// Entity.resetHeight()
	@Override
	protected void spawnJetParticle() {

		final float factor = this.jetStrength / 3.0F;

		if (++this.soundCount % 15 == 0) {
			final float volume = factor > 1.0F ? 1.0F : factor;
			final float pitch = 1.0F - 0.9F * factor + (RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.2F;

			this.worldObj.playSound(this.posX, this.posY, this.posZ, splashSound, SoundCategory.BLOCKS, volume, pitch,
					false);
		}

		// final int bubbleCount = (int) (factor * 2.0F);
		int splashCount = (int) (this.jetStrength * this.jetStrength / 1.5F);
		if (splashCount < 1)
			splashCount = 1;

		// for (int i = 0; (float) i < bubbleCount; ++i) {
		// final double xOffset = (RANDOM.nextFloat() * 2.0F - 1.0F);
		// final double zOffset = (RANDOM.nextFloat() * 2.0F - 1.0F);
		// final double motionX = 0;
		// final double motionY = 0 - (double) (RANDOM.nextFloat() * 0.2F);
		// final double motionZ = 0;
		// ParticleHelper.spawnParticle(EnumParticleTypes.WATER_BUBBLE,
		// this.posX + xOffset, (double) (this.posY),
		// this.posZ + (double) zOffset, motionX, motionY, motionZ);
		// }

		for (int j = 0; (float) j < splashCount; ++j) {
			final double xOffset = (RANDOM.nextFloat() * 2.0F - 1.0F);
			final double zOffset = (RANDOM.nextFloat() * 2.0F - 1.0F);
			final double motionX = xOffset * this.jetStrength / 40.0D;
			final double motionY = (RANDOM.nextFloat()) * this.jetStrength / 30.0D;
			final double motionZ = zOffset * this.jetStrength / 40.D;
			final Particle particle = new ParticleWaterSpray(this.worldObj, this.posX + xOffset, (double) (this.posY),
					this.posZ + zOffset, motionX, motionY, motionZ);
			addParticle(particle);
		}
	}

}
