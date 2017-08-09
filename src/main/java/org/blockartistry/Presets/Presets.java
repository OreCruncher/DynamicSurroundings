package org.blockartistry.Presets;

import java.io.File;
import java.util.Arrays;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.Presets.proxy.Proxy;
import org.blockartistry.lib.ForgeUtils;
import org.blockartistry.lib.Localization;
import org.blockartistry.lib.VersionChecker;
import org.blockartistry.lib.logging.ModLog;

import net.minecraft.client.Minecraft;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@net.minecraftforge.fml.common.Mod(
		modid = Presets.MOD_ID,
		useMetadata = true,
		dependencies = Presets.DEPENDENCIES,
		version = Presets.VERSION,
		acceptedMinecraftVersions = DSurround.MINECRAFT_VERSIONS,
		guiFactory = Presets.GUI_FACTORY,
		updateJSON = Presets.UPDATE_URL,
		clientSideOnly = true
)
public class Presets {
	public static final String MOD_ID = "presets";
	public static final String API_ID = MOD_ID + "API";
	public static final String RESOURCE_ID = "presets";
	public static final String MOD_NAME = "Presets";
	public static final String VERSION = "@VERSION@";
	public static final String MINECRAFT_VERSIONS = "[1.12,1.12.1]";
	public static final String DEPENDENCIES = "";
	public static final String GUI_FACTORY = "org.blockartistry.Presets.gui.ConfigGuiFactory";
	public static final String UPDATE_URL = "https://raw.githubusercontent.com/OreCruncher/DynamicSurroundings/master/version.json";

	@Instance(MOD_ID)
	protected static Presets instance;

	@Nonnull
	public static Presets instance() {
		return instance;
	}

	@SidedProxy(clientSide = "org.blockartistry.Presets.proxy.ProxyClient", serverSide = "org.blockartistry.Presets.proxy.Proxy")
	protected static Proxy proxy;

	@Nonnull
	public static Proxy proxy() {
		return proxy;
	}

	protected static ModLog logger = ModLog.NULL_LOGGER;
	protected static Configuration config;

	@Nonnull
	public static Configuration config() {
		return config;
	}

	@Nonnull
	public static ModLog log() {
		return logger;
	}

	protected static File dataDirectory;

	@Nonnull
	public static File dataDirectory() {
		return dataDirectory;
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	public static Profiler getProfiler() {
		return Minecraft.getMinecraft().mcProfiler;
	}

	public Presets() {
		logger = ModLog.setLogger(MOD_ID, LogManager.getLogger(MOD_ID));
	}

	@EventHandler
	public void preInit(@Nonnull final FMLPreInitializationEvent event) {

		MinecraftForge.EVENT_BUS.register(this);

		// Load up our configuration
		dataDirectory = new File(event.getModConfigurationDirectory(), Presets.MOD_ID);
		dataDirectory.mkdirs();
		config = new Configuration(new File(dataDirectory, Presets.MOD_ID + ".cfg"), Presets.VERSION);

		config.load();
		ModOptions.load(config);
		config.save();

		logger.setDebug(ModOptions.enableDebugLogging);

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
			final ModMetadata data = ForgeUtils.getModMetadata(Presets.MOD_ID);
			if (data != null) {
				data.name = Localization.format("presets.metadata.Name");
				data.credits = Localization.format("presets.metadata.Credits");
				data.description = Localization.format("presets.metadata.Description");
				data.authorList = Arrays
						.asList(StringUtils.split(Localization.format("presets.metadata.Authors"), ','));
			}
		}
	}

	@EventHandler
	public void loadCompleted(@Nonnull final FMLLoadCompleteEvent event) {
		proxy.loadCompleted(event);
	}

	////////////////////////
	//
	// Client state events
	//
	////////////////////////
	@SubscribeEvent
	public void playerLogin(final PlayerLoggedInEvent event) {
		if (ModOptions.enableVersionChecking)
			new VersionChecker(Presets.MOD_ID, "msg.NewVersion.presets").playerLogin(event);
	}

}
