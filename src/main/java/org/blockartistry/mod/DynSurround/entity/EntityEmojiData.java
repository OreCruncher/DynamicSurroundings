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

package org.blockartistry.mod.DynSurround.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.api.entity.ActionState;
import org.blockartistry.mod.DynSurround.api.entity.EmojiType;
import org.blockartistry.mod.DynSurround.api.entity.EmotionalState;
import org.blockartistry.mod.DynSurround.api.entity.EntityCapability;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public final class EntityEmojiData implements ICapabilityProvider, IEntityEmojiSettable {

	public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(DSurround.MOD_ID, "entityEmojiData");

	private boolean isDirty = false;
	private ActionState actionState = ActionState.NONE;
	private EmotionalState emotionalState = EmotionalState.NEUTRAL;
	private EmojiType emojiType = EmojiType.NONE;

	@Override
	public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
		return capability == EntityCapability.EMOJI;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
		if (capability == EntityCapability.EMOJI)
			return (T) this;
		return null;
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

}
