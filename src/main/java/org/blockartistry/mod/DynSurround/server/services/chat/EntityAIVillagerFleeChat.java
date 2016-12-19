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

package org.blockartistry.mod.DynSurround.server.services.chat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;

public class EntityAIVillagerFleeChat extends EntityAIChat {

	public static final int PRIORITY = 990;

	protected final Predicate<Entity>[] preds;

	@SuppressWarnings("unchecked")
	public EntityAIVillagerFleeChat(@Nonnull final EntityLiving entity) {
		super(entity, "villager.flee");

		this.preds = new Predicate[] { EntitySelectors.CAN_AI_TARGET, new Predicate<Entity>() {
			public boolean apply(@Nullable Entity entity) {
				return entity.isEntityAlive()
						&& EntityAIVillagerFleeChat.this.theEntity.getEntitySenses().canSee(entity);
			}
		}, Predicates.<Entity> alwaysTrue() };
	}

	protected boolean villagerThreatened() {
		final AxisAlignedBB bbox = this.theEntity.getEntityBoundingBox().expand((double) 8.0, 3.0D, (double) 8.0);
		return !this.theEntity.worldObj
				.<EntityZombie> getEntitiesWithinAABB(EntityZombie.class, bbox, Predicates.and(this.preds)).isEmpty();
	}

	@Override
	public void updateTask() {
		if (getWorldTicks() < this.nextChat)
			return;
		super.startExecuting();
	}

	@Override
	public boolean shouldExecute() {
		return villagerThreatened();
	}

	@Override
	public void startExecuting() {
		this.nextChat = getWorldTicks();
	}
}
