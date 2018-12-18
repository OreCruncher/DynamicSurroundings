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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.lib.task.Scheduler;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerDisconnectionFromClientEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber(modid = ModInfo.MOD_ID)
public final class Network {

	// Need to track player/clients that have connected and do not have the
	// mod installed. We do not want to send packets to those clients and
	// cause Mayhem.
	private static final Field player = ReflectionHelper.findField(NetworkDispatcher.class, "player");
	private static final Set<UUID> blockList = new ObjectOpenHashSet<>();

	private static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(ModInfo.MOD_ID);

	public static void initialize() {

		int discriminator = 0;

		// Server -> Client messages
		NETWORK.registerMessage(PacketWeatherUpdate.PacketHandler.class, PacketWeatherUpdate.class, ++discriminator,
				Side.CLIENT);
		NETWORK.registerMessage(PacketSpeechBubble.PacketHandler.class, PacketSpeechBubble.class, ++discriminator,
				Side.CLIENT);
		NETWORK.registerMessage(PacketEntityData.PacketHandler.class, PacketEntityData.class, ++discriminator,
				Side.CLIENT);
		NETWORK.registerMessage(PacketThunder.PacketHandler.class, PacketThunder.class, ++discriminator, Side.CLIENT);
		NETWORK.registerMessage(PacketEnvironment.PacketHandler.class, PacketEnvironment.class, ++discriminator,
				Side.CLIENT);
		NETWORK.registerMessage(PacketServerData.PacketHandler.class, PacketServerData.class, ++discriminator,
				Side.CLIENT);

		// Client -> Server messages
	}

	@SubscribeEvent
	public static void clientConnect(@Nonnull final ServerConnectionFromClientEvent event) {
		final NetworkDispatcher dispatcher = NetworkDispatcher.get(event.getManager());
		if (dispatcher != null) {
			try {
				final EntityPlayerMP p = (EntityPlayerMP) player.get(dispatcher);
				final String version = dispatcher.getModList().get(ModInfo.MOD_ID);
				if (StringUtils.isEmpty(version)) {
					// Block the player from receiving network packets
					blockList.add(p.getPersistentID());
					ModBase.log().info("Player [%s] connected without having %s installed", p.getDisplayNameString(),
							ModInfo.MOD_NAME);
				} else {
					// Make sure the UUID is not in the list in case there was something lingering
					blockList.remove(p.getPersistentID());
					ModBase.log().info("Player [%s] connected with %s %s", p.getDisplayNameString(), ModInfo.MOD_NAME,
							version);
				}
			} catch (@Nonnull final Throwable t) {
				t.printStackTrace();
			}
		}
	}

	// Handle catastrophic client disconnects, like network disruptions, crashes,
	// etc.
	@SubscribeEvent
	public static void clientDisconnect(@Nonnull final ServerDisconnectionFromClientEvent event) {
		final NetworkDispatcher dispatcher = NetworkDispatcher.get(event.getManager());
		if (dispatcher != null) {
			try {
				final EntityPlayerMP p = (EntityPlayerMP) player.get(dispatcher);
				blockList.remove(p.getPersistentID());
			} catch (@Nonnull final Throwable t) {
				t.printStackTrace();
			}
		}
	}

	// Handle normal client disconnects, like the player quitting.
	@SubscribeEvent
	public static void clientDisconnect(@Nonnull final PlayerLoggedOutEvent event) {
		blockList.remove(event.player.getPersistentID());
	}

	// Package level helper method to fire client side events based on incoming
	// packets
	@SideOnly(Side.CLIENT)
	static void postEvent(@Nonnull final Event event) {
		postEvent(Side.CLIENT, event);
	}

	private static void postEvent(@Nonnull final Side side, @Nonnull final Event event) {
		Scheduler.schedule(side, () -> MinecraftForge.EVENT_BUS.post(event));
	}

	// Package level helper method to fire server side events based on incoming
	// packets
	static void postEventServer(@Nonnull final Event event) {
		postEvent(Side.SERVER, event);
	}

	private static void sendToList(@Nonnull final Stream<EntityPlayerMP> players, @Nonnull final IMessage msg) {
		synchronized (NETWORK) {
			players.forEach(p -> NETWORK.sendTo(msg, p));
		}
	}

	private static <T extends EntityPlayer> Stream<EntityPlayerMP> generateStream(@Nonnull final Collection<T> c) {
		//@formatter:off
		return c.stream()
			.filter(p -> !(p instanceof FakePlayer))
			.filter(p -> !blockList.contains(p.getPersistentID()))
			.map(p -> (EntityPlayerMP) p);
		//@formatter:on
	}

	// Basic server -> client packet routines
	public static void sendToPlayer(@Nonnull final EntityPlayerMP player, @Nonnull final IMessage msg) {
		synchronized (NETWORK) {
			if (!blockList.contains(player.getPersistentID()))
				NETWORK.sendTo(msg, player);
		}
	}

	public static void sendToEntityViewers(@Nonnull final Entity entity, @Nonnull final IMessage msg) {
		final Set<? extends EntityPlayer> players = ((WorldServer) entity.getEntityWorld()).getEntityTracker()
				.getTrackingPlayers(entity);
		final Stream<EntityPlayerMP> list = generateStream(players);
		sendToList(list, msg);
	}

	public static void sendToDimension(final int dimensionId, @Nonnull final IMessage msg) {
		final WorldServer world = DimensionManager.getWorld(dimensionId);
		if (world != null) {
			final Stream<EntityPlayerMP> players = generateStream(
					DimensionManager.getWorld(dimensionId).playerEntities);
			sendToList(players, msg);
		}
	}

	public static void sendToAll(@Nonnull final IMessage msg) {
		final Stream<EntityPlayerMP> players = generateStream(
				FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers());
		sendToList(players, msg);
	}

	public static void sendToAllAround(@Nonnull final Locus point, @Nonnull final IMessage msg) {
		final WorldServer world = DimensionManager.getWorld(point.dimension);
		if (world != null) {
			final double rSq = point.range * point.range;
			//@formatter:off
			final Stream<EntityPlayerMP> players =
				generateStream(DimensionManager.getWorld(point.dimension).playerEntities)
				.filter(p -> p.getDistanceSq(point.x, point.y, point.z) <= rSq);
			//@formatter:on
			sendToList(players, msg);
		}
	}

	// Basic client -> server packet routines
	@SideOnly(Side.CLIENT)
	public static void sendToServer(@Nonnull final IMessage msg) {
		NETWORK.sendToServer(msg);
	}
}
