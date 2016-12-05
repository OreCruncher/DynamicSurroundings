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

import org.blockartistry.mod.DynSurround.client.DamageEffectHandler;
import org.blockartistry.mod.DynSurround.client.DamageEffectHandler.HealthData;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketHealthChange implements IMessage, IMessageHandler<PacketHealthChange, IMessage> {

	private UUID entityId;
	private float posX;
	private float posY;
	private float posZ;
	private boolean isCritical;
	private int amount;

	public PacketHealthChange() {
		
	}
	
	public PacketHealthChange(final HealthData data) {
		this.entityId = data.entityId;
		this.posX = data.posX;
		this.posY = data.posY;
		this.posZ = data.posZ;
		this.isCritical = data.isCritical;
		this.amount = data.amount;
	}

	public IMessage onMessage(final PacketHealthChange message, final MessageContext ctx) {
		DamageEffectHandler.handleEvent(new HealthData(message.entityId, message.posX, message.posY, message.posZ, message.isCritical, message.amount));
		return null;
	}

	@Override
	public void fromBytes(final ByteBuf buf) {
		this.entityId = new UUID(buf.readLong(), buf.readLong());
		this.posX = buf.readFloat();
		this.posY = buf.readFloat();
		this.posZ = buf.readFloat();
		this.isCritical = buf.readBoolean();
		this.amount = buf.readInt();
	}

	@Override
	public void toBytes(final ByteBuf buf) {
		buf.writeLong(this.entityId.getMostSignificantBits());
		buf.writeLong(this.entityId.getLeastSignificantBits());
		buf.writeFloat(this.posX);
		buf.writeFloat(this.posY);
		buf.writeFloat(this.posZ);
		buf.writeBoolean(this.isCritical);
		buf.writeInt(this.amount);
	}

}
