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

import org.blockartistry.DynSurround.event.ServerDataEvent;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketServerData implements IMessage {

	public static class PacketHandler implements IMessageHandler<PacketServerData, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final PacketServerData message, @Nullable final MessageContext ctx) {
			Network.postEvent(
					new ServerDataEvent(message.tMap, message.meanTickTime, message.free, message.total, message.max));
			return null;
		}
	}

	private double meanTickTime;
	private TIntDoubleHashMap tMap;
	private int free;
	private int total;
	private int max;

	public PacketServerData() {

	}

	public PacketServerData(@Nonnull final TIntDoubleHashMap tps, final double meanTickTime, final int memFree,
			int memTotal, int memMax) {
		this.meanTickTime = meanTickTime;
		this.tMap = tps;
		this.free = memFree;
		this.total = memTotal;
		this.max = memMax;
	}

	@Override
	public void fromBytes(@Nonnull final ByteBuf buf) {
		this.meanTickTime = buf.readDouble();
		int len = buf.readInt();
		this.tMap = new TIntDoubleHashMap(len);
		while (len-- != 0) {
			this.tMap.put(buf.readInt(), buf.readDouble());
		}
		this.free = buf.readInt();
		this.total = buf.readInt();
		this.max = buf.readInt();
	}

	@Override
	public void toBytes(@Nonnull final ByteBuf buf) {
		buf.writeDouble(this.meanTickTime);
		buf.writeInt(this.tMap.size());
		final TIntDoubleIterator i = this.tMap.iterator();
		while (i.hasNext()) {
			i.advance();
			buf.writeInt(i.key());
			buf.writeDouble(i.value());
		}
		buf.writeInt(this.free);
		buf.writeInt(this.total);
		buf.writeInt(this.max);
	}

}
