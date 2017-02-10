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

package org.blockartistry.mod.DynSurround.proxy;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.ModEnvironment;
import org.blockartistry.mod.DynSurround.commands.CommandDS;
import org.blockartistry.mod.DynSurround.entity.EntityEmojiCapability;
import org.blockartistry.mod.DynSurround.entity.EntityEventHandler;
import org.blockartistry.mod.DynSurround.network.Network;
import org.blockartistry.mod.DynSurround.server.services.ServiceManager;
import org.blockartistry.mod.DynSurround.util.Localization;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
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
import net.minecraftforge.fml.relauncher.Side;

public class Proxy {
	
	protected void registerLanguage() {
		Localization.initialize(Side.SERVER);
	}
	
	public boolean isRunningAsServer() {
		return true;
	}
	
	public Side effectiveSide() {
		return Side.SERVER;
	}

	public void preInit(@Nonnull final FMLPreInitializationEvent event) {
		registerLanguage();
	}

	public void init(@Nonnull final FMLInitializationEvent event) {
		ModEnvironment.initialize();

		Network.initialize();
		
		// General event handlers
		EntityEventHandler.register();
		
		// Capabilities
		EntityEmojiCapability.register();
	}

	public void postInit(@Nonnull final FMLPostInitializationEvent event) {
	}
	
	public void loadCompleted(@Nonnull final FMLLoadCompleteEvent event) {
	}

	public void clientConnect(@Nonnull final ClientConnectedToServerEvent event) {
		// NOTHING SHOULD BE HERE - OVERRIDE IN ProxyClient!
	}
	
	public void clientDisconnect(@Nonnull final ClientDisconnectionFromServerEvent event) {
		// NOTHING SHOULD BE HERE - OVERRIDE IN ProxyClient!
	}

	public void serverAboutToStart(@Nonnull final FMLServerAboutToStartEvent event) {
		ServiceManager.initialize();
	}

	public void serverStarting(@Nonnull final FMLServerStartingEvent event) {
		final MinecraftServer server = event.getServer();
		final ICommandManager command = server.getCommandManager();
		final ServerCommandManager serverCommand = (ServerCommandManager) command;
		serverCommand.registerCommand(new CommandDS());
	}
	
	public void serverStopping(@Nonnull final FMLServerStoppingEvent event) {

	}
	
	public void serverStopped(@Nonnull final FMLServerStoppedEvent event) {
		ServiceManager.deinitialize();
	}

}
