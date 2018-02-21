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

import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.event.ThunderEvent;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketThunder implements IMessage  {
	
	public static class PacketHandler implements IMessageHandler<PacketThunder, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final PacketThunder message, @Nullable final MessageContext ctx) {
			final World world = EnvironState.getWorld();
			if(world != null && world.provider.getDimension() == message.dimension)
				Network.postEvent(new ThunderEvent(world, message.doFlash, message.pos));
			return null;
		}
	}

	private int dimension;
	private boolean doFlash;
	private BlockPos pos;

	public PacketThunder() {
	}

	public PacketThunder(final int dimensionId, final boolean doFlash, final BlockPos pos) {
		this.dimension = dimensionId;
		this.doFlash = doFlash;
		this.pos = pos;
	}

	@Override
	public void fromBytes(@Nonnull final ByteBuf buf) {
		this.dimension = buf.readShort();
		this.doFlash = buf.readBoolean();
		final int x = buf.readInt();
		final int y = buf.readInt();
		final int z = buf.readInt();
		this.pos = new BlockPos(x, y, z);
	}

	@Override
	public void toBytes(@Nonnull final ByteBuf buf) {
		buf.writeShort(this.dimension);
		buf.writeBoolean(this.doFlash);
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
	}

}