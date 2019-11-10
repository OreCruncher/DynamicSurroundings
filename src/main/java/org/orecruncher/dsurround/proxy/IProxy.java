package org.orecruncher.dsurround.proxy;

import javax.annotation.Nonnull;

import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public interface IProxy {

	boolean isRunningAsServer();

	Side effectiveSide();

	// General methods that are side agnostic
	default void preInit(@Nonnull final FMLPreInitializationEvent event) { }

	default void init(@Nonnull final FMLInitializationEvent event) { }

	default void postInit(@Nonnull final FMLPostInitializationEvent event) { }

	default void loadCompleted(@Nonnull final FMLLoadCompleteEvent event) { }

	// Client side specific event handlers
	default void clientConnect(@Nonnull final ClientConnectedToServerEvent event) { }

	default void clientDisconnect(@Nonnull final ClientDisconnectionFromServerEvent event) { }

	// Server side specific event handlers
	default void serverAboutToStart(@Nonnull final FMLServerAboutToStartEvent event) { }

	default void serverStarting(@Nonnull final FMLServerStartingEvent event) { }

	default void serverStopping(@Nonnull final FMLServerStoppingEvent event) { }

	default void serverStopped(@Nonnull final FMLServerStoppedEvent event) { }

	// Get the correct worker queue for the side
	IThreadListener getThreadListener(@Nonnull final MessageContext context);

}
