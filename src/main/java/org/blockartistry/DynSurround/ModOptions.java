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

package org.blockartistry.DynSurround;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.data.Profiles;
import org.blockartistry.lib.ConfigProcessor;
import org.blockartistry.lib.VersionHelper;
import org.blockartistry.lib.ConfigProcessor.Comment;
import org.blockartistry.lib.ConfigProcessor.DefaultValue;
import org.blockartistry.lib.ConfigProcessor.Hidden;
import org.blockartistry.lib.ConfigProcessor.LangKey;
import org.blockartistry.lib.ConfigProcessor.RangeFloat;
import org.blockartistry.lib.ConfigProcessor.RangeInt;
import org.blockartistry.lib.ConfigProcessor.Parameter;
import org.blockartistry.lib.ConfigProcessor.RestartRequired;
import com.google.common.collect.ImmutableList;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public final class ModOptions {

	// Various version breaks for upgrading options automatically
	private static final String VERSION_A = "3.2.4.0";

	// Trace bits for debugging
	public static class Trace {
		public static final int TRUE_SOUND_VOLUME = 0x1;
		public static final int TICK_PROFILE = 0x2;
	};

	private ModOptions() {
	}

	public static final String CATEGORY_ASM = "asm";
	public static final String CONFIG_ENABLE_SOUND_CACHING = "Enable Sound Caching";
	public static final String CONFIG_ENABLE_WEATHER = "Enable Weather Control";
	public static final String CONFIG_ENABLE_RESET_WEATHER_ON_SLEEP = "Enable Weather Reset on Sleep Control";

	@Parameter(category = CATEGORY_ASM, property = CONFIG_ENABLE_SOUND_CACHING)
	@DefaultValue("true")
	@LangKey("cfg.asm.EnableSoundCache")
	@Comment("Enable ASM transformations to permit sound caching")
	@RestartRequired(server = true)
	public static boolean enableSoundCache = true;

	@Parameter(category = CATEGORY_ASM, property = CONFIG_ENABLE_WEATHER)
	@DefaultValue("true")
	@LangKey("cfg.asm.EnableWeather")
	@Comment("Enable ASM transformations to permit weather (rain, snow, splash, dust storms, auroras)")
	@RestartRequired(server = true)
	public static boolean enableWeatherASM = true;

	@Parameter(category = CATEGORY_ASM, property = CONFIG_ENABLE_RESET_WEATHER_ON_SLEEP)
	@DefaultValue("true")
	@LangKey("cfg.asm.EnableSleepReset")
	@Comment("Enable ASM transformations to allow control of player sleep impact on weather reset")
	@RestartRequired(server = true)
	public static boolean enableResetOnSleepASM = true;

	public static final String CATEGORY_LOGGING_CONTROL = "logging";
	public static final String CONFIG_ENABLE_DEBUG_LOGGING = "Enable Debug Logging";
	public static final String CONFIG_ENABLE_ONLINE_VERSION_CHECK = "Enable Online Version Check";
	public static final String CONFIG_ENABLE_DEBUG_DIALOG = "Enable Debug Dialog";
	public static final String CONFIG_REPORT_SERVER_STATS = "Report Server Stats";
	public static final String CONFIG_DEBUG_FLAG_MASK = "Debug Flag Mask";
	private static final List<String> loggingSort = Arrays.asList(CONFIG_ENABLE_ONLINE_VERSION_CHECK,
			CONFIG_ENABLE_DEBUG_LOGGING, CONFIG_REPORT_SERVER_STATS, CONFIG_DEBUG_FLAG_MASK);

	@Parameter(category = CATEGORY_LOGGING_CONTROL, property = CONFIG_ENABLE_DEBUG_LOGGING)
	@DefaultValue("false")
	@LangKey("cfg.logging.EnableDebug")
	@Comment("Enables/disables debug logging of the mod")
	@RestartRequired
	public static boolean enableDebugLogging = false;

	@Parameter(category = CATEGORY_LOGGING_CONTROL, property = CONFIG_ENABLE_ONLINE_VERSION_CHECK)
	@DefaultValue("true")
	@LangKey("cfg.logging.VersionCheck")
	@Comment("Enables/disables display of version check information")
	@RestartRequired
	public static boolean enableVersionChecking = true;

	@Parameter(category = CATEGORY_LOGGING_CONTROL, property = CONFIG_ENABLE_DEBUG_DIALOG)
	@DefaultValue("false")
	@LangKey("cfg.logging.DebugDialog")
	@Comment("Enables/disables display of debug dialog")
	@RestartRequired
	public static boolean showDebugDialog = false;

	@Parameter(category = CATEGORY_LOGGING_CONTROL, property = CONFIG_REPORT_SERVER_STATS)
	@DefaultValue("false")
	@LangKey("cfg.logging.ServerStats")
	@Comment("Enables/disables reporting of server stats")
	public static boolean reportServerStats = false;

	@Parameter(category = CATEGORY_LOGGING_CONTROL, property = CONFIG_DEBUG_FLAG_MASK)
	@DefaultValue("0")
	@LangKey("cfg.logging.FlagMask")
	@Comment("Bitmask for toggling various debug traces")
	@Hidden
	public static int debugFlagMask = 0;

	public static final String CATEGORY_RAIN = "rain";
	public static final String CONFIG_VANILLA_RAIN = "Use Vanilla Algorithms";
	public static final String CONFIG_RAIN_PARTICLE_BASE = "Particle Count Base";
	public static final String CONFIG_ALLOW_DESERT_DUST = "Desert DustJetEffect";
	public static final String CONFIG_RESET_RAIN_ON_SLEEP = "Reset Rain on Sleep";
	public static final String CONFIG_RAIN_ACTIVE_TIME_CONST = "Active duration of rain, constant";
	public static final String CONFIG_RAIN_ACTIVE_TIME_VARIABLE = "Active duration of rain, variable";
	public static final String CONFIG_RAIN_INACTIVE_TIME_CONST = "Inactive duration of rain, constant";
	public static final String CONFIG_RAIN_INACTIVE_TIME_VARIABLE = "Inactive duration of rain, variable";
	public static final String CONFIG_STORM_ACTIVE_TIME_CONST = "Active duration of thunder, constant";
	public static final String CONFIG_STORM_ACTIVE_TIME_VARIABLE = "Active duration of thunder, variable";
	public static final String CONFIG_STORM_INACTIVE_TIME_CONST = "Inactive duration of thunder, constant";
	public static final String CONFIG_STORM_INACTIVE_TIME_VARIABLE = "Inactive duration of thunder, variable";
	public static final String CONFIG_ENABLE_BACKGROUND_THUNDER = "Enable Background Thunder";
	public static final String CONFIG_THUNDER_THRESHOLD = "Rain Intensity for Background Thunder";
	public static final String CONFIG_RAIN_RIPPLE_STYLE = "Style of rain water ripple";

	private static final List<String> rainSort = Arrays.asList(CONFIG_VANILLA_RAIN, CONFIG_RAIN_RIPPLE_STYLE,
			CONFIG_ALLOW_DESERT_DUST, CONFIG_RESET_RAIN_ON_SLEEP, CONFIG_RAIN_PARTICLE_BASE,
			CONFIG_RAIN_ACTIVE_TIME_CONST, CONFIG_RAIN_ACTIVE_TIME_VARIABLE, CONFIG_RAIN_INACTIVE_TIME_CONST,
			CONFIG_RAIN_INACTIVE_TIME_VARIABLE, CONFIG_STORM_ACTIVE_TIME_CONST, CONFIG_STORM_ACTIVE_TIME_VARIABLE,
			CONFIG_STORM_INACTIVE_TIME_CONST, CONFIG_STORM_INACTIVE_TIME_VARIABLE, CONFIG_ENABLE_BACKGROUND_THUNDER,
			CONFIG_THUNDER_THRESHOLD);

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_VANILLA_RAIN)
	@DefaultValue("false")
	@LangKey("cfg.rain.VanillaRain")
	@Comment("Let Vanilla handle rain intensity and time windows")
	@RestartRequired
	public static boolean doVanillaRain = false;

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_RAIN_RIPPLE_STYLE)
	@DefaultValue("0")
	@LangKey("cfg.rain.RippleStyle")
	@RangeInt(min = 0, max = 2)
	@Comment("0: original round, 1: darker round, 2: square")
	public static int rainRippleStyle = 0;

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_RAIN_PARTICLE_BASE)
	@DefaultValue("100")
	@LangKey("cfg.rain.ParticleCount")
	@RangeInt(min = 0, max = 500)
	@Comment("Base count of rain splash particles to generate per tick")
	public static int particleCountBase = 100;

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_RESET_RAIN_ON_SLEEP)
	@DefaultValue("true")
	@LangKey("cfg.rain.ResetOnSleep")
	@Comment("Reset rain/thunder when all players sleep")
	public static boolean resetRainOnSleep = true;

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_RAIN_ACTIVE_TIME_CONST)
	@DefaultValue("12000")
	@LangKey("cfg.rain.ActiveTimeConst")
	@RangeInt(min = 0)
	@Comment("Base time rain is active, in ticks")
	public static int rainActiveTimeConst = 12000;

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_RAIN_ACTIVE_TIME_VARIABLE)
	@DefaultValue("12000")
	@LangKey("cfg.rain.ActiveTimeVariable")
	@RangeInt(min = 0)
	@Comment("Variable amount of ticks rain is active, added to the base")
	public static int rainActiveTimeVariable = 12000;

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_RAIN_INACTIVE_TIME_CONST)
	@DefaultValue("12000")
	@LangKey("cfg.rain.InactiveTimeConst")
	@RangeInt(min = 0)
	@Comment("Base time rain is inactive, in ticks")
	public static int rainInactiveTimeConst = 12000;

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_RAIN_INACTIVE_TIME_VARIABLE)
	@DefaultValue("168000")
	@LangKey("cfg.rain.InactiveTimeVariable")
	@RangeInt(min = 0)
	@Comment("Variable amount of ticks rain is inactive, added to the base")
	public static int rainInactiveTimeVariable = 168000;

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_STORM_ACTIVE_TIME_CONST)
	@DefaultValue("3600")
	@LangKey("cfg.rain.StormActiveTimeConst")
	@RangeInt(min = 0)
	@Comment("Base time storm (thunder) is active, in ticks")
	public static int stormActiveTimeConst = 3600;

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_STORM_ACTIVE_TIME_VARIABLE)
	@DefaultValue("12000")
	@LangKey("cfg.rain.StormActiveTimeVariable")
	@RangeInt(min = 0)
	@Comment("Variable amount of ticks storm (thunder) is active, added to the base")
	public static int stormActiveTimeVariable = 12000;

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_STORM_INACTIVE_TIME_CONST)
	@DefaultValue("12000")
	@LangKey("cfg.rain.StormInactiveTimeConst")
	@RangeInt(min = 0)
	@Comment("Base time storm (thunder) is inactive, in ticks")
	public static int stormInactiveTimeConst = 12000;

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_STORM_INACTIVE_TIME_VARIABLE)
	@DefaultValue("168000")
	@LangKey("cfg.rain.StormInactiveTimeVariable")
	@RangeInt(min = 0)
	@Comment("Variable amount of ticks storm (thunder) is inactive, added to the base")
	public static int stormInactiveTimeVariable = 12000;

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_ENABLE_BACKGROUND_THUNDER)
	@DefaultValue("true")
	@LangKey("cfg.rain.EnableThunder")
	@Comment("Allow background thunder when storming")
	public static boolean allowBackgroundThunder = true;

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_THUNDER_THRESHOLD)
	@DefaultValue("0.75")
	@LangKey("cfg.rain.ThunderThreshold")
	@RangeFloat(min = 0)
	@Comment("Minimum rain intensity level for background thunder to occur")
	public static float stormThunderThreshold = 0.75F;

	public static final String CATEGORY_FOG = "fog";
	public static final String CONFIG_ALLOW_DESERT_FOG = "Desert Fog";
	public static final String CONFIG_DESERT_FOG_FACTOR = "Desert Fog Factor";
	public static final String CONFIG_ENABLE_ELEVATION_HAZE = "Elevation Haze";
	public static final String CONFIG_ELEVATION_HAZE_FACTOR = "Elevation Haze Factor";
	public static final String CONFIG_ELEVATION_HAZE_AS_BAND = "Elevation Haze as Band";
	public static final String CONFIG_ENABLE_BIOME_FOG = "Biomes Fog";
	public static final String CONFIG_BIOME_FOG_FACTOR = "Biomes Fog Factor";
	private static final List<String> fogSort = Arrays.asList(CONFIG_ALLOW_DESERT_FOG, CONFIG_DESERT_FOG_FACTOR,
			CONFIG_ENABLE_BIOME_FOG, CONFIG_BIOME_FOG_FACTOR, CONFIG_ENABLE_ELEVATION_HAZE,
			CONFIG_ELEVATION_HAZE_FACTOR, CONFIG_ELEVATION_HAZE_AS_BAND);

	@Parameter(category = CATEGORY_FOG, property = CONFIG_ALLOW_DESERT_FOG)
	@DefaultValue("true")
	@LangKey("cfg.fog.DesertFog")
	@Comment("Enable/disable desert fog when raining")
	public static boolean allowDesertFog = true;

	@Parameter(category = CATEGORY_FOG, property = CONFIG_DESERT_FOG_FACTOR)
	@DefaultValue("1.0")
	@LangKey("cfg.fog.DesertFogFactor")
	@RangeFloat(min = 0.0F, max = 1.0F)
	@Comment("Visibility factor to apply to desert fog (higher is thicker)")
	public static float desertFogFactor = 1.0F;

	@Parameter(category = CATEGORY_FOG, property = CONFIG_ENABLE_ELEVATION_HAZE)
	@DefaultValue("true")
	@LangKey("cfg.fog.ElevationHaze")
	@Comment("Higher the player elevation the more haze that is experienced")
	public static boolean enableElevationHaze = true;

	@Parameter(category = CATEGORY_FOG, property = CONFIG_ELEVATION_HAZE_FACTOR)
	@DefaultValue("1.0")
	@LangKey("cfg.fog.ElevationHazFactor")
	@RangeFloat(min = 0.0F, max = 1.4F)
	@Comment("Visibility factor to apply to elevation haze (higher is thicker)")
	public static float elevationHazeFactor = 1.0F;

	@Parameter(category = CATEGORY_FOG, property = CONFIG_ELEVATION_HAZE_AS_BAND)
	@DefaultValue("true")
	@LangKey("cfg.fog.ElevationHazeBand")
	@Comment("Calculate haze as a band at cloud height rather than gradient to build height")
	public static boolean elevationHazeAsBand = true;

	@Parameter(category = CATEGORY_FOG, property = CONFIG_ENABLE_BIOME_FOG)
	@DefaultValue("true")
	@LangKey("cfg.fog.BiomeFog")
	@Comment("Enable biome specific fog density and color")
	public static boolean enableBiomeFog = true;

	@Parameter(category = CATEGORY_FOG, property = CONFIG_BIOME_FOG_FACTOR)
	@DefaultValue("1.0")
	@LangKey("cfg.fog.BiomeFogFactor")
	@RangeFloat(min = 0.0F, max = 1.9F)
	@Comment("Visibility factor to apply to biome fog (higher is thicker)")
	public static float biomeFogFactor = 1.0F;

	public static final String CATEGORY_GENERAL = "general";
	public static final String CONFIG_EXTERNAL_SCRIPTS = "External Configuration Files";
	public static final String CONFIG_MIN_RAIN_STRENGTH = "Default Minimum Rain Strength";
	public static final String CONFIG_MAX_RAIN_STRENGTH = "Default Maximum Rain Strength";
	public static final String CONFIG_FX_RANGE = "Special Effect Range";
	public static final String CONFIG_DISABLE_SUSPEND = "Disable Water Suspend Particles";
	public static final String CONFIG_STARTUP_SOUND_LIST = "Startup Sound List";
	public static final String CONFIG_HIDE_CHAT_NOTICES = "Hide Chat Notices";
	private static final List<String> generalSort = ImmutableList.<String>builder()
			.add(CONFIG_HIDE_CHAT_NOTICES, CONFIG_DISABLE_SUSPEND, CONFIG_FX_RANGE, CONFIG_MIN_RAIN_STRENGTH,
					CONFIG_MAX_RAIN_STRENGTH, CONFIG_EXTERNAL_SCRIPTS, CONFIG_STARTUP_SOUND_LIST)
			.build();

	@Parameter(category = CATEGORY_GENERAL, property = CONFIG_HIDE_CHAT_NOTICES)
	@DefaultValue("false")
	@LangKey("cfg.general.HideChat")
	@Comment("Toggles display of Dynamic Surroundings chat notices")
	public static boolean hideChatNotices = false;

	@Parameter(category = CATEGORY_GENERAL, property = CONFIG_DISABLE_SUSPEND)
	@DefaultValue("false")
	@LangKey("cfg.general.Suspend")
	@Comment("Enable/disable water depth particle effect")
	@RestartRequired(server = true)
	public static boolean disableWaterSuspendParticle = false;

	@Parameter(category = CATEGORY_GENERAL, property = CONFIG_MIN_RAIN_STRENGTH)
	@DefaultValue("0.0")
	@LangKey("cfg.general.MinRainStrength")
	@RangeFloat(min = 0.0F, max = 1.0F)
	@Comment("Default minimum rain strength for a dimension")
	public static float defaultMinRainStrength = 0.0F;

	@Parameter(category = CATEGORY_GENERAL, property = CONFIG_MAX_RAIN_STRENGTH)
	@DefaultValue("1.0")
	@LangKey("cfg.general.MaxRainStrength")
	@RangeFloat(min = 0.0F, max = 1.0F)
	@Comment("Default maximum rain strength for a dimension")
	public static float defaultMaxRainStrength = 1.0F;

	@Parameter(category = CATEGORY_GENERAL, property = CONFIG_FX_RANGE)
	@DefaultValue("24")
	@LangKey("cfg.general.FXRange")
	@RangeInt(min = 16, max = 64)
	@Comment("Block radius/range around player for special effect application")
	public static int specialEffectRange = 24;

	@Parameter(category = CATEGORY_GENERAL, property = CONFIG_EXTERNAL_SCRIPTS)
	@DefaultValue("")
	@LangKey("cfg.general.ExternalScripts")
	@Comment("Configuration files for customization")
	public static String[] externalScriptFiles = {};

	@Parameter(category = CATEGORY_GENERAL, property = CONFIG_STARTUP_SOUND_LIST)
	@DefaultValue("minecraft:entity.experience_orb.pickup,minecraft:entity.chicken.egg")
	@LangKey("cfg.general.StartupSounds")
	@Comment("Possible sounds to play when client reaches main game menu")
	public static String[] startupSoundList = { "minecraft:entity.experience_orb.pickup",
			"minecraft:entity.chicken.egg" };

	public static final String CATEGORY_AURORA = "aurora";
	public static final String CONFIG_AURORA_ENABLED = "Enabled";
	public static final String CONFIG_AURORA_SHADER = "Use Shaders";
	private static final List<String> auroraSort = Arrays.asList(CONFIG_AURORA_ENABLED, CONFIG_AURORA_SHADER);

	@Parameter(category = CATEGORY_AURORA, property = CONFIG_AURORA_ENABLED)
	@DefaultValue("true")
	@LangKey("cfg.aurora.EnableAurora")
	@Comment("Enable/disable Aurora processing on server/client")
	public static boolean auroraEnable = true;

	@Parameter(category = CATEGORY_AURORA, property = CONFIG_AURORA_SHADER)
	@DefaultValue("true")
	@LangKey("cfg.aurora.EnableShader")
	@Comment("Use shader when rendering aurora")
	@RestartRequired(world = true)
	public static boolean auroraUseShader = true;

	public static final String CATEGORY_BIOMES = "biomes";
	public static final String CONFIG_BIOME_SEALEVEL = "Overworld Sealevel Override";
	public static final String CONFIG_BIOME_ALIASES = "Biomes Alias";
	private static final List<String> biomesSort = Arrays.asList(CONFIG_BIOME_SEALEVEL, CONFIG_BIOME_ALIASES);

	@Parameter(category = CATEGORY_BIOMES, property = CONFIG_BIOME_SEALEVEL)
	@DefaultValue("0")
	@LangKey("cfg.biomes.Sealevel")
	@RangeInt(min = 0, max = 255)
	@Comment("Sealevel to set for Overworld (0 use default for World)")
	public static int worldSealevelOverride = 0;

	@Parameter(category = CATEGORY_BIOMES, property = CONFIG_BIOME_ALIASES)
	@DefaultValue("")
	@LangKey("cfg.biomes.Aliases")
	@Comment("Biomes alias list")
	public static String[] biomeAliases = {};

	public static final String CATEGORY_BLOCK = "block";

	public static final String CATEGORY_BLOCK_EFFECTS = "block.effects";
	public static final String CONFIG_BLOCK_EFFECT_STEAM = "Enable Steam";
	public static final String CONFIG_BLOCK_EFFECT_FIRE = "Enable FireJetEffect Jets";
	public static final String CONFIG_BLOCK_EFFECT_BUBBLE = "Enable Bubbles";
	public static final String CONFIG_BLOCK_EFFECT_DUST = "Enable DustJetEffect Motes";
	public static final String CONFIG_BLOCK_EFFECT_FOUNTAIN = "Enable FountainJetEffect";
	public static final String CONFIG_BLOCK_EFFECT_FIREFLY = "Enable Fireflies";
	public static final String CONFIG_BLOCK_EFFECT_SPLASH = "Enable Water Splash";

	@Parameter(category = CATEGORY_BLOCK_EFFECTS, property = CONFIG_BLOCK_EFFECT_STEAM)
	@DefaultValue("true")
	@LangKey("cfg.block.effects.Steam")
	@Comment("Enable Steam Jets where lava meets water")
	public static boolean enableSteamJets = true;

	@Parameter(category = CATEGORY_BLOCK_EFFECTS, property = CONFIG_BLOCK_EFFECT_FIRE)
	@DefaultValue("true")
	@LangKey("cfg.block.effects.Fire")
	@Comment("Enable FireJetEffect Jets in lava")
	public static boolean enableFireJets = true;

	@Parameter(category = CATEGORY_BLOCK_EFFECTS, property = CONFIG_BLOCK_EFFECT_BUBBLE)
	@DefaultValue("true")
	@LangKey("cfg.block.effects.Bubble")
	@Comment("Enable BubbleJetEffect Jets under water")
	public static boolean enableBubbleJets = true;

	@Parameter(category = CATEGORY_BLOCK_EFFECTS, property = CONFIG_BLOCK_EFFECT_DUST)
	@DefaultValue("true")
	@LangKey("cfg.block.effects.Dust")
	@Comment("Enable DustJetEffect motes dropping from blocks")
	public static boolean enableDustJets = true;

	@Parameter(category = CATEGORY_BLOCK_EFFECTS, property = CONFIG_BLOCK_EFFECT_FOUNTAIN)
	@DefaultValue("true")
	@LangKey("cfg.block.effects.Fountain")
	@Comment("Enable FountainJetEffect jets")
	public static boolean enableFountainJets = true;

	@Parameter(category = CATEGORY_BLOCK_EFFECTS, property = CONFIG_BLOCK_EFFECT_FIREFLY)
	@DefaultValue("true")
	@LangKey("cfg.block.effects.Fireflies")
	@Comment("Enable Firefly effect around plants")
	public static boolean enableFireflies = true;

	@Parameter(category = CATEGORY_BLOCK_EFFECTS, property = CONFIG_BLOCK_EFFECT_SPLASH)
	@DefaultValue("true")
	@LangKey("cfg.block.effects.Splash")
	@Comment("Enable Water Splash effects when water spills down")
	public static boolean enableWaterSplash = true;

	public static final String CATEGORY_SOUND = "sound";
	public static final String CONFIG_ENABLE_BIOME_SOUNDS = "Enable Biomes Sounds";
	public static final String CONFIG_MASTER_SOUND_FACTOR = "Master Sound Scale Factor";
	public static final String CONFIG_AUTO_CONFIG_CHANNELS = "Autoconfigure Channels";
	public static final String CONFIG_NORMAL_CHANNEL_COUNT = "Number Normal Channels";
	public static final String CONFIG_STREAMING_CHANNEL_COUNT = "Number Streaming Channels";
	public static final String CONFIG_STREAM_BUFFER_SIZE = "Stream Buffer Size";
	public static final String CONFIG_STREAM_BUFFER_COUNT = "Number of Stream Buffers per Channel";
	public static final String CONFIG_MUTE_WHEN_BACKGROUND = "Mute when Background";
	public static final String CONFIG_ENABLE_JUMP_SOUND = "Jump Sound";
	public static final String CONFIG_ENABLE_EQUIP_SOUND = "Equip Sound";
	public static final String CONFIG_SWORD_AS_TOOL_EQUIP_SOUND = "Sword Equip as Tool";
	public static final String CONFIG_ENABLE_CRAFTING_SOUND = "Crafting Sound";
	public static final String CONFIG_FOOTSTEPS_SOUND_FACTOR = "Footsteps Sound Factor";
	public static final String CONFIG_FOOTSTEPS_QUAD = "Footsteps as Quadruped";
	public static final String CONFIG_ENABLE_ARMOR_SOUND = "Armor Sound";
	public static final String CONFIG_SOUND_CULL_THRESHOLD = "Sound Culling Threshold";
	public static final String CONFIG_CULLED_SOUNDS = "Culled Sounds";
	public static final String CONFIG_BLOCKED_SOUNDS = "Blocked Sounds";
	public static final String CONFIG_SOUND_VOLUMES = "Sound Volume";
	public static final String CONFIG_THUNDER_VOLUME = "Thunder Volume";
	public static final String CONFIG_ENABLE_BATTLEMUSIC = "Battle Music";
	private static final List<String> soundsSort = Arrays.asList(CONFIG_ENABLE_BIOME_SOUNDS, CONFIG_MASTER_SOUND_FACTOR,
			CONFIG_FOOTSTEPS_SOUND_FACTOR, CONFIG_FOOTSTEPS_QUAD, CONFIG_ENABLE_ARMOR_SOUND, CONFIG_ENABLE_JUMP_SOUND,
			CONFIG_ENABLE_EQUIP_SOUND, CONFIG_SWORD_AS_TOOL_EQUIP_SOUND, CONFIG_ENABLE_CRAFTING_SOUND,
			CONFIG_AUTO_CONFIG_CHANNELS, CONFIG_NORMAL_CHANNEL_COUNT, CONFIG_STREAMING_CHANNEL_COUNT,
			CONFIG_STREAM_BUFFER_SIZE, CONFIG_STREAM_BUFFER_COUNT, CONFIG_MUTE_WHEN_BACKGROUND, CONFIG_THUNDER_VOLUME,
			CONFIG_BLOCKED_SOUNDS, CONFIG_SOUND_CULL_THRESHOLD, CONFIG_CULLED_SOUNDS, CONFIG_SOUND_VOLUMES,
			CONFIG_ENABLE_BATTLEMUSIC);

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_ENABLE_BIOME_SOUNDS)
	@DefaultValue("true")
	@LangKey("cfg.sound.BiomeSounds")
	@Comment("Enable biome background and spot sounds")
	public static boolean enableBiomeSounds = true;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_MASTER_SOUND_FACTOR)
	@DefaultValue("1.0")
	@LangKey("cfg.sound.MasterScale")
	@RangeFloat(min = 0.0F, max = 1.0F)
	@Comment("Master volume scale factor for biome and block sounds")
	public static float masterSoundScaleFactor = 1.0F;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_AUTO_CONFIG_CHANNELS)
	@DefaultValue("true")
	@LangKey("cfg.sound.AutoConfig")
	@Comment("Automatically configure sound channels")
	@RestartRequired(server = true)
	public static boolean autoConfigureChannels = true;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_NORMAL_CHANNEL_COUNT)
	@DefaultValue("28")
	@LangKey("cfg.sound.NormalChannels")
	@RangeInt(min = 28, max = 255)
	@Comment("Number of normal sound channels to configure in the sound system (manual)")
	@RestartRequired(server = true)
	public static int normalSoundChannelCount = 28;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_STREAMING_CHANNEL_COUNT)
	@DefaultValue("4")
	@LangKey("cfg.sound.StreamingChannels")
	@RangeInt(min = 4, max = 255)
	@Comment("Number of streaming sound channels to configure in the sound system (manual)")
	@RestartRequired(server = true)
	public static int streamingSoundChannelCount = 4;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_STREAM_BUFFER_SIZE)
	@DefaultValue("0")
	@LangKey("cfg.sound.StreamBufferSize")
	@RangeInt(min = 0)
	@Comment("Size of a stream buffer in kilobytes (0: system default - usually 128K bytes)")
	@RestartRequired(server = true)
	public static int streamBufferSize = 0;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_STREAM_BUFFER_COUNT)
	@DefaultValue("0")
	@LangKey("cfg.sound.StreamBufferCount")
	@RangeInt(min = 0, max = 8)
	@Comment("Number of stream buffers per channel (0: system default - usually 3 buffers)")
	@RestartRequired(server = true)
	public static int streamBufferCount = 0;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_MUTE_WHEN_BACKGROUND)
	@DefaultValue("true")
	@LangKey("cfg.sound.Mute")
	@Comment("Mute sound when Minecraft is in the background")
	public static boolean muteWhenBackground = true;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_THUNDER_VOLUME)
	@DefaultValue("10000")
	@LangKey("cfg.sound.ThunderVolume")
	@Comment("Sound Volume of Thunder")
	@RangeFloat(min = 15F, max = 10000F)
	public static float thunderVolume = 10000F;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_ENABLE_JUMP_SOUND)
	@DefaultValue("true")
	@LangKey("cfg.sound.Jump")
	@Comment("Enable player Jump sound effect")
	public static boolean enableJumpSound = true;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_ENABLE_EQUIP_SOUND)
	@DefaultValue("true")
	@LangKey("cfg.sound.Equip")
	@Comment("Enable Weapon/Tool Equip sound effect")
	public static boolean enableEquipSound = true;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_SWORD_AS_TOOL_EQUIP_SOUND)
	@DefaultValue("false")
	@LangKey("cfg.sound.SwordEquipAsTool")
	@Comment("Enable Sword Equip sound as Tool")
	public static boolean swordEquipAsTool = false;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_ENABLE_CRAFTING_SOUND)
	@DefaultValue("true")
	@LangKey("cfg.sound.Craft")
	@Comment("Enable Item Crafted sound effect")
	public static boolean enableCraftingSound = true;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_FOOTSTEPS_SOUND_FACTOR)
	@DefaultValue("0.35")
	@LangKey("cfg.sound.FootstepScale")
	@RangeFloat(min = 0.0F, max = 1.0F)
	@Comment("Volume scale factor for footstep sounds")
	public static float footstepsSoundFactor = 0.35F;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_FOOTSTEPS_QUAD)
	@DefaultValue("false")
	@LangKey("cfg.sound.FootstepQuad")
	@Comment("Simulate quadruped with Footstep effects (horse)")
	public static boolean foostepsQuadruped = false;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_ENABLE_ARMOR_SOUND)
	@DefaultValue("true")
	@LangKey("cfg.sound.Armor")
	@Comment("Enable/disable armor sounds when moving")
	public static boolean enableArmorSounds = true;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_SOUND_CULL_THRESHOLD)
	@DefaultValue("20")
	@LangKey("cfg.sound.CullInterval")
	@RangeInt(min = 0)
	@Comment("Ticks between culled sound events (0 to disable culling)")
	public static int soundCullingThreshold = 20;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_ENABLE_BATTLEMUSIC)
	@DefaultValue("false")
	@LangKey("cfg.sound.BattleMusic")
	@Comment("Enable/disable Battle Music")
	public static boolean enableBattleMusic = false;

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_CULLED_SOUNDS)
	@DefaultValue("minecraft:block.water.ambient,minecraft:block.lava.ambient,minecraft:entity.sheep.ambient,minecraft:entity.chicken.ambient,minecraft:entity.cow.ambient,minecraft:entity.pig.ambient")
	@LangKey("cfg.sound.CulledSounds")
	@Comment("Sounds to cull from frequent playing")
	@Hidden
	public static String[] culledSounds = { "minecraft:block.water.ambient", "minecraft:block.lava.ambient",
			"minecraft:entity.sheep.ambient", "minecraft:entity.chicken.ambient", "minecraft:entity.cow.ambient",
			"minecraft:entity.pig.ambient" };

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_BLOCKED_SOUNDS)
	@DefaultValue("dsurround:bison")
	@LangKey("cfg.sound.BlockedSounds")
	@Comment("Sounds to block from playing")
	@Hidden
	public static String[] blockedSounds = { "dsurround:bison" };

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_SOUND_VOLUMES)
	@DefaultValue("")
	@LangKey("cfg.sound.SoundVolumes")
	@Comment("Individual sound volume scaling factors")
	@Hidden
	public static String[] soundVolumes = {};

	public static final String CATEGORY_PLAYER = "player";
	public static final String CONFIG_SUPPRESS_POTION_PARTICLES = "Suppress Potion Particles";
	public static final String CONFIG_ENABLE_POPOFFS = "Damage Popoffs";
	public static final String CONFIG_SHOW_CRIT_WORDS = "Show Crit Words";
	public static final String CONFIG_HURT_THRESHOLD = "Hurt Threshold";
	public static final String CONFIG_HUNGER_THRESHOLD = "Hunger Threshold";
	public static final String CONFIG_ENABLE_FOOTPRINTS = "Footprints";
	public static final String CONFIG_FOOTPRINT_STYLE = "Footprint Style";
	public static final String CONFIG_SHOW_BREATH = "Show Frost Breath";
	private static final List<String> playerSort = Arrays.asList(CONFIG_SUPPRESS_POTION_PARTICLES,
			CONFIG_ENABLE_POPOFFS, CONFIG_SHOW_CRIT_WORDS, CONFIG_ENABLE_FOOTPRINTS, CONFIG_FOOTPRINT_STYLE,
			CONFIG_HURT_THRESHOLD, CONFIG_HUNGER_THRESHOLD, CONFIG_SHOW_BREATH);

	@Parameter(category = CATEGORY_PLAYER, property = CONFIG_SUPPRESS_POTION_PARTICLES)
	@DefaultValue("false")
	@LangKey("cfg.player.PotionParticles")
	@Comment("Suppress player's potion particles from rendering")
	public static boolean suppressPotionParticles = false;

	@Parameter(category = CATEGORY_PLAYER, property = CONFIG_ENABLE_POPOFFS)
	@DefaultValue("true")
	@LangKey("cfg.player.Popoffs")
	@Comment("Controls display of damage pop-offs when an entity is damaged")
	public static boolean enableDamagePopoffs = true;

	@Parameter(category = CATEGORY_PLAYER, property = CONFIG_SHOW_CRIT_WORDS)
	@DefaultValue("true")
	@LangKey("cfg.player.CritWords")
	@Comment("Display random power word on critical hit")
	public static boolean showCritWords = true;

	@Parameter(category = CATEGORY_PLAYER, property = CONFIG_ENABLE_FOOTPRINTS)
	@DefaultValue("true")
	@LangKey("cfg.player.Footprints")
	@Comment("Enable player footprints")
	public static boolean enableFootprints = true;

	@Parameter(category = CATEGORY_PLAYER, property = CONFIG_FOOTPRINT_STYLE)
	@DefaultValue("6")
	@LangKey("cfg.player.FootprintStyle")
	@Comment("0: shoe print, 1: square print, 2: horse hoof, 3: bird, 4: paw, 5: solid square, 6: lowres square")
	@RangeInt(min = 0, max = 6)
	public static int footprintStyle = 6;

	@Parameter(category = CATEGORY_PLAYER, property = CONFIG_HURT_THRESHOLD)
	@DefaultValue("8")
	@LangKey("cfg.player.HurtThreshold")
	@Comment("Amount of health bar remaining to trigger player hurt sound (0 disable)")
	@RangeInt(min = 0, max = 10)
	public static int playerHurtThreshold = 8;

	@Parameter(category = CATEGORY_PLAYER, property = CONFIG_HUNGER_THRESHOLD)
	@DefaultValue("8")
	@LangKey("cfg.player.HungerThreshold")
	@Comment("Amount of food bar remaining to trigger player hunger sound (0 disable)")
	@RangeInt(min = 0, max = 10)
	public static int playerHungerThreshold = 8;

	@Parameter(category = CATEGORY_PLAYER, property = CONFIG_SHOW_BREATH)
	@DefaultValue("true")
	@LangKey("cfg.player.ShowBreath")
	@Comment("Show player frost breath in cold weather")
	public static boolean showBreath = true;

	public static final String CATEGORY_POTION_HUD = "player.potion hud";
	public static final String CONFIG_POTION_HUD_NONE = "No Potion HUD";
	public static final String CONFIG_POTION_HUD_ENABLE = "Enable";
	public static final String CONFIG_POTION_HUD_TRANSPARENCY = "Transparency";
	public static final String CONFIG_POTION_HUD_LEFT_OFFSET = "Horizontal Offset";
	public static final String CONFIG_POTION_HUD_TOP_OFFSET = "Vertical Offset";
	public static final String CONFIG_POTION_HUD_SCALE = "Display Scale";
	public static final String CONFIG_POTION_HUD_ANCHOR = "HUD Location";
	private static final List<String> potionHudSort = Arrays.asList(CONFIG_POTION_HUD_NONE, CONFIG_POTION_HUD_ENABLE,
			CONFIG_POTION_HUD_TRANSPARENCY, CONFIG_POTION_HUD_SCALE, CONFIG_POTION_HUD_ANCHOR,
			CONFIG_POTION_HUD_TOP_OFFSET, CONFIG_POTION_HUD_LEFT_OFFSET);

	@Parameter(category = CATEGORY_POTION_HUD, property = CONFIG_POTION_HUD_NONE)
	@DefaultValue("false")
	@LangKey("cfg.player.potionHud.NoHUD")
	@Comment("Disables Vanilla and Dynamic Surroundings potion HUD")
	public static boolean potionHudNone = false;

	@Parameter(category = CATEGORY_POTION_HUD, property = CONFIG_POTION_HUD_ENABLE)
	@DefaultValue("true")
	@LangKey("cfg.player.potionHud.Enable")
	@Comment("Enable display of potion icons in display")
	public static boolean potionHudEnabled = true;

	@Parameter(category = CATEGORY_POTION_HUD, property = CONFIG_POTION_HUD_TRANSPARENCY)
	@DefaultValue("0.75")
	@LangKey("cfg.player.potionHud.Transparency")
	@RangeFloat(min = 0.0F, max = 1.0F)
	@Comment("Transparency factor for icons (higher more solid)")
	public static float potionHudTransparency = 0.75F;

	@Parameter(category = CATEGORY_POTION_HUD, property = CONFIG_POTION_HUD_LEFT_OFFSET)
	@DefaultValue("5")
	@LangKey("cfg.player.potionHud.LeftOffset")
	@RangeInt(min = 0)
	@Comment("Offset from left side of screen")
	public static int potionHudLeftOffset = 5;

	@Parameter(category = CATEGORY_POTION_HUD, property = CONFIG_POTION_HUD_TOP_OFFSET)
	@DefaultValue("5")
	@LangKey("cfg.player.potionHud.TopOffset")
	@RangeInt(min = 0)
	@Comment("Offset from top of screen")
	public static int potionHudTopOffset = 5;

	@Parameter(category = CATEGORY_POTION_HUD, property = CONFIG_POTION_HUD_SCALE)
	@DefaultValue("0.75")
	@LangKey("cfg.player.potionHud.Scale")
	@RangeFloat(min = 0.0F, max = 1.0F)
	@Comment("Size scale of icons (lower is smaller)")
	public static float potionHudScale = 0.75F;

	@Parameter(category = CATEGORY_POTION_HUD, property = CONFIG_POTION_HUD_ANCHOR)
	@DefaultValue("0")
	@LangKey("cfg.player.potionHud.Location")
	@RangeInt(min = 0, max = 1)
	@Comment("Area of the display the Potion HUD is displayed (0 upper left, 1 upper right)")
	public static int potionHudAnchor = 0;

	public static final String CATEGORY_SPEECHBUBBLES = "speechbubbles";
	public static final String CONFIG_OPTION_ENABLE_SPEECHBUBBLES = "Enable SpeechBubbles";
	public static final String CONFIG_OPTION_ENABLE_ENTITY_CHAT = "Enable Entity Chat";
	public static final String CONFIG_OPTION_ENABLE_EMOJIS = "Enable Entity Emojis";
	public static final String CONFIG_OPTION_SPEECHBUBBLE_DURATION = "Display Duration";
	public static final String CONFIG_OPTION_SPEECHBUBBLE_RANGE = "Visibility Range";

	@Parameter(category = CATEGORY_SPEECHBUBBLES, property = CONFIG_OPTION_ENABLE_SPEECHBUBBLES)
	@DefaultValue("false")
	@LangKey("cfg.speech.EnableSpeechBubbles")
	@Comment("Enables/disables speech bubbles above player heads")
	public static boolean enableSpeechBubbles = false;

	@Parameter(category = CATEGORY_SPEECHBUBBLES, property = CONFIG_OPTION_ENABLE_ENTITY_CHAT)
	@DefaultValue("false")
	@LangKey("cfg.speech.EnableEntityChat")
	@Comment("Enables/disables entity chat bubbles")
	public static boolean enableEntityChat = false;

	@Parameter(category = CATEGORY_SPEECHBUBBLES, property = CONFIG_OPTION_ENABLE_EMOJIS)
	@DefaultValue("false")
	@LangKey("cfg.speech.EnableEntityEmojis")
	@Comment("Enables/disables entity emojis")
	public static boolean enableEntityEmojis = false;

	@Parameter(category = CATEGORY_SPEECHBUBBLES, property = CONFIG_OPTION_SPEECHBUBBLE_DURATION)
	@DefaultValue("7")
	@LangKey("cfg.speech.Duration")
	@RangeFloat(min = 5.0F, max = 15.0F)
	@Comment("Number of seconds to display speech before removing")
	public static float speechBubbleDuration = 7.0F;

	@Parameter(category = CATEGORY_SPEECHBUBBLES, property = CONFIG_OPTION_SPEECHBUBBLE_RANGE)
	@DefaultValue("16")
	@LangKey("cfg.speech.Range")
	@RangeInt(min = 16, max = 32)
	@Comment("Range at which a SpeechBubble is visible.  Filtering occurs server side.")
	public static float speechBubbleRange = 16;

	private static final List<String> speechBubbleSort = Arrays.asList(CONFIG_OPTION_ENABLE_SPEECHBUBBLES,
			CONFIG_OPTION_ENABLE_ENTITY_CHAT, CONFIG_OPTION_ENABLE_EMOJIS, CONFIG_OPTION_SPEECHBUBBLE_DURATION,
			CONFIG_OPTION_SPEECHBUBBLE_RANGE);

	public static final String CATEGORY_EXPLOSIONS = "explosions";
	public static final String CONFIG_ENABLE_EXPLOSIONS = "Enable Explosion Enhancement";
	public static final String CONFIG_ADD_MOB_PARTICLES = "Add Mob Models";

	@Parameter(category = CATEGORY_EXPLOSIONS, property = CONFIG_ENABLE_EXPLOSIONS)
	@DefaultValue("true")
	@LangKey("cfg.explosions.EnableExplosions")
	@Comment("Enables/disables explosion enhancement")
	public static boolean enableExplosionEnhancement = false;

	@Parameter(category = CATEGORY_EXPLOSIONS, property = CONFIG_ADD_MOB_PARTICLES)
	@DefaultValue("false")
	@LangKey("cfg.explosions.AddMobs")
	@Comment("Enables/disables addition of mob models in explosion debris")
	public static boolean addMobParticles = false;

	public static final String CATEGORY_LIGHT_LEVEL = "lightlevel";
	public static final String CONFIG_LL_RANGE = "Block Range";
	public static final String CONFIG_LL_MOB_SPAWN_THRESHOLD = "Mob Spawn Threshold";
	public static final String CONFIG_LL_DISPLAY_MODE = "Display Mode";
	public static final String CONFIG_LL_HIDE_SAFE = "Hide Safe";
	public static final String CONFIG_LL_INDICATE_CAUTION = "Indicate Caution";
	public static final String CONFIG_LL_COLORS = "Color Set";

	private static final List<String> llSort = Arrays.asList(CONFIG_LL_RANGE, CONFIG_LL_MOB_SPAWN_THRESHOLD,
			CONFIG_LL_DISPLAY_MODE, CONFIG_LL_HIDE_SAFE, CONFIG_LL_INDICATE_CAUTION, CONFIG_LL_COLORS);

	@Parameter(category = CATEGORY_LIGHT_LEVEL, property = CONFIG_LL_RANGE)
	@DefaultValue("24")
	@LangKey("cfg.lightlevel.Range")
	@Comment("Range from player to analyze light levels")
	@RangeInt(min = 16, max = 32)
	public static int llBlockRange = 24;

	@Parameter(category = CATEGORY_LIGHT_LEVEL, property = CONFIG_LL_MOB_SPAWN_THRESHOLD)
	@DefaultValue("7")
	@LangKey("cfg.lightlevel.MobSpawnThreshold")
	@Comment("Light level at which mobs can spawn")
	@RangeInt(min = 0, max = 15)
	public static int llSpawnThreshold = 7;

	@Parameter(category = CATEGORY_LIGHT_LEVEL, property = CONFIG_LL_DISPLAY_MODE)
	@DefaultValue("0")
	@LangKey("cfg.lightlevel.DisplayMode")
	@Comment("0: Block Light, 1: Block Light + Sky Light")
	@RangeInt(min = 0, max = 1)
	public static int llDisplayMode = 0;

	@Parameter(category = CATEGORY_LIGHT_LEVEL, property = CONFIG_LL_HIDE_SAFE)
	@DefaultValue("false")
	@LangKey("cfg.lightlevel.HideSafe")
	@Comment("Hide light level information for blocks that are considered safe")
	public static boolean llHideSafe = false;

	@Parameter(category = CATEGORY_LIGHT_LEVEL, property = CONFIG_LL_INDICATE_CAUTION)
	@DefaultValue("true")
	@LangKey("cfg.lightlevel.IndicateCaution")
	@Comment("Indicate current light levels that will change at night which could result in mob spawns")
	public static boolean llIndicateCaution = true;

	@Parameter(category = CATEGORY_LIGHT_LEVEL, property = CONFIG_LL_COLORS)
	@DefaultValue("0")
	@LangKey("cfg.lightlevel.Colors")
	@Comment("Color set: 0 bright, 1 dark")
	@RangeInt(min = 0, max = 1)
	public static int llColors = 0;

	public static final String CATEGORY_COMPASS = "compass";
	public static final String CONFIG_COMPASS_ENABLE = "Enable Compass";
	public static final String CONFIG_CLOCK_ENABLE = "Enable Clock";
	public static final String CONFIG_COMPASS_STYLE = "Compass Style";
	public static final String CONFIG_COMPASS_TRANSPARENCY = "Transparency";
	public static final String CONFIG_COMPASS_COORD_FORMAT = "Cood Format";

	@Parameter(category = CATEGORY_COMPASS, property = CONFIG_COMPASS_ENABLE)
	@DefaultValue("true")
	@LangKey("cfg.compass.Enable")
	@Comment("Enable/disable compass HUD when compass is held")
	public static boolean enableCompass = true;

	@Parameter(category = CATEGORY_COMPASS, property = CONFIG_CLOCK_ENABLE)
	@DefaultValue("true")
	@LangKey("cfg.compass.ClockEnable")
	@Comment("Enable/disable clock HUD when clock is held")
	public static boolean enableClock = true;

	@Parameter(category = CATEGORY_COMPASS, property = CONFIG_COMPASS_STYLE)
	@DefaultValue("0")
	@LangKey("cfg.compass.Style")
	@Comment("Style of compass bar")
	@RangeInt(min = 0, max = 6)
	public static int compassStyle = 0;

	@Parameter(category = CATEGORY_COMPASS, property = CONFIG_COMPASS_TRANSPARENCY)
	@DefaultValue("0.4")
	@LangKey("cfg.compass.Transparency")
	@Comment("Compass transparency")
	@RangeFloat(min = 0F, max = 1.0F)
	public static float compassTransparency = 0.4F;

	@Parameter(category = CATEGORY_COMPASS, property = CONFIG_COMPASS_COORD_FORMAT)
	@DefaultValue("x: %1$d, z: %3$d")
	@LangKey("cfg.compass.Format")
	@Comment("Format string for location coordinates")
	public static String compassCoordFormat = "x: %1$d, z: %3$d";

	private static final List<String> compassSort = Arrays.asList(CONFIG_COMPASS_ENABLE, CONFIG_COMPASS_STYLE,
			CONFIG_COMPASS_TRANSPARENCY);

	public static final String CATEGORY_COMMANDS = "commands";
	public static final String CONFIG_COMMANDS_DS = "commands./ds";
	public static final String CONFIG_COMMANDS_CALC = "commands./calc";
	public static final String CONFIG_COMMAND_NAME = "name";
	public static final String CONFIG_COMMAND_ALIAS = "alias";

	@Parameter(category = CONFIG_COMMANDS_DS, property = CONFIG_COMMAND_NAME)
	@DefaultValue("ds")
	@LangKey("cfg.commands.DS.Name")
	@Comment("Name of the command")
	public static String commandNameDS = "ds";

	@Parameter(category = CONFIG_COMMANDS_DS, property = CONFIG_COMMAND_ALIAS)
	@DefaultValue("dsurround rain")
	@LangKey("cfg.commands.DS.Alias")
	@Comment("Alias for the command")
	public static String commandAliasDS = "dsurround rain";

	@Parameter(category = CONFIG_COMMANDS_CALC, property = CONFIG_COMMAND_NAME)
	@DefaultValue("calc")
	@LangKey("cfg.commands.Calc.Name")
	@Comment("Name of the command")
	public static String commandNameCalc = "calc";

	@Parameter(category = CONFIG_COMMANDS_CALC, property = CONFIG_COMMAND_ALIAS)
	@DefaultValue("c math")
	@LangKey("cfg.commands.Calc.Alias")
	@Comment("Alias for the command")
	public static String commandAliasCalc = "c math";

	public static final String CATEGORY_FEATURES = "features";
	public static final String CONFIG_FEATURES_ALLOW_LLHUD = "Allow Light Level HUD";
	public static final String CONFIG_FEATURES_ALLOW_CHUNKBOARDERS = "Allow Chunk Border HUD";
	public static final String CONFIG_FEATURES_ALLOW_COMPASSCLOCK = "Allow Compass and Clock HUD";

	@Parameter(category = CATEGORY_FEATURES, property = CONFIG_FEATURES_ALLOW_LLHUD)
	@DefaultValue("true")
	@Comment("Allow the Light Level HUD")
	@Hidden
	public static boolean allowLightLevelHUD = true;

	@Parameter(category = CATEGORY_FEATURES, property = CONFIG_FEATURES_ALLOW_CHUNKBOARDERS)
	@DefaultValue("true")
	@Comment("Allow the Chunk Border HUD")
	@Hidden
	public static boolean allowChunkBorderHUD = true;

	@Parameter(category = CATEGORY_FEATURES, property = CONFIG_FEATURES_ALLOW_COMPASSCLOCK)
	@DefaultValue("true")
	@Comment("Allow the Compass and Clock HUD")
	@Hidden
	public static boolean allowCompassClockHUD = true;

	public static final String CATEGORY_PROFILES = "profiles";

	private static void setDefault(@Nonnull final Configuration config, @Nonnull final String cat,
			@Nonnull final String prop, final float prevDefault, final float newDefault) {
		final ConfigCategory cc = config.getCategory(cat);
		if (cc != null) {
			final Property p = cc.get(prop);
			if (p != null) {
				final float cv = (float) p.getDouble();
				if (cv == prevDefault)
					p.set(newDefault);
			}
		}
	}

	public static void load(final Configuration config) {

		// Patch up values from older config if needed
		if (VersionHelper.compareVersions(config.getLoadedConfigVersion(), VERSION_A) < 0) {
			setDefault(config, CATEGORY_SOUND, CONFIG_FOOTSTEPS_SOUND_FACTOR, 0.15F, 0.5F);
		}

		ConfigProcessor.process(config, ModOptions.class);
		if (DSurround.config() != null)
			Profiles.tickle();

		// CATEGORY: asm
		config.setCategoryRequiresMcRestart(CATEGORY_ASM, true);
		config.setCategoryRequiresWorldRestart(CATEGORY_ASM, true);
		config.setCategoryComment(CATEGORY_ASM, "Controls ASM transforms Dynamic Surroundings performs at startup");
		config.setCategoryLanguageKey(CATEGORY_ASM, "cfg.asm.cat.ASM");

		// CATEGORY: Logging
		config.setCategoryRequiresMcRestart(CATEGORY_LOGGING_CONTROL, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_LOGGING_CONTROL, false);
		config.setCategoryComment(CATEGORY_LOGGING_CONTROL, "Defines how Dynamic Surroundings logging will behave");
		config.setCategoryPropertyOrder(CATEGORY_LOGGING_CONTROL, new ArrayList<String>(loggingSort));
		config.setCategoryLanguageKey(CATEGORY_LOGGING_CONTROL, "cfg.logging.cat.Logging");

		// CATEGORY: Rain
		config.setCategoryRequiresMcRestart(CATEGORY_RAIN, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_RAIN, false);
		config.setCategoryComment(CATEGORY_RAIN, "Options that control rain effects in the client");
		config.setCategoryPropertyOrder(CATEGORY_RAIN, new ArrayList<String>(rainSort));
		config.setCategoryLanguageKey(CATEGORY_RAIN, "cfg.rain.cat.Rain");

		// CATEGORY: General
		config.setCategoryRequiresMcRestart(CATEGORY_GENERAL, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_GENERAL, false);
		config.setCategoryComment(CATEGORY_GENERAL, "Miscellaneous settings");
		config.setCategoryPropertyOrder(CATEGORY_GENERAL, new ArrayList<String>(generalSort));
		config.setCategoryLanguageKey(CATEGORY_GENERAL, "cfg.general.cat.General");

		// CATEGORY: Player
		config.setCategoryRequiresMcRestart(CATEGORY_PLAYER, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_PLAYER, false);
		config.setCategoryComment(CATEGORY_PLAYER, "General options for defining sound and effects the player entity");
		config.setCategoryPropertyOrder(CATEGORY_PLAYER, new ArrayList<String>(playerSort));
		config.setCategoryLanguageKey(CATEGORY_PLAYER, "cfg.player.cat.Player");

		// CATEGORY: Aurora
		config.setCategoryRequiresMcRestart(CATEGORY_AURORA, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_AURORA, false);
		config.setCategoryComment(CATEGORY_AURORA, "Options that control Aurora behavior and rendering");
		config.setCategoryPropertyOrder(CATEGORY_AURORA, new ArrayList<String>(auroraSort));
		config.setCategoryLanguageKey(CATEGORY_AURORA, "cfg.aurora.cat.Aurora");

		// CATEGORY: Fog
		config.setCategoryRequiresMcRestart(CATEGORY_FOG, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_FOG, false);
		config.setCategoryComment(CATEGORY_FOG, "Options that control the various fog effects in the client");
		config.setCategoryPropertyOrder(CATEGORY_FOG, new ArrayList<String>(fogSort));
		config.setCategoryLanguageKey(CATEGORY_FOG, "cfg.fog.cat.Fog");

		// CATEGORY: Biomes
		config.setCategoryRequiresMcRestart(CATEGORY_BIOMES, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_BIOMES, false);
		config.setCategoryComment(CATEGORY_BIOMES, "Options for controlling biome sound/effects");
		config.setCategoryPropertyOrder(CATEGORY_BIOMES, new ArrayList<String>(biomesSort));
		config.setCategoryLanguageKey(CATEGORY_BIOMES, "cfg.biomes.cat.Biomes");

		// CATEGORY: Block
		config.setCategoryRequiresMcRestart(CATEGORY_BLOCK, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_BLOCK, false);
		config.setCategoryComment(CATEGORY_BLOCK, "Options for defining block specific sounds/effects");
		config.setCategoryLanguageKey(CATEGORY_BLOCK, "cfg.block.cat.Blocks");

		// CATEGORY: Block.effects
		config.setCategoryRequiresMcRestart(CATEGORY_BLOCK_EFFECTS, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_BLOCK_EFFECTS, false);
		config.setCategoryComment(CATEGORY_BLOCK_EFFECTS, "Options for disabling various block effects");
		config.setCategoryLanguageKey(CATEGORY_BLOCK_EFFECTS, "cfg.block.effects.cat.BlockEffects");

		// CATEGORY: Sound
		config.setCategoryRequiresMcRestart(CATEGORY_SOUND, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_SOUND, false);
		config.setCategoryComment(CATEGORY_SOUND, "General options for defining sound effects");
		config.setCategoryPropertyOrder(CATEGORY_SOUND, new ArrayList<String>(soundsSort));
		config.setCategoryLanguageKey(CATEGORY_SOUND, "cfg.sound.cat.Sound");

		// CATEGORY: player.potion hud
		config.setCategoryRequiresMcRestart(CATEGORY_POTION_HUD, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_POTION_HUD, false);
		config.setCategoryComment(CATEGORY_POTION_HUD, "Options for the Potion HUD overlay");
		config.setCategoryPropertyOrder(CATEGORY_POTION_HUD, new ArrayList<String>(potionHudSort));
		config.setCategoryLanguageKey(CATEGORY_POTION_HUD, "cfg.player.potionHud.cat.PotionHud");

		// CATEGORY: SpeechBubbles
		config.setCategoryRequiresMcRestart(CATEGORY_SPEECHBUBBLES, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_SPEECHBUBBLES, false);
		config.setCategoryComment(CATEGORY_SPEECHBUBBLES, "Options for configuring SpeechBubbles");
		config.setCategoryPropertyOrder(CATEGORY_SPEECHBUBBLES, new ArrayList<String>(speechBubbleSort));
		config.setCategoryLanguageKey(CATEGORY_SPEECHBUBBLES, "cfg.speech.cat.Speech");

		// CATEGORY: Explosions
		config.setCategoryRequiresMcRestart(CATEGORY_EXPLOSIONS, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_EXPLOSIONS, false);
		config.setCategoryComment(CATEGORY_EXPLOSIONS, "Options for configuring Explosion Enhancement");
		config.setCategoryLanguageKey(CATEGORY_EXPLOSIONS, "cfg.explosions.cat.Explosions");

		// CATEGORY: lightlevel
		config.setCategoryRequiresMcRestart(CATEGORY_LIGHT_LEVEL, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_LIGHT_LEVEL, false);
		config.setCategoryComment(CATEGORY_LIGHT_LEVEL, "Options for configuring Light Level HUD");
		config.setCategoryPropertyOrder(CATEGORY_LIGHT_LEVEL, new ArrayList<String>(llSort));
		config.setCategoryLanguageKey(CATEGORY_LIGHT_LEVEL, "cfg.lightlevel.cat.LightLevel");

		// CATEGORY: compass
		config.setCategoryRequiresMcRestart(CATEGORY_COMPASS, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_COMPASS, false);
		config.setCategoryComment(CATEGORY_COMPASS, "Options for configuring compass HUD");
		config.setCategoryPropertyOrder(CATEGORY_COMPASS, new ArrayList<String>(compassSort));
		config.setCategoryLanguageKey(CATEGORY_COMPASS, "cfg.compass.cat.Compass");

		// CATEGORY: commands
		config.setCategoryRequiresMcRestart(CATEGORY_COMMANDS, true);
		config.setCategoryRequiresWorldRestart(CATEGORY_COMMANDS, true);
		config.setCategoryComment(CATEGORY_COMMANDS, "Options for configuring commands");
		config.setCategoryLanguageKey(CATEGORY_COMMANDS, "cfg.commands.cat.Commands");

		// CATEGORY: features
		config.setCategoryRequiresMcRestart(CATEGORY_FEATURES, true);
		config.setCategoryRequiresWorldRestart(CATEGORY_FEATURES, true);
		config.setCategoryComment(CATEGORY_FEATURES, "Controls whether features are available");

		// CATEGORY: profiles
		config.setCategoryRequiresMcRestart(CATEGORY_PROFILES, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_PROFILES, false);
		config.setCategoryComment(CATEGORY_PROFILES, "Enable/disable application of built in profiles");
		config.setCategoryLanguageKey(CATEGORY_PROFILES, "cfg.profiles.cat.Profiles");

		// Iterate through the config list looking for properties without
		// comments. These will be scrubbed.
		if (DSurround.config() != null)
			for (final String cat : config.getCategoryNames())
				scrubCategory(config.getCategory(cat));

	}

	private static void scrubCategory(final ConfigCategory category) {
		final List<String> killList = new ArrayList<String>();
		for (final Entry<String, Property> entry : category.entrySet())
			if (StringUtils.isEmpty(entry.getValue().getComment()))
				killList.add(entry.getKey());

		for (final String kill : killList)
			category.remove(kill);
	}
}
