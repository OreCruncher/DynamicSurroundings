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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.network.Network;
import org.blockartistry.mod.DynSurround.server.services.SpeechBubbleService;
import org.blockartistry.mod.DynSurround.util.Translations;
import org.blockartistry.mod.DynSurround.util.XorShiftRandom;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EntitySelectors;

public class EntityAIChat extends EntityAIBase {

	public static final int PRIORITY = 1000;

	protected static final XorShiftRandom RANDOM = new XorShiftRandom();
	protected static final long RESCHEDULE_THRESHOLD = 100;

	private static class EntityChatData {
		public int baseInterval = 400;
		public int baseRandom = 1200;
		public MessageTable table = new MessageTable();
	}

	private static final Map<String, EntityChatData> messages = new HashMap<String, EntityChatData>();

	private static class ProcessPred implements Predicate<Entry<String, String>> {

		private final Pattern TYPE_PATTERN = Pattern.compile("chat\\.([a-zA-Z.]*)\\.[0-9]*$");
		private final Pattern WEIGHT_PATTERN = Pattern.compile("^([0-9]*),(.*)");

		public ProcessPred() {
		}

		@Override
		public boolean apply(final Entry<String, String> input) {
			final Matcher matcher1 = TYPE_PATTERN.matcher(input.getKey());
			if (matcher1.matches()) {
				final String key = matcher1.group(1).toLowerCase();
				final Matcher matcher2 = WEIGHT_PATTERN.matcher(input.getValue());
				if (matcher2.matches()) {
					EntityChatData data = messages.get(key);
					if (data == null)
						messages.put(key, data = new EntityChatData());
					final String weight = matcher2.group(1);
					data.table.add(Integer.parseInt(weight), input.getKey());
				} else {
					ModLog.warn("Invalid value in language file: %s", input.getValue());
				}
			} else {
				ModLog.warn("Invalid key in language file: %s", input.getKey());
			}

			return true;
		}

	}

	static {
		final Translations xlate = new Translations();
		xlate.load("/assets/dsurround/data/chat/", Translations.DEFAULT_LANGUAGE);
		xlate.forAll(new ProcessPred());

		EntityChatData data = messages.get(EntityList.getEntityStringFromClass(EntitySquid.class).toLowerCase());
		data.baseRandom = 600;

		data = messages.get("villager.flee");
		data.baseInterval = 75;
		data.baseRandom = 75;
	}

	public static boolean hasMessages(final Entity entity) {
		return messages.get(entity.getName().toLowerCase()) != null;
	}

	private int getBase() {
		return this.data.baseInterval;
	}

	private int getRandom() {
		return this.data.baseRandom;
	}

	protected final EntityChatData data;
	protected final EntityLiving theEntity;
	protected long lastChat;

	public EntityAIChat(final EntityLiving entity) {
		this(entity, null);
	}

	public EntityAIChat(final EntityLiving entity, final String entityName) {
		final String theName = StringUtils.isEmpty(entityName) ? EntityList.getEntityStringFromClass(entity.getClass()).toLowerCase()
				: entityName;
		this.data = messages.get(theName);
		this.theEntity = entity;
		this.lastChat = entity.getEntityWorld().getTotalWorldTime() + getNextChatTime();
		this.setMutexBits(1 << 27);
	}

	protected long getWorldTicks() {
		return this.theEntity.getEntityWorld().getTotalWorldTime();
	}

	protected String getChatMessage() {
		return this.data.table.next().messageId;
	}

	protected int getNextChatTime() {
		return getBase() + RANDOM.nextInt(getRandom());
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
			this.lastChat = getWorldTicks() + getNextChatTime();
		}
	}

	@Override
	public boolean shouldExecute() {
		final long delta = this.lastChat - getWorldTicks();
		if (delta <= -RESCHEDULE_THRESHOLD) {
			this.lastChat = getWorldTicks() + getNextChatTime();
			return false;
		}
		return delta <= 0;
	}

}
