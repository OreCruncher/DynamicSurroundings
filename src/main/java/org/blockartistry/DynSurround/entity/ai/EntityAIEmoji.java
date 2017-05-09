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

package org.blockartistry.DynSurround.entity.ai;

import org.blockartistry.DynSurround.api.entity.EmotionalState;
import org.blockartistry.DynSurround.api.entity.EntityCapability;
import org.blockartistry.DynSurround.entity.EmojiDataTables;
import org.blockartistry.DynSurround.entity.IEmojiDataSettable;
import org.blockartistry.lib.EntityUtils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIEmoji extends EntityAIBase {

	public static final int PRIORITY = 400;

	protected final EntityLiving subject;
	protected IEmojiDataSettable data;
	public EntityAIEmoji(final EntityLiving subject) {
		this.subject = subject;
		this.data = (IEmojiDataSettable) subject.getCapability(EntityCapability.EMOJI, null);
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

		if(this.data.isDirty())
			this.data.sync();
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
