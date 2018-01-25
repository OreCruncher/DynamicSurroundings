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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.lib.effects.EntityEffect;
import org.blockartistry.lib.effects.IEntityEffectFactory;
import org.blockartistry.lib.effects.IEntityEffectHandlerState;
import org.blockartistry.lib.effects.IEntityEffectFactoryFilter;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VillagerChatEffect extends EntityEffect {

	static {
		// Setup the flee timers for villagers
		EntityChatEffect.setTimers("villager.flee", 250, 200);
	}

	protected final Predicate<Entity>[] preds;
	protected final EntityChatEffect normalChat;
	protected final EntityChatEffect fleeChat;
	protected boolean runningScared = false;

	@SuppressWarnings("unchecked")
	public VillagerChatEffect(@Nonnull final Entity entity) {

		final EntityVillager villager = (EntityVillager) entity;
		this.preds = new Predicate[] { EntitySelectors.CAN_AI_TARGET, new Predicate<Entity>() {
			public boolean apply(@Nullable Entity entity) {
				return entity.isEntityAlive() && villager.getEntitySenses().canSee(entity);
			}
		}, Predicates.<Entity>alwaysTrue() };

		this.normalChat = new EntityChatEffect(entity);
		this.fleeChat = new EntityChatEffect(entity, "villager.flee");
	}

	@Override
	public void intitialize(@Nonnull final IEntityEffectHandlerState state) {
		super.intitialize(state);
		this.normalChat.intitialize(state);
		this.fleeChat.intitialize(state);
	}

	@Override
	public void update() {
		if (!ModOptions.enableEntityChat)
			return;

		final Optional<Entity> e = this.getState().subject();
		if (e.isPresent()) {

			// Children don't speak - makes them suspicious...
			final EntityVillager entity = (EntityVillager) e.get();
			if (entity.isChild())
				return;

			if (this.villagerThreatened(entity)) {
				this.runningScared = true;
				this.fleeChat.update();
			} else {
				if (this.runningScared) {
					this.runningScared = false;
					this.normalChat.genNextChatTime();
				}
				this.normalChat.update();
			}
		}

	}

	protected boolean villagerThreatened(final Entity entity) {
		final AxisAlignedBB bbox = entity.getEntityBoundingBox().expand((double) 8.0, 3.0D, (double) 8.0);
		return !entity.worldObj
				.<EntityZombie>getEntitiesWithinAABB(EntityZombie.class, bbox, Predicates.and(this.preds)).isEmpty();
	}

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = new IEntityEffectFactoryFilter() {
		@Override
		public boolean applies(@Nonnull final Entity e) {
			return e instanceof EntityVillager && EntityChatEffect.hasMessages(e);
		}
	};

	public static class Factory implements IEntityEffectFactory {

		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity) {
			return ImmutableList.of(new VillagerChatEffect(entity));
		}
	}

}
