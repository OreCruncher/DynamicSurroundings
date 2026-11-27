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

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.effects.EntityEffect;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactory;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactoryFilter;
import org.orecruncher.dsurround.client.effects.IEntityEffectHandlerState;
import org.orecruncher.dsurround.client.footsteps.Generator;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.effect.EntityEffectInfo;
import org.orecruncher.lib.random.XorShiftRandom;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityFootprintEffect extends EntityEffect {

	protected static final Random RANDOM = XorShiftRandom.current();

	// The entity types whose footsteps failed, so each is logged once
	private static final Set<Class<?>> FAILED = new HashSet<>();

	protected Generator generator;
	// Footsteps failed for this entity, and are off for it until its effects are made again (out of range and back,
	// a config reload)
	protected boolean failed;

	@Nonnull
	@Override
	public String name() {
		return "Footstep/Prints";
	}

	@Override
	public void initialize(@Nonnull final IEntityEffectHandlerState state) {
		super.initialize(state);
		getState().subject().ifPresent(e -> this.generator = RegistryManager.FOOTSTEPS.createGenerator((EntityLivingBase) e));
	}

	@Override
	public void update(@Nonnull final Entity subject) {
		if (this.generator == null || this.failed)
			return;
		try {
			this.generator.generateFootsteps((EntityLivingBase) subject);
		} catch (final Throwable t) {
			// Footsteps run in the entity's own update, so an error here would crash the game ("Ticking entity"):
			// most likely a mod's block under its feet (its shape, whether it is a ladder) that can't answer here
			this.failed = true;
			this.generator = null;
			if (FAILED.add(subject.getClass()))
				ModBase.log().error(String.format("Footsteps failed for %s at %s and are off for it",
						subject.getName(), subject.getPosition()), t);
		}
	}

	@Override
	public String toString() {
		if (this.generator == null)
			return "NULL GENERATOR";
		return super.toString() + ": " + this.generator.getPedometer();
	}

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = (@Nonnull final Entity e,
			@Nonnull final EntityEffectInfo eei) -> eei.effects.contains("footprint");

	public static class Factory implements IEntityEffectFactory {

		@Nonnull
		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity) {
			return ImmutableList
					.of(entity instanceof EntityPlayer ? new PlayerFootprintEffect() : new EntityFootprintEffect());
		}
	}

	@SideOnly(Side.CLIENT)
	private static class PlayerFootprintEffect extends EntityFootprintEffect {

		protected int lastStyle;

		@Nonnull
		@Override
		public String name() {
			return "Player Footstep/Prints";
		}

		@Override
		public void initialize(@Nonnull final IEntityEffectHandlerState state) {
			super.initialize(state);
			this.lastStyle = ModOptions.effects.footprintStyle;
		}

		@Override
		public void update(@Nonnull final Entity subject) {
			if (!this.failed && this.lastStyle != ModOptions.effects.footprintStyle) {
				this.generator = RegistryManager.FOOTSTEPS.createGenerator((EntityLivingBase) subject);
				this.lastStyle = ModOptions.effects.footprintStyle;
			}
			super.update(subject);
		}

	}

}
