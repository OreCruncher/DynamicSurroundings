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

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.registry.EntityEffectInfo;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An IEntityEffectFactoryFilter is used by the EntityEffectLibrary to determine
 * if a particular EntityEffect would be applied to an Entity.
 *
 */
@SideOnly(Side.CLIENT)
@FunctionalInterface
public interface IEntityEffectFactoryFilter {

	/**
	 * Evaluates the Entity to determine if an EntityEffect will apply.
	 *
	 * @param entity
	 *            The subject of the evaluation
	 * @param eei
	 *            An object containing the Entities effect parameters
	 * @return true if the EntityEffect applies, false otherwise
	 */
	boolean applies(@Nonnull final Entity entity, @Nonnull final EntityEffectInfo eei);

}
