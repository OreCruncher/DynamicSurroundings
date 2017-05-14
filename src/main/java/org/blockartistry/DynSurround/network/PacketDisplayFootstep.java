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

import org.blockartistry.DynSurround.api.events.FootstepEvent;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketDisplayFootstep implements IMessage {

	public static class PacketHandler implements IMessageHandler<PacketDisplayFootstep, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final PacketDisplayFootstep message, @Nullable final MessageContext ctx) {
			Network.postEvent(
					new FootstepEvent.Display(message.dimensionId, message.pos, message.rotation, message.isRightFoot));
			return null;
		}
	}

	private int dimensionId = 0;
	private Vec3d pos = Vec3d.ZERO;
	private float rotation = 0F;
	private boolean isRightFoot = false;

	public PacketDisplayFootstep() {

	}

	public PacketDisplayFootstep(final int dimensionId, @Nonnull final Vec3d pos, final float rotation,
			final boolean rightFoot) {
		this.dimensionId = dimensionId;
		this.pos = pos;
		this.rotation = rotation;
		this.isRightFoot = rightFoot;
	}
	
	// Package level
	PacketDisplayFootstep(@Nonnull final PacketGenerateFootstep packet) {
		this.dimensionId = packet.dimensionId;
		this.pos = packet.pos;
		this.rotation = packet.rotation;
		this.isRightFoot = packet.isRightFoot;
	}

	@Override
	public void fromBytes(@Nonnull final ByteBuf buf) {
		this.dimensionId = buf.readInt();
		final double x = buf.readFloat();
		final double y = buf.readFloat();
		final double z = buf.readFloat();
		this.pos = new Vec3d(x, y, z);
		this.rotation = buf.readFloat();
		this.isRightFoot = buf.readBoolean();
	}

	@Override
	public void toBytes(@Nonnull final ByteBuf buf) {
		buf.writeInt(this.dimensionId);
		buf.writeFloat((float) this.pos.xCoord);
		buf.writeFloat((float) this.pos.yCoord);
		buf.writeFloat((float) this.pos.zCoord);
		buf.writeFloat(this.rotation);
		buf.writeBoolean(this.isRightFoot);
	}

}
