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

package org.orecruncher.dsurround.network;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.capabilities.CapabilitySpeechData;
import org.orecruncher.dsurround.capabilities.speech.ISpeechData;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.lib.WorldUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSpeechBubble implements IMessage {

	protected int entityId;
	protected String message;

	public PacketSpeechBubble() {
		// Needed for client side creation
	}

	public PacketSpeechBubble(@Nonnull final Entity player, @Nonnull final String message) {
		this.entityId = player.getEntityId();
		this.message = message;
	}

	@Override
	public void fromBytes(@Nonnull final ByteBuf buf) {
		this.entityId = buf.readInt();
		this.message = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(@Nonnull final ByteBuf buf) {
		buf.writeInt(this.entityId);
		ByteBufUtils.writeUTF8String(buf, this.message);
	}

	public static class PacketHandler implements IMessageHandler<PacketSpeechBubble, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final PacketSpeechBubble message, @Nullable final MessageContext ctx) {
			if (ctx != null && ModOptions.speechbubbles.enableSpeechBubbles) {
				ModBase.proxy().getThreadListener(ctx).addScheduledTask(() -> {
					final World world = EnvironState.getWorld();
					if (world != null) {
						final Entity entity = WorldUtils.locateEntity(world, message.entityId);
						if (!(entity instanceof EntityPlayer))
							return;
						final ISpeechData data = CapabilitySpeechData.getCapability(entity);
						if (data != null)
							data.addMessage(message.message,
									(int) (ModOptions.speechbubbles.speechBubbleDuration * 20F));
					}
				});
			}
			return null;
		}
	}

}
