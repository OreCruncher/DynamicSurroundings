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

package org.orecruncher.dsurround;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.orecruncher.dsurround.proxy.Proxy;
import org.orecruncher.lib.ForgeUtils;
import org.orecruncher.lib.Localization;
import org.orecruncher.lib.VersionChecker;
import org.orecruncher.lib.VersionHelper;
import org.orecruncher.lib.logging.ModLog;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@net.minecraftforge.fml.common.Mod(modid = ModBase.MOD_ID, useMetadata = true, dependencies = ModBase.DEPENDENCIES, version = ModBase.VERSION, acceptedMinecraftVersions = ModBase.MINECRAFT_VERSIONS, guiFactory = ModBase.GUI_FACTORY, updateJSON = ModBase.UPDATE_URL, certificateFingerprint = ModBase.FINGERPRINT)
public class ModBase {
	public static final String MOD_ID = "dsurround";
	public static final String API_ID = MOD_ID + "API";
	public static final String RESOURCE_ID = "dsurround";
	public static final String MOD_NAME = "Dynamic Surroundings";
	public static final String VERSION = "@VERSION@";
	public static final String MINECRAFT_VERSIONS = "[1.12.2,)";
	public static final String DEPENDENCIES = "required-after:forge@[14.23.2.2635,); after:galacticraftcore; after:ambientsounds";
	public static final String GUI_FACTORY = "org.orecruncher.dsurround.client.gui.ConfigGuiFactory";
	public static final String UPDATE_URL = "https://raw.githubusercontent.com/OreCruncher/DynamicSurroundings/master/version.json";
	public static final String FINGERPRINT = "7a2128d395ad96ceb9d9030fbd41d035b435753a";

	public static final String SERVER_VERSION = "3.4.9.0";

	@Instance(MOD_ID)
	protected static ModBase instance;

	@Nonnull
	public static ModBase instance() {
		return instance;
	}

	@SidedProxy(clientSide = "org.orecruncher.dsurround.proxy.ProxyClient", serverSide = "org.orecruncher.dsurround.proxy.Proxy")
	protected static Proxy proxy;
	protected static ModLog logger = ModLog.NULL_LOGGER;
	protected static Configuration config;
	protected static File dataDirectory;
	protected static boolean installedOnServer;
	protected static boolean devMode;

	@Nonnull
	public static Proxy proxy() {
		return proxy;
	}

	@Nonnull
	public static Configuration config() {
		return config;
	}

	@Nonnull
	public static ModLog log() {
		return logger;
	}

	@Nonnull
	public static File dataDirectory() {
		return dataDirectory;
	}

	public static boolean isInstalledOnServer() {
		return installedOnServer;
	}

	public static boolean isDeveloperMode() {
		return devMode;
	}

	@SideOnly(Side.CLIENT)
	public static boolean routePacketToServer() {
		return ModBase.isInstalledOnServer() && !Minecraft.getMinecraft().isIntegratedServerRunning();
	}

	public ModBase() {
		logger = ModLog.setLogger(ModBase.MOD_ID, LogManager.getLogger(MOD_ID));

		final String cmdText = System.getProperty("dsurround.devMode");
		if (!StringUtils.isEmpty(cmdText) && "true".equals(cmdText)) {
			devMode = true;
			logger.info("RUNNING IN DEVELOPMENT MODE");
		}
	}

	@EventHandler
	public void preInit(@Nonnull final FMLPreInitializationEvent event) {

		MinecraftForge.EVENT_BUS.register(this);

		// Load up our configuration
		dataDirectory = new File(event.getModConfigurationDirectory(), ModBase.MOD_ID);
		dataDirectory.mkdirs();
		config = new Configuration(new File(dataDirectory, ModBase.MOD_ID + ".cfg"), ModBase.VERSION);

		config.load();
		ModOptions.load(config);
		config.save();

		logger.setDebug(ModOptions.logging.enableDebugLogging);
		logger.setTraceMask(ModOptions.logging.debugFlagMask);

		proxy.preInit(event);
	}

	@EventHandler
	public void init(@Nonnull final FMLInitializationEvent event) {
		proxy.init(event);
	}

	@EventHandler
	public void postInit(@Nonnull final FMLPostInitializationEvent event) {

		proxy.postInit(event);
		config.save();

		// Patch up metadata
		if (!proxy.isRunningAsServer()) {
			final ModMetadata data = ForgeUtils.getModMetadata(ModBase.MOD_ID);
			if (data != null) {
				data.name = Localization.format("dsurround.metadata.Name");
				data.credits = Localization.format("dsurround.metadata.Credits");
				data.description = Localization.format("dsurround.metadata.Description");
				data.authorList = Arrays
						.asList(StringUtils.split(Localization.format("dsurround.metadata.Authors"), ','));
			}
		}
	}

	@EventHandler
	public void loadCompleted(@Nonnull final FMLLoadCompleteEvent event) {
		proxy.loadCompleted(event);
	}

	@EventHandler
	public void onFingerprintViolation(@Nonnull final FMLFingerprintViolationEvent event) {
		log().warn("Invalid fingerprint detected!");
	}

	////////////////////////
	//
	// Client state events
	//
	////////////////////////

	@NetworkCheckHandler
	public boolean checkModLists(@Nonnull final Map<String, String> modList, @Nonnull final Side side) {
		final String modVersion = modList.get(ModBase.MOD_ID);

		if (side == Side.SERVER) {
			installedOnServer = !StringUtils.isEmpty(modVersion);
			if (installedOnServer) {
				log().info("%s version %s is installed on the server", MOD_NAME, modVersion);
				if (VersionHelper.compareVersions(modVersion, SERVER_VERSION) < 0) {
					log().info("For the best experience the server should be running at least version %s",
							SERVER_VERSION);
				}
			}
		}

		// Fall through. The mod is not installed on the server
		// meaning it is a vanilla box or a forge server without
		// the mod.
		return true;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void clientConnect(@Nonnull final ClientConnectedToServerEvent event) {
		proxy.clientConnect(event);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void clientDisconnect(@Nonnull final ClientDisconnectionFromServerEvent event) {
		proxy.clientDisconnect(event);
		installedOnServer = false;
	}

	@SubscribeEvent
	public void playerLogin(final PlayerLoggedInEvent event) {
		if (ModOptions.logging.enableVersionChecking)
			new VersionChecker(ModBase.MOD_ID, "dsurround.msg.NewVersion.dsurround").playerLogin(event);
	}

	////////////////////////
	//
	// Server state events
	//
	////////////////////////
	@EventHandler
	public void serverAboutToStart(@Nonnull final FMLServerAboutToStartEvent event) {
		proxy.serverAboutToStart(event);
	}

	@EventHandler
	public void serverStarting(@Nonnull final FMLServerStartingEvent event) {
		proxy.serverStarting(event);
	}

	@EventHandler
	public void serverStopping(@Nonnull final FMLServerStoppingEvent event) {
		proxy.serverStopping(event);
	}

	@EventHandler
	public void serverStopped(@Nonnull final FMLServerStoppedEvent event) {
		proxy.serverStopped(event);
	}

}
