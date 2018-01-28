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

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IEntityEffectState extends IEffectState {

	/**
	 * The Entity subject the effect is associated with. May be null if the Entity
	 * is no longer in scope.
	 * 
	 * @return Optional with a reference to the subject Entity, if any.
	 */
	@Nonnull
	Optional<Entity> subject();
	
	/**
	 * Indicates if the subject is alive.
	 * @return true if the subject is alive, false otherwise
	 */
	boolean isSubjectAlive();
	
	/**
	 * Determines the distance between the Entity subject and the specified Entity.
	 * 
	 * @param entity
	 *            The Entity to which the distance is measured.
	 * @return The distance between the two Entities in blocks, squared.
	 */
	double distanceSq(@Nonnull final Entity entity);
	
	/**
	 * Returns the total world time, in ticks, the entity belongs to.
	 * 
	 * @return Total world time
	 */
	long getWorldTime();

}
