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

package org.blockartistry.DynSurround.client.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.registry.EntityEffectInfo;
import org.blockartistry.lib.collections.ObjectArray;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Central repository for a collection of IEntityEffectFactory instances and the
 * IFactoryFilters associated with them. Typically there will be a single
 * instance of the EntityEffectLibrary for a project, but multiples can be
 * created based on the circumstances.
 */
@SideOnly(Side.CLIENT)
public class EntityEffectLibrary {

	protected final ObjectArray<IEntityEffectFactoryFilter> filters = new ObjectArray<>();
	protected final ObjectArray<IEntityEffectFactory> factories = new ObjectArray<>();
	protected final IParticleHelper particleHelper;
	protected final ISoundHelper soundHelper;

	public EntityEffectLibrary(@Nonnull final IParticleHelper ph, @Nonnull final ISoundHelper sh) {
		this.particleHelper = ph;
		this.soundHelper = sh;
	}

	/**
	 * Registers an IEntityEffectFactoryFilter/IEntityEffectFactory pair. The filter
	 * is used by the EntityEffectLibrary to determine if an EntityEffect applies to
	 * a target entity.
	 *
	 * @param filter
	 *            IEntityEffectFactoryFilter used to determine if the
	 *            IEntityEffectFactory should be used to create an EntityEffect.
	 * @param factory
	 *            IEntityEffectFactory used to create an EntityEffect if the
	 *            IEntityEffectFactoryFilter returns true.
	 */
	public void register(@Nonnull final IEntityEffectFactoryFilter filter,
			@Nonnull final IEntityEffectFactory factory) {
		this.filters.add(filter);
		this.factories.add(factory);
	}

	/**
	 * Creates an EntityEffectHandler for the specified Entity. The IEffects
	 * attached to the EntityEffectHandler is determined by an IFactoryFitler. An
	 * EntityEffectHandler will always be created.
	 *
	 * @param entity
	 *            The subject Entity for which an EntityEffectHandler is created
	 * @return An EntityEffectHandler for the Entity
	 */
	@Nonnull
	public Optional<EntityEffectHandler> create(@Nonnull final Entity entity) {
		final List<EntityEffect> effectToApply = new ArrayList<>();

		final EntityEffectInfo eei = ClientRegistry.EFFECTS.getEffects(entity);
		for (int i = 0; i < this.filters.size(); i++)
			if (this.filters.get(i).applies(entity, eei)) {
				final List<EntityEffect> r = this.factories.get(i).create(entity, eei);
				effectToApply.addAll(r);
			}

		final EntityEffectHandler result;
		if (effectToApply.size() > 0) {
			result = new EntityEffectHandler(entity, effectToApply, this.particleHelper, this.soundHelper);
		} else {
			// No effects. Return a dummy handler.
			result = new EntityEffectHandler.Dummy(entity);
		}

		return Optional.of(result);
	}

}
