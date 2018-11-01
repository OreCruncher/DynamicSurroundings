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

import org.orecruncher.dsurround.event.WeatherUpdateEvent;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketWeatherUpdate implements IMessage {

	public static class PacketHandler implements IMessageHandler<PacketWeatherUpdate, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final PacketWeatherUpdate message, @Nullable final MessageContext ctx) {
			Network.postEvent(new WeatherUpdateEvent(message.dimension, message.intensity, message.maxIntensity,
					message.nextRainChange, message.thunderStrength, message.thunderChange, message.thunderEvent));
			return null;
		}
	}

	/**
	 * Strength of rainfall
	 */
	private float intensity;
	private float maxIntensity;
	private int nextRainChange;
	private float thunderStrength;
	private int thunderChange;
	private int thunderEvent;

	/**
	 * Dimension where the rainfall is occurring
	 */
	private int dimension;

	public PacketWeatherUpdate() {
	}

	public PacketWeatherUpdate(final int dimension, final float intensity, final float maxIntensity,
			final int nextRainChange, final float thunderStrength, final int thunderChange, final int thunderEvent) {
		this.dimension = dimension;
		this.intensity = intensity;
		this.maxIntensity = maxIntensity;
		this.nextRainChange = nextRainChange;
		this.thunderStrength = thunderStrength;
		this.thunderChange = thunderChange;
		this.thunderEvent = thunderEvent;
	}

	@Override
	public void fromBytes(@Nonnull final ByteBuf buf) {
		this.dimension = buf.readShort();
		this.intensity = buf.readFloat();
		this.maxIntensity = buf.readFloat();
		this.nextRainChange = buf.readInt();
		this.thunderStrength = buf.readFloat();
		this.thunderChange = buf.readInt();
		this.thunderEvent = buf.readInt();
	}

	@Override
	public void toBytes(@Nonnull final ByteBuf buf) {
		buf.writeShort(this.dimension);
		buf.writeFloat(this.intensity);
		buf.writeFloat(this.maxIntensity);
		buf.writeInt(this.nextRainChange);
		buf.writeFloat(this.thunderStrength);
		buf.writeInt(this.thunderChange);
		buf.writeInt(this.thunderEvent);
	}

}
