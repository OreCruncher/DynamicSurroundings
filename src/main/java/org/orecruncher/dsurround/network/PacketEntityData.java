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

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.capabilities.CapabilityEntityData;
import org.orecruncher.dsurround.capabilities.entitydata.IEntityData;
import org.orecruncher.dsurround.capabilities.entitydata.IEntityDataSettable;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.lib.WorldUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketEntityData implements IMessage {

	private int entityId;
	private boolean isAttacking;
	private boolean isFleeing;

	public PacketEntityData() {
		// Needed for client side creation
	}

	public PacketEntityData(@Nonnull final IEntityData data) {
		this.entityId = data.getEntityId();
		this.isAttacking = data.isAttacking();
		this.isFleeing = data.isFleeing();
	}

	@Override
	public void fromBytes(@Nonnull final ByteBuf buf) {
		this.entityId = buf.readInt();
		this.isAttacking = buf.readBoolean();
		this.isFleeing = buf.readBoolean();
	}

	@Override
	public void toBytes(@Nonnull final ByteBuf buf) {
		buf.writeInt(this.entityId);
		buf.writeBoolean(this.isAttacking);
		buf.writeBoolean(this.isFleeing);
	}

	public static class PacketHandler implements IMessageHandler<PacketEntityData, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final PacketEntityData message, @Nullable final MessageContext ctx) {
			if (ctx != null) {
				ModBase.proxy().getThreadListener(ctx).addScheduledTask(() -> {
					final Entity entity = WorldUtils.locateEntity(EnvironState.getWorld(), message.entityId);
					if (entity != null) {
						final IEntityDataSettable data = (IEntityDataSettable) CapabilityEntityData
								.getCapability(entity);
						if (data != null) {
							data.setAttacking(message.isAttacking);
							data.setFleeing(message.isFleeing);
						}
					}
				});
			}
			return null;
		}
	}
}
