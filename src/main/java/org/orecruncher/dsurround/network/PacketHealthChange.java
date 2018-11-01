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

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketHealthChange implements IMessage {

	public static class PacketHandler implements IMessageHandler<PacketHealthChange, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final PacketHealthChange message, @Nullable final MessageContext ctx) {
			// Ignore - client produces all footsteps without the server
			return null;
		}
	}

	private int entityId;
	private float posX;
	private float posY;
	private float posZ;
	private boolean isCritical;
	private int amount;

	public PacketHealthChange() {

	}

	public PacketHealthChange(final int id, final float x, final float y, final float z, final boolean isCritical,
			final int amount) {
		this.entityId = id;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.isCritical = isCritical;
		this.amount = amount;
	}

	@Override
	public void fromBytes(@Nonnull final ByteBuf buf) {
		this.entityId = buf.readInt();
		this.posX = buf.readFloat();
		this.posY = buf.readFloat();
		this.posZ = buf.readFloat();
		this.isCritical = buf.readBoolean();
		this.amount = buf.readInt();
	}

	@Override
	public void toBytes(@Nonnull final ByteBuf buf) {
		buf.writeInt(this.entityId);
		buf.writeFloat(this.posX);
		buf.writeFloat(this.posY);
		buf.writeFloat(this.posZ);
		buf.writeBoolean(this.isCritical);
		buf.writeInt(this.amount);
	}

}
