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
package org.orecruncher.dsurround.client.effects;

import javax.annotation.Nonnull;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.client.sound.ISoundInstance;

import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Interface common to all states within the effect framework.
 */
@SideOnly(Side.CLIENT)
public interface IEffectState {

	/**
	 * Used to add a Particle to the system.
	 *
	 * @param particle The Particle instance to add to the particle system.
	 */
	void addParticle(@Nonnull final Particle particle);

	/**
	 * Used to play a sound.
	 *
	 * @param sound The sound to play
	 * @return true if the sound is queued for play; false otherwise
	 */
	boolean playSound(@Nonnull final ISoundInstance sound);

	/**
	 * Stops the specified sound in the sound system from playing.
	 *
	 * @param sound The sound to stop playing
	 */
	void stopSound(@Nonnull final ISoundInstance sound);

	/**
	 * Creates a SoundInstance<> object for the specified SoundEffect centered at the
	 * Entity. If the Entity is the current active player the sound will be
	 * non-attenuated.
	 *
	 * @param se     SoundEffect to use as the basis of the sound
	 * @param player The player location of where the sound will be generated
	 * @return A SoundInstance<?> with applicable properties set
	 */
	@Nonnull
	ISoundInstance createSound(@Nonnull final SoundEffect se, @Nonnull final Entity player);

	/**
	 * Indicates if the specified player is the one sitting behind the screen.
	 *
	 * @param player The EntityPlayer to check
	 * @return true if it is the local player, false otherwise
	 */
	boolean isActivePlayer(@Nonnull final Entity player);

	/**
	 * Obtain a reference to the client's player
	 *
	 * @return Reference to the EntityPlayer. Will not be null.
	 */
	@Nonnull
	EntityPlayer thePlayer();

}
