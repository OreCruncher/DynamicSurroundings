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

import org.blockartistry.mod.DynSurround.api.entity.EmotionalState;
import org.blockartistry.mod.DynSurround.api.entity.EntityCapability;
import org.blockartistry.mod.DynSurround.entity.EmojiDataTables;
import org.blockartistry.mod.DynSurround.entity.IEntityEmojiSettable;
import org.blockartistry.mod.DynSurround.network.Network;
import org.blockartistry.mod.DynSurround.server.services.SpeechBubbleService;
import org.blockartistry.mod.DynSurround.util.EntityUtils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class EntityAIEmoji extends EntityAIBase {

	public static final int PRIORITY = 400;

	private static final int INTERVAL = 20;

	protected final EntityLiving subject;
	protected IEntityEmojiSettable data;
	private long nextChat;

	public EntityAIEmoji(final EntityLiving subject) {
		this.subject = subject;
		this.data = (IEntityEmojiSettable) subject.getCapability(EntityCapability.EMOJI, null);
	}

	@Override
	public boolean shouldExecute() {
		return true;
	}

	@Override
	public void startExecuting() {
	}

	@Override
	public void updateTask() {
		updateActionState();
		updateEmotionalState();
		updateEmoji();

		if (this.data.isDirty() || this.subject.getEntityWorld().getWorldTime() > this.nextChat) {
			final TargetPoint point = Network.getTargetPoint(this.subject, SpeechBubbleService.SPEECH_BUBBLE_RANGE);
			Network.sendEntityEmoteUpdate(this.subject.getPersistentID(), this.data.getActionState(),
					this.data.getEmotionalState(), this.data.getEmojiType(), point);
			this.data.clearDirty();
			this.nextChat = this.subject.getEntityWorld().getTotalWorldTime() + INTERVAL;
		}
	}

	protected void updateActionState() {
		this.data.setActionState(EmojiDataTables.assess(this.subject));
	}

	protected void updateEmotionalState() {
		EmotionalState newState = this.data.getActionState().getEmotionalState();

		if (EntityUtils.hasNegativePotionEffects(this.subject)) {
			newState = EmotionalState.SICK;
		} else if (this.subject.getHealth() <= (this.subject.getMaxHealth() / 2.0F)) {
			newState = EmotionalState.HURT;
		}

		this.data.setEmotionalState(newState);
	}

	protected void updateEmoji() {
		this.data.setEmojiType(EmojiDataTables.getEmoji(this.data.getActionState(), this.data.getEmotionalState()));
	}

}
