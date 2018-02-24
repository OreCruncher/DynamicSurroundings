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

package org.blockartistry.DynSurround.entity;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.network.Network;
import org.blockartistry.DynSurround.network.PacketEntityEmote;

import net.minecraft.entity.Entity;

public final class EmojiData implements IEmojiDataSettable {

	public static final int NO_ENTITY = -1;

	private final Entity entity;
	private boolean isDirty = false;
	private ActionState actionState = ActionState.NONE;
	private EmotionalState emotionalState = EmotionalState.NEUTRAL;
	private EmojiType emojiType = EmojiType.NONE;

	public EmojiData(@Nonnull final Entity entity) {
		this.entity = entity;
	}

	@Override
	public int getEntityId() {
		return this.entity != null ? this.entity.getEntityId() : NO_ENTITY;
	}

	@Override
	public void setActionState(@Nonnull final ActionState state) {
		if (this.actionState != state) {
			this.actionState = state;
			this.isDirty = true;
		}
	}

	@Override
	public void setEmotionalState(EmotionalState state) {
		if (this.emotionalState != state) {
			this.emotionalState = state;
			this.isDirty = true;
		}
	}

	@Override
	public void setEmojiType(EmojiType type) {
		if (this.emojiType != type) {
			this.emojiType = type;
			this.isDirty = true;
		}
	}

	@Override
	public boolean isDirty() {
		return this.isDirty;
	}

	@Override
	public void clearDirty() {
		this.isDirty = false;
	}

	@Override
	@Nonnull
	public ActionState getActionState() {
		return this.actionState;
	}

	@Override
	@Nonnull
	public EmotionalState getEmotionalState() {
		return this.emotionalState;
	}

	@Override
	@Nonnull
	public EmojiType getEmojiType() {
		return this.emojiType;
	}

	@Override
	public void sync() {
		if (this.entity != null && !this.entity.world.isRemote) {
			Network.sendToEntityViewers(this.entity, new PacketEntityEmote(this));
			clearDirty();
		}
	}

}
