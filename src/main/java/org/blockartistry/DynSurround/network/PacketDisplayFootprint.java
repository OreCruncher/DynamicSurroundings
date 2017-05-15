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

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.api.events.FootstepEvent;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketDisplayFootprint implements IMessage {

	public static class PacketHandler implements IMessageHandler<PacketDisplayFootprint, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final PacketDisplayFootprint message, @Nullable final MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				// Don't forward if it the current player sent it
				final EntityPlayer player = EnvironState.getPlayer();
				if (player != null && message.entityId != player.getEntityId())
					Network.postEvent(new FootstepEvent.Display(message.pos, message.rotation, message.isRightFoot));
			} else {
				// No event - turn around quick and broadcast to necessary
				// clients. This should take place on a Netty thread.
				final TargetPoint point = Network.getTargetPoint(ctx.getServerHandler().playerEntity,
						ModOptions.specialEffectRange);
				Network.sendToAllAround(point, message);
			}
			return null;
		}
	}

	// Package level
	protected int entityId;
	protected Vec3d pos = Vec3d.ZERO;
	protected float rotation = 0F;
	protected boolean isRightFoot = false;

	public PacketDisplayFootprint() {

	}

	public PacketDisplayFootprint(final int entityId, @Nonnull final Vec3d pos, final float rotation,
			final boolean rightFoot) {
		this.entityId = entityId;
		this.pos = pos;
		this.rotation = rotation;
		this.isRightFoot = rightFoot;
	}

	@Override
	public void fromBytes(@Nonnull final ByteBuf buf) {
		this.entityId = buf.readInt();
		final double x = buf.readFloat();
		final double y = buf.readFloat();
		final double z = buf.readFloat();
		this.pos = new Vec3d(x, y, z);
		this.rotation = buf.readFloat();
		this.isRightFoot = buf.readBoolean();
	}

	@Override
	public void toBytes(@Nonnull final ByteBuf buf) {
		buf.writeInt(this.entityId);
		buf.writeFloat((float) this.pos.xCoord);
		buf.writeFloat((float) this.pos.yCoord);
		buf.writeFloat((float) this.pos.zCoord);
		buf.writeFloat(this.rotation);
		buf.writeBoolean(this.isRightFoot);
	}

}
