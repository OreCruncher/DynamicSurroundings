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

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.world.World;

/*
 * Base for particle entities that are long lived and generate
 * other particles as a jet.  This entity does not render - just
 * serves as a particle factory.
 */
@SideOnly(Side.CLIENT)
public abstract class ParticleJet extends ParticleSystem {

	protected final int jetStrength;
	protected final int updateFrequency;

	public ParticleJet(final int strength, final World world, final double x, final double y, final double z) {
		this(0, strength, world, x, y, z, 3);
	}

	public ParticleJet(final int layer, final int strength, final World world, final double x, final double y,
			final double z, final int freq) {
		super(layer, world, x, y, z);

		this.setAlphaF(0.0F);
		this.jetStrength = strength;
		this.updateFrequency = freq;
		this.particleMaxAge = (RANDOM.nextInt(strength) + 2) * 20;
		this.setParticleLimit(strength * strength * 5);
	}

	/*
	 * Override in derived class to provide particle for the jet.
	 */
	protected abstract void spawnJetParticle();

	public boolean shouldDie() {
		return this.particleAge >= this.particleMaxAge;
	}

	/*
	 * During update see if a particle needs to be spawned so that it can rise
	 * up.
	 */
	@Override
	public void think() {

		// Check to see if a particle needs to be generated
		if (this.particleAge % this.updateFrequency == 0) {
			spawnJetParticle();
		}

		// Grow older
		this.particleAge++;
	}
}
