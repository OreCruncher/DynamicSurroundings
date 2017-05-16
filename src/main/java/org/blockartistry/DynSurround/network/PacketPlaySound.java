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

package org.blockartistry.DynSurround.network;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.client.event.PlayDistributedSoundEvent;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.sound.BasicSound;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketPlaySound implements IMessage {

	public static class PacketHandler implements IMessageHandler<PacketPlaySound, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final PacketPlaySound message, @Nullable final MessageContext ctx) {
			// Don't forward if it the current player sent it
			if (!message.locus.isAssociatedEntity(EnvironState.getPlayer())) {
				Network.postEvent(new PlayDistributedSoundEvent(message.soundClass, message.nbt));
			}
			return null;
		}
	}

	public static class PacketHandlerServer implements IMessageHandler<PacketPlaySound, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final PacketPlaySound message, @Nullable final MessageContext ctx) {
			// No event - turn around quick and broadcast to necessary
			// clients. This should take place on a Netty thread.
			Network.sendToAllAround(message.locus, message);
			return null;
		}
	}

	// Sounds have a range of 16 blocks per normal
	private static final int RANGE = 16;

	protected Locus locus;
	protected String soundClass;
	protected NBTTagCompound nbt = new NBTTagCompound();

	public PacketPlaySound() {

	}

	public PacketPlaySound(@Nonnull final Entity entity, @Nonnull final BasicSound<?> sound) {
		this.locus = new Locus(entity, sound.getXPosF(), sound.getYPosF(), sound.getZPosF(), RANGE);
		this.soundClass = sound.getClass().getName();
		this.nbt = sound.serializeNBT();
	}

	@Override
	public void fromBytes(@Nonnull final ByteBuf buf) {
		this.locus = new Locus(buf);
		this.soundClass = ByteBufUtils.readUTF8String(buf);
		this.nbt = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(@Nonnull final ByteBuf buf) {
		this.locus.toBytes(buf);
		ByteBufUtils.writeUTF8String(buf, this.soundClass);
		ByteBufUtils.writeTag(buf, this.nbt);
	}

}
