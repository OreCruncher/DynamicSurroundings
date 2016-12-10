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

import java.util.UUID;

import org.blockartistry.mod.DynSurround.Module;
import org.blockartistry.mod.DynSurround.client.HealthEffectHandler.HealthData;
import org.blockartistry.mod.DynSurround.data.AuroraData;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class Network {
	
	private static int discriminator = 0;
	
	private Network() {
	}

	public static SimpleNetworkWrapper network;

	public static void initialize() {
		network = NetworkRegistry.INSTANCE.newSimpleChannel(Module.MOD_ID);
		network.registerMessage(PacketRainIntensity.class, PacketRainIntensity.class, ++discriminator, Side.CLIENT);
		network.registerMessage(PacketAurora.class, PacketAurora.class, ++discriminator, Side.CLIENT);
		network.registerMessage(PacketHealthChange.class, PacketHealthChange.class, ++discriminator, Side.CLIENT);
		network.registerMessage(PacketSpeechBubble.class, PacketSpeechBubble.class, ++discriminator, Side.CLIENT);
	}

	public static void sendRainIntensity(final float intensity, final int dimension) {
		network.sendToDimension(new PacketRainIntensity(intensity, dimension), dimension);
	}
	
	public static void sendAurora(final AuroraData data, final int dimension) {
		network.sendToDimension(new PacketAurora(data), dimension);
	}

	public static void sendHealthUpdate(final HealthData data, final int dimension) {
		network.sendToDimension(new PacketHealthChange(data), dimension);
	}
	
	public static void sendChatBubbleUpdate(final UUID playerId, final String message, final EntityPlayerMP target) {
		network.sendTo(new PacketSpeechBubble(playerId, message), target);
	}
}
