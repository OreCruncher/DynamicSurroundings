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

package org.blockartistry.mod.DynSurround;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.util.ConfigProcessor;
import org.blockartistry.mod.DynSurround.util.ConfigProcessor.Comment;
import org.blockartistry.mod.DynSurround.util.ConfigProcessor.Hidden;
import org.blockartistry.mod.DynSurround.util.ConfigProcessor.MinMaxFloat;
import org.blockartistry.mod.DynSurround.util.ConfigProcessor.MinMaxInt;
import org.blockartistry.mod.DynSurround.util.ConfigProcessor.Parameter;
import org.blockartistry.mod.DynSurround.util.ConfigProcessor.RestartRequired;

import com.google.common.collect.ImmutableList;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public final class ModOptions {

	private ModOptions() {
	}

	public static final String CATEGORY_LOGGING_CONTROL = "logging";
	public static final String CONFIG_ENABLE_DEBUG_LOGGING = "Enable Debug Logging";
	public static final String CONFIG_ENABLE_ONLINE_VERSION_CHECK = "Enable Online Version Check";
	private static final List<String> loggingSort = Arrays.asList(CONFIG_ENABLE_ONLINE_VERSION_CHECK,
			CONFIG_ENABLE_DEBUG_LOGGING);

	@Parameter(category = CATEGORY_LOGGING_CONTROL, property = CONFIG_ENABLE_DEBUG_LOGGING, defaultValue = "false")
	@Comment("Enables/disables debug logging of the mod")
	@RestartRequired
	public static boolean enableDebugLogging = false;
	@Parameter(category = CATEGORY_LOGGING_CONTROL, property = CONFIG_ENABLE_ONLINE_VERSION_CHECK, defaultValue = "true")
	@Comment("Enables/disables online version checking")
	@RestartRequired
	public static boolean enableVersionChecking = true;

	public static final String CATEGORY_RAIN = "rain";
	public static final String CONFIG_RAIN_VOLUME = "Sound Level";
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

	private static final List<String> rainSort = Arrays.asList(CONFIG_RAIN_VOLUME, CONFIG_ALLOW_DESERT_DUST,
			CONFIG_RESET_RAIN_ON_SLEEP, CONFIG_RAIN_PARTICLE_BASE, CONFIG_RAIN_ACTIVE_TIME_CONST,
			CONFIG_RAIN_ACTIVE_TIME_VARIABLE, CONFIG_RAIN_INACTIVE_TIME_CONST, CONFIG_RAIN_INACTIVE_TIME_VARIABLE,
			CONFIG_STORM_ACTIVE_TIME_CONST, CONFIG_STORM_ACTIVE_TIME_VARIABLE, CONFIG_STORM_INACTIVE_TIME_CONST,
			CONFIG_STORM_INACTIVE_TIME_VARIABLE);

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_RAIN_VOLUME, defaultValue = "1.0")
	@MinMaxFloat(min = 0.0F, max = 1.0F)
	@Comment("Volume scale factor to apply to rain sound level to adjust")
	public static float soundLevel = 1.0F;
	@Parameter(category = CATEGORY_RAIN, property = CONFIG_RAIN_PARTICLE_BASE, defaultValue = "100")
	@MinMaxInt(min = 0, max = 500)
	@Comment("Base count of rain splash particles to generate per tick")
	public static int particleCountBase = 100;
	@Parameter(category = CATEGORY_RAIN, property = CONFIG_RESET_RAIN_ON_SLEEP, defaultValue = "true")
	@Comment("Reset rain/thunder when all players sleep")
	public static boolean resetRainOnSleep = true;

	@Parameter(category = CATEGORY_RAIN, property = CONFIG_RAIN_ACTIVE_TIME_CONST, defaultValue = "12000")
	@MinMaxInt(min = 0)
	@Comment("Base time rain is active, in ticks")
	public static int rainActiveTimeConst = 12000;
	@Parameter(category = CATEGORY_RAIN, property = CONFIG_RAIN_ACTIVE_TIME_VARIABLE, defaultValue = "12000")
	@MinMaxInt(min = 0)
	@Comment("Variable amount of ticks rain is active, added to the base")
	public static int rainActiveTimeVariable = 12000;
	@Parameter(category = CATEGORY_RAIN, property = CONFIG_RAIN_INACTIVE_TIME_CONST, defaultValue = "12000")
	@MinMaxInt(min = 0)
	@Comment("Base time rain is inactive, in ticks")
	public static int rainInactiveTimeConst = 12000;
	@Parameter(category = CATEGORY_RAIN, property = CONFIG_RAIN_INACTIVE_TIME_VARIABLE, defaultValue = "168000")
	@MinMaxInt(min = 0)
	@Comment("Variable amount of ticks rain is inactive, added to the base")
	public static int rainInactiveTimeVariable = 168000;
	@Parameter(category = CATEGORY_RAIN, property = CONFIG_STORM_ACTIVE_TIME_CONST, defaultValue = "3600")
	@MinMaxInt(min = 0)
	@Comment("Base time storm (thunder) is active, in ticks")
	public static int stormActiveTimeConst = 3600;
	@Parameter(category = CATEGORY_RAIN, property = CONFIG_STORM_ACTIVE_TIME_VARIABLE, defaultValue = "12000")
	@MinMaxInt(min = 0)
	@Comment("Variable amount of ticks storm (thunder) is active, added to the base")
	public static int stormActiveTimeVariable = 12000;
	@Parameter(category = CATEGORY_RAIN, property = CONFIG_STORM_INACTIVE_TIME_CONST, defaultValue = "12000")
	@MinMaxInt(min = 0)
	@Comment("Base time storm (thunder) is inactive, in ticks")
	public static int stormInactiveTimeConst = 12000;
	@Parameter(category = CATEGORY_RAIN, property = CONFIG_STORM_INACTIVE_TIME_VARIABLE, defaultValue = "168000")
	@MinMaxInt(min = 0)
	@Comment("Variable amount of ticks storm (thunder) is inactive, added to the base")
	public static int stormInactiveTimeVariable = 12000;

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

	@Parameter(category = CATEGORY_FOG, property = CONFIG_ALLOW_DESERT_FOG, defaultValue = "true")
	@Comment("Allow desert fog when raining")
	public static boolean allowDesertFog = true;
	@Parameter(category = CATEGORY_FOG, property = CONFIG_DESERT_FOG_FACTOR, defaultValue = "1.0")
	@MinMaxFloat(min = 0.0F, max = 5.0F)
	@Comment("Visibility factor to apply to desert fog (higher is thicker)")
	public static float desertFogFactor = 1.0F;
	@Parameter(category = CATEGORY_FOG, property = CONFIG_ENABLE_ELEVATION_HAZE, defaultValue = "true")
	@Comment("Higher the player elevation the more haze that is experienced")
	public static boolean enableElevationHaze = true;
	@Parameter(category = CATEGORY_FOG, property = CONFIG_ELEVATION_HAZE_FACTOR, defaultValue = "1.0")
	@MinMaxFloat(min = 0.0F, max = 5.0F)
	@Comment("Visibility factor to apply to elevation haze (higher is thicker)")
	public static float elevationHazeFactor = 1.0F;
	@Parameter(category = CATEGORY_FOG, property = CONFIG_ELEVATION_HAZE_AS_BAND, defaultValue = "true")
	@Comment("Calculate haze as a band at cloud height rather than gradient to build height")
	public static boolean elevationHazeAsBand = true;
	@Parameter(category = CATEGORY_FOG, property = CONFIG_ENABLE_BIOME_FOG, defaultValue = "true")
	@Comment("Enable biome specific fog density and color")
	public static boolean enableBiomeFog = true;
	@Parameter(category = CATEGORY_FOG, property = CONFIG_BIOME_FOG_FACTOR, defaultValue = "1.0")
	@MinMaxFloat(min = 0.0F, max = 5.0F)
	@Comment("Visibility factor to apply to biome fog (higher is thicker)")
	public static float biomeFogFactor = 1.0F;

	public static final String CATEGORY_GENERAL = "general";
	public static final String CONFIG_EXTERNAL_SCRIPTS = "External Configuration Files";
	public static final String CONFIG_MIN_RAIN_STRENGTH = "Default Minimum Rain Strength";
	public static final String CONFIG_MAX_RAIN_STRENGTH = "Default Maximum Rain Strength";
	public static final String CONFIG_THUNDER_THRESHOLD = "Default Thunder Effect Threshold";
	public static final String CONFIG_FX_RANGE = "Special Effect Range";
	private static final List<String> generalSort = ImmutableList.<String> builder().add(CONFIG_FX_RANGE,
			CONFIG_MIN_RAIN_STRENGTH, CONFIG_MAX_RAIN_STRENGTH, CONFIG_THUNDER_THRESHOLD, CONFIG_EXTERNAL_SCRIPTS)
			.build();

	@Parameter(category = CATEGORY_GENERAL, property = CONFIG_MIN_RAIN_STRENGTH, defaultValue = "0.0")
	@MinMaxFloat(min = 0.0F, max = 1.0F)
	@Comment("Default minimum rain strength for a dimension")
	public static float defaultMinRainStrength = 0.0F;
	@Parameter(category = CATEGORY_GENERAL, property = CONFIG_MAX_RAIN_STRENGTH, defaultValue = "1.0")
	@MinMaxFloat(min = 0.0F, max = 1.0F)
	@Comment("Default maximum rain strength for a dimension")
	public static float defaultMaxRainStrength = 1.0F;
	@Parameter(category = CATEGORY_GENERAL, property = CONFIG_THUNDER_THRESHOLD, defaultValue = "0.5")
	@MinMaxFloat(min = 0.0F, max = 1.0F)
	@Comment("Rain strength threshold for when thunder can be triggered")
	public static float defaultThunderThreshold = 0.5F;
	@Parameter(category = CATEGORY_GENERAL, property = CONFIG_FX_RANGE, defaultValue = "16")
	@MinMaxInt(min = 16, max = 64)
	@Comment("Block radius/range around player for special effect application")
	public static int specialEffectRange = 16;
	@Parameter(category = CATEGORY_GENERAL, property = CONFIG_EXTERNAL_SCRIPTS, defaultValue = "")
	@Comment("Configuration files for customization")
	@RestartRequired
	public static String[] externalScriptFiles = {};

	public static final String CATEGORY_AURORA = "aurora";
	public static final String CONFIG_AURORA_ENABLED = "Enabled";
	public static final String CONFIG_Y_PLAYER_RELATIVE = "Height Player Relative";
	public static final String CONFIG_PLAYER_FIXED_HEIGHT = "Player Fixed Height";
	public static final String CONFIG_MULTIPLE_BANDS = "Multiple Bands";
	public static final String CONFIG_AURORA_ANIMATE = "Animate";
	public static final String CONFIG_AURORA_SPAWN_OFFSET = "Spawn Offset";
	private static final List<String> auroraSort = Arrays.asList(CONFIG_AURORA_ENABLED, CONFIG_AURORA_ANIMATE,
			CONFIG_MULTIPLE_BANDS, CONFIG_Y_PLAYER_RELATIVE, CONFIG_PLAYER_FIXED_HEIGHT, CONFIG_AURORA_SPAWN_OFFSET);

	@Parameter(category = CATEGORY_AURORA, property = CONFIG_AURORA_ENABLED, defaultValue = "true")
	@Comment("Whether to enable Aurora processing on server/client")
	@RestartRequired
	public static boolean auroraEnable = true;
	@Parameter(category = CATEGORY_AURORA, property = CONFIG_Y_PLAYER_RELATIVE, defaultValue = "true")
	@Comment("true to keep the aurora at a height above player; false to fix it to an altitude")
	public static boolean auroraHeightPlayerRelative = true;
	@Parameter(category = CATEGORY_AURORA, property = CONFIG_PLAYER_FIXED_HEIGHT, defaultValue = "64.0")
	@MinMaxFloat(min = 16.0F, max = 2048.0F)
	@Comment("Number of blocks to say fixed above player if Aurora is player relative")
	public static float playerFixedHeight = 64.0F;
	@Parameter(category = CATEGORY_AURORA, property = CONFIG_MULTIPLE_BANDS, defaultValue = "true")
	@Comment("Allow Auroras with multiple bands")
	public static boolean auroraMultipleBands = true;
	@Parameter(category = CATEGORY_AURORA, property = CONFIG_AURORA_ANIMATE, defaultValue = "true")
	@Comment("Animate Aurora so it waves")
	public static boolean auroraAnimate = true;
	@Parameter(category = CATEGORY_AURORA, property = CONFIG_AURORA_SPAWN_OFFSET, defaultValue = "150")
	@MinMaxInt(min = 0, max = 200)
	@Comment("Number of blocks north of player location to spawn an aurora")
	public static int auroraSpawnOffset = 150;

	public static final String CATEGORY_BIOMES = "biomes";
	public static final String CONFIG_BIOME_ALIASES = "Biomes Alias";
	private static final List<String> biomesSort = Arrays.asList(CONFIG_BIOME_ALIASES);

	@Parameter(category = CATEGORY_BIOMES, property = CONFIG_BIOME_ALIASES, defaultValue = "")
	@Comment("Biomes alias list")
	@RestartRequired
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
	@Parameter(category = CATEGORY_BLOCK_EFFECTS, property = CONFIG_BLOCK_EFFECT_STEAM, defaultValue = "true")
	@Comment("Enable Steam Jets where lava meets water")
	@RestartRequired
	public static boolean enableSteamJets = true;
	@Parameter(category = CATEGORY_BLOCK_EFFECTS, property = CONFIG_BLOCK_EFFECT_FIRE, defaultValue = "true")
	@Comment("Enable FireJetEffect Jets in lava")
	@RestartRequired
	public static boolean enableFireJets = true;
	@Parameter(category = CATEGORY_BLOCK_EFFECTS, property = CONFIG_BLOCK_EFFECT_BUBBLE, defaultValue = "true")
	@Comment("Enable BubbleJetEffect Jets under water")
	@RestartRequired
	public static boolean enableBubbleJets = true;
	@Parameter(category = CATEGORY_BLOCK_EFFECTS, property = CONFIG_BLOCK_EFFECT_DUST, defaultValue = "true")
	@Comment("Enable DustJetEffect motes dropping from blocks")
	@RestartRequired
	public static boolean enableDustJets = true;
	@Parameter(category = CATEGORY_BLOCK_EFFECTS, property = CONFIG_BLOCK_EFFECT_FOUNTAIN, defaultValue = "true")
	@Comment("Enable FountainJetEffect jets")
	@RestartRequired
	public static boolean enableFountainJets = true;
	@Parameter(category = CATEGORY_BLOCK_EFFECTS, property = CONFIG_BLOCK_EFFECT_FIREFLY, defaultValue = "true")
	@Comment("Enable Firefly effect around plants")
	@RestartRequired
	public static boolean enableFireflies = true;
	@Parameter(category = CATEGORY_BLOCK_EFFECTS, property = CONFIG_BLOCK_EFFECT_SPLASH, defaultValue = "true")
	@Comment("Enable Water Splash effects when water spills down")
	@RestartRequired
	public static boolean enableWaterSplash = true;

	public static final String CATEGORY_SOUND = "sound";
	public static final String CONFIG_ENABLE_BIOME_SOUNDS = "Enable Biomes Sounds";
	public static final String CONFIG_MASTER_SOUND_FACTOR = "Master Sound Scale Factor";
	public static final String CONFIG_AUTO_CONFIG_CHANNELS = "Autoconfigure Channels";
	public static final String CONFIG_NORMAL_CHANNEL_COUNT = "Number Normal Channels";
	public static final String CONFIG_STREAMING_CHANNEL_COUNT = "Number Streaming Channels";
	public static final String CONFIG_ENABLE_JUMP_SOUND = "Jump Sound";
	public static final String CONFIG_ENABLE_SWING_SOUND = "Swing Sound";
	public static final String CONFIG_ENABLE_CRAFTING_SOUND = "Crafting Sound";
	public static final String CONFIG_ENABLE_BOW_PULL_SOUND = "Bow Pull Sound";
	public static final String CONFIG_ENABLE_FOOTSTEPS_SOUND = "FootstepsRegistry";
	public static final String CONFIG_FOOTSTEPS_SOUND_FACTOR = "FootstepsRegistry Sound Factor";
	public static final String CONFIG_SOUND_CULL_THRESHOLD = "Sound Culling Threshold";
	public static final String CONFIG_CULLED_SOUNDS = "Culled Sounds";
	public static final String CONFIG_BLOCKED_SOUNDS = "Blocked Sounds";
	public static final String CONFIG_SOUND_VOLUMES = "Sound Volume";
	private static final List<String> soundsSort = Arrays.asList(CONFIG_ENABLE_BIOME_SOUNDS, CONFIG_MASTER_SOUND_FACTOR,
			CONFIG_ENABLE_FOOTSTEPS_SOUND, CONFIG_FOOTSTEPS_SOUND_FACTOR, CONFIG_ENABLE_JUMP_SOUND,
			CONFIG_ENABLE_SWING_SOUND, CONFIG_ENABLE_CRAFTING_SOUND, CONFIG_ENABLE_BOW_PULL_SOUND,
			CONFIG_AUTO_CONFIG_CHANNELS, CONFIG_NORMAL_CHANNEL_COUNT, CONFIG_STREAMING_CHANNEL_COUNT,
			CONFIG_BLOCKED_SOUNDS, CONFIG_SOUND_CULL_THRESHOLD, CONFIG_CULLED_SOUNDS, CONFIG_SOUND_VOLUMES);

	@Parameter(category = CATEGORY_SOUND, property = CONFIG_ENABLE_BIOME_SOUNDS, defaultValue = "true")
	@Comment("Enable biome background and spot sounds")
	@RestartRequired
	public static boolean enableBiomeSounds = true;
	@Parameter(category = CATEGORY_SOUND, property = CONFIG_MASTER_SOUND_FACTOR, defaultValue = "0.5")
	@MinMaxFloat(min = 0.0F, max = 1.0F)
	@Comment("Master volume scale factor for biome and block sounds")
	public static float masterSoundScaleFactor = 0.5F;
	@Parameter(category = CATEGORY_SOUND, property = CONFIG_AUTO_CONFIG_CHANNELS, defaultValue = "true")
	@Comment("Automatically configure sound channels")
	@RestartRequired
	public static boolean autoConfigureChannels = true;
	@Parameter(category = CATEGORY_SOUND, property = CONFIG_NORMAL_CHANNEL_COUNT, defaultValue = "28")
	@MinMaxInt(min = 28)
	@Comment("Number of normal sound channels to configure in the sound system (manual)")
	@RestartRequired
	public static int normalSoundChannelCount = 28;
	@Parameter(category = CATEGORY_SOUND, property = CONFIG_STREAMING_CHANNEL_COUNT, defaultValue = "4")
	@MinMaxInt(min = 4)
	@Comment("Number of streaming sound channels to configure in the sound system (manual)")
	@RestartRequired
	public static int streamingSoundChannelCount = 4;
	@Parameter(category = CATEGORY_SOUND, property = CONFIG_ENABLE_JUMP_SOUND, defaultValue = "true")
	@Comment("Enable player Jump sound effect")
	@RestartRequired
	public static boolean enableJumpSound = true;
	@Parameter(category = CATEGORY_SOUND, property = CONFIG_ENABLE_SWING_SOUND, defaultValue = "true")
	@Comment("Enable Weapon Swing sound effect")
	@RestartRequired
	public static boolean enableSwingSound = true;
	@Parameter(category = CATEGORY_SOUND, property = CONFIG_ENABLE_CRAFTING_SOUND, defaultValue = "true")
	@Comment("Enable Item Crafted sound effect")
	@RestartRequired
	public static boolean enableCraftingSound = true;
	@Parameter(category = CATEGORY_SOUND, property = CONFIG_ENABLE_BOW_PULL_SOUND, defaultValue = "true")
	@Comment("Enable Bow Pull sound effect")
	@RestartRequired
	public static boolean enableBowPullSound = true;
	@Parameter(category = CATEGORY_SOUND, property = CONFIG_ENABLE_FOOTSTEPS_SOUND, defaultValue = "true")
	@Comment("Enable Footstep sound effects")
	@RestartRequired
	public static boolean enableFootstepSounds = true;
	@Parameter(category = CATEGORY_SOUND, property = CONFIG_FOOTSTEPS_SOUND_FACTOR, defaultValue = "0.15")
	@MinMaxFloat(min = 0.0F, max = 1.0F)
	@Comment("Volume scale factor for footstep sounds")
	public static float footstepsSoundFactor = 0.15F;
	@Parameter(category = CATEGORY_SOUND, property = CONFIG_SOUND_CULL_THRESHOLD, defaultValue = "20")
	@MinMaxInt(min = 0)
	@Comment("Ticks between culled sound events (0 to disable culling)")
	public static int soundCullingThreshold = 20;
	@Parameter(category = CATEGORY_SOUND, property = CONFIG_CULLED_SOUNDS, defaultValue = "minecraft:block.water.ambient,minecraft:block.lava.ambient,minecraft:entity.sheep.ambient,minecraft:entity.chicken.ambient,minecraft:entity.cow.ambient,minecraft:entity.pig.ambient")
	@Comment("Sounds to cull from frequent playing")
	@RestartRequired
	public static String[] culledSounds = { "minecraft:block.water.ambient", "minecraft:block.lava.ambient",
			"minecraft:entity.sheep.ambient", "minecraft:entity.chicken.ambient", "minecraft:entity.cow.ambient",
			"minecraft:entity.pig.ambient" };
	@Parameter(category = CATEGORY_SOUND, property = CONFIG_BLOCKED_SOUNDS, defaultValue = "dsurround:bison")
	@Comment("Sounds to block from playing")
	@Hidden
	public static String[] blockedSounds = { "dsurround:bison" };
	@Parameter(category = CATEGORY_SOUND, property = CONFIG_SOUND_VOLUMES, defaultValue = "")
	@Comment("Individual sound volume scaling factors")
	@Hidden
	public static String[] soundVolumes = {};

	public static final String CATEGORY_PLAYER = "player";
	public static final String CONFIG_SUPPRESS_POTION_PARTICLES = "Suppress Potion Particles";
	public static final String CONFIG_ENABLE_POPOFFS = "Damage Popoffs";
	public static final String CONFIG_HURT_THRESHOLD = "Hurt Threshold";
	public static final String CONFIG_HUNGER_THRESHOLD = "Hunger Threshold";
	private static final List<String> playerSort = Arrays.asList(CONFIG_SUPPRESS_POTION_PARTICLES,
			CONFIG_ENABLE_POPOFFS, CONFIG_HURT_THRESHOLD, CONFIG_HUNGER_THRESHOLD);

	@Parameter(category = CATEGORY_PLAYER, property = CONFIG_SUPPRESS_POTION_PARTICLES, defaultValue = "false")
	@Comment("Suppress player's potion particles from rendering")
	@RestartRequired
	public static boolean suppressPotionParticles = false;
	@Parameter(category = CATEGORY_PLAYER, property = CONFIG_ENABLE_POPOFFS, defaultValue = "true")
	@Comment("Controls display of damage pop-offs when an entity is damaged")
	@RestartRequired
	public static boolean enableDamagePopoffs = true;
	@Parameter(category = CATEGORY_PLAYER, property = CONFIG_HURT_THRESHOLD, defaultValue = "8")
	@Comment("Amount of health bar remaining to trigger player hurt sound (0 disable)")
	@MinMaxInt(min = 0, max = 10)
	public static int playerHurtThreshold = 8;
	@Parameter(category = CATEGORY_PLAYER, property = CONFIG_HUNGER_THRESHOLD, defaultValue = "8")
	@Comment("Amount of food bar remaining to trigger player hunger sound (0 disable)")
	@MinMaxInt(min = 0, max = 10)
	public static int playerHungerThreshold = 8;

	public static final String CATEGORY_POTION_HUD = "player.potion hud";
	public static final String CONFIG_POTION_HUD_ENABLE = "Enable";
	public static final String CONFIG_POTION_HUD_TRANSPARENCY = "Transparency";
	public static final String CONFIG_POTION_HUD_LEFT_OFFSET = "Horizontal Offset";
	public static final String CONFIG_POTION_HUD_TOP_OFFSET = "Vertical Offset";
	public static final String CONFIG_POTION_HUD_SCALE = "Display Scale";
	public static final String CONFIG_POTION_HUD_ANCHOR = "HUD Location";
	private static final List<String> potionHudSort = Arrays.asList(CONFIG_POTION_HUD_ENABLE,
			CONFIG_POTION_HUD_TRANSPARENCY, CONFIG_POTION_HUD_SCALE, CONFIG_POTION_HUD_ANCHOR,
			CONFIG_POTION_HUD_TOP_OFFSET, CONFIG_POTION_HUD_LEFT_OFFSET);

	@Parameter(category = CATEGORY_POTION_HUD, property = CONFIG_POTION_HUD_ENABLE, defaultValue = "true")
	@Comment("Enable display of potion icons in display")
	@RestartRequired
	public static boolean potionHudEnabled = true;
	@Parameter(category = CATEGORY_POTION_HUD, property = CONFIG_POTION_HUD_TRANSPARENCY, defaultValue = "0.75")
	@MinMaxFloat(min = 0.0F, max = 1.0F)
	@Comment("Transparency factor for icons (higher more solid)")
	public static float potionHudTransparency = 0.75F;
	@Parameter(category = CATEGORY_POTION_HUD, property = CONFIG_POTION_HUD_LEFT_OFFSET, defaultValue = "5")
	@MinMaxInt(min = 0)
	@Comment("Offset from left side of screen")
	public static int potionHudLeftOffset = 5;
	@Parameter(category = CATEGORY_POTION_HUD, property = CONFIG_POTION_HUD_TOP_OFFSET, defaultValue = "5")
	@MinMaxInt(min = 0)
	@Comment("Offset from top of screen")
	public static int potionHudTopOffset = 5;
	@Parameter(category = CATEGORY_POTION_HUD, property = CONFIG_POTION_HUD_SCALE, defaultValue = "0.75")
	@MinMaxFloat(min = 0.0F, max = 1.0F)
	@Comment("Size scale of icons (lower is smaller)")
	public static float potionHudScale = 0.75F;
	@Parameter(category = CATEGORY_POTION_HUD, property = CONFIG_POTION_HUD_ANCHOR, defaultValue = "1")
	@MinMaxInt(min = 0, max = 1)
	@Comment("Area of the display the Potion HUD is displayed (0 upper left, 1 upper right)")
	public static int potionHudAnchor = 1;

	public static final String CATEGORY_SPEECHBUBBLES = "speechbubbles";
	public static final String CONFIG_OPTION_ENABLE_SPEECHBUBBLES = "Enable SpeechBubbles";
	public static final String CONFIG_OPTION_ENABLE_ENTITY_CHAT = "Enable Entity Chat";
	public static final String CONFIG_OPTION_ENABLE_EMOJIS = "Enable Entity Emojis";
	public static final String CONFIG_OPTION_SPEECHBUBBLE_DURATION = "Display Duration";
	public static final String CONFIG_OPTION_SPEECHBUBBLE_RANGE = "Visibility Range";
	@Parameter(category = CATEGORY_SPEECHBUBBLES, property = CONFIG_OPTION_ENABLE_SPEECHBUBBLES, defaultValue = "true")
	@Comment("Enables/disables speech bubbles above player heads")
	public static boolean enableSpeechBubbles = true;
	@Parameter(category = CATEGORY_SPEECHBUBBLES, property = CONFIG_OPTION_ENABLE_ENTITY_CHAT, defaultValue = "true")
	@Comment("Enables/disables entity chat bubbles")
	public static boolean enableEntityChat = true;
	@Parameter(category = CATEGORY_SPEECHBUBBLES, property = CONFIG_OPTION_ENABLE_EMOJIS, defaultValue = "true")
	@Comment("Enables/disables entity emojis")
	public static boolean enableEntityEmojis = true;
	@Parameter(category = CATEGORY_SPEECHBUBBLES, property = CONFIG_OPTION_SPEECHBUBBLE_DURATION, defaultValue = "7")
	@MinMaxFloat(min = 5.0F, max = 15.0F)
	@Comment("Number of seconds to display speech before removing")
	public static float speechBubbleDuration = 7.0F;
	@Parameter(category = CATEGORY_SPEECHBUBBLES, property = CONFIG_OPTION_SPEECHBUBBLE_RANGE, defaultValue = "32")
	@MinMaxInt(min = 16, max = 64)
	@Comment("Range at which a Speech BubbleJetEffect is visibile.  Filtering occurs server side.")
	@RestartRequired
	public static float speechBubbleRange = 32;

	private static final List<String> speechBubbleSort = Arrays.asList(CONFIG_OPTION_ENABLE_SPEECHBUBBLES,
			CONFIG_OPTION_ENABLE_ENTITY_CHAT, CONFIG_OPTION_SPEECHBUBBLE_DURATION, CONFIG_OPTION_SPEECHBUBBLE_RANGE);

	public static void load(final Configuration config) {

		ConfigProcessor.process(config, ModOptions.class);

		// CATEGORY: Logging
		config.setCategoryRequiresMcRestart(CATEGORY_LOGGING_CONTROL, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_LOGGING_CONTROL, false);
		config.setCategoryComment(CATEGORY_LOGGING_CONTROL, "Defines how Dynamic Surroundings logging will behave");
		config.setCategoryPropertyOrder(CATEGORY_LOGGING_CONTROL, new ArrayList<String>(loggingSort));

		// CATEGORY: Rain
		config.setCategoryRequiresMcRestart(CATEGORY_RAIN, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_RAIN, false);
		config.setCategoryComment(CATEGORY_RAIN, "Options that control rain effects in the client");
		config.setCategoryPropertyOrder(CATEGORY_RAIN, new ArrayList<String>(rainSort));

		// CATEGORY: General
		config.setCategoryRequiresMcRestart(CATEGORY_GENERAL, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_GENERAL, false);
		config.setCategoryComment(CATEGORY_GENERAL, "Miscellaneous settings");
		config.setCategoryPropertyOrder(CATEGORY_GENERAL, new ArrayList<String>(generalSort));

		// CATEGORY: Player
		config.setCategoryRequiresMcRestart(CATEGORY_PLAYER, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_PLAYER, false);
		config.setCategoryComment(CATEGORY_PLAYER, "General options for defining sound and effects the player entity");
		config.setCategoryPropertyOrder(CATEGORY_PLAYER, new ArrayList<String>(playerSort));

		// CATEGORY: Aurora
		config.setCategoryRequiresMcRestart(CATEGORY_AURORA, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_AURORA, false);
		config.setCategoryComment(CATEGORY_AURORA, "Options that control Aurora behavior and rendering");
		config.setCategoryPropertyOrder(CATEGORY_AURORA, new ArrayList<String>(auroraSort));

		// CATEGORY: Fog
		config.setCategoryRequiresMcRestart(CATEGORY_FOG, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_FOG, false);
		config.setCategoryComment(CATEGORY_FOG, "Options that control the various fog effects in the client");
		config.setCategoryPropertyOrder(CATEGORY_FOG, new ArrayList<String>(fogSort));

		// CATEGORY: Biomes
		config.setCategoryRequiresMcRestart(CATEGORY_BIOMES, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_BIOMES, false);
		config.setCategoryComment(CATEGORY_BIOMES, "Options for controlling biome sound/effects");
		config.setCategoryPropertyOrder(CATEGORY_BIOMES, new ArrayList<String>(biomesSort));

		// CATEGORY: Block
		config.setCategoryRequiresMcRestart(CATEGORY_BLOCK, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_BLOCK, false);
		config.setCategoryComment(CATEGORY_BLOCK, "Options for defining block specific sounds/effects");

		// CATEGORY: Block.effects
		config.setCategoryRequiresMcRestart(CATEGORY_BLOCK_EFFECTS, true);
		config.setCategoryRequiresWorldRestart(CATEGORY_BLOCK_EFFECTS, true);
		config.setCategoryComment(CATEGORY_BLOCK_EFFECTS, "Options for disabling various block effects");

		// CATEGORY: Sound
		config.setCategoryRequiresMcRestart(CATEGORY_SOUND, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_SOUND, false);
		config.setCategoryComment(CATEGORY_SOUND, "General options for defining sound effects");
		config.setCategoryPropertyOrder(CATEGORY_SOUND, new ArrayList<String>(soundsSort));

		// CATEGORY: player.potion hud
		config.setCategoryRequiresMcRestart(CATEGORY_POTION_HUD, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_POTION_HUD, false);
		config.setCategoryComment(CATEGORY_POTION_HUD, "Options for the Potion HUD overlay");
		config.setCategoryPropertyOrder(CATEGORY_POTION_HUD, new ArrayList<String>(potionHudSort));

		// CATEGORY: SpeechBubbles
		config.setCategoryRequiresMcRestart(CATEGORY_SPEECHBUBBLES, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_SPEECHBUBBLES, false);
		config.setCategoryComment(CATEGORY_SPEECHBUBBLES, "Options configuring SpeechBubbles");
		config.setCategoryPropertyOrder(CATEGORY_SPEECHBUBBLES, new ArrayList<String>(speechBubbleSort));

		// Iterate through the config list looking for properties without
		// comments. These will
		// be scrubbed.
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
