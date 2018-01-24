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
package org.blockartistry.lib.effects;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An EntityEffectHandler is responsible for managing the effects that are attached to
 * an entity.
 */
@SideOnly(Side.CLIENT)
public class EntityEffectHandler implements IEntityEffectHandlerState {

	protected final WeakReference<Entity> subject;
	protected final List<IEntityEffect> activeEffects;
	protected boolean isAlive = true;

	public EntityEffectHandler(final Entity entity, final List<IEntityEffect> effects) {
		this.subject = new WeakReference<Entity>(entity);
		this.activeEffects = effects;
	}

	/**
	 * Updates the state of the EntityEffectHandler as well as the state of the IEffects
	 * that are attached.
	 */
	public void update() {
		if (!this.isAlive())
			return;

		final Entity entity = this.subject.get();
		this.isAlive = entity != null && entity.isEntityAlive();

		for (final IEntityEffect e : activeEffects)
			e.update(this);
	}

	/**
	 * Instructs the EntityEffectHandler that it should cleanup state because it is about
	 * to die.
	 */
	public void die() {
		this.isAlive = false;
		for (final IEntityEffect e : this.activeEffects)
			e.die(this);
	}

	// ================================================
	// IEntityEffectHandlerState interface
	// ================================================

	/**
	 * Whether the EntityEffectHandler is alive or dead.
	 * 
	 * @return true if the EntityEffectHandler is active, false otherwise.
	 */
	@Override
	public boolean isAlive() {
		return this.isAlive;
	}

	/**
	 * The Entity subject the EntityEffectHandler is associated with. May be null if the
	 * Entity is no longer in scope.
	 * 
	 * @return Optional with a reference to the subject Entity, if any.
	 */
	@Override
	public Optional<Entity> subject() {
		return Optional.ofNullable(this.subject.get());
	}

	/**
	 * Determines the distance between the Entity subject and the specified Entity.
	 * 
	 * @param entity
	 *            The Entity to which the distance is measured.
	 * @return The distance between the two Entities in blocks, squared.
	 */
	@Override
	public double distanceSq(final Entity player) {
		final Entity e = this.subject.get();
		if (e == null)
			return Double.MAX_VALUE;
		return e.getDistanceSqToEntity(player);
	}

	/**
	 * Returns the current world tick.
	 * 
	 * TODO: Refactor to avoid EnvironState dependency
	 */
	@Override
	public int getCurrentTick() {
		return EnvironState.getTickCounter();
	}

	/**
	 * Obtain a reference to the client's player
	 * 
	 * @return Reference to the EntityPlayer. Will not be null.
	 */
	@Nonnull
	public Optional<EntityPlayer> thePlayer() {
		return Optional.of(Minecraft.getMinecraft().thePlayer);
	}

	/**
	 * Used by an IEntityEffect to add a Particle to the system.
	 * 
	 * @param particle
	 *            The Particle instance to add to the particle system.
	 */
	@Override
	public void addParticle(final Particle particle) {
		ParticleHelper.addParticle(particle);
	}

	/**
	 * Used by an IEntityEffect to play a sound.
	 * 
	 * TODO: Need to sort this. Not sure what it looks like yet.
	 */
	@Override
	public void playSound() {

	}
}
