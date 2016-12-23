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

package org.blockartistry.mod.DynSurround.entity.ai;

import org.blockartistry.mod.DynSurround.entity.ActionState;
import org.blockartistry.mod.DynSurround.entity.EmojiDataTables;
import org.blockartistry.mod.DynSurround.entity.EmojiType;
import org.blockartistry.mod.DynSurround.entity.EmotionalState;
import org.blockartistry.mod.DynSurround.network.Network;
import org.blockartistry.mod.DynSurround.server.services.SpeechBubbleService;
import org.blockartistry.mod.DynSurround.util.EntityUtils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class EntityAIEmoji extends EntityAIBase {

	public static final int PRIORITY = 400;

	private final EntityLiving subject;
	private ActionState actionState = ActionState.NONE;
	private EmotionalState emotionalState = EmotionalState.NEUTRAL;
	private EmojiType emoji = EmojiType.NONE;
	private long nextChat;

	public EntityAIEmoji(final EntityLiving subject) {
		this.subject = subject;
	}

	public ActionState getActionState() {
		return this.actionState;
	}

	public EmotionalState getEmotionalState() {
		return this.emotionalState;
	}

	@Override
	public boolean shouldExecute() {
		return this.subject.getEntityWorld().getTotalWorldTime() >= this.nextChat;
	}

	@Override
	public void startExecuting() {
		updateActionState();
		updateEmotionalState();
		updateEmoji();

		final TargetPoint point = Network.getTargetPoint(this.subject, SpeechBubbleService.SPEECH_BUBBLE_RANGE);
		Network.sendEntityEmoteUpdate(this.subject.getPersistentID(), this.actionState, this.emotionalState, this.emoji, point);
		this.nextChat = this.subject.getEntityWorld().getTotalWorldTime() + 20;
	}

	protected void updateActionState() {
		this.actionState = EmojiDataTables.assess(this.subject);
	}

	protected void updateEmotionalState() {
		EmotionalState newState = this.actionState.getState();

		if (this.subject.getHealth() <= this.subject.getMaxHealth() / 2.0F) {
			newState = EmotionalState.SAD;
		} else if (EntityUtils.hasNegativePotionEffects(this.subject)) {
			newState = EmotionalState.SAD;
		}

		this.emotionalState = newState;
	}
	
	protected void updateEmoji() {
		this.emoji = EmojiDataTables.getEmoji(this.actionState, this.emotionalState);
	}

}
