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
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.capabilities.CapabilityEntityData;
import org.orecruncher.dsurround.capabilities.entitydata.IEntityData;
import org.orecruncher.dsurround.client.effects.EntityEffect;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactory;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactoryFilter;
import org.orecruncher.dsurround.client.effects.IEntityEffectHandlerState;
import org.orecruncher.dsurround.registry.effect.EntityEffectInfo;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.EntitySelectors;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VillagerChatEffect extends EntityEffect {

	static {
		// Setup the flee timers for villagers
		EntityChatEffect.setTimers("villager.flee", 250, 200);
	}

	protected final Predicate<Entity> pred;
	protected final EntityChatEffect normalChat;
	protected final EntityChatEffect fleeChat;
	protected boolean runningScared = false;

	public VillagerChatEffect(@Nonnull final Entity entity) {
		final EntityVillager villager = (EntityVillager) entity;
		this.pred = EntitySelectors.CAN_AI_TARGET
				.and(input -> input.isEntityAlive() && villager.getEntitySenses().canSee(input));
		this.normalChat = new EntityChatEffect(entity);
		this.fleeChat = new EntityChatEffect(entity, "villager.flee");
	}

	@Override
	public String name() {
		return "Villager Chat";
	}

	@Override
	public void intitialize(@Nonnull final IEntityEffectHandlerState state) {
		super.intitialize(state);
		this.normalChat.intitialize(state);
		this.fleeChat.intitialize(state);
	}

	@Override
	public void update(@Nonnull final Entity subject) {
		if (!ModOptions.speechbubbles.enableEntityChat)
			return;

		// Children don't speak - makes them suspicious...
		final EntityVillager entity = (EntityVillager) subject;
		if (entity.isChild())
			return;

		if (villagerThreatened(entity)) {
			this.runningScared = true;
			this.fleeChat.update(subject);
		} else {
			if (this.runningScared) {
				this.runningScared = false;
				this.normalChat.genNextChatTime();
			}
			this.normalChat.update(subject);
		}

	}

	protected boolean villagerThreatened(final Entity entity) {
		// If there is server side support, use the entity capability data. If not
		// present or the server doesn't support, fall through and look for entities
		// of the appropriate type.
		if (ModBase.isInstalledOnServer()) {
			final IEntityData data = CapabilityEntityData.getCapability(entity);
			if (data != null)
				return data.isFleeing();
		}

		for (final Entity e : entity.getEntityWorld().loadedEntityList) {
			if (e.getDistanceSq(entity) <= 64.0) {
				// From EntityVillager's AvoidEntity AI
				if (e instanceof EntityZombie || e instanceof EntityEvoker || e instanceof EntityVex
						|| e instanceof EntityVindicator) {
					if (this.pred.test(e)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = (@Nonnull final Entity e,
			@Nonnull final EntityEffectInfo eei) -> eei.effects.contains("chat") && e instanceof EntityVillager
					&& EntityChatEffect.hasMessages(e);

	public static class Factory implements IEntityEffectFactory {

		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity, @Nonnull final EntityEffectInfo eei) {
			return ImmutableList.of(new VillagerChatEffect(entity));
		}
	}

}
