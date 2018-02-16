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

import java.util.List;
import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.registry.EntityEffectInfo;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An IEntityEffectFactory creates an EntityEffect when it gets attached to an
 * EntityEffectHandler.
 */
@SideOnly(Side.CLIENT)
@FunctionalInterface
public interface IEntityEffectFactory {

	/**
	 * Creates 0 or more IEffects for the specified Entity. It is possible that some
	 * condition on the Entity may prevent creation.
	 * 
	 * @param entity
	 *            The subject of the EntityEffect
	 * @param eei
	 *            An object containing the Entities configuration parameters
	 * @return A list of 0 or more IEffects to attach
	 */
	@Nonnull
	List<EntityEffect> create(@Nonnull final Entity entity, @Nonnull final EntityEffectInfo eei);

}
