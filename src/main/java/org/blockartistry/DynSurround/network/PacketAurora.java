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

import org.blockartistry.DynSurround.api.events.AuroraSpawnEvent;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.data.AuroraData;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketAurora implements IMessage {

	public static class PacketHandler implements IMessageHandler<PacketAurora, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final PacketAurora message, @Nullable final MessageContext ctx) {
			final World world = EnvironState.getWorld();
			if (world != null && world.provider.getDimension() == message.dimension)
				Network.postEvent(new AuroraSpawnEvent(world, message.posX, message.posZ, message.seed,
						message.colorSet, message.preset));
			return null;
		}
	}

	private int dimension;
	private long seed;
	private int posX;
	private int posZ;
	private int colorSet;
	private int preset;

	public PacketAurora() {
	}

	public PacketAurora(@Nonnull final AuroraData data) {
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

	@Override
	public void fromBytes(@Nonnull final ByteBuf buf) {
		this.dimension = buf.readShort();
		this.seed = buf.readLong();
		this.posX = buf.readInt();
		this.posZ = buf.readInt();
		this.colorSet = buf.readByte();
		this.preset = buf.readByte();
	}

	@Override
	public void toBytes(@Nonnull final ByteBuf buf) {
		buf.writeShort(this.dimension);
		buf.writeLong(this.seed);
		buf.writeInt(this.posX);
		buf.writeInt(this.posZ);
		buf.writeByte(this.colorSet);
		buf.writeByte(this.preset);
	}

}
