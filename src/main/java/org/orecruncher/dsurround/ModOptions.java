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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.data.Profiles;
import org.orecruncher.lib.ConfigProcessor;
import org.orecruncher.lib.ConfigProcessor.Category;
import org.orecruncher.lib.ConfigProcessor.Comment;
import org.orecruncher.lib.ConfigProcessor.DefaultValue;
import org.orecruncher.lib.ConfigProcessor.Hidden;
import org.orecruncher.lib.ConfigProcessor.LangKey;
import org.orecruncher.lib.ConfigProcessor.Option;
import org.orecruncher.lib.ConfigProcessor.RangeFloat;
import org.orecruncher.lib.ConfigProcessor.RangeInt;
import org.orecruncher.lib.ConfigProcessor.RestartRequired;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public final class ModOptions {

	public static class Trace {
		// Trace bits for debugging
	};

	private ModOptions() {
	}

	public static final String CATEGORY_ASM = "asm";
	public static final String CONFIG_ENABLE_SOUND_CACHING = "Enable Sound Caching";
	public static final String CONFIG_ENABLE_WEATHER = "Enable Weather Control";
	public static final String CONFIG_DISABLE_ARROW_CRITICAL_TRAIL = "Disable Arrow Critical Particle Trail";
	public static final String CONFIG_ENABLE_SYNC_SOUND_MANAGER = "Enable synchronized for SoundManager";
	public static final String CONFIG_ENABLE_SYNC_PARTICLE_MANAGER = "Enable synchronized for ParticleManager";

	@Category(CATEGORY_ASM)
	@LangKey("dsurround.cfg.asm.cat.ASM")
	@Comment("Controls ASM transforms Dynamic Surroundings performs at startup")
	@RestartRequired(server = true, world = true)
	public static class asm {

		public static String PATH = null;

		@Option(CONFIG_ENABLE_SOUND_CACHING)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.asm.EnableSoundCache")
		@Comment("Enable ASM transformations to permit sound caching")
		@RestartRequired(server = true)
		public static boolean enableSoundCache = true;

		@Option(CONFIG_ENABLE_WEATHER)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.asm.EnableWeather")
		@Comment("Enable ASM transformations to permit weather (rain, snow, splash, dust storms, auroras)")
		@RestartRequired(server = true)
		public static boolean enableWeatherASM = true;

		@Option(CONFIG_DISABLE_ARROW_CRITICAL_TRAIL)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.asm.DisableArrow")
		@Comment("Disable particle trail left by an arrow when it flies")
		@RestartRequired(server = true)
		public static boolean disableArrowParticleTrail = true;

		@Option(CONFIG_ENABLE_SYNC_SOUND_MANAGER)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.asm.EnableSMSync")
		@Comment("Enable synchronized attribute on SoundManager public methods")
		@RestartRequired(server = true)
		public static boolean enableSoundManagerSync = false;

		@Option(CONFIG_ENABLE_SYNC_PARTICLE_MANAGER)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.asm.EnablePMSync")
		@Comment("Enable synchronized attribute on ParticleManager public methods")
		@RestartRequired(server = true)
		public static boolean enableParticleManagerSync = false;
	}

	public static final String CATEGORY_LOGGING_CONTROL = "logging";
	public static final String CONFIG_ENABLE_ONLINE_VERSION_CHECK = "Enable Online Version Check";
	public static final String CONFIG_ENABLE_DEBUG_LOGGING = "Enable Debug Logging";
	public static final String CONFIG_ENABLE_DEBUG_DIALOG = "Enable Debug Dialog";
	public static final String CONFIG_REPORT_SERVER_STATS = "Report Server Stats";
	public static final String CONFIG_DEBUG_FLAG_MASK = "Debug Flag Mask";

	@Category(CATEGORY_LOGGING_CONTROL)
	@LangKey("dsurround.cfg.logging.cat.Logging")
	@Comment("Defines how Dynamic Surroundings logging will behave")
	public static class logging {

		public static String PATH = null;
		public static final List<String> SORT = Arrays.asList(CONFIG_ENABLE_ONLINE_VERSION_CHECK,
				CONFIG_ENABLE_DEBUG_LOGGING, CONFIG_REPORT_SERVER_STATS, CONFIG_DEBUG_FLAG_MASK,
				CONFIG_ENABLE_DEBUG_DIALOG);

		@Option(CONFIG_ENABLE_DEBUG_LOGGING)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.logging.EnableDebug")
		@Comment("Enables/disables debug logging of the mod")
		@RestartRequired
		public static boolean enableDebugLogging = false;

		@Option(CONFIG_ENABLE_ONLINE_VERSION_CHECK)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.logging.VersionCheck")
		@Comment("Enables/disables display of version check information")
		@RestartRequired
		public static boolean enableVersionChecking = true;

		@Option(CONFIG_ENABLE_DEBUG_DIALOG)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.logging.DebugDialog")
		@Comment("Enables/disables display of debug dialog")
		@RestartRequired
		public static boolean showDebugDialog = false;

		@Option(CONFIG_REPORT_SERVER_STATS)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.logging.ServerStats")
		@Comment("Enables/disables reporting of server stats")
		public static boolean reportServerStats = false;

		@Option(CONFIG_DEBUG_FLAG_MASK)
		@DefaultValue("0")
		@LangKey("dsurround.cfg.logging.FlagMask")
		@Comment("Bitmask for toggling various debug traces")
		@Hidden
		public static int debugFlagMask = 0;
	}

	public static final String CATEGORY_RAIN = "rain";
	public static final String CONFIG_VANILLA_RAIN = "Use Vanilla Algorithms";
	public static final String CONFIG_ENABLE_BACKGROUND_THUNDER = "Enable Background Thunder";
	public static final String CONFIG_THUNDER_THRESHOLD = "Rain Intensity for Background Thunder";
	public static final String CONFIG_RAIN_RIPPLE_STYLE = "Style of rain water ripple";

	@Category(CATEGORY_RAIN)
	@LangKey("dsurround.cfg.rain.cat.Rain")
	@Comment("Options that control rain effects in the client")
	public static class rain {

		public static String PATH = null;
		public static final List<String> SORT = Arrays.asList(CONFIG_VANILLA_RAIN, CONFIG_RAIN_RIPPLE_STYLE,
				CONFIG_ENABLE_BACKGROUND_THUNDER, CONFIG_THUNDER_THRESHOLD);

		@Option(CONFIG_VANILLA_RAIN)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.rain.VanillaRain")
		@Comment("Let Vanilla handle rain intensity and time windows")
		@RestartRequired
		public static boolean doVanillaRain = false;

		@Option(CONFIG_RAIN_RIPPLE_STYLE)
		@DefaultValue("0")
		@LangKey("dsurround.cfg.rain.RippleStyle")
		@RangeInt(min = 0, max = 2)
		@Comment("0: original round, 1: darker round, 2: square")
		public static int rainRippleStyle = 0;

		@Option(CONFIG_ENABLE_BACKGROUND_THUNDER)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.rain.EnableThunder")
		@Comment("Allow background thunder when storming")
		public static boolean allowBackgroundThunder = true;

		@Option(CONFIG_THUNDER_THRESHOLD)
		@DefaultValue("0.75")
		@LangKey("dsurround.cfg.rain.ThunderThreshold")
		@RangeFloat(min = 0)
		@Comment("Minimum rain intensity level for background thunder to occur")
		public static float stormThunderThreshold = 0.75F;
	}

	public static final String CATEGORY_FOG = "fog";
	public static final String CONFIG_ENABLE_FOG_PROCESSING = "Enable Fog Processing";
	public static final String CONFIG_ENABLE_MORNING_FOG = "Morning Fog";
	public static final String CONFIG_MORNING_FOG_CHANCE = "Morning Fog Chance";
	public static final String CONFIG_ENABLE_WEATHER_FOG = "Weather Fog";
	public static final String CONFIG_ENABLE_BEDROCK_FOG = "Bedrock Fog";
	public static final String CONFIG_ALLOW_DESERT_FOG = "Desert Fog";
	public static final String CONFIG_ENABLE_ELEVATION_HAZE = "Elevation Haze";
	public static final String CONFIG_ENABLE_BIOME_FOG = "Biomes Fog";

	@Category(CATEGORY_FOG)
	@LangKey("dsurround.cfg.fog.cat.Fog")
	@Comment("Options that control the various fog effects in the client")
	public static class fog {

		public static String PATH = null;
		public static final List<String> SORT = Arrays.asList(CONFIG_ENABLE_FOG_PROCESSING, CONFIG_ENABLE_MORNING_FOG,
				CONFIG_MORNING_FOG_CHANCE, CONFIG_ENABLE_WEATHER_FOG, CONFIG_ENABLE_BEDROCK_FOG,
				CONFIG_ALLOW_DESERT_FOG, CONFIG_ENABLE_BIOME_FOG, CONFIG_ENABLE_ELEVATION_HAZE);

		@Option(CONFIG_ENABLE_FOG_PROCESSING)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.fog.Enable")
		@Comment("Enable/disable fog processing")
		public static boolean enableFogProcessing = true;

		@Option(CONFIG_ENABLE_MORNING_FOG)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.fog.EnableMorning")
		@Comment("Show morning fog that eventually burns off")
		public static boolean enableMorningFog = true;

		@Option(CONFIG_ENABLE_WEATHER_FOG)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.fog.EnableWeather")
		@Comment("Increase fog based on the strength of rain")
		public static boolean enableWeatherFog = true;

		@Option(CONFIG_ENABLE_BEDROCK_FOG)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.fog.EnableBedrock")
		@Comment("Increase fog at bedrock layers")
		public static boolean enableBedrockFog = true;

		@Option(CONFIG_ALLOW_DESERT_FOG)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.fog.DesertFog")
		@Comment("Enable/disable desert fog when raining")
		public static boolean allowDesertFog = true;

		@Option(CONFIG_ENABLE_ELEVATION_HAZE)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.fog.ElevationHaze")
		@Comment("Higher the player elevation the more haze that is experienced")
		public static boolean enableElevationHaze = true;

		@Option(CONFIG_ENABLE_BIOME_FOG)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.fog.BiomeFog")
		@Comment("Enable biome specific fog density and color")
		public static boolean enableBiomeFog = true;

		@Option(CONFIG_MORNING_FOG_CHANCE)
		@DefaultValue("1")
		@RangeInt(min = 1, max = 10)
		@LangKey("dsurround.cfg.fog.MorningFogChance")
		@Comment("Chance morning fog will occurs expressed as 1 in N")
		public static int morningFogChance = 1;
	}

	public static final String CATEGORY_GENERAL = "general";
	public static final String CONFIG_EXTERNAL_SCRIPTS = "External Configuration Files";
	public static final String CONFIG_MIN_RAIN_STRENGTH = "Default Minimum Rain Strength";
	public static final String CONFIG_MAX_RAIN_STRENGTH = "Default Maximum Rain Strength";
	public static final String CONFIG_FX_RANGE = "Special Effect Range";
	public static final String CONFIG_DISABLE_SUSPEND = "Disable Water Suspend Particles";
	public static final String CONFIG_STARTUP_SOUND_LIST = "Startup Sound List";
	public static final String CONFIG_HIDE_CHAT_NOTICES = "Hide Chat Notices";
	public static final String CONFIG_ENABLE_CLIENT_CHUNK_CACHING = "Enable Client Chunk Caching";

	@Category(CATEGORY_GENERAL)
	@LangKey("dsurround.cfg.general.cat.General")
	@Comment("Miscellaneous settings")
	public static class general {

		public static String PATH = null;
		public static final List<String> SORT = Arrays.asList(CONFIG_HIDE_CHAT_NOTICES, CONFIG_DISABLE_SUSPEND,
				CONFIG_FX_RANGE, CONFIG_MIN_RAIN_STRENGTH, CONFIG_MAX_RAIN_STRENGTH, CONFIG_EXTERNAL_SCRIPTS,
				CONFIG_STARTUP_SOUND_LIST, CONFIG_ENABLE_CLIENT_CHUNK_CACHING);

		@Option(CONFIG_HIDE_CHAT_NOTICES)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.general.HideChat")
		@Comment("Toggles display of Dynamic Surroundings chat notices")
		public static boolean hideChatNotices = false;

		@Option(CONFIG_DISABLE_SUSPEND)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.general.Suspend")
		@Comment("Enable/disable water depth particle effect")
		@RestartRequired(server = true)
		public static boolean disableWaterSuspendParticle = false;

		@Option(CONFIG_MIN_RAIN_STRENGTH)
		@DefaultValue("0.0")
		@LangKey("dsurround.cfg.general.MinRainStrength")
		@RangeFloat(min = 0.0F, max = 1.0F)
		@Comment("Default minimum rain strength for a dimension")
		public static float defaultMinRainStrength = 0.0F;

		@Option(CONFIG_MAX_RAIN_STRENGTH)
		@DefaultValue("1.0")
		@LangKey("dsurround.cfg.general.MaxRainStrength")
		@RangeFloat(min = 0.0F, max = 1.0F)
		@Comment("Default maximum rain strength for a dimension")
		public static float defaultMaxRainStrength = 1.0F;

		@Option(CONFIG_FX_RANGE)
		@DefaultValue("24")
		@LangKey("dsurround.cfg.general.FXRange")
		@RangeInt(min = 16, max = 64)
		@Comment("Block radius/range around player for special effect application")
		public static int specialEffectRange = 24;

		@Option(CONFIG_EXTERNAL_SCRIPTS)
		@DefaultValue("")
		@LangKey("dsurround.cfg.general.ExternalScripts")
		@Comment("Configuration files for customization")
		public static String[] externalScriptFiles = {};

		@Option(CONFIG_STARTUP_SOUND_LIST)
		@DefaultValue("minecraft:entity.experience_orb.pickup,minecraft:entity.chicken.egg")
		@LangKey("dsurround.cfg.general.StartupSounds")
		@Comment("Possible sounds to play when client reaches main game menu")
		public static String[] startupSoundList = { "minecraft:entity.experience_orb.pickup",
				"minecraft:entity.chicken.egg" };

		@Option(CONFIG_ENABLE_CLIENT_CHUNK_CACHING)
		@DefaultValue("true")
		@Comment("Enable/disable client side chunk caching for performance")
		@LangKey("dsurround.cfg.general.ChunkCaching")
		public static boolean enableClientChunkCaching = true;
	}

	public static final String CATEGORY_AURORA = "aurora";
	public static final String CONFIG_AURORA_ENABLED = "Enabled";
	public static final String CONFIG_AURORA_SHADER = "Use Shaders";

	@Category(CATEGORY_AURORA)
	@LangKey("dsurround.cfg.aurora.cat.Aurora")
	@Comment("Options that control Aurora behavior and rendering")
	public static class aurora {

		public static String PATH = null;
		public static final List<String> SORT = Arrays.asList(CONFIG_AURORA_ENABLED, CONFIG_AURORA_SHADER);

		@Option(CONFIG_AURORA_ENABLED)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.aurora.EnableAurora")
		@Comment("Enable/disable Aurora processing on server/client")
		public static boolean auroraEnable = true;

		@Option(CONFIG_AURORA_SHADER)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.aurora.EnableShader")
		@Comment("Use shader when rendering aurora")
		@RestartRequired(world = true)
		public static boolean auroraUseShader = true;
	}

	public static final String CATEGORY_BIOMES = "biomes";
	public static final String CONFIG_BIOME_SEALEVEL = "Overworld Sealevel Override";
	public static final String CONFIG_BIOME_ALIASES = "Biomes Alias";
	public static final String CONFIG_BIOME_DIM_BLACKLIST = "Dimension Blacklist";

	@Category(CATEGORY_BIOMES)
	@LangKey("dsurround.cfg.biomes.cat.Biomes")
	@Comment("Options for controlling biome sound/effects")
	public static class biomes {

		public static String PATH = null;
		public static final List<String> SORT = Arrays.asList(CONFIG_BIOME_SEALEVEL, CONFIG_BIOME_ALIASES,
				CONFIG_BIOME_DIM_BLACKLIST);

		@Option(CONFIG_BIOME_SEALEVEL)
		@DefaultValue("0")
		@LangKey("dsurround.cfg.biomes.Sealevel")
		@RangeInt(min = 0, max = 255)
		@Comment("Sealevel to set for Overworld (0 use default for World)")
		public static int worldSealevelOverride = 0;

		@Option(CONFIG_BIOME_ALIASES)
		@DefaultValue("")
		@LangKey("dsurround.cfg.biomes.Aliases")
		@Comment("Biomes alias list")
		public static String[] biomeAliases = {};

		@Option(CONFIG_BIOME_DIM_BLACKLIST)
		@DefaultValue("")
		@LangKey("dsurround.cfg.biomes.DimBlacklist")
		@Comment("Dimension IDs where biome sounds will not be played")
		public static String[] dimensionBlacklist = {};
	}

	public static final String CATEGORY_BLOCK = "block";
	public static final String CATEGORY_BLOCK_EFFECTS = "effects";
	public static final String CONFIG_BLOCK_EFFECT_STEAM = "Enable Steam";
	public static final String CONFIG_BLOCK_EFFECT_FIRE = "Enable FireJetEffect Jets";
	public static final String CONFIG_BLOCK_EFFECT_BUBBLE = "Enable Bubbles";
	public static final String CONFIG_BLOCK_EFFECT_DUST = "Enable DustJetEffect Motes";
	public static final String CONFIG_BLOCK_EFFECT_FOUNTAIN = "Enable FountainJetEffect";
	public static final String CONFIG_BLOCK_EFFECT_FIREFLY = "Enable Fireflies";
	public static final String CONFIG_BLOCK_EFFECT_SPLASH = "Enable Water Splash";

	@Category(CATEGORY_BLOCK)
	@LangKey("dsurround.cfg.block.cat.Blocks")
	@Comment("Options for defining block specific sounds/effects")
	public static class block {

		public static String PATH = null;

		@Category(CATEGORY_BLOCK_EFFECTS)
		@LangKey("dsurround.cfg.block.effects.cat.BlockEffects")
		@Comment("Options for disabling various block effects")
		public static class effects {

			public static String PATH = null;

			@Option(CONFIG_BLOCK_EFFECT_STEAM)
			@DefaultValue("true")
			@LangKey("dsurround.cfg.block.effects.Steam")
			@Comment("Enable Steam Jets where lava meets water")
			public static boolean enableSteamJets = true;

			@Option(CONFIG_BLOCK_EFFECT_FIRE)
			@DefaultValue("true")
			@LangKey("dsurround.cfg.block.effects.Fire")
			@Comment("Enable FireJetEffect Jets in lava")
			public static boolean enableFireJets = true;

			@Option(CONFIG_BLOCK_EFFECT_BUBBLE)
			@DefaultValue("true")
			@LangKey("dsurround.cfg.block.effects.Bubble")
			@Comment("Enable BubbleJetEffect Jets under water")
			public static boolean enableBubbleJets = true;

			@Option(CONFIG_BLOCK_EFFECT_DUST)
			@DefaultValue("true")
			@LangKey("dsurround.cfg.block.effects.Dust")
			@Comment("Enable DustJetEffect motes dropping from blocks")
			public static boolean enableDustJets = true;

			@Option(CONFIG_BLOCK_EFFECT_FOUNTAIN)
			@DefaultValue("true")
			@LangKey("dsurround.cfg.block.effects.Fountain")
			@Comment("Enable FountainJetEffect jets")
			public static boolean enableFountainJets = true;

			@Option(CONFIG_BLOCK_EFFECT_FIREFLY)
			@DefaultValue("true")
			@LangKey("dsurround.cfg.block.effects.Fireflies")
			@Comment("Enable Firefly effect around plants")
			public static boolean enableFireflies = true;

			@Option(CONFIG_BLOCK_EFFECT_SPLASH)
			@DefaultValue("true")
			@LangKey("dsurround.cfg.block.effects.Splash")
			@Comment("Enable Water Splash effects when water spills down")
			public static boolean enableWaterSplash = true;
		}
	}

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
	public static final String CONFIG_FOOTSTEPS_QUAD = "Footsteps as Quadruped";
	public static final String CONFIG_FOOTSTEPS_CADENCE = "First Person Footstep Cadence";
	public static final String CONFIG_ENABLE_ARMOR_SOUND = "Armor Sound";
	public static final String CONFIG_ENABLE_SWING_SOUND = "Swing Sound";
	public static final String CONFIG_ENABLE_PUDDLE_SOUND = "Rain Puddle Sound";
	public static final String CONFIG_SOUND_CULL_THRESHOLD = "Sound Culling Threshold";
	public static final String CONFIG_CULLED_SOUNDS = "Culled Sounds";
	public static final String CONFIG_BLOCKED_SOUNDS = "Blocked Sounds";
	public static final String CONFIG_SOUND_VOLUMES = "Sound Volume";
	public static final String CONFIG_THUNDER_VOLUME = "Thunder Volume";
	public static final String CONFIG_ENABLE_BATTLEMUSIC = "Battle Music";

	@Category(CATEGORY_SOUND)
	@LangKey("dsurround.cfg.sound.cat.Sound")
	@Comment("General options for defining sound effects")
	public static class sound {

		public static String PATH = null;
		public static final List<String> SORT = Arrays.asList(CONFIG_ENABLE_BIOME_SOUNDS, CONFIG_FOOTSTEPS_QUAD,
				CONFIG_FOOTSTEPS_CADENCE, CONFIG_ENABLE_ARMOR_SOUND, CONFIG_ENABLE_SWING_SOUND,
				CONFIG_ENABLE_PUDDLE_SOUND, CONFIG_ENABLE_JUMP_SOUND, CONFIG_ENABLE_EQUIP_SOUND,
				CONFIG_SWORD_AS_TOOL_EQUIP_SOUND, CONFIG_ENABLE_CRAFTING_SOUND, CONFIG_AUTO_CONFIG_CHANNELS,
				CONFIG_NORMAL_CHANNEL_COUNT, CONFIG_STREAMING_CHANNEL_COUNT, CONFIG_STREAM_BUFFER_SIZE,
				CONFIG_STREAM_BUFFER_COUNT, CONFIG_MUTE_WHEN_BACKGROUND, CONFIG_THUNDER_VOLUME, CONFIG_BLOCKED_SOUNDS,
				CONFIG_SOUND_CULL_THRESHOLD, CONFIG_CULLED_SOUNDS, CONFIG_SOUND_VOLUMES, CONFIG_ENABLE_BATTLEMUSIC);

		@Option(CONFIG_ENABLE_BIOME_SOUNDS)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.sound.BiomeSounds")
		@Comment("Enable biome background and spot sounds")
		public static boolean enableBiomeSounds = true;

		@Option(CONFIG_AUTO_CONFIG_CHANNELS)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.sound.AutoConfig")
		@Comment("Automatically configure sound channels")
		@RestartRequired(server = true)
		public static boolean autoConfigureChannels = true;

		@Option(CONFIG_NORMAL_CHANNEL_COUNT)
		@DefaultValue("28")
		@LangKey("dsurround.cfg.sound.NormalChannels")
		@RangeInt(min = 28, max = 255)
		@Comment("Number of normal sound channels to configure in the sound system (manual)")
		@RestartRequired(server = true)
		public static int normalSoundChannelCount = 28;

		@Option(CONFIG_STREAMING_CHANNEL_COUNT)
		@DefaultValue("4")
		@LangKey("dsurround.cfg.sound.StreamingChannels")
		@RangeInt(min = 4, max = 255)
		@Comment("Number of streaming sound channels to configure in the sound system (manual)")
		@RestartRequired(server = true)
		public static int streamingSoundChannelCount = 4;

		@Option(CONFIG_STREAM_BUFFER_SIZE)
		@DefaultValue("0")
		@LangKey("dsurround.cfg.sound.StreamBufferSize")
		@RangeInt(min = 0)
		@Comment("Size of a stream buffer in kilobytes (0: system default - usually 128K bytes)")
		@RestartRequired(server = true)
		public static int streamBufferSize = 0;

		@Option(CONFIG_STREAM_BUFFER_COUNT)
		@DefaultValue("0")
		@LangKey("dsurround.cfg.sound.StreamBufferCount")
		@RangeInt(min = 0, max = 8)
		@Comment("Number of stream buffers per channel (0: system default - usually 3 buffers)")
		@RestartRequired(server = true)
		public static int streamBufferCount = 0;

		@Option(CONFIG_MUTE_WHEN_BACKGROUND)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.sound.Mute")
		@Comment("Mute sound when Minecraft is in the background")
		public static boolean muteWhenBackground = true;

		@Option(CONFIG_THUNDER_VOLUME)
		@DefaultValue("10000")
		@LangKey("dsurround.cfg.sound.ThunderVolume")
		@Comment("Sound Volume of Thunder")
		@RangeFloat(min = 15F, max = 10000F)
		public static float thunderVolume = 10000F;

		@Option(CONFIG_ENABLE_JUMP_SOUND)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.sound.Jump")
		@Comment("Enable player Jump sound effect")
		public static boolean enableJumpSound = true;

		@Option(CONFIG_ENABLE_EQUIP_SOUND)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.sound.Equip")
		@Comment("Enable Weapon/Tool Equip sound effect")
		public static boolean enableEquipSound = true;

		@Option(CONFIG_SWORD_AS_TOOL_EQUIP_SOUND)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.sound.SwordEquipAsTool")
		@Comment("Enable Sword Equip sound as Tool")
		public static boolean swordEquipAsTool = false;

		@Option(CONFIG_ENABLE_CRAFTING_SOUND)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.sound.Craft")
		@Comment("Enable Item Crafted sound effect")
		public static boolean enableCraftingSound = true;

		@Option(CONFIG_FOOTSTEPS_QUAD)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.sound.FootstepQuad")
		@Comment("Simulate quadruped with Footstep effects (horse)")
		public static boolean foostepsQuadruped = false;

		@Option(CONFIG_FOOTSTEPS_CADENCE)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.sound.FootstepCadence")
		@Comment("true to match first person arm swing; false to match 3rd person leg animation")
		public static boolean firstPersonFootstepCadence = true;

		@Option(CONFIG_ENABLE_ARMOR_SOUND)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.sound.Armor")
		@Comment("Enable/disable armor sounds when moving")
		public static boolean enableArmorSounds = true;

		@Option(CONFIG_ENABLE_SWING_SOUND)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.sound.Swing")
		@Comment("Enable/disable item swing sounds")
		public static boolean enableSwingSounds = true;

		@Option(CONFIG_ENABLE_PUDDLE_SOUND)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.sound.Puddle")
		@Comment("Enable/disable rain puddle sound when moving in the rain")
		public static boolean enablePuddleSound = true;

		@Option(CONFIG_SOUND_CULL_THRESHOLD)
		@DefaultValue("20")
		@LangKey("dsurround.cfg.sound.CullInterval")
		@RangeInt(min = 0)
		@Comment("Ticks between culled sound events (0 to disable culling)")
		public static int soundCullingThreshold = 20;

		@Option(CONFIG_ENABLE_BATTLEMUSIC)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.sound.BattleMusic")
		@Comment("Enable/disable Battle Music")
		public static boolean enableBattleMusic = false;

		@Option(CONFIG_CULLED_SOUNDS)
		@DefaultValue("minecraft:block.water.ambient,minecraft:block.lava.ambient,minecraft:entity.sheep.ambient,minecraft:entity.chicken.ambient,minecraft:entity.cow.ambient,minecraft:entity.pig.ambient")
		@LangKey("dsurround.cfg.sound.CulledSounds")
		@Comment("Sounds to cull from frequent playing")
		@Hidden
		public static String[] culledSounds = { "minecraft:block.water.ambient", "minecraft:block.lava.ambient",
				"minecraft:entity.sheep.ambient", "minecraft:entity.chicken.ambient", "minecraft:entity.cow.ambient",
				"minecraft:entity.pig.ambient" };

		@Option(CONFIG_BLOCKED_SOUNDS)
		@DefaultValue("dsurround:bison")
		@LangKey("dsurround.cfg.sound.BlockedSounds")
		@Comment("Sounds to block from playing")
		@Hidden
		public static String[] blockedSounds = { "dsurround:bison" };

		@Option(CONFIG_SOUND_VOLUMES)
		@DefaultValue("")
		@LangKey("dsurround.cfg.sound.SoundVolumes")
		@Comment("Individual sound volume scaling factors")
		@Hidden
		public static String[] soundVolumes = {};
	}

	public static final String CATEGORY_PLAYER = "player";
	public static final String CONFIG_SUPPRESS_POTION_PARTICLES = "Suppress Potion Particles";
	public static final String CONFIG_ENABLE_POPOFFS = "Damage Popoffs";
	public static final String CONFIG_SHOW_CRIT_WORDS = "Show Crit Words";
	public static final String CONFIG_HURT_THRESHOLD = "Hurt Threshold";
	public static final String CONFIG_HUNGER_THRESHOLD = "Hunger Threshold";
	public static final String CONFIG_ENABLE_FOOTPRINTS = "Footprints";
	public static final String CONFIG_FOOTPRINT_STYLE = "Footprint Style";
	public static final String CONFIG_SHOW_BREATH = "Show Frost Breath";

	public static final String CATEGORY_POTION_HUD = "potion hud";
	public static final String CONFIG_POTION_HUD_NONE = "No Potion HUD";
	public static final String CONFIG_POTION_HUD_ENABLE = "Enable";
	public static final String CONFIG_POTION_HUD_TRANSPARENCY = "Transparency";
	public static final String CONFIG_POTION_HUD_LEFT_OFFSET = "Horizontal Offset";
	public static final String CONFIG_POTION_HUD_TOP_OFFSET = "Vertical Offset";
	public static final String CONFIG_POTION_HUD_SCALE = "Display Scale";
	public static final String CONFIG_POTION_HUD_ANCHOR = "HUD Location";

	@Category(CATEGORY_PLAYER)
	@LangKey("dsurround.cfg.player.cat.Player")
	@Comment("General options for defining sound and effects the player entity")
	public static class player {

		public static String PATH = null;
		public static final List<String> SORT = Arrays.asList(CONFIG_SUPPRESS_POTION_PARTICLES, CONFIG_ENABLE_POPOFFS,
				CONFIG_SHOW_CRIT_WORDS, CONFIG_ENABLE_FOOTPRINTS, CONFIG_FOOTPRINT_STYLE, CONFIG_HURT_THRESHOLD,
				CONFIG_HUNGER_THRESHOLD, CONFIG_SHOW_BREATH);

		@Option(CONFIG_SUPPRESS_POTION_PARTICLES)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.player.PotionParticles")
		@Comment("Suppress player's potion particles from rendering")
		public static boolean suppressPotionParticles = false;

		@Option(CONFIG_ENABLE_POPOFFS)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.player.Popoffs")
		@Comment("Controls display of damage pop-offs when an entity is damaged")
		public static boolean enableDamagePopoffs = true;

		@Option(CONFIG_SHOW_CRIT_WORDS)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.player.CritWords")
		@Comment("Display random power word on critical hit")
		public static boolean showCritWords = true;

		@Option(CONFIG_ENABLE_FOOTPRINTS)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.player.Footprints")
		@Comment("Enable player footprints")
		public static boolean enableFootprints = true;

		@Option(CONFIG_FOOTPRINT_STYLE)
		@DefaultValue("6")
		@LangKey("dsurround.cfg.player.FootprintStyle")
		@Comment("0: shoe print, 1: square print, 2: horse hoof, 3: bird, 4: paw, 5: solid square, 6: lowres square")
		@RangeInt(min = 0, max = 6)
		public static int footprintStyle = 6;

		@Option(CONFIG_HURT_THRESHOLD)
		@DefaultValue("8")
		@LangKey("dsurround.cfg.player.HurtThreshold")
		@Comment("Amount of health bar remaining to trigger player hurt sound (0 disable)")
		@RangeInt(min = 0, max = 10)
		public static int playerHurtThreshold = 8;

		@Option(CONFIG_HUNGER_THRESHOLD)
		@DefaultValue("8")
		@LangKey("dsurround.cfg.player.HungerThreshold")
		@Comment("Amount of food bar remaining to trigger player hunger sound (0 disable)")
		@RangeInt(min = 0, max = 10)
		public static int playerHungerThreshold = 8;

		@Option(CONFIG_SHOW_BREATH)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.player.ShowBreath")
		@Comment("Show player frost breath in cold weather")
		public static boolean showBreath = true;

		@Category(CATEGORY_POTION_HUD)
		@LangKey("dsurround.cfg.player.potionHud.cat.PotionHud")
		@Comment("Options for the Potion HUD overlay")
		public static class potionHUD {

			public static String PATH = null;
			public static final List<String> SORT = Arrays.asList(CONFIG_POTION_HUD_NONE, CONFIG_POTION_HUD_ENABLE,
					CONFIG_POTION_HUD_TRANSPARENCY, CONFIG_POTION_HUD_SCALE, CONFIG_POTION_HUD_ANCHOR,
					CONFIG_POTION_HUD_TOP_OFFSET, CONFIG_POTION_HUD_LEFT_OFFSET);

			@Option(CONFIG_POTION_HUD_NONE)
			@DefaultValue("false")
			@LangKey("dsurround.cfg.player.potionHud.NoHUD")
			@Comment("Disables Vanilla and Dynamic Surroundings potion HUD")
			public static boolean potionHudNone = false;

			@Option(CONFIG_POTION_HUD_ENABLE)
			@DefaultValue("true")
			@LangKey("dsurround.cfg.player.potionHud.Enable")
			@Comment("Enable display of potion icons in display")
			public static boolean potionHudEnabled = true;

			@Option(CONFIG_POTION_HUD_TRANSPARENCY)
			@DefaultValue("0.75")
			@LangKey("dsurround.cfg.player.potionHud.Transparency")
			@RangeFloat(min = 0.0F, max = 1.0F)
			@Comment("Transparency factor for icons (higher more solid)")
			public static float potionHudTransparency = 0.75F;

			@Option(CONFIG_POTION_HUD_LEFT_OFFSET)
			@DefaultValue("5")
			@LangKey("dsurround.cfg.player.potionHud.LeftOffset")
			@RangeInt(min = 0)
			@Comment("Offset from left side of screen")
			public static int potionHudLeftOffset = 5;

			@Option(CONFIG_POTION_HUD_TOP_OFFSET)
			@DefaultValue("5")
			@LangKey("dsurround.cfg.player.potionHud.TopOffset")
			@RangeInt(min = 0)
			@Comment("Offset from top of screen")
			public static int potionHudTopOffset = 5;

			@Option(CONFIG_POTION_HUD_SCALE)
			@DefaultValue("0.75")
			@LangKey("dsurround.cfg.player.potionHud.Scale")
			@RangeFloat(min = 0.0F, max = 1.0F)
			@Comment("Size scale of icons (lower is smaller)")
			public static float potionHudScale = 0.75F;

			@Option(CONFIG_POTION_HUD_ANCHOR)
			@DefaultValue("0")
			@LangKey("dsurround.cfg.player.potionHud.Location")
			@RangeInt(min = 0, max = 1)
			@Comment("Area of the display the Potion HUD is displayed (0 upper left, 1 upper right)")
			public static int potionHudAnchor = 0;
		}
	}

	public static final String CATEGORY_SPEECHBUBBLES = "speechbubbles";
	public static final String CONFIG_OPTION_ENABLE_SPEECHBUBBLES = "Enable SpeechBubbles";
	public static final String CONFIG_OPTION_ENABLE_ENTITY_CHAT = "Enable Entity Chat";
	public static final String CONFIG_OPTION_ENABLE_EMOJIS = "Enable Entity Emojis";
	public static final String CONFIG_OPTION_SPEECHBUBBLE_DURATION = "Display Duration";
	public static final String CONFIG_OPTION_SPEECHBUBBLE_RANGE = "Visibility Range";
	public static final String CONFIG_OPTION_ANIMANIA_BADGES = "Animania Badges";

	@Category(CATEGORY_SPEECHBUBBLES)
	@LangKey("dsurround.cfg.speech.cat.Speech")
	@Comment("Options for configuring SpeechBubbles")
	public static class speechbubbles {

		public static String PATH = null;
		public static final List<String> SORT = Arrays.asList(CONFIG_OPTION_ENABLE_SPEECHBUBBLES,
				CONFIG_OPTION_ENABLE_ENTITY_CHAT, CONFIG_OPTION_ENABLE_EMOJIS, CONFIG_OPTION_SPEECHBUBBLE_DURATION,
				CONFIG_OPTION_SPEECHBUBBLE_RANGE, CONFIG_OPTION_ANIMANIA_BADGES);

		@Option(CONFIG_OPTION_ENABLE_SPEECHBUBBLES)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.speech.EnableSpeechBubbles")
		@Comment("Enables/disables speech bubbles above player heads")
		public static boolean enableSpeechBubbles = false;

		@Option(CONFIG_OPTION_ENABLE_ENTITY_CHAT)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.speech.EnableEntityChat")
		@Comment("Enables/disables entity chat bubbles")
		public static boolean enableEntityChat = false;

		@Option(CONFIG_OPTION_ENABLE_EMOJIS)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.speech.EnableEntityEmojis")
		@Comment("Enables/disables entity emojis")
		public static boolean enableEntityEmojis = false;

		@Option(CONFIG_OPTION_SPEECHBUBBLE_DURATION)
		@DefaultValue("7")
		@LangKey("dsurround.cfg.speech.Duration")
		@RangeFloat(min = 5.0F, max = 15.0F)
		@Comment("Number of seconds to display speech before removing")
		public static float speechBubbleDuration = 7.0F;

		@Option(CONFIG_OPTION_SPEECHBUBBLE_RANGE)
		@DefaultValue("16")
		@LangKey("dsurround.cfg.speech.Range")
		@RangeInt(min = 16, max = 32)
		@Comment("Range at which a SpeechBubble is visible.  Filtering occurs server side.")
		public static float speechBubbleRange = 16;

		@Option(CONFIG_OPTION_ANIMANIA_BADGES)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.speech.AnimaniaBadges")
		@Comment("Enable/disable display of food/water badges over Animania mobs")
		@RestartRequired(world = true, server = true)
		public static boolean enableAnimaniaBadges = true;
	}

	public static final String CATEGORY_EXPLOSIONS = "explosions";
	public static final String CONFIG_ENABLE_EXPLOSIONS = "Enable Explosion Enhancement";
	public static final String CONFIG_ADD_MOB_PARTICLES = "Add Mob Models";

	@Category(CATEGORY_EXPLOSIONS)
	@LangKey("dsurround.cfg.explosions.cat.Explosions")
	@Comment("Options for configuring Explosion Enhancement")
	public static class explosions {

		public static String PATH = null;

		@Option(CONFIG_ENABLE_EXPLOSIONS)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.explosions.EnableExplosions")
		@Comment("Enables/disables explosion enhancement")
		public static boolean enableExplosionEnhancement = false;

		@Option(CONFIG_ADD_MOB_PARTICLES)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.explosions.AddMobs")
		@Comment("Enables/disables addition of mob models in explosion debris")
		public static boolean addMobParticles = false;
	}

	public static final String CATEGORY_LIGHT_LEVEL = "lightlevel";
	public static final String CONFIG_LL_RANGE = "Block Range";
	public static final String CONFIG_LL_MOB_SPAWN_THRESHOLD = "Mob Spawn Threshold";
	public static final String CONFIG_LL_DISPLAY_MODE = "Display Mode";
	public static final String CONFIG_LL_HIDE_SAFE = "Hide Safe";
	public static final String CONFIG_LL_INDICATE_CAUTION = "Indicate Caution";
	public static final String CONFIG_LL_COLORS = "Color Set";

	@Category(CATEGORY_LIGHT_LEVEL)
	@LangKey("dsurround.cfg.lightlevel.cat.LightLevel")
	@Comment("Options for configuring Light Level HUD")
	public static class lightlevel {

		public static String PATH = null;
		public static final List<String> SORT = Arrays.asList(CONFIG_LL_RANGE, CONFIG_LL_MOB_SPAWN_THRESHOLD,
				CONFIG_LL_DISPLAY_MODE, CONFIG_LL_HIDE_SAFE, CONFIG_LL_INDICATE_CAUTION, CONFIG_LL_COLORS);

		@Option(CONFIG_LL_RANGE)
		@DefaultValue("24")
		@LangKey("dsurround.cfg.lightlevel.Range")
		@Comment("Range from player to analyze light levels")
		@RangeInt(min = 16, max = 32)
		public static int llBlockRange = 24;

		@Option(CONFIG_LL_MOB_SPAWN_THRESHOLD)
		@DefaultValue("7")
		@LangKey("dsurround.cfg.lightlevel.MobSpawnThreshold")
		@Comment("Light level at which mobs can spawn")
		@RangeInt(min = 0, max = 15)
		public static int llSpawnThreshold = 7;

		@Option(CONFIG_LL_DISPLAY_MODE)
		@DefaultValue("0")
		@LangKey("dsurround.cfg.lightlevel.DisplayMode")
		@Comment("0: Block Light, 1: Block Light + Sky Light")
		@RangeInt(min = 0, max = 1)
		public static int llDisplayMode = 0;

		@Option(CONFIG_LL_HIDE_SAFE)
		@DefaultValue("false")
		@LangKey("dsurround.cfg.lightlevel.HideSafe")
		@Comment("Hide light level information for blocks that are considered safe")
		public static boolean llHideSafe = false;

		@Option(CONFIG_LL_INDICATE_CAUTION)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.lightlevel.IndicateCaution")
		@Comment("Indicate current light levels that will change at night which could result in mob spawns")
		public static boolean llIndicateCaution = true;

		@Option(CONFIG_LL_COLORS)
		@DefaultValue("1")
		@LangKey("dsurround.cfg.lightlevel.Colors")
		@Comment("Color set: 0 bright, 1 dark")
		@RangeInt(min = 0, max = 1)
		public static int llColors = 1;
	}

	public static final String CATEGORY_COMPASS = "compass";
	public static final String CONFIG_COMPASS_ENABLE = "Enable Compass";
	public static final String CONFIG_CLOCK_ENABLE = "Enable Clock";
	public static final String CONFIG_COMPASS_STYLE = "Compass Style";
	public static final String CONFIG_COMPASS_TRANSPARENCY = "Transparency";
	public static final String CONFIG_COMPASS_COORD_FORMAT = "Coord Format";

	@Category(CATEGORY_COMPASS)
	@LangKey("dsurround.cfg.compass.cat.Compass")
	@Comment("Options for configuring compass HUD")
	public static class compass {

		public static String PATH = null;
		public static final List<String> SORT = Arrays.asList(CONFIG_COMPASS_ENABLE, CONFIG_COMPASS_STYLE,
				CONFIG_COMPASS_TRANSPARENCY, CONFIG_COMPASS_COORD_FORMAT, CONFIG_CLOCK_ENABLE);

		@Option(CONFIG_COMPASS_ENABLE)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.compass.Enable")
		@Comment("Enable/disable compass HUD when compass is held")
		public static boolean enableCompass = true;

		@Option(CONFIG_CLOCK_ENABLE)
		@DefaultValue("true")
		@LangKey("dsurround.cfg.compass.ClockEnable")
		@Comment("Enable/disable clock HUD when clock is held")
		public static boolean enableClock = true;

		@Option(CONFIG_COMPASS_STYLE)
		@DefaultValue("0")
		@LangKey("dsurround.cfg.compass.Style")
		@Comment("Style of compass bar")
		@RangeInt(min = 0, max = 6)
		public static int compassStyle = 0;

		@Option(CONFIG_COMPASS_TRANSPARENCY)
		@DefaultValue("0.4")
		@LangKey("dsurround.cfg.compass.Transparency")
		@Comment("Compass transparency")
		@RangeFloat(min = 0F, max = 1.0F)
		public static float compassTransparency = 0.4F;

		@Option(CONFIG_COMPASS_COORD_FORMAT)
		@DefaultValue("x: %1$d, z: %3$d")
		@LangKey("dsurround.cfg.compass.Format")
		@Comment("Format string for location coordinates")
		public static String compassCoordFormat = "x: %1$d, z: %3$d";
	}

	public static final String CATEGORY_COMMANDS = "commands";
	public static final String CONFIG_COMMANDS_DS = "/ds";
	public static final String CONFIG_COMMANDS_CALC = "/calc";
	public static final String CONFIG_COMMAND_NAME = "name";
	public static final String CONFIG_COMMAND_ALIAS = "alias";

	@Category(CATEGORY_COMMANDS)
	@LangKey("dsurround.cfg.commands.cat.Commands")
	@Comment("Options for configuring commands")
	@RestartRequired(server = true, world = true)
	public static class commands {

		public static String PATH = null;

		@Category(CONFIG_COMMANDS_DS)
		public static class ds {

			public static String PATH = null;

			@Option(CONFIG_COMMAND_NAME)
			@DefaultValue("ds")
			@LangKey("dsurround.cfg.commands.DS.Name")
			@Comment("Name of the command")
			public static String commandNameDS = "ds";

			@Option(CONFIG_COMMAND_ALIAS)
			@DefaultValue("dsurround rain")
			@LangKey("dsurround.cfg.commands.DS.Alias")
			@Comment("Alias for the command")
			public static String commandAliasDS = "dsurround rain";
		}

		@Category(CONFIG_COMMANDS_CALC)
		public static class calc {

			public static String PATH = null;

			@Option(CONFIG_COMMAND_NAME)
			@DefaultValue("calc")
			@LangKey("dsurround.cfg.commands.Calc.Name")
			@Comment("Name of the command")
			public static String commandNameCalc = "calc";

			@Option(CONFIG_COMMAND_ALIAS)
			@DefaultValue("c math")
			@LangKey("dsurround.cfg.commands.Calc.Alias")
			@Comment("Alias for the command")
			public static String commandAliasCalc = "c math";
		}
	}

	public static final String CATEGORY_FEATURES = "features";
	public static final String CONFIG_FEATURES_ALLOW_LLHUD = "Allow Light Level HUD";
	public static final String CONFIG_FEATURES_ALLOW_CHUNKBOARDERS = "Allow Chunk Border HUD";
	public static final String CONFIG_FEATURES_ALLOW_COMPASSCLOCK = "Allow Compass and Clock HUD";

	@Category(CATEGORY_FEATURES)
	@Comment("Controls whether features are available")
	@RestartRequired(server = true, world = true)
	public static class features {

		public static String PATH = null;

		@Option(CONFIG_FEATURES_ALLOW_LLHUD)
		@DefaultValue("true")
		@Comment("Allow the Light Level HUD")
		@Hidden
		public static boolean allowLightLevelHUD = true;

		@Option(CONFIG_FEATURES_ALLOW_CHUNKBOARDERS)
		@DefaultValue("true")
		@Comment("Allow the Chunk Border HUD")
		@Hidden
		public static boolean allowChunkBorderHUD = true;

		@Option(CONFIG_FEATURES_ALLOW_COMPASSCLOCK)
		@DefaultValue("true")
		@Comment("Allow the Compass and Clock HUD")
		@Hidden
		public static boolean allowCompassClockHUD = true;
	}

	public static final String CATEGORY_PROFILES = "profiles";

	@Category(CATEGORY_PROFILES)
	@LangKey("dsurround.cfg.profiles.cat.Profiles")
	@Comment("Enable/disable application of built in profiles")
	public static class profiles {

		public static String PATH = null;
		// Dynamically created during initialization
	}

	public static void load(final Configuration config) {

		ConfigProcessor.process(config, ModOptions.class);
		if (ModBase.config() != null)
			Profiles.tickle();

		// Iterate through the config list looking for properties without
		// comments. These will be scrubbed.
		if (ModBase.config() != null)
			for (final String cat : config.getCategoryNames())
				scrubCategory(config.getCategory(cat));
	}

	private static void scrubCategory(final ConfigCategory category) {
		final List<String> killList = new ArrayList<>();
		for (final Entry<String, Property> entry : category.entrySet())
			if (StringUtils.isEmpty(entry.getValue().getComment()))
				killList.add(entry.getKey());

		for (final String kill : killList)
			category.remove(kill);
	}
}
