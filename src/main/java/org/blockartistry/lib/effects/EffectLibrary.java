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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.blockartistry.lib.collections.ObjectArray;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Central repository for a collection of IEffectFactory instances and the
 * IFactoryFilters associated with them. Typically there will be a single
 * instance of the EffectLibrary for a project, but multiples can be created
 * based on the circumstances.
 */
@SideOnly(Side.CLIENT)
public class EffectLibrary {

	protected final ObjectArray<IFactoryFilter> filters = new ObjectArray<IFactoryFilter>();
	protected final ObjectArray<IEffectFactory> factories = new ObjectArray<IEffectFactory>();

	public EffectLibrary() {

	}

	/**
	 * Registers an IFactoryFilter/IEffectFactory pair. The filter is used by the
	 * EffectLibrary to determine if an IEffect applies to a target entity.
	 * 
	 * @param filter
	 *            IFactoryFilter used to determine if the IEffectFactory should be
	 *            used to create an IEffect.
	 * @param factory
	 *            IEffectFactory used to create an IEffect if the IFactoryFilter
	 *            returns true.
	 */
	public void register(@Nonnull final IFactoryFilter filter, @Nonnull final IEffectFactory factory) {
		this.filters.add(filter);
		this.factories.add(factory);
	}

	/**
	 * Creates an EffectHandler for the specified Entity. The IEffects attached to
	 * the EffectHandler is determined by an IFactoryFitler. An EffectHandler will
	 * always be created.
	 * 
	 * @param entity
	 *            The subject Entity for which an EffectHandler is created
	 * @return An EffectHandler for the Entity
	 */
	@Nonnull
	public Optional<EffectHandler> create(@Nonnull final Entity entity) {
		final List<IEffect> effectToApply = new ArrayList<IEffect>();
		for (int i = 0; i < this.filters.size(); i++)
			if (this.filters.get(i).applies(entity)) {
				final List<IEffect> r = this.factories.get(i).create(entity);
				effectToApply.addAll(r);
			}

		final EffectHandler result;
		if (effectToApply.size() > 0)
			result = new EffectHandler(entity, effectToApply);
		else
			// No effects. Return a dummy handler.
			// TODO: Revisit - can it be made more slim?
			result = new EffectHandler(entity, ImmutableList.of()) {
				@Override
				public void update() {
				}
			};

		return Optional.of(result);
	}

}
