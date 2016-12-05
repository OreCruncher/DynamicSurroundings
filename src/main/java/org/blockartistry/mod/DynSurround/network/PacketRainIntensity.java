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

import org.blockartistry.mod.DynSurround.client.storm.StormProperties;
import org.blockartistry.mod.DynSurround.util.PlayerUtils;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketRainIntensity implements IMessage, IMessageHandler<PacketRainIntensity, IMessage> {

	/**
	 * Strength of rainfall
	 */
	private float intensity;

	/**
	 * Dimension where the rainfall is occurring
	 */
	private int dimension;
	
	public PacketRainIntensity() {
	}

	public PacketRainIntensity(final float intensity, final int dimension) {
		this.intensity = intensity;
		this.dimension = dimension;
	}

	public void fromBytes(final ByteBuf buf) {
		this.intensity = buf.readFloat();
		this.dimension = buf.readInt();
	}

	public void toBytes(final ByteBuf buf) {
		buf.writeFloat(this.intensity);
		buf.writeInt(this.dimension);
	}

	@Override
	public IMessage onMessage(final PacketRainIntensity message, final MessageContext ctx) {
		// If the player is in the dimension set the intensity.  Otherwise
		// ignore.
		if (message.dimension == PlayerUtils.getClientPlayerDimension()) {
			StormProperties.setIntensity(message.intensity);
		}
		return null;
	}
}
