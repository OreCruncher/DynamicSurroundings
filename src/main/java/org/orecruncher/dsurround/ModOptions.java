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
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.registry.config.Profiles;
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

// http://www.minecraftforge.net/forum/topic/67260-1122-forge-config-system-and-setconfigentryclass/
public final class ModOptions {
	
	protected static final String PREFIX = ModInfo.MOD_ID + ".cfg";

	public static class Trace {
		public static final int SOUND_PLAY = 0x1;
		public static final int FOOTSTEP_ACOUSTIC = 0x2;
	};

	public static final String CATEGORY_ASM = "asm";

	@Category(CATEGORY_ASM)
	@LangKey(asm.PREFIX)
	@Comment("Controls ASM/Mixin transforms at startup")
	public static class asm {
		
		protected static final String PREFIX = ModOptions.PREFIX + ".asm";

		@Option("Enable Weather Control")
		@DefaultValue("true")
		@LangKey(asm.PREFIX + ".EnableWeather")
		@Comment("Enable weather rendering and handling")
		public static boolean enableWeatherASM = true;

		@Option("Disable Arrow Critical Particle Trail")
		@DefaultValue("true")
		@LangKey(asm.PREFIX + ".DisableArrow")
		@Comment("Disable particle trail left by an arrow when it flies")
		public static boolean disableArrowParticleTrail = true;

		@Option("Disable Potion Icons in Inventory Display")
		@DefaultValue("false")
		@LangKey(asm.PREFIX + ".DisablePotionIcons")
		@Comment("Disable Potion Icons in Inventory Display")
		public static boolean disablePotionIconsInInventory = false;
		
		@Option("Enable Search Option in Configuration")
		@DefaultValue("true")
		@LangKey(asm.PREFIX + ".EnableOptionSearch")
		@Comment("Enable search field in config option display")
		public static boolean enableOptionSearchASM = true;
	}

	public static final String CATEGORY_LOGGING_CONTROL = "logging";

	@Category(CATEGORY_LOGGING_CONTROL)
	@LangKey(logging.PREFIX)
	@Comment("Defines how logging will behave")
	public static class logging {

		protected static final String PREFIX = ModOptions.PREFIX + ".logging";

		@Option("Enable Debug Logging")
		@DefaultValue("false")
		@LangKey(logging.PREFIX + ".EnableDebug")
		@Comment("Enables/disables debug log tracing")
		@RestartRequired
		public static boolean enableDebugLogging = false;

		@Option("Enable Online Version Check")
		@DefaultValue("true")
		@LangKey(logging.PREFIX + ".VersionCheck")
		@Comment("Enables/disables display of version check information")
		@RestartRequired
		public static boolean enableVersionChecking = true;

		@Option("Report Server Stats")
		@DefaultValue("false")
		@LangKey(logging.PREFIX + ".ServerStats")
		@Comment("Enables/disables reporting of server TPS and memory stats (has to be enabled server side as well)")
		public static boolean reportServerStats = false;

		@Option("Debug Flag Mask")
		@DefaultValue("0")
		@LangKey(logging.PREFIX + ".FlagMask")
		@Comment("Bitmask for toggling various debug traces")
		@Hidden
		public static int debugFlagMask = 0;
	}

	public static final String CATEGORY_RAIN = "rain";

	@Category(CATEGORY_RAIN)
	@LangKey(rain.PREFIX)
	@Comment("Options that control rain effects in the client")
	public static class rain {

		protected static final String PREFIX = ModOptions.PREFIX + ".rain";

		@Option("Use Vanilla Algorithms")
		@DefaultValue("false")
		@LangKey(rain.PREFIX + ".VanillaRain")
		@Comment("Let Vanilla handle rain intensity and time windows")
		@RestartRequired
		public static boolean doVanillaRain = false;

		@Option("Use Vanilla Rain Sound")
		@DefaultValue("false")
		@LangKey(rain.PREFIX + ".UseVanillaSound")
		@Comment("Use the Vanilla rain sound rather than the modified one")
		@RestartRequired(server = true, world = true)
		public static boolean useVanillaRainSound = false;

		@Option("Style of rain water ripple")
		@DefaultValue("3")
		@LangKey(rain.PREFIX + ".RippleStyle")
		@RangeInt(min = 0, max = 3)
		@Comment("0: original round, 1: darker round, 2: square, 3: pixelated")
		public static int rainRippleStyle = 3;

		@Option("Enable Background Thunder")
		@DefaultValue("true")
		@LangKey(rain.PREFIX + ".EnableThunder")
		@Comment("Allow background thunder when storming")
		public static boolean allowBackgroundThunder = true;

		@Option("Rain Intensity for Background Thunder")
		@DefaultValue("0.75")
		@LangKey(rain.PREFIX + ".ThunderThreshold")
		@RangeFloat(min = 0)
		@Comment("Minimum rain intensity level for background thunder to occur")
		public static float stormThunderThreshold = 0.75F;

		@Option("Default Minimum Rain Strength")
		@DefaultValue("0.0")
		@LangKey(rain.PREFIX + ".MinRainStrength")
		@RangeFloat(min = 0.0F, max = 1.0F)
		@Comment("Default minimum rain strength for a dimension")
		public static float defaultMinRainStrength = 0.0F;

		@Option("Default Maximum Rain Strength")
		@DefaultValue("1.0")
		@LangKey(rain.PREFIX + ".MaxRainStrength")
		@RangeFloat(min = 0.0F, max = 1.0F)
		@Comment("Default maximum rain strength for a dimension")
		public static float defaultMaxRainStrength = 1.0F;

		@Option("Enable Netherrack and Magma Splash Effect")
		@DefaultValue("true")
		@LangKey(rain.PREFIX + ".EnableMagmaNetherrack")
		@Comment("Enable lava particle rain splash effect on Netherrack and Magma blocks")
		public static boolean enableNetherrackMagmaSplashEffect = true;

	}

	public static final String CATEGORY_FOG = "fog";

	@Category(CATEGORY_FOG)
	@LangKey(fog.PREFIX)
	@Comment("Options that control the various fog effects in the client")
	public static class fog {

		protected static final String PREFIX = ModOptions.PREFIX + ".fog";

		@Option("Enable Fog Processing")
		@DefaultValue("true")
		@LangKey(fog.PREFIX + ".Enable")
		@Comment("Enable/disable fog processing")
		public static boolean enableFogProcessing = true;

		@Option("Morning Fog")
		@DefaultValue("true")
		@LangKey(fog.PREFIX + ".EnableMorning")
		@Comment("Show morning fog that eventually burns off")
		public static boolean enableMorningFog = true;

		@Option("Weather Fog")
		@DefaultValue("true")
		@LangKey(fog.PREFIX + ".EnableWeather")
		@Comment("Increase fog based on the strength of rain")
		public static boolean enableWeatherFog = true;

		@Option("Bedrock Fog")
		@DefaultValue("true")
		@LangKey(fog.PREFIX + ".EnableBedrock")
		@Comment("Increase fog at bedrock layers")
		public static boolean enableBedrockFog = true;

		@Option("Desert Fog")
		@DefaultValue("true")
		@LangKey(fog.PREFIX + ".DesertFog")
		@Comment("Enable/disable desert fog when raining")
		public static boolean allowDesertFog = true;

		@Option("Elevation Haze")
		@DefaultValue("true")
		@LangKey(fog.PREFIX + ".ElevationHaze")
		@Comment("Higher the player elevation the more haze that is experienced")
		public static boolean enableElevationHaze = true;

		@Option("Biomes Fog")
		@DefaultValue("true")
		@LangKey(fog.PREFIX + ".BiomeFog")
		@Comment("Enable biome specific fog density and color")
		public static boolean enableBiomeFog = true;

		@Option("Morning Fog Chance")
		@DefaultValue("1")
		@RangeInt(min = 1, max = 10)
		@LangKey(fog.PREFIX + ".MorningFogChance")
		@Comment("Chance morning fog will occurs expressed as 1 in N (1 means always)")
		public static int morningFogChance = 1;
	}

	public static final String CATEGORY_GENERAL = "general";

	@Category(CATEGORY_GENERAL)
	@LangKey(general.PREFIX)
	@Comment("Miscellaneous settings")
	public static class general {

		protected static final String PREFIX = ModOptions.PREFIX + ".general";

		@Option("External Configuration Files")
		@DefaultValue("")
		@LangKey(general.PREFIX + ".ExternalScripts")
		@Comment("Configuration files for customization")
		public static String[] externalScriptFiles = {};

		@Option("Startup Sound List")
		@DefaultValue("minecraft:entity.experience_orb.pickup,minecraft:entity.chicken.egg")
		@LangKey(general.PREFIX + ".StartupSounds")
		@Comment("Possible sounds to play when client finishes loading and reaches the main game menu")
		//@formatter:off
		public static String[] startupSoundList = {
				"minecraft:entity.experience_orb.pickup",
				"minecraft:entity.chicken.egg"
			};
		//@formatter:on

		@Option("Enable Client Chunk Caching")
		@DefaultValue("true")
		@LangKey(general.PREFIX + ".ChunkCaching")
		@Comment("Enable/disable client side chunk caching (performance)")
		public static boolean enableClientChunkCaching = true;
	}

	public static final String CATEGORY_AURORA = "aurora";

	@Category(CATEGORY_AURORA)
	@LangKey(aurora.PREFIX)
	@Comment("Options that control Aurora behavior and rendering")
	public static class aurora {

		protected static final String PREFIX = ModOptions.PREFIX + ".aurora";

		@Option("Enabled")
		@DefaultValue("true")
		@LangKey(aurora.PREFIX + ".EnableAurora")
		@Comment("Enable/disable aurora processing and rendering")
		public static boolean auroraEnable = true;

		@Option("Use Shaders")
		@DefaultValue("true")
		@LangKey(aurora.PREFIX + ".EnableShader")
		@Comment("Use shader when rendering aurora")
		@RestartRequired(world = true)
		public static boolean auroraUseShader = true;

		@Option("Maximum Bands")
		@DefaultValue("3")
		@LangKey(aurora.PREFIX + ".MaxBands")
		@Comment("Maximum number of bands to render")
		@RangeInt(min = 1, max = 3)
		public static int maxBands = 3;
	}

	public static final String CATEGORY_BIOMES = "biomes";

	@Category(CATEGORY_BIOMES)
	@LangKey(biomes.PREFIX)
	@Comment("Options for controlling biome sound/effects")
	public static class biomes {

		protected static final String PREFIX = ModOptions.PREFIX + ".biomes";

		@Option("Overworld Sealevel Override")
		@DefaultValue("0")
		@LangKey(biomes.PREFIX + ".Sealevel")
		@RangeInt(min = 0, max = 255)
		@Comment("Sealevel to set for Overworld (0 use default for World)")
		public static int worldSealevelOverride = 0;

		@Option("Biomes Alias")
		@DefaultValue("")
		@LangKey(biomes.PREFIX + ".Aliases")
		@Comment("Biomes alias list")
		public static String[] biomeAliases = {};

		@Option("Dimension Blacklist")
		@DefaultValue("")
		@LangKey(biomes.PREFIX + ".DimBlacklist")
		@Comment("Dimension IDs where biome sounds will not be played")
		public static String[] dimensionBlacklist = {};
	}

	public static final String CATEGORY_EFFECTS = "effects";

	@Category(CATEGORY_EFFECTS)
	@LangKey(effects.PREFIX)
	@Comment("Options for controlling various effects")
	public static class effects {

		protected static final String PREFIX = ModOptions.PREFIX + ".effects";

		@Option("Special Effect Range")
		@DefaultValue("24")
		@LangKey(effects.PREFIX + ".FXRange")
		@RangeInt(min = 16, max = 64)
		@Comment("Block radius/range around player for special effect application")
		public static int specialEffectRange = 24;

		@Option("Disable Water Suspend Particles")
		@DefaultValue("false")
		@LangKey(effects.PREFIX + ".Suspend")
		@Comment("Enable/disable water depth particle effect")
		@RestartRequired(server = true)
		public static boolean disableWaterSuspendParticle = false;

		@Option("Waterfall Cutoff")
		@DefaultValue("0")
		@LangKey(effects.PREFIX + ".WaterfallCutoff")
		@RangeInt(min = 0, max = 16)
		@Comment("Waterfall strength below which sounds will not play (> 10 to turn off)")
		public static int waterfallCutoff = 0;

		@Option("Enable Steam")
		@DefaultValue("true")
		@LangKey(effects.PREFIX + ".Steam")
		@Comment("Enable Steam Jets where lava meets water")
		public static boolean enableSteamJets = true;

		@Option("Enable FireJetEffect Jets")
		@DefaultValue("true")
		@LangKey(effects.PREFIX + ".Fire")
		@Comment("Enable FireJetEffect Jets in lava")
		public static boolean enableFireJets = true;

		@Option("Enable Bubbles")
		@DefaultValue("true")
		@LangKey(effects.PREFIX + ".Bubble")
		@Comment("Enable BubbleJetEffect Jets under water")
		public static boolean enableBubbleJets = true;

		@Option("Enable DustJetEffect Motes")
		@DefaultValue("true")
		@LangKey(effects.PREFIX + ".Dust")
		@Comment("Enable DustJetEffect motes dropping from blocks")
		public static boolean enableDustJets = true;

		@Option("Enable FountainJetEffect")
		@DefaultValue("true")
		@LangKey(effects.PREFIX + ".Fountain")
		@Comment("Enable FountainJetEffect jets")
		public static boolean enableFountainJets = true;

		@Option("Enable Fireflies")
		@DefaultValue("true")
		@LangKey(effects.PREFIX + ".Fireflies")
		@Comment("Enable Firefly effect around plants")
		public static boolean enableFireflies = true;

		@Option("Enable Water Splash")
		@DefaultValue("true")
		@LangKey(effects.PREFIX + ".Splash")
		@Comment("Enable Water Splash effects when water spills down")
		public static boolean enableWaterSplash = true;

		@Option("Damage Popoffs")
		@DefaultValue("true")
		@LangKey(effects.PREFIX + ".Popoffs")
		@Comment("Controls display of damage pop-offs when an entity is damaged/healed")
		public static boolean enableDamagePopoffs = true;

		@Option("Show Crit Words")
		@DefaultValue("true")
		@LangKey(effects.PREFIX + ".CritWords")
		@Comment("Display random power word on critical hit")
		public static boolean showCritWords = true;

		@Option("Footprints")
		@DefaultValue("true")
		@LangKey(effects.PREFIX + ".Footprints")
		@Comment("Enable player footprints")
		public static boolean enableFootprints = true;

		@Option("Footprint Style")
		@DefaultValue("6")
		@LangKey(effects.PREFIX + ".FootprintStyle")
		@Comment("0: shoe print, 1: square print, 2: horse hoof, 3: bird, 4: paw, 5: solid square, 6: lowres square")
		@RangeInt(min = 0, max = 6)
		public static int footprintStyle = 6;

		@Option("Show Frost Breath")
		@DefaultValue("true")
		@LangKey(effects.PREFIX + ".ShowBreath")
		@Comment("Show player breath in cold weather and underwater")
		public static boolean showBreath = true;
	}

	public static final String CATEGORY_SOUND = "sound";
	public static final String CONFIG_SOUND_SETTINGS = "Sound Settings";

	@Category(CATEGORY_SOUND)
	@LangKey(sound.PREFIX)
	@Comment("General options for defining sound effects")
	public static class sound {

		protected static final String PREFIX = ModOptions.PREFIX + ".sound";

		@Option("Battle Music")
		@DefaultValue("false")
		@LangKey(sound.PREFIX + ".BattleMusic")
		@Comment("Enable/disable Battle Music (must also have BattleMusic resource pack installed to hear)")
		public static boolean enableBattleMusic = false;

		@Option("Enable Biomes Sounds")
		@DefaultValue("true")
		@LangKey(sound.PREFIX + ".BiomeSounds")
		@Comment("Enable biome background and spot sounds")
		public static boolean enableBiomeSounds = true;

		@Option("Autoconfigure Channels")
		@DefaultValue("true")
		@LangKey(sound.PREFIX + ".AutoConfig")
		@Comment("Automatically configure sound channels")
		@RestartRequired(server = true)
		public static boolean autoConfigureChannels = true;

		@Option("Number Normal Channels")
		@DefaultValue("28")
		@LangKey(sound.PREFIX + ".NormalChannels")
		@RangeInt(min = 28, max = 255)
		@Comment("Number of normal sound channels to configure in the sound system (manual)")
		@RestartRequired(server = true)
		public static int normalSoundChannelCount = 28;

		@Option("Number Streaming Channels")
		@DefaultValue("4")
		@LangKey(sound.PREFIX + ".StreamingChannels")
		@RangeInt(min = 4, max = 255)
		@Comment("Number of streaming sound channels to configure in the sound system (manual)")
		@RestartRequired(server = true)
		public static int streamingSoundChannelCount = 4;

		@Option("Stream Buffer Size")
		@DefaultValue("32")
		@LangKey(sound.PREFIX + ".StreamBufferSize")
		@RangeInt(min = 0)
		@Comment("Size of a stream buffer in kilobytes (0: system default - usually 128K bytes)")
		@RestartRequired(server = true)
		public static int streamBufferSize = 32;

		@Option("Number of Stream Buffers per Channel")
		@DefaultValue("0")
		@LangKey(sound.PREFIX + ".StreamBufferCount")
		@RangeInt(min = 0, max = 8)
		@Comment("Number of stream buffers per channel (0: system default - usually 3 buffers)")
		@RestartRequired(server = true)
		public static int streamBufferCount = 0;

		@Option("Mute when Background")
		@DefaultValue("true")
		@LangKey(sound.PREFIX + ".Mute")
		@Comment("Mute sound when Minecraft is in the background")
		public static boolean muteWhenBackground = true;

		@Option("Thunder Volume")
		@DefaultValue("10000")
		@LangKey(sound.PREFIX + ".ThunderVolume")
		@Comment("Sound Volume of Thunder")
		@RangeFloat(min = 15F, max = 10000F)
		public static float thunderVolume = 10000F;

		@Option("Jump Sound")
		@DefaultValue("false")
		@LangKey(sound.PREFIX + ".Jump")
		@Comment("Enable player Jump sound effect")
		public static boolean enableJumpSound = false;

		@Option("Equip Sound")
		@DefaultValue("true")
		@LangKey(sound.PREFIX + ".Equip")
		@Comment("Enable Weapon/Tool Equip sound effect")
		public static boolean enableEquipSound = true;

		@Option("Sword Equip as Tool")
		@DefaultValue("false")
		@LangKey(sound.PREFIX + ".SwordEquipAsTool")
		@Comment("Enable Sword Equip sound as Tool")
		@RestartRequired(world = true, server = true)
		public static boolean swordEquipAsTool = false;

		@Option("Crafting Sound")
		@DefaultValue("true")
		@LangKey(sound.PREFIX + ".Craft")
		@Comment("Enable Item Crafted sound effect")
		public static boolean enableCraftingSound = true;

		@Option("Footsteps as Quadruped")
		@DefaultValue("false")
		@LangKey(sound.PREFIX + ".FootstepQuad")
		@Comment("Simulate quadruped with Footstep effects (horse)")
		public static boolean foostepsQuadruped = false;

		@Option("First Person Footstep Cadence")
		@DefaultValue("true")
		@LangKey(sound.PREFIX + ".FootstepCadence")
		@Comment("true to match first person arm swing; false to match 3rd person leg animation")
		public static boolean firstPersonFootstepCadence = true;

		@Option("Armor Sound")
		@DefaultValue("true")
		@LangKey(sound.PREFIX + ".Armor")
		@Comment("Enable/disable armor sounds when moving")
		public static boolean enableArmorSounds = true;

		@Option("Swing Sound")
		@DefaultValue("true")
		@LangKey(sound.PREFIX + ".Swing")
		@Comment("Enable/disable item swing sounds")
		public static boolean enableSwingSounds = true;

		@Option("Rain Puddle Sound")
		@DefaultValue("true")
		@LangKey(sound.PREFIX + ".Puddle")
		@Comment("Enable/disable rain puddle sound when moving in the rain")
		public static boolean enablePuddleSound = true;

		@Option("Sound Culling Threshold")
		@DefaultValue("20")
		@LangKey(sound.PREFIX + ".CullInterval")
		@RangeInt(min = 0)
		@Comment("Ticks between culled sound events (0 to disable culling)")
		public static int soundCullingThreshold = 20;

		@Option(CONFIG_SOUND_SETTINGS)
		@Hidden
		@DefaultValue("minecraft:block.water.ambient cull,minecraft:block.lava.ambient cull,minecraft:entity.sheep.ambient cull,minecraft:entity.chicken.ambient cull,minecraft:entity.cow.ambient cull,minecraft:entity.pig.ambient cull,dsurround:bison block,dsurround:elephant block,dsurround:gnatt block,dsurround:insectbuzz block,dsurround:hiss block,dsurround:rattlesnake block")
		@LangKey(sound.PREFIX + ".SoundSettings")
		@Comment("Configure how each individual sound will be handled (block, cull, and volume scale)")
		//@formatter:off
		public static String[] soundSettings = {
				"minecraft:block.water.ambient cull",
				"minecraft:block.lava.ambient cull",
				"minecraft:entity.sheep.ambient cull",
				"minecraft:entity.chicken.ambient cull",
				"minecraft:entity.cow.ambient cull",
				"minecraft:entity.pig.ambient cull",
				"dsurround:bison block",
				"dsurround:elephant block",
				"dsurround:gnatt block",
				"dsurround:insectbuzz block",
				"dsurround:hiss block",
				"dsurround:rattlesnake block"
			};
		//@formatter:on
	}

	public static final String CATEGORY_PLAYER = "player";

	@Category(CATEGORY_PLAYER)
	@LangKey(player.PREFIX)
	@Comment("General options for defining sound and effects the player entity")
	public static class player {

		protected static final String PREFIX = ModOptions.PREFIX + ".player";

		@Option("Suppress Potion Particles")
		@DefaultValue("false")
		@LangKey(player.PREFIX + ".PotionParticles")
		@Comment("Suppress rendering of player's potion particles")
		public static boolean suppressPotionParticles = false;

		@Option("Hurt Threshold")
		@DefaultValue("0.25")
		@LangKey(player.PREFIX + ".HurtThreshold")
		@Comment("Percentage of player health bar remaining to trigger player hurt sound (0 disable)")
		@RangeFloat(min = 0, max = 0.5F)
		public static float playerHurtThreshold = 0.25F;

		@Option("Hunger Threshold")
		@DefaultValue("8")
		@LangKey(player.PREFIX + ".HungerThreshold")
		@Comment("Amount of food bar remaining to trigger player hunger sound (0 disable)")
		@RangeInt(min = 0, max = 10)
		public static int playerHungerThreshold = 8;
	}

	public static final String CATEGORY_SPEECHBUBBLES = "speechbubbles";

	@Category(CATEGORY_SPEECHBUBBLES)
	@LangKey(speechbubbles.PREFIX)
	@Comment("Options for configuring SpeechBubbles")
	public static class speechbubbles {

		protected static final String PREFIX = ModOptions.PREFIX + ".speech";

		@Option("Enable SpeechBubbles")
		@DefaultValue("false")
		@LangKey(speechbubbles.PREFIX + ".EnableSpeechBubbles")
		@Comment("Enables/disables speech bubbles above player heads (needs to be enabled server side as well)")
		public static boolean enableSpeechBubbles = false;

		@Option("Enable Entity Chat")
		@DefaultValue("false")
		@LangKey(speechbubbles.PREFIX + ".EnableEntityChat")
		@Comment("Enables/disables entity chat bubbles")
		public static boolean enableEntityChat = false;

		@Option("Display Duration")
		@DefaultValue("7")
		@LangKey(speechbubbles.PREFIX + ".Duration")
		@RangeFloat(min = 5.0F, max = 15.0F)
		@Comment("Number of seconds to display speech before removing")
		public static float speechBubbleDuration = 7.0F;

		@Option("Visibility Range")
		@DefaultValue("16")
		@LangKey(speechbubbles.PREFIX + ".Range")
		@RangeInt(min = 16, max = 32)
		@Comment("Range at which a SpeechBubble is visible (filtering occurs server side)")
		public static float speechBubbleRange = 16;

		@Option("Animania Badges")
		@DefaultValue("true")
		@LangKey(speechbubbles.PREFIX + ".AnimaniaBadges")
		@Comment("Enable/disable display of food/water badges over Animania mobs")
		@RestartRequired(world = true, server = true)
		public static boolean enableAnimaniaBadges = true;
	}

	public static final String CATEGORY_COMMANDS = "commands";

	@Category(CATEGORY_COMMANDS)
	@LangKey(commands.PREFIX)
	@Comment("Options for configuring commands")
	@RestartRequired(server = true, world = true)
	public static class commands {

		protected static final String PREFIX = ModOptions.PREFIX + ".commands";

		@Category("/ds")
		public static class ds {

			protected static final String PREFIX = commands.PREFIX + ".DS";

			@Option("name")
			@DefaultValue("ds")
			@LangKey(ds.PREFIX + ".Name")
			@Comment("Name of the command")
			public static String commandNameDS = "ds";

			@Option("alias")
			@DefaultValue("dsurround rain")
			@LangKey(ds.PREFIX + ".Alias")
			@Comment("Alias for the command")
			public static String commandAliasDS = "dsurround rain";
		}

		@Category("/calc")
		public static class calc {

			protected static final String PREFIX = commands.PREFIX + ".Calc";

			@Option("name")
			@DefaultValue("calc")
			@LangKey(calc.PREFIX + ".Name")
			@Comment("Name of the command")
			public static String commandNameCalc = "calc";

			@Option("alias")
			@DefaultValue("c math")
			@LangKey(calc.PREFIX + ".Alias")
			@Comment("Alias for the command")
			public static String commandAliasCalc = "c math";
		}
	}

	public static final String CATEGORY_PROFILES = "profiles";

	@Category(CATEGORY_PROFILES)
	@LangKey(profiles.PREFIX)
	@Comment("Enable/disable application of built in profiles")
	public static class profiles {

		protected static final String PREFIX = ModOptions.PREFIX + ".profiles";
		// Dynamically created during initialization
	}

	public static void load(final Configuration config) {

		ConfigProcessor.process(config, ModOptions.class);
		if (ModBase.config() != null)
			Profiles.tickle();

		// Iterate through the config list looking for properties without
		// comments. These will be scrubbed.
		if (ModBase.config() != null)
			for (final String cat : config.getCategoryNames()) {
				scrubCategory(config.getCategory(cat));
			}
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
