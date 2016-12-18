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

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.blockartistry.mod.DynSurround.network.Network;
import org.blockartistry.mod.DynSurround.server.services.SpeechBubbleService;
import org.blockartistry.mod.DynSurround.util.XorShiftRandom;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EntitySelectors;

public class EntityAIChat extends EntityAIBase {

	public static final int PRIORITY = 1000;

	protected static final XorShiftRandom RANDOM = new XorShiftRandom();

	private static class EntityChatData {
		public int baseInterval = 400;
		public int baseRandom = 1200;
		public MessageTable table = new MessageTable();
	}

	private static final Map<Class<?>, EntityChatData> messages = new IdentityHashMap<Class<?>, EntityChatData>();

	static {
		EntityChatData data = new EntityChatData();
		data.table.add(10, "chat.villager0");
		data.table.add(15, "chat.villager1");
		data.table.add(20, "chat.villager2");
		data.table.add(20, "chat.villager3");
		data.table.add(10, "chat.villager4");
		data.table.add(10, "chat.villager5");
		data.table.add(15, "chat.villager6");
		data.table.add(10, "chat.villager7");
		data.table.add(15, "chat.villager8");
		data.table.add(15, "chat.villager9");
		data.table.add(20, "chat.villager10");
		data.table.add(20, "chat.villager11");
		data.table.add(15, "chat.villager12");
		data.table.add(15, "chat.villager13");
		data.table.add(15, "chat.villager14");
		data.table.add(20, "chat.villager15");
		data.table.add(10, "chat.villager16");
		data.table.add(15, "chat.villager17");
		data.table.add(10, "chat.villager18");
		data.table.add(10, "chat.villager19");
		data.table.add(15, "chat.villager20");
		data.table.add(20, "chat.villager21");
		data.table.add(15, "chat.villager22");
		data.table.add(10, "chat.villager23");
		data.table.add(15, "chat.villager24");
		data.table.add(10, "chat.villager25");
		messages.put(EntityVillager.class, data);

		data = new EntityChatData();
		data.table.add(25, "chat.zombie0");
		data.table.add(20, "chat.zombie1");
		data.table.add(10, "chat.zombie2");
		data.table.add(5, "chat.zombie3");
		messages.put(EntityZombie.class, data);

		data = new EntityChatData();
		data.baseRandom = 600;
		data.table.add(10, "chat.squid0");
		data.table.add(10, "chat.squid1");
		messages.put(EntitySquid.class, data);
	}
	
	public static boolean hasMessages(final Entity entity) {
		return messages.get(entity.getClass()) != null;
	}

	private int getBase(final EntityLiving entity) {
		return this.data.baseInterval;
	}

	private int getRandom(final EntityLiving entity) {
		return this.data.baseRandom;
	}

	protected final EntityChatData data;
	protected final EntityLiving theEntity;
	protected long lastChat;

	public EntityAIChat(final EntityLiving entity) {
		this.data = messages.get(entity.getClass());
		this.theEntity = entity;
		this.lastChat = entity.getEntityWorld().getTotalWorldTime() + getNextChatTime();
		this.setMutexBits(1 << 27);
	}

	protected String getChatMessage() {
		return this.data.table.next().messageId;
	}

	protected int getNextChatTime() {
		return getBase(this.theEntity) + RANDOM.nextInt(getRandom(this.theEntity));
	}

	@Override
	public void startExecuting() {
		final EntityLiving entity = this.theEntity;
		final Predicate<Entity> filter = EntitySelectors.withinRange(entity.posX, entity.posY, entity.posZ,
				SpeechBubbleService.SPEECH_BUBBLE_RANGE);
		final List<EntityPlayerMP> players = entity.getEntityWorld().getPlayers(EntityPlayerMP.class, filter);
		if (!players.isEmpty()) {
			final String message = getChatMessage();
			if (message != null) {
				for (final EntityPlayerMP player : players)
					Network.sendChatBubbleUpdate(entity.getPersistentID(), message, true, player);
			}
			this.lastChat = entity.getEntityWorld().getTotalWorldTime() + getNextChatTime();
		}
	}

	@Override
	public boolean shouldExecute() {
		final long currentTime = this.theEntity.worldObj.getTotalWorldTime();
		return currentTime > lastChat;
	}

}
