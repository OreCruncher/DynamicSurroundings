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
import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.footsteps.system.Generator;
import org.blockartistry.DynSurround.event.DiagnosticEvent;
import org.blockartistry.DynSurround.registry.EntityEffectInfo;
import org.blockartistry.lib.effects.EntityEffect;
import org.blockartistry.lib.effects.IEntityEffectFactory;
import org.blockartistry.lib.effects.IEntityEffectFactoryFilter;
import org.blockartistry.lib.effects.IEntityEffectHandlerState;
import org.blockartistry.lib.random.XorShiftRandom;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityFootprintEffect extends EntityEffect {

	protected static final Random RANDOM = XorShiftRandom.current();

	protected Generator generator;
	protected int lastStyle;
	protected boolean eventRegistered;

	public EntityFootprintEffect() {

	}

	@Override
	public String name() {
		return "Footstep/Prints";
	}

	@Override
	public void intitialize(@Nonnull final IEntityEffectHandlerState state) {
		super.intitialize(state);

		final EntityLivingBase entity = (EntityLivingBase) this.getState().subject().get();
		this.generator = ClientRegistry.FOOTSTEPS.createGenerator(entity);
		this.lastStyle = ModOptions.player.footprintStyle;

		if (entity instanceof EntityPlayerSP) {
			this.eventRegistered = true;
			MinecraftForge.EVENT_BUS.register(this);
		}
	}

	@Override
	public void update(@Nonnull final Entity subject) {
		final EntityLivingBase entity = (EntityLivingBase) subject;
		if (this.getState().isActivePlayer(entity) && this.lastStyle != ModOptions.player.footprintStyle) {
			this.generator = ClientRegistry.FOOTSTEPS.createGenerator(entity);
			this.lastStyle = ModOptions.player.footprintStyle;
		}
		
		this.generator.generateFootsteps(entity);
	}

	@Override
	public void die() {
		if(this.eventRegistered)
			MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void diagnostic(@Nonnull final DiagnosticEvent.Gather event) {
		event.output.add("Footsteps: " + this.generator.toString());
	}

	@Override
	public String toString() {
		return super.toString() + ": " + this.generator.getPedometer();
	}

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = new IEntityEffectFactoryFilter() {
		@Override
		public boolean applies(@Nonnull final Entity e, @Nonnull final EntityEffectInfo eei) {
			return eei.effects.contains("footprint");
		}
	};

	public static class Factory implements IEntityEffectFactory {

		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity, @Nonnull final EntityEffectInfo eei) {
			return ImmutableList.of(new EntityFootprintEffect());
		}
	}

}
