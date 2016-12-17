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

import org.blockartistry.mod.DynSurround.client.handlers.AuroraEffectHandler;
import org.blockartistry.mod.DynSurround.data.AuroraData;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketAurora implements IMessage, IMessageHandler<PacketAurora, IMessage> {

	private int dimension;
	private long seed;
	private int posX;
	private int posZ;
	private int colorSet;
	private int preset;

	public PacketAurora() {
	}

	public PacketAurora(final AuroraData data) {
		this(data.dimensionId, data.seed, data.posX, data.posZ, data.colorSet, data.preset);
	}

	public PacketAurora(final int dimensionId, final long seed, final int posX, final int posZ, final int colorSet,
			final int preset) {
		this.dimension = dimensionId;
		this.seed = seed;
		this.posX = posX;
		this.posZ = posZ;
		this.colorSet = colorSet;
		this.preset = preset;
	}

	public void fromBytes(final ByteBuf buf) {
		this.dimension = buf.readInt();
		this.seed = buf.readLong();
		this.posX = buf.readInt();
		this.posZ = buf.readInt();
		this.colorSet = buf.readByte();
		this.preset = buf.readByte();
	}

	public void toBytes(final ByteBuf buf) {
		buf.writeInt(this.dimension);
		buf.writeLong(this.seed);
		buf.writeInt(this.posX);
		buf.writeInt(this.posZ);
		buf.writeByte(this.colorSet);
		buf.writeByte(this.preset);
	}

	@Override
	public IMessage onMessage(final PacketAurora message, final MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(new Runnable() {
			public void run() {
				AuroraEffectHandler.addAurora(new AuroraData(message.dimension, message.posX, message.posZ,
						message.seed, message.colorSet, message.preset));
			}
		});
		return null;
	}
}
