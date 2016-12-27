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

package org.blockartistry.mod.DynSurround.network;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.api.entity.ActionState;
import org.blockartistry.mod.DynSurround.api.entity.EmojiType;
import org.blockartistry.mod.DynSurround.api.entity.EmotionalState;
import org.blockartistry.mod.DynSurround.api.events.EntityEmojiEvent;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketEntityEmote implements IMessage {

	public static class PacketHandler implements IMessageHandler<PacketEntityEmote, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final PacketEntityEmote message, @Nullable final MessageContext ctx) {
			Network.postEvent(new EntityEmojiEvent(message.entityId, message.actionState, message.emotionalState, message.emojiType));
			return null;
		}
	}

	private UUID entityId;
	private ActionState actionState;
	private EmotionalState emotionalState;
	private EmojiType emojiType;

	public PacketEntityEmote() {

	}

	public PacketEntityEmote(@Nonnull final UUID entityId, @Nonnull final ActionState action,
			@Nonnull final EmotionalState emotion, @Nonnull final EmojiType emojiType) {
		this.entityId = entityId;
		this.actionState = action;
		this.emotionalState = emotion;
		this.emojiType = emojiType;
	}

	@Override
	public void fromBytes(@Nonnull final ByteBuf buf) {
		this.entityId = new UUID(buf.readLong(), buf.readLong());
		this.actionState = ActionState.get(buf.readByte());
		this.emotionalState = EmotionalState.get(buf.readByte());
		this.emojiType = EmojiType.get(buf.readByte());
	}

	@Override
	public void toBytes(@Nonnull final ByteBuf buf) {
		buf.writeLong(this.entityId.getMostSignificantBits());
		buf.writeLong(this.entityId.getLeastSignificantBits());
		buf.writeByte(ActionState.getId(this.actionState));
		buf.writeByte(EmotionalState.getId(this.emotionalState));
		buf.writeByte(EmojiType.getId(this.emojiType));
	}

}
