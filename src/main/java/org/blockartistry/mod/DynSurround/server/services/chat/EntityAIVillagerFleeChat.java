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

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;

public class EntityAIVillagerFleeChat extends EntityAIChat {

	public static final int PRIORITY = 900;
	
	private static final MessageTable MESSAGES = new MessageTable();
	
	static {
		MESSAGES.add(20, "chat.villager.flee0");
		MESSAGES.add(20, "chat.villager.flee1");
		MESSAGES.add(5, "chat.villager.flee2");
		MESSAGES.add(15, "chat.villager.flee3");
		MESSAGES.add(10, "chat.villager.flee4");
	}

	protected final Predicate<Entity> canBeSeenSelector;
	protected final Predicate<Entity>[] preds;

	@SuppressWarnings("unchecked")
	public EntityAIVillagerFleeChat(final EntityLiving entity) {
		super(entity);

		this.canBeSeenSelector = new Predicate<Entity>() {
			public boolean apply(@Nullable Entity entity) {
				return entity.isEntityAlive()
						&& EntityAIVillagerFleeChat.this.theEntity.getEntitySenses().canSee(entity);
			}
		};

		this.preds = new Predicate[] { EntitySelectors.CAN_AI_TARGET, this.canBeSeenSelector,
				Predicates.<Entity> alwaysTrue() };
	}

	@Override
	protected String getChatMessage() {
		return MESSAGES.getMessage();
	}

	@Override
	protected int getNextChatTime() {
		return 75 + RANDOM.nextInt(75);
	}

	protected boolean villagerThreatened() {
		final AxisAlignedBB bbox = this.theEntity.getEntityBoundingBox().expand((double) 8.0, 3.0D, (double) 8.0);
		return !this.theEntity.worldObj
				.<EntityZombie> getEntitiesWithinAABB(EntityZombie.class, bbox, Predicates.and(this.preds)).isEmpty();
	}

	@Override
	public boolean shouldExecute() {
		return super.shouldExecute() && villagerThreatened();
	}
}
