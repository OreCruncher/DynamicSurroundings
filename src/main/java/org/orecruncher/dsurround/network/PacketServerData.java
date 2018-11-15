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

import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.client.handlers.DiagnosticHandler;
import org.orecruncher.dsurround.client.handlers.EffectManager;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketServerData implements IMessage {

	private double meanTickTime;
	private Int2DoubleOpenHashMap tMap;
	private int free;
	private int total;
	private int max;

	public PacketServerData() {

	}

	public PacketServerData(@Nonnull final Int2DoubleOpenHashMap tps, final double meanTickTime, final int memFree,
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
		this.tMap = new Int2DoubleOpenHashMap(len);
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
		this.tMap.int2DoubleEntrySet().forEach(entry -> {
			buf.writeInt(entry.getIntKey());
			buf.writeDouble(entry.getDoubleValue());
		});
		buf.writeInt(this.free);
		buf.writeInt(this.total);
		buf.writeInt(this.max);
	}

	@Nonnull
	private static TextFormatting getTpsFormatPrefix(final int tps) {
		if (tps <= 10)
			return TextFormatting.RED;
		if (tps <= 15)
			return TextFormatting.YELLOW;
		return TextFormatting.GREEN;
	}

	public static class PacketHandler implements IMessageHandler<PacketServerData, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final PacketServerData message, @Nullable final MessageContext ctx) {
			ModBase.proxy().getThreadListener(ctx).addScheduledTask(() -> {
				final ArrayList<String> data = new ArrayList<>();
				final int diff = message.total - message.free;
				data.add(TextFormatting.GOLD + "Server Information");
				data.add(String.format("Mem: %d%% %03d/%3dMB", diff * 100 / message.max, diff, message.max));
				data.add(String.format("Allocated: %d%% %3dMB", message.total * 100 / message.max, message.total));
				final int tps = (int) Math.min(1000.0D / message.meanTickTime, 20.0D);
				data.add(String.format("Ticktime Overall:%s %5.3fms (%d TPS)", getTpsFormatPrefix(tps),
						message.meanTickTime, tps));
				message.tMap.int2DoubleEntrySet().forEach(entry -> {
					final String dimName = DimensionManager.getProviderType(entry.getIntKey()).getName();
					final int tps1 = (int) Math.min(1000.0D / entry.getDoubleValue(), 20.0D);
					data.add(String.format("%s (%d):%s %7.3fms (%d TPS)", dimName, entry.getIntKey(),
							getTpsFormatPrefix(tps1), entry.getDoubleValue(), tps1));
				});
				Collections.sort(data.subList(4, data.size()));
				final DiagnosticHandler handler = EffectManager.instance().lookupService(DiagnosticHandler.class);
				handler.setServerTPSReport(data);
			});
			return null;
		}
	}

}
