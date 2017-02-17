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

import java.util.ArrayDeque;
import javax.annotation.Nonnull;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class ParticleSystem extends ParticleBase {

	protected final int fxLayer;
	protected final BlockPos position;
	protected final ArrayDeque<Particle> myParticles = new ArrayDeque<Particle>();
	private int particleLimit;

	protected ParticleSystem(final World worldIn, final double posXIn, final double posYIn, final double posZIn) {
		this(0, worldIn, posXIn, posYIn, posZIn);
		
		this.particleLimit = 6;
	}

	protected ParticleSystem(final int renderPass, final World worldIn, final double posXIn, final double posYIn,
			final double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);

		this.fxLayer = renderPass;
		this.position = new BlockPos(this.posX, this.posY, this.posZ);
	}

	@Nonnull
	public BlockPos getPos() {
		return this.position;
	}
	
	public void setParticleLimit(final int limit) {
		this.particleLimit = limit;
	}
	
	public int getParticleLimit() {
		final int setting = Minecraft.getMinecraft().gameSettings.particleSetting;
		if(setting == 2)
			return 0;
		return setting == 0 ? this.particleLimit : this.particleLimit / 2;
	}
	
	public void addParticle(final Particle particle) {
		if (particle.getFXLayer() != this.getFXLayer()) {
			throw new RuntimeException("Invalid particle for fx layer!");
		} else if(this.myParticles.size() < getParticleLimit()) {
			this.myParticles.add(particle);
		}
	}

	@Override
	public void renderParticle(final VertexBuffer buffer, final Entity entityIn, final float partialTicks,
			final float rotX, final float rotZ, final float rotYZ, final float rotXY, final float rotXZ) {
		for (final Particle p : this.myParticles)
			p.renderParticle(buffer, entityIn, partialTicks, rotX, rotZ, rotYZ, rotXY, rotXZ);
	}

	/**
	 * By default a system will stay alive indefinitely until the
	 * ParticleSystemManager kills it. Override to provide termination
	 * capability.
	 */
	public boolean shouldDie() {
		return false;
	}

	/**
	 * Indicates whether to transfer the particle list over to the
	 * regular minecraft particle manager when the system dies.
	 * Useful for things like fire jets where the flames need to die
	 * out naturally.
	 */
	public boolean moveParticlesOnDeath() {
		return true;
	}
	
	protected void moveParticles() {
		if(!moveParticlesOnDeath())
			return;
		
		for(final Particle p: this.myParticles)
			if(p.isAlive())
				ParticleHelper.addParticle(p);
		
		this.myParticles.clear();
	}
	
	@Override
	public final void onUpdate() {
		// Let the system mull over what it wants to do
		this.think();
		
		if(this.shouldDie()) {
			this.moveParticles();
			this.setExpired();
		}

		if (!this.isAlive())
			return;

		// Iterate through the list doing updates
		for (final Particle p : this.myParticles)
			p.onUpdate();

		// Remove the dead ones
		Iterables.removeIf(this.myParticles, new Predicate<Particle>() {
			@Override
			public boolean apply(final Particle input) {
				return !input.isAlive();
			}
		});
	}

	// Override to provide some sort of intelligence to the system. The
	// logic can do things like add new particles, remove old ones, update
	// positions, etc. Will be invoked during the systems onUpdate()
	// call.
	public abstract void think();

	@Override
	public int getFXLayer() {
		return this.fxLayer;
	}

}
