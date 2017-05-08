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

import java.util.UUID;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.api.entity.ActionState;
import org.blockartistry.DynSurround.api.entity.EmojiType;
import org.blockartistry.DynSurround.api.entity.EmotionalState;
import gnu.trove.map.hash.TIntDoubleHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class Network {

	private static int discriminator = 0;

	private Network() {
	}

	private static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(DSurround.MOD_ID);

	public static void initialize() {

		NETWORK.registerMessage(PacketWeatherUpdate.PacketHandler.class, PacketWeatherUpdate.class, ++discriminator,
				Side.CLIENT);
		NETWORK.registerMessage(PacketHealthChange.PacketHandler.class, PacketHealthChange.class, ++discriminator,
				Side.CLIENT);
		NETWORK.registerMessage(PacketSpeechBubble.PacketHandler.class, PacketSpeechBubble.class, ++discriminator,
				Side.CLIENT);
		NETWORK.registerMessage(PacketEntityEmote.PacketHandler.class, PacketEntityEmote.class, ++discriminator,
				Side.CLIENT);
		NETWORK.registerMessage(PacketThunder.PacketHandler.class, PacketThunder.class, ++discriminator, Side.CLIENT);
		NETWORK.registerMessage(PacketEnvironment.PacketHandler.class, PacketEnvironment.class, ++discriminator,
				Side.CLIENT);
		NETWORK.registerMessage(PacketServerData.PacketHandler.class, PacketServerData.class, ++discriminator,
				Side.CLIENT);

	}

	@Nonnull
	public static TargetPoint getTargetPoint(@Nonnull final Entity entity, final double range) {
		return new TargetPoint(entity.getEntityWorld().provider.getDimension(), entity.posX, entity.posY, entity.posZ,
				range);
	}

	// Package level helper method to fire events based on incoming packets
	@SideOnly(Side.CLIENT)
	static void postEvent(@Nonnull final Event event) {
		Minecraft.getMinecraft().addScheduledTask(new Runnable() {
			public void run() {
				MinecraftForge.EVENT_BUS.post(event);
			}
		});
	}

	public static void sendWeatherUpdate(final int dimension, final float intensity, final float maxIntensity,
			final int nextRainChange, final float thunderStrength, final int thunderChange, final int thunderEvent) {
		NETWORK.sendToDimension(new PacketWeatherUpdate(dimension, intensity, maxIntensity, nextRainChange,
				thunderStrength, thunderChange, thunderEvent), dimension);
	}

	public static void sendHealthUpdate(@Nonnull final UUID id, final float x, final float y, final float z,
			final boolean isCritical, final int amount, @Nonnull final TargetPoint point) {
		NETWORK.sendToAllAround(new PacketHealthChange(id, x, y, z, isCritical, amount), point);
	}

	public static void sendChatBubbleUpdate(@Nonnull final UUID playerId, @Nonnull final String message,
			final boolean translate, @Nonnull final TargetPoint point) {
		NETWORK.sendToAllAround(new PacketSpeechBubble(playerId, message, translate), point);
	}

	public static void sendEntityEmoteUpdate(@Nonnull final UUID id, @Nonnull final ActionState action,
			@Nonnull final EmotionalState emotion, @Nonnull final EmojiType type, final int dimensionId) {
		NETWORK.sendToDimension(new PacketEntityEmote(id, action, emotion, type), dimensionId);
	}

	public static void sendThunder(final int dimensionId, final boolean doFlash, final float x, final float y,
			final float z) {
		NETWORK.sendToDimension(new PacketThunder(dimensionId, doFlash, new BlockPos(x, y, z)), dimensionId);
	}

	public static void sendEnvironmentUpdate(final EntityPlayer player, final boolean inVillage) {
		NETWORK.sendTo(new PacketEnvironment(inVillage), (EntityPlayerMP) player);
	}

	public static void sendServerDataUpdate(@Nonnull final TIntDoubleHashMap dimTps, final double meanTps,
			final int freeMemory, final int totalMemory, final int maxMemory) {
		NETWORK.sendToAll(new PacketServerData(dimTps, meanTps, freeMemory, totalMemory, maxMemory));
	}
}
