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
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.DynSurround.client.handlers.SoundEffectHandler;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.sound.BasicSound;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.client.sound.SoundEngine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An EntityEffectHandler is responsible for managing the effects that are
 * attached to an entity.
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
	 * Updates the state of the EntityEffectHandler as well as the state of the
	 * IEffects that are attached.
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
	 * Instructs the EntityEffectHandler that it should cleanup state because it is
	 * about to die.
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
	 * The Entity subject the EntityEffectHandler is associated with. May be null if
	 * the Entity is no longer in scope.
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
	 * @param The
	 *            sound to play
	 * @return Unique ID identifying the sound in the sound system
	 */
	@Override
	@Nullable
	public String playSound(@Nonnull final BasicSound<?> sound) {
		return SoundEffectHandler.INSTANCE.playSound(sound);
	}

	/**
	 * Stops the specified sound in the sound system from playing.
	 * 
	 * @param soundId
	 */
	@Override
	public void stopSound(@Nonnull final String soundId) {
		// TODO: This needs refactor. Should go through the SoundEffectHandler I think.
		SoundEngine.instance().stopSound(soundId, SoundCategory.PLAYERS);
	}

	/**
	 * Creates a BasicSound<> object for the specified SoundEffect centered at the
	 * Entity. If the Entity is the current active player the sound will be
	 * non-attenuated.
	 * 
	 * @param se SoundEffect to use as the basis of the sound
	 * @param player The player location of where the sound will be generated
	 * @return A BasicSound<?> with applicable properties set 
	 */
	@Override
	@Nonnull
	public BasicSound<?> createSound(@Nonnull final SoundEffect se, @Nonnull final EntityPlayer player) {
		if (this.isActivePlayer(player))
			return se.createSound(player, false);
		return se.createSound(player);
	}

	/**
	 * Determines if the specified Entity is the current active player.
	 * 
	 * @param player
	 *            The Entity to evaluate
	 * @return true if the Entity is the current player, false otherwise
	 */
	@Override
	public boolean isActivePlayer(@Nonnull final Entity player) {
		return EnvironState.isPlayer(player);
	}
}
