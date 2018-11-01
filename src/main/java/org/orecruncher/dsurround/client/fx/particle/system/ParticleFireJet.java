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

package org.orecruncher.dsurround.client.fx.particle.system;

import org.orecruncher.dsurround.client.handlers.SoundEffectHandler;
import org.orecruncher.dsurround.client.sound.Sounds;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFlame;
import net.minecraft.client.particle.ParticleLava;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleFireJet extends ParticleJet {

	protected final boolean isLava;
	protected final IParticleFactory factory;
	protected final int particleId;
	protected boolean soundFired;

	public ParticleFireJet(final int strength, final World world, final double x, final double y, final double z) {
		super(strength, world, x, y, z);
		this.isLava = RANDOM.nextInt(3) == 0;

		this.particleId = this.isLava ? EnumParticleTypes.LAVA.getParticleID()
				: EnumParticleTypes.FLAME.getParticleID();
		if (this.isLava)
			this.factory = new ParticleLava.Factory();
		else
			this.factory = new ParticleFlame.Factory();
	}

	@Override
	protected void soundUpdate() {
		if (!this.soundFired) {
			this.soundFired = true;
			SoundEffectHandler.INSTANCE.playSoundAt(getPos(), Sounds.FIRE, 0);
		}
	}

	@Override
	protected void spawnJetParticle() {
		final double speedY = this.isLava ? 0 : this.jetStrength / 10.0D;
		final Particle particle = this.factory.createParticle(this.particleId, this.world, this.posX, this.posY,
				this.posZ, 0D, speedY, 0D);
		if (!this.isLava) {
			final ParticleFlame flame = (ParticleFlame) particle;
			flame.flameScale *= this.jetStrength;
		}
		addParticle(particle);
	}
}
