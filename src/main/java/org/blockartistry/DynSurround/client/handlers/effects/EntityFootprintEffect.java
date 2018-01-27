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
package org.blockartistry.DynSurround.client.handlers.effects;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.footsteps.system.Generator;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.event.ReloadEvent;
import org.blockartistry.lib.effects.EntityEffect;
import org.blockartistry.lib.effects.IEntityEffectFactory;
import org.blockartistry.lib.effects.IEntityEffectFactoryFilter;
import org.blockartistry.lib.effects.IEntityEffectHandlerState;
import org.blockartistry.lib.random.XorShiftRandom;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityFootprintEffect extends EntityEffect {

	protected static final Random RANDOM = XorShiftRandom.current();

	protected Generator generator;

	public EntityFootprintEffect() {

	}

	public void intitialize(@Nonnull final IEntityEffectHandlerState state) {
		super.intitialize(state);

		final EntityLivingBase entity = (EntityLivingBase) this.getState().subject().get();
		this.generator = ClientRegistry.FOOTSTEPS.createGenerator(entity);
		if (entity instanceof EntityPlayer)
			MinecraftForge.EVENT_BUS.register(this);
	}

	protected boolean isMoving(@Nonnull final Entity entity) {
		return entity.distanceWalkedModified != entity.prevDistanceWalkedModified;
	}

	// The player have changed settings that affect footstep/print generation (e.g.
	// Quadruped)
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onEvent(@Nonnull final ReloadEvent.Configuration event) {
		if (event.side == null || event.side == Side.CLIENT) {
			this.generator = ClientRegistry.FOOTSTEPS
					.createGenerator((EntityLivingBase) this.getState().subject().get());
		}
	}

	@Override
	public void die() {
		super.die();
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@Override
	public void update() {
		final Optional<Entity> e = this.getState().subject();
		if (e.isPresent()) {
			final EntityLivingBase entity = (EntityLivingBase) e.get();
			this.generator.generateFootsteps(entity);
		}
	}

	// Currently restricted to the active player. Have stuff to unwind in the
	// footprint code.
	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = new IEntityEffectFactoryFilter() {
		@Override
		public boolean applies(@Nonnull final Entity e) {
			return EnvironState.isPlayer(e) || e instanceof EntityVillager;
		}
	};

	public static class Factory implements IEntityEffectFactory {

		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity) {
			return ImmutableList.of(new EntityFootprintEffect());
		}
	}

}
