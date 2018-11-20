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
package org.orecruncher.dsurround.client.handlers.effects;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.effects.EntityEffect;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactory;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactoryFilter;
import org.orecruncher.dsurround.client.effects.IEntityEffectHandlerState;
import org.orecruncher.dsurround.client.footsteps.system.Generator;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.effect.EntityEffectInfo;
import org.orecruncher.lib.random.XorShiftRandom;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityFootprintEffect extends EntityEffect {

	protected static final Random RANDOM = XorShiftRandom.current();

	protected Generator generator;
	protected int lastStyle;

	public EntityFootprintEffect() {

	}

	@Override
	public String name() {
		return "Footstep/Prints";
	}

	@Override
	public void intitialize(@Nonnull final IEntityEffectHandlerState state) {
		super.intitialize(state);

		final EntityLivingBase entity = (EntityLivingBase) getState().subject().get();
		this.generator = RegistryManager.FOOTSTEPS.createGenerator(entity);
		this.lastStyle = ModOptions.effects.footprintStyle;
	}

	@Override
	public void update(@Nonnull final Entity subject) {
		final EntityLivingBase entity = (EntityLivingBase) subject;
		if (this.lastStyle != ModOptions.effects.footprintStyle && getState().isActivePlayer(entity)) {
			this.generator = RegistryManager.FOOTSTEPS.createGenerator(entity);
			this.lastStyle = ModOptions.effects.footprintStyle;
		}

		this.generator.generateFootsteps(entity);
	}

	@Override
	public String toString() {
		return super.toString() + ": " + this.generator.getPedometer();
	}

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = (@Nonnull final Entity e,
			@Nonnull final EntityEffectInfo eei) -> eei.effects.contains("footprint");

	public static class Factory implements IEntityEffectFactory {

		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity, @Nonnull final EntityEffectInfo eei) {
			return ImmutableList.of(new EntityFootprintEffect());
		}
	}

}
