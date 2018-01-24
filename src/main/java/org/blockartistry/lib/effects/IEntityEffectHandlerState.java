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

import java.util.Optional;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.sound.BasicSound;
import org.blockartistry.DynSurround.client.sound.SoundEffect;

import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * State from the EntityEffectHandler that is being provided to an IEntityEffect
 * during processing.
 */
@SideOnly(Side.CLIENT)
public interface IEntityEffectHandlerState {

	/**
	 * Whether the EntityEffectHandler is alive or dead.
	 * 
	 * @return true if the EntityEffectHandler is active, false otherwise.
	 */
	boolean isAlive();

	/**
	 * The Entity subject the EntityEffectHandler is associated with. May be null if
	 * the Entity is no longer in scope.
	 * 
	 * @return Optional with a reference to the subject Entity, if any.
	 */
	@Nonnull
	Optional<Entity> subject();

	/**
	 * Determines the distance between the Entity subject and the specified Entity.
	 * 
	 * @param entity
	 *            The Entity to which the distance is measured.
	 * @return The distance between the two Entities in blocks, squared.
	 */
	double distanceSq(@Nonnull final Entity entity);

	/**
	 * Returns the current world tick.
	 */
	int getCurrentTick();

	/**
	 * Obtain a reference to the client's player
	 * 
	 * @return Reference to the EntityPlayer
	 */
	@Nonnull
	Optional<EntityPlayer> thePlayer();

	/**
	 * Used by an IEntityEffect to add a Particle to the system.
	 * 
	 * @param particle
	 *            The Particle instance to add to the particle system.
	 */
	void addParticle(@Nonnull final Particle particle);

	/**
	 * Used by an IEntityEffect to play a sound.
	 * 
	 * @param sound
	 *            The sound to play
	 * @return Unique ID identifying the sound in the sound system
	 */
	String playSound(@Nonnull final BasicSound<?> sound);

	/**
	 * Stops the specified sound in the sound system from playing.
	 * @param soundId
	 */
	void stopSound(@Nonnull final String soundId);
	
	/**
	 * Creates a BasicSound<> object for the specified SoundEffect centered at the
	 * Entity. If the Entity is the current active player the sound will be
	 * non-attenuated.
	 * 
	 * @param se SoundEffect to use as the basis of the sound
	 * @param player The player location of where the sound will be generated
	 * @return A BasicSound<?> with applicable properties set 
	 */
	BasicSound<?> createSound(@Nonnull final SoundEffect se, @Nonnull final EntityPlayer player);

	/**
	 * Indicates if the specified player is the one sitting behind the screen.
	 * 
	 * @param player
	 *            The EntityPlayer to check
	 * @return true if it is the local player, false otherwise
	 */
	boolean isActivePlayer(@Nonnull final Entity player);
}
