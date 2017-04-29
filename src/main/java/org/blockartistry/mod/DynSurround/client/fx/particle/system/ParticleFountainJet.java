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

package org.blockartistry.mod.DynSurround.client.fx.particle.system;

import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleFountain;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class ParticleFountainJet extends ParticleJet {

	protected final IBlockState block;

	public ParticleFountainJet(final int strength, final World world, final double x, final double y, final double z,
			final IBlockState block) {
		super(1, strength, world, x, y, z, 1);
		this.block = block;
	}

	@Override
	protected void spawnJetParticle() {
		final double motionX = RANDOM.nextGaussian() * 0.03D;
		final double motionZ = RANDOM.nextGaussian() * 0.03D;
		final double x = this.posX + RANDOM.nextGaussian() * 0.2D;
		final double z = this.posZ + RANDOM.nextGaussian() * 0.2D;
		final Particle particle = new ParticleFountain(this.world, x, this.posY, z, motionX, 0.5D, motionZ, this.block)
				.init();
		addParticle(particle);
	}

}
