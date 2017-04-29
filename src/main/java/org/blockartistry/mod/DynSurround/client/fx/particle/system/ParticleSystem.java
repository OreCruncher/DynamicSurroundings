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

import java.util.LinkedList;
import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.lib.random.XorShiftRandom;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleMoteAdapter;
import org.blockartistry.mod.DynSurround.client.fx.particle.mote.IParticleMote;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class ParticleSystem {

	protected static final Predicate<IParticleMote> REMOVE_CRITERIA = new Predicate<IParticleMote>() {
		@Override
		public boolean apply(@Nonnull final IParticleMote input) {
			return !input.isAlive();
		}
	};

	protected static final Random RANDOM = XorShiftRandom.current();
	protected static final GameSettings SETTINGS = Minecraft.getMinecraft().gameSettings;

	protected final World world;
	protected final double posX;
	protected final double posY;
	protected final double posZ;
	protected final BlockPos position;
	protected final LinkedList<IParticleMote> myParticles = Lists.newLinkedList();
	protected int particleLimit;
	protected boolean isAlive = true;

	protected ParticleSystem(final World worldIn, final double posXIn, final double posYIn, final double posZIn) {
		this.world = worldIn;
		this.posX = posXIn;
		this.posY = posYIn;
		this.posZ = posZIn;
		this.position = new BlockPos(posXIn, posYIn, posZIn);
		this.setParticleLimit(6);
	}

	@Nonnull
	public BlockPos getPos() {
		return this.position;
	}

	public void setParticleLimit(final int limit) {
		this.particleLimit = limit;
	}

	public int getCurrentParticleCount() {
		return this.myParticles.size();
	}

	public int getParticleLimit() {
		switch (SETTINGS.particleSetting) {
		case 2:
			return 0;
		case 0:
			return this.particleLimit;
		default:
			return this.particleLimit / 2;
		}
	}

	public boolean hasSpace() {
		return getCurrentParticleCount() < getParticleLimit();
	}

	/*
	 * Adds a particle to the internal tracking list as well as adds it to the
	 * Minecraft particle manager.
	 */
	public void addParticle(@Nonnull final Particle particle) {
		if (hasSpace()) {
			this.myParticles.add(new ParticleMoteAdapter(particle));
			ParticleHelper.addParticle(particle);
		}
	}

	/*
	 * This assumes that the caller has queued the mote to the appropriate
	 * collection.
	 */
	public void addParticle(@Nonnull final IParticleMote particle) {
		if (hasSpace())
			this.myParticles.add(particle);
	}

	public boolean isAlive() {
		return this.isAlive;
	}

	public void setExpired() {
		this.isAlive = false;
		this.cleanUp();
	}

	/*
	 * By default a system will stay alive indefinitely until the
	 * ParticleSystemManager kills it. Override to provide termination
	 * capability.
	 */
	public boolean shouldDie() {
		return false;
	}

	/*
	 * Perform any cleanup activities prior to dying.
	 */
	protected void cleanUp() {
		this.myParticles.clear();
	}

	/*
	 * Update the state of the particle system. Any particles are queued into
	 * the Minecraft particle system or to a ParticleCollection so they do not
	 * have to be ticked.
	 */
	public void onUpdate() {
		// Let the system mull over what it wants to do
		this.think();

		if (this.shouldDie()) {
			this.isAlive = false;
			this.cleanUp();
		}

		if (this.isAlive()) {
			// Remove the dead ones
			Iterables.removeIf(this.myParticles, REMOVE_CRITERIA);

			// Update any sounds
			this.soundUpdate();
		}
	}

	/*
	 * Override to provide sound for the particle effect. Will be invoked
	 * whenever the particle system is updated by the particle manager.
	 */
	protected void soundUpdate() {

	}

	/*
	 * Override to provide some sort of intelligence to the system. The logic
	 * can do things like add new particles, remove old ones, update positions,
	 * etc. Will be invoked during the systems onUpdate() call.
	 */
	public abstract void think();

}
