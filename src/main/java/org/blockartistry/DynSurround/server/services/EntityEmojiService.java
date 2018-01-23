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

package org.blockartistry.DynSurround.server.services;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.entity.ai.EntityAIEmoji;
import org.blockartistry.DynSurround.entity.ai.EntityAIVillagerEmoji;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class EntityEmojiService extends Service {

	EntityEmojiService() {
		super("EntityEmojiService");
	}

	protected void addEmojiAI(@Nonnull final EntityLiving entity) {
		if (entity instanceof EntityVillager)
			entity.tasks.addTask(EntityAIEmoji.PRIORITY, new EntityAIVillagerEmoji(entity));
		else
			entity.tasks.addTask(EntityAIEmoji.PRIORITY, new EntityAIEmoji(entity));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
	public void onJoinWorld(@Nonnull final EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityLiving) {
			final EntityLiving entity = (EntityLiving) event.getEntity();
			addEmojiAI(entity);
		}
	}

}
