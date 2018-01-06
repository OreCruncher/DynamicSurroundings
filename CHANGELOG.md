###DynamicSurroundings-1.11.2-3.4.8.1
**Fixes**
* Open Terrain Generator compatibility changes.  No more crash loops!

###DynamicSurroundings-1.11.2-3.4.8.0
** What's New**
* Display the clock hud when player is looking at an item frame that contains a clock.

**Fixes**
* Removed all that sound engine restart stuff and replaced with patches to the underlying Minecraft sound engine to avoid the situation all together.  Thanks to CreativeMD and his work on getting to the bottom of things!  This should eliminate a variety of reported problems up through and including:
    * Sound Engine restart lag
    * Frequency of the sound engine crashes
    * Various repeating errors in the client log related to sound muting and sounds not being found
    * Crashes due to sound engine being yanked out from under other mods (such as IC2) 
* Sometimes a sound instance would not play.  When this occurs something like "Error in class 'LibraryLWJGLOpenAL'" would show in the log.  Put in some additional code to make sure the sound information is flushed down into the sound engine. 
* OpenEye: Sometimes the display wasn't created thus causing a crash in background mute processing
* OpenEye: Sometimes the player reference wasn't initialized when hud processing was performed

**Changes**
* Built against Forge 1.11.2-13.20.1.2579
* Reworked internal ASM transforms for better compatibility
* Refined Battle Music logic to only apply it to entities that such as Mobs, Players, Polar Bears, and Golems.  Excludes passives that can attack such as wolves (but not the skellies they go after).  Goal is to reduce Battle Music fatigue.

**NOTE: If you use Ambient Sounds 2.0 as well as Dynamic Surroundings make sure Ambient Sounds is updated to at least v2.2.1!  If you don't the sound engine patches will not be applied.**

###DynamicSurroundings-1.11.2-3.4.7.2
**What's New**
* General config option for turning off Dynamic Surroundings chat messages when toggling things like light level HUD, chunk border display, etc.

**Fixes**
* Footstep/item swap sounds playing L/R of the player when moving, such as strafing. Noticeable when using headphones.
* Client NPE/CME when connection rejected because of a mod mismatch on server.

**Changes**
* Further restrict desert dust effect to biomes with < 0.1 rainfall - should rain in BoP Steppe now.

###DynamicSurroundings-1.11.2-3.4.7.1
**What's New**
* Added built-in profiles for disabling things like Nether weather and biome dust.  Config options are accessible using the Built-in Profiles button from Dynamic Surroundings main configuration page.
* Sound Option to have the sword equip sound be the tool equip sound.  (This is for those sword enthusiasts that don't like the default metallic ring.)
* Added a "low res" footprint style (id 6)

**Fixes**
* Guard against the possibility that a startup sound is not found within the SoundEvent registry.
* Steam jets will now disappear when water source removed

**Changes**
* Updated Galacticraft mod support (no more green fog on Mars!)

###DynamicSurroundings-1.11.2-3.4.7.0
**What's New**
* Added Red Shouldered Hawk to the raptor sound set.
* Added bullfrog sound to the lilypad block.  The chance a lily will play the normal frog croak is 3x that of the bullfrog.

**Fixes**
* Defensive code when recovering sound system to prevent NPE
* Defensive code when connecting to a remote server with heavy lag where entity capability data arrives before the client entity list is initialized. 
* No more underwater falls in water because of falling blocks during worldgen or when digging out resources. (I hope!)

**Changes**
* Modified weight of the red tailed hawk sound so it does not play as often as compared to other raptor sounds.

###DynamicSurroundings-1.11.2-3.4.6.2
**What's New**
* Mod support:
    * Astral Sorcery
    * Charset
    * Glass Hearts
    * Terraqueous
    
**Fixes**
* OpenEye: NPE in getFogColor() - world provider property is null for some reason
* OpenEye: SoundSystem reference was null for some reason
* OpenEye: Use blocks creative tab reference in call to getBlockSubtypes()
* Fixed sound disappearing when editing configuration while attached to a remote server

**Changes**
* Refreshed support for the following mods:
    * Ceramics
    * Forestry
    * Rustic
    
###DynamicSurroundings-1.11.2-3.4.6.1
**Fixes**
* Cleanup waterfall splash effect.  Should disappear properly, and small falls no longer sound like the men's room at a pub during happy hour.
* Fix permissions level for calc command.

**Changes**
* Built against Forge 13.20.1.2425
* Improved performance of sound system restart after it crashes.

###DynamicSurroundings-1.11.2-3.4.6.0
**What's New**
* The Dynamic Surroundings JAR is now signed.  This shouldn't affect your game play.
* Solid square footprint style (5)
* Support CoFHCore covers (generate footstep sounds based on the cover, not the block)

**Fixes**
* Weather rendering with Tough as Nails should work again

**Changes**
* Mod profile updates for "out of the box" support:
    * Added Hatchery, Chickens, Iron Chest, Advanced Generators, Storage Drawers, Simple Generators, Simple Barrels, Ceramics, Cooking for Blockheads, Ender Storage, Ex Nihlio Adscensio, Ex Compressum, Thermal Foundation, Thermal Expansion, Thermal Dynamics, Reliquia, Refined Storage, Tiny Progressions, Solar Flux Reborn, Big Reactors, Blood Magic, RFTools, Quark
    * Refreshed Minecraft, Biomes O'Plenty, Tinker's Construct, Harvestcraft, Natura, Tough as Nails, Subtratum, Actually Additions, Gravel Ores

###DynamicSurroundings-1.11.2-3.4.5.7
**What's New**
* "Built-in" Preset configurations for:
    * Minecraft/Dynamic Surroundings settings based on computer capability
    * SkyBlock maps
    * Turning on entity Emoji's, chat, and player speech bubbles
* Support for the Gravel Ore Mod by Elucent
    
**Fixes**
* Cap ParticleCollection particle count to reduce lag created because of an excessive number of particles generated due to unusual terrain
* Sometimes a waterfall sound source didn't want to go away.

###DynamicSurroundings-1.11.2-3.4.5.6
**Fixes**
* Array out of bounds exception processing waterfall column

###DynamicSurroundings-1.11.2-3.4.5.5
**What's New**
* Russian (ru_RU) translations for Presets!/Dynamic Surroundings (thanks Xottab-DUTY!)
* Deserts have a wind style background sound
* Support for Simple Corn
* Savanna has daytime and nighttime biome sounds
* Raptor (bald eagle/red tailed hawk) spot sounds
* Config file options to turn off certain features
    * Used by modpack authors to control player experience
    * Applies to Light Level, Compass/Clock, and Chunk Fencing HUDs
    * When turned off feature will not be available in game
    * Config options for said features will also be suppressed from the config GUI
    * Keybindings for features are disabled
* Configuration options for specifying startup sound list
    * To prevent sound from playing remove all entries from config list

**Fixes**
* No more "hanging chad" footprints when walking off the edge of a block onto tall grass plant
* Tooltip no longer blocks the volume slider control in the Individual Sound Config GUI
* Added missing Tinker's Shovel and Scythe sounds

**Changes**
* Sacred Springs biome from BoP have forest like sounds rather than jungle
* AbyssalCraft Darklands biome should not have dust
* Changed village anvil to be lower pitch and not as frequent
* Square and hoof footprint textures are darker and alpha processing changed to make the prints less stark
* Limit village sounds to Overworld
* Support new Chisel API (facades)
* Improved waterfall sounds; multiple different sounds based on the strength of the fall

###DynamicSurroundings-1.11.2-3.4.5.4
**Fixes**
* Light level hud should now show correct values
* Mod should now load for Minecraft version 1.11

**Changes**
* Don't spam sound engine restart needed if auto-restart is not enabled and sound stream thread dies

###DynamicSurroundings-1.11.2-3.4.5.3
**Changes**
* Improved area block scan/processing efficiency
* Optimized footstep sound processing
* Use ASM to hook Minecraft sound stream loading to improve responsiveness and reduce stream errors; can be turned off in config if needed

###DynamicSurroundings-1.11.2-3.4.5.2
**Fixes**
* Changed volume scales using the volume dialog weren't saved in the config file (weren't sticky)
* Setting footstep sound volume scale to 0 will revert to normal Vanilla footstep sounds
* Fix compatibility with latest ActualMusic (v1.2.0.114+)

**Changes**
* WIP: Galacticraft/Planets support for biomes/dimensions (based on the work of Ezer'Arch)

###DynamicSurroundings-1.11.2-3.4.5.1
**Fixes**
* An empty potion bubble would display in the HUD when a Tinker's trait effect happened.  Added additional checks to prevent it from happening.
* No more insect buzz spot sound when it is raining
* Address crash related to Galacticraft Orbital Station dimension

**Changes**
* Footprints age faster when raining; more intense the storm the faster the rate of decay

###DynamicSurroundings-1.11.2-3.4.5.0
**Fixes**
* Playing a sound in the Individual Sound Configuration dialog will mute playing music.  This will only work if Dynamic Surroundings replaces Vanilla's MusicTicker (i.e. if Actual Music is installed it will not work).
* Stop playing sound when the Individual Sound Configuration dialog is closed.
* A lot of sound effects were assigned the AMBIENT sound category; fixed bad logic - categories should be auto assigned based on sounds.json

**Changes**
* Built against Forge 13.20.0.2304
* Battle Music volumes have been increased, and are now properly placed into the MUSIC sound category
* Individual sound scale factors can now be up to 400 (4x)
* Added fog property to dimension config that controls whether Dynamic Surroundings applies fog/dust effects to a dimension.  By default it is only ON for Overworld and Nether.  (Out of the box compatibility with Galacticraft.)
* Better rendering performance for footprints.
* Two new footprint styles: (3) bird and (4) animal paw

###DynamicSurroundings-1.11.2-3.4.4.0
**Fixes**
* OpenEye report of NPE in sound muting
* Do not replace MusicTicker if Actual Music is installed.  It is recommended that Battle Music not be enabled if you are using Actual Music because of song clash.
* Footprints will be properly lit; should stand out less in darkness
* Ice Plains and Tundra no longer have desert dust effect

**Changes**
* New rain option to configure Dynamic Surroundings to use Vanilla processing of rain/thunder rather than it's own
    * Provides generic compatibility when needed (or some folks just like Vanilla rain)
    * Vanilla will have a an intensity of 100 when rain kicks into full gear
    * Background thunder will always be triggered if it is currently storming (can be turned off with a different setting)
    * Dynamic Surroundings rendering of dust/rain textures as well as rain splash particle effects will still be present
    * Other mods may manipulate the vanilla rain/thunder settings in world data.  This will affect how Dynamic Surroundings behaves.
    
###DynamicSurroundings-1.11.2-3.4.3.0
**What's New**
* Ding or Egg Pop sound when client starts.
* Distributed footprints, meaning footprints of other players will display on your client
    * Requires server side install of Dynamic Surroundings; if not installed server side you will only see your prints as usual
    * Footprint style based on what your client is configured for
    * Will not display if player is sneaking
    * Quadruped determined by a given player's client
* Distributed sounds
    * Requires server side install of Dynamic Surroundings; if not installed server side you will only hear your sounds as usual
    * Footstep and item use actions will trigger sound for players nearby
    * Footstep sounds will not play if player is sneaking
    * Each player can still block sounds per normal
* Mini-boss battle music.  Will play if entity is a boss but not a Dragon or Wither.

**Fixes**
* Emoji's no longer render behind water texture
* Aurora was rendering funny with OptiFine installed.  This does not address the "gray aurora" effect with some of the advanced shader packs.
* FINALLY killed pesky exception when Biome Dictionary becomes somewhat inconsistent.
* Cleaned up emoji capability data - should be more consistent.

**Changes**
* Built against Forge 13.20.0.2296
* Reworked emoji particles into motes.  Should speed up rendering when having large quantities of entities in an area all doing something.
* Background Minecraft music will fade when battle music plays, then fade back in when battle ends.
* Battle scanner changes
    * Boss battle only requires a boss to be in range, no LOS requirement
    * Mob battle requires the mob to be attacking something, and the mob seeing the player or the player seeing the mob

###DynamicSurroundings-1.11.2-3.4.2.0
**What's New**
* Remember [Battle Music](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/1291283-battle-music-v1-5-12-july-make-your-battles-epic)?  Dynamic Surroundings has it's own version!  Currently it is disabled by default and can be turned on under Sound Options.
    * It's WIP - looking for the right sounds.  Suggestions for tracks appreciated.  (Need mini-boss track.)
    * 3 tracks - generic battle as well as Wither and Dragon
    * Uses internal data from emojis; deploy mod on the server for better effect
    * If not installed on a server BattleMusic will only play for bosses
    * Turn down the master music volume slider to have a better experience (Vanilla music is too loud IMO)
    * [Rules can be set](https://github.com/OreCruncher/DynamicSurroundings/wiki/Tutorial:-Condition-Strings) for the "BattleMusic" fake biome in the configuration so that pack authors can provide their own music
* Option to disable the auto-restart of a crashed sound system.  You will still get chat messages indicating that the sound system has crashed and client should be restarted.
* Tooltip for sounds in the Individual Sound Configuration dialog will now display attribution information if applicable.  (I get a lot of sounds from www.freesound.org.)

**Fixes**
* Handle situation where the Biome registry becomes inconsistant with the Biome.PLAINS identity value (mod compatibility)

**Changes**
* Refactored mcp.json (the main configuration file for Dynamic Surroundings):
    * Coyote, Owl, Woodpecker, and Crow sounds are available in more biomes
* Auroras render in a different location:
    * Render at chunk view distance; has to be at least 6 to render
    * Render across the "background" behind terrain
    * Band base is at sea level and scales higher the larger the client chunk view distance
    * Aurora options that no longer apply have been removed
    * Main purpose of the change is to give them more of a borealis feel and become a sky painting rather than a "hey, look, a couple of colored bands in the sky". 

###DynamicSurroundings-1.11.2-3.4.1.0
**What's New**
* Aurora spawn is now 100% client side!  This means auroras will display in polar biomes even though a server may not have Dynamic Surroundings installed.
    * The "seed" for the aurora is based on the current Minecraft day of the dimension in question
    * Auroras will be a fixed X,Z distance from the player regardless of the direction of movement (i.e. you can no longer fly up into them)
    * Aurora Y will obey the settings in the configuration file
    * They will fade once you move out of a polar biome
    * Because aurora processing is 100% client side there is no server side code

**Fixes**
* NPE when holding a stack of nether stars in order to inspect a block that has no corresponding Item.
* OpenEye: SoundManager NPE when muting sound
* BoP Algae no longer cause underwater waterfalls

**Changes**
* Minecraft preset application is smarter about making setting changes that can trigger some lag.  For example, it will no longer cause a resource refresh if the current mipmap level is the same as the one in the preset.
* Filter Dynamic Surroundings "asm" and "logging" categories from preset configuration.
* Biome matching rules for sounds now use traits rather than regex name matching.
    * Each biome has traits via the BiomeDictionary.  For example a Desert biome has the traits of HOT, DRY, and SANDY.
    * Possible to have multiple background sounds streaming for a biome (ie. Birch Forest Hills will have the regular Forest sounds as well as the Hills wind sound)
    * Should provide better "out of the box" support for modded biomes Dynamic Surroundings is not directly aware of
    * It's not 100% perfect, so if you come across a biome that has non-sensical sounds let me know.  The configs can be tweaked.
    * Configuration files created by other authors can still use regex name matching since backward compatibility is maintained.

###DynamicSurroundings-1.11.2-3.4.0.0
**What's New**
* Presets!
    * Saves Minecraft and Dynamic Surroundings settings to an external configuration file.  Useful to use after getting your client and modpack tuned the way you want.
    * Apply saved settings anytime you want
    * Can have multiple preset files depending on your needs.  (Like tweaking Minecraft settings trying to get the right setup and saving in between attempts.)
    * Access the Presets! dialog by using the mod configuration system or pressing P while in game.
    * Saved presets are in Json format so you can use your favorite Json editor to hand modify.
    * You can edit out settings from the file if you do not want to overwrite when applying.
    * Json files can be shared - just copy from/to the ./minecraft/config/presets directory.
* Presets! is currently embedded within Dynamic Surroundings.  (If you didn't know a single JAR can host multiple mods.)  Depending on success I may split into a standalone mod and provide an API so that other mods can tap into the feature.
* Presets! is a work in progress.  This means it may be tweaked/changed based on feedback.

**Changes**
* Internal reorganization and refactor.  Shouldn't see any differences outside of what is listed here.

###DynamicSurroundings-1.11.2-3.3.8.0
**What's New**
* Crow (dsurround:crow) spot sound; occurs in forests, bogs, fens, etc. (American Crow)
    * Also occurs in "dead" biomes (Dead Forest, Dead Swamp, etc.)
* New biome background sound for bogs, fens, marshes, bayou, etc.
* Option to override sea level definition for Overworld; useful when terrain generators have sea levels other than 63, for example OTG and Biome Bundle.  Can be found in the config GUI: Biome Options -> Overworld Sealevel Override
* Integrated Dynamics Meneglin biome similar to Taiga

**Fixes**
* Rain intensity generation with custom min/max boundaries
* Screen blackout when hitting F1 with aurora present
* Stack overflow in biome registry when OTG is installed
* Superflat worlds are no longer Underground

**Changes**
* Tweaked swim sound volume equation so its a bit louder
* Lava drop falling into water produces steam cloud
* Manipulate master sound gain (PaulsCode) to mute rather than MASTER sound category
* Adjusted toolbar sounds to be lower in volume
* Water ripples on any Material.WATER blocks that are at default state (i.e. full); will occur on BoP liquid blocks such as poison, honey, and blood
* Water splash particle effect is slightly smaller.

###DynamicSurroundings-1.11.2-3.3.7.0
**What's New**
* Item equip sounds ala MAtmos.  Swapping items in the hot bar will trigger sounds based on item type.  Can be turned off in config. Works main hand as well as off hand.
* Added new sound type for tools for swinging/use.
* Rain on water blocks produce water ripple particle effect rather than splash effect.
* Water/lava drops falling into water produce water ripple particle effect.

**Fixes**
* Defensive code for NPE reported via OpenEye (EnvironState.tick)
* Removed culled sound list from config GUI because of new "Individual Sound Configuration" dialog

**Changes**
* Updated the various item use/swing sounds.
* Pumpkins and melons no longer sound squishy when walked on (acoustic profile organic_dry).
* Player centered sounds (item swing, jump, etc.) will play at the player location rather than some random location around the player.
* Magma block fixups - can spark when rained on like netherrack, and counts as a lava block for steam jet spawn.
* Removed rain sound scale factor; control using the individual sound scale controls (dsurround:rain, minecraft:weather.rain, and minecraft:weather.rain.above)
* Tweaked rain sound volume processing:
    * Splash sound moved to WEATHER sound category (was AMBIENT for some reason)
    * Ensure minimum sound volume for low intensity rain
    * Volume variation to give some texture
    * Vary pitch to reduce dust storm drone harmonic

###DynamicSurroundings-1.11.2-3.3.6.1
**What's New**
* Option to enable/disable clock HUD independent of compass HUD.  This will let the player use a compass HUD from another mod but keep Dynamic Surroundings clock HUD.
* Options to change command names and aliases so that command conflicts can be easily mitigated.  Found in the configuration menu under "Command Options".
    * The /calc command is a client side command and changes will only affect the client
    * The /ds command is server side so for a dedicated server changes must be made on the server.  (For single player/LAN the client is also the server so it uses the same config.)

**Fixes**
* Compass bar will no longer render inappropriately when holding a clock. 
* Biome sound slider in volume dialog incorrectly bound to rain sound scale
* Added missing rain sound volume scale to volume dialog
* Critical popoff text will render half block above the damage amount instead of on top of it

###DynamicSurroundings-1.11.2-3.3.6.0
**What's New**
* Added new horseshoe footprint style.
* Quadruped support.  Enable option under Sound Options.  Turn on to have footstep sounds like a quadruped (horse).  Useful with [Mine Little Pony](http://minelittlepony-mod.com/).  Try with the new horseshoe footprint!  Note that MLP is not required for the quadruped sounds and horseshoe prints to work.
* Consolidated the various sound configuration dialogs into one
    * New dialog can be found under "Individual Sound Configuration"
    * Can toggle sound blocking, culling, and set individual sound scale factors
    * dsurround.cfg schema is not altered
* Sound Play button in the "Individual Sound Configuration" dialog
    * Playing the sound will play at the current slide volume
    * Pressing button again when labeled stop will stop the sound
    * Sounds play on MASTER category so MASTER slider will influence volume
    * The actual volume in game may vary because mods may use different volume settings

**Fixes**
* OpenEye report related to rain splash particle rendering.

**Changes**
* Merged clock and compass HUD so they share the same text panel
* Color panel behind volume control dialog

###DynamicSurroundings-1.11.2-3.3.5.2
**Fixes**
* Strange rendering caused by client side block event handler throwing an NPE due to race condition

###DynamicSurroundings-1.11.2-3.3.5.1
**Fixes**
* LWJGL having difficulties digesting default sound device name

**Changes**
* Updated Tough as Nails support (v1.11-2.0.5)

###DynamicSurroundings-1.11.2-3.3.5.0
**What's New**
* Clock HUD when holding a Minecraft clock
    * Similar in concept to the Compass
    * Displays Minecraft day information
    * Displays elapsed time of the current Minecraft session
    * Tied to the enable/disable and transparency of the Compass HUD
* Volume Quickset Dialog that allows for configuring the Master Sound Volume as well as the volume scale for Biome Sounds and Footsteps.  Activate by pressing V.

**Fixes**
* NPE initializing the sound manager before mod proxy is created

**Changes**
* Built against Forge 13.20.0.2281
* Fancified compass and debug hud with text panels
* Disabling desert and biome fog effects will turn off fog processing within Dynamic Surroundings.  Intended to address compatibility with certain mod configurations (DS + BOP + OptFine).
* Default footprint style is now square
* Fireflies will be out at sunset as well as night
* Changed a dry biome definition from being no rainfall to having a rainfall of < 0.2F
* Code cleanup and performance tweaks

###DynamicSurroundings-1.11.2-3.3.4.1
**Fixes**
* Exception modifying immutable collection when config reloads after editing
* Simply Jetpacks sound would not stop when it should

**Changes**
* Updated/Improved/Fancified the Dynamic Surroundings mod information screen
* Debug HUD that describes blocks in the world when in creative mode and holding a stack of nether stars.  Helps a modpack/resource pack developers in modifying Dynamic Surroundings configuration files.
* Show debug information in an item's tooltip if Dynamic Surroundings debugging is enabled.

###DynamicSurroundings-1.11.2-3.3.4.0
**What's New**
* F9 to show Minecraft's chunk border fencing.  Basically it is a simplified shortcut for F3+G.
* Chat feedback when player toggles display modes, like chunk border fencing and light level
* Option to disable the water suspend particle effect.  Can reduce particle load if you spend a lot of time around water, or if you want clean water.

**Fixes**
* Overload of sound engine with sounds; noticeable around large water falls where it would sound like it was clicking.
* Sporadic crash when Dynamic Surroundings debug logging was enabled and F3 debug screen was up.
* No more repeating/clicking sounds when you die.  Something is attempting to join a dead player to the world every 1-2 seconds.  Dynamic Surroundings no longer clears out the currently playing sounds when a dead player attempts to join a world.
* Sometimes a repeating sound would not go away when it has completed fade.

**Changes**
* Built against Forge 13.20.0.2262.  The config option GUI system changed slightly to accommodate so please report any strangeness you may encounter.
* Replace Minecraft SoundManager with slightly modified one to improve playing of looping sound clips and simplify volume scale hook
* Use Item Java class hierarchy to identify sound to play when player swings an item.  This allows for a modded item to be "detected" and handled appropriately if derived from an existing known class (i.e. a modded hoe that derives from ItemHoe will get the swing sound of a hoe.)
* Reworked waterfall splash and dust mote particle rendering to squeeze more performance.
* Capped number of streaming sound channels at 16.
* Increased firefly spawn rate quite a bit, and smoothed out the Y trajectory.  Fireflies spawn around `red_flower`, `yellow_flower`, and `double_plant`.  It does NOT include `tallgrass`.  (The two block tall grass is actually a `double_plant`.)
* Randomize the initial sound play for waterfall splash.  This should even out the sound effect of large falls a bit.
* General performance tweaks related to internal data tables.

###DynamicSurroundings-1.11.2-3.3.3.1
**What's New**
* Mod Support: Railcraft

**Changes**
* Aurora rendering was moved to a different rendering phase so that it can render properly even when rain/snow rendering is replaced by other mods (Weather2).  (Weather 2 is currently not available for 1.11.x, but will be supported when a version exists.)
* Use different RNG; improves area scanning performance a bit

###DynamicSurroundings-1.11.2-3.3.3.0
**What's New**
* Option to disable display of critical hit pop-off text
* Option to mute sounds when Minecraft is in the desktop background; defaults to ON
* Option to disable both Vanilla and Dynamic Surroundings potion HUD
* [WIP] Added **client side** command /calc for doing math homework while playing Minecraft
    * Not immersion related, but the logic was already there in support of configuration logical expressions
    * Command: /calc *expression* (ex. **/calc (2+2)/3**).
* Server side TPS/memory reporting to client
    * Added because I use it for putting together private packs
    * Turned off by default; controls server side behavior
    * Mod needs to be on the server to report
    * Reports per dimension TPS and server memory
    * Updates once a second server side if enabled
    * Information displayed in debug screen (F3)
* Attempt automatic restart of crashed sound system
* Mod support: AbyssalCraft, Rustic, Power Advantage, Steam Advantage, Streams, WTF's Expedition
    * Waterfall splash effect + Streams gives the impression of rapids

###DynamicSurroundings-1.11.2-3.3.2.1
**What's New**
* Mod support for Ars Magica 2, Rockhounding: Surface, Rockhounding: Ore Tiers
    * These mods currently not available for 1.11.x; will support when they are

**Fixes**
* No more sound clicking when moving fast; was noticeable when flying over a beach
* Fixed footstep sounds when walking on fences
* Forest sounds in Flower Forest
* Select proper rain/dust/snow precipitation texture; issue was when a dust biome (Cold Desert) was next to a snow biome (Ice Plain) the wrong precipitation texture was selected
* Sometimes area fog calc would be incorrect when logging in.

**Changes**
* No sound attenuation for player centered sounds
* Updated ru_RU.lang
* Sandstone step sound for minecraft:magma
* Thicken biome fog a bit to be closer to original thickness
* Change default footstep volume sound factor to 0.35 to diminish "sharpness" of higher volumes
* Change default light level render style to be surface with rotation

###DynamicSurroundings-1.11.2-3.3.2.0
**What's New**
* Nether dimension background sound (may need some tweaking)
* Added another compass rose texture (style 6).
* Mod support:
    * Actually Additions
    * Thermal Expansion (when available)

**Fixes**
* MCP mappings for 1.11.2 appear to be lying. Sound scaling feature should work again.

**Changes**
* Take into account Wasteland Forest ([Wasteland Mod](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2721105-wasteland-mod-the-lost)) when applying biome rules
* Initialize sound channels when SoundSetupEvent is raised

###DynamicSurroundings-1.11.2-3.3.1.1
**Fixes**
* Repeated "java.lang.IllegalArgumentException: value already present" when playing sound.

###DynamicSurroundings-1.11.2-3.3.1.0
**What's New**
* Player definable format string for compass coordinate display
    * It's a [Java format String](https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html)
    * x coord "1$", y coord "2$", z coord "3$"
    * Default string is "x: %1$d, z: %3$d" (without the quotes)

**Fixes**
* Added defensive code to AreaFogScanner in order to prevent obscure NPE reported on OpenEye.

**Changes**
* Alter fog calculation routine to handle fog at render distances > 16 chunks.
* Added code to kill water drip particle effect if it spawns in a non-air block.  Should fix up Minecraft's tendency of spawning water particles under wet sponge blocks even thought the block beneath is water.  Result is that Ocean Monuments with wet sponge blocks will not be as noisy.
* Cleaned up enable/disable potion HUD so the client does not have to be restarted.

###DynamicSurroundings-1.11.2-3.3.0.0
**What's New**
* Compass HUD!  When holding a compass in either hand location information will be displayed above/below the crosshair.      Trying for a more immersive version of the classic compass HUD.
    * The HUD cannot be moved, and will be hidden when not holding a compass.
    * This feature can be disabled.
    * Choice of 5 different HUD styles: 0-3 classic "bar" style; 4-5 compass rose style

**Fixes**
* Don't add particle to ParticleSystem past it's limit.
* Player Blindness and Nightvision effect on fog tinting.
* NPE loading config when block effect disabled.
* Fixed block edge walk routine looking up/down rather than south/north.  (The off-foot sound would be the sound below the air block.)

**Changes**
* Cap the number of outstanding particles for a particle system.  Attempt to address issue with high waterfalls producing large number of splash particles.

###DynamicSurroundings-1.11.2-3.2.8.1
**What's New**
* Config option to override default Minecraft thunder volume of 10000.

**Changes**
* Reduced the amount of "clipping" the light level frustum produced.  Light level textures will show at the boundry of the player field of view.

###DynamicSurroundings-1.11.2-3.2.8.0
**What's New!**
* Added new Light Level HUD style - surface with rotation.  Displays the light level on the block surface and rotated toward the player.  Snaps to the NWSE directions.
* Light Level HUD display mode is now configurable so you can set your favorite default.
* Underwater background sound.  This is for when the player's head is in water, but not in a watery biome.

**Fixes**
* Incorrect default fog colors for the Nether and The End were being used.

**Changes**
* Increased the default number of sound channels.  Modern sound systems generally have 255 channels, and the update will use 196 of them for sound (38 of which will be streaming channels).  The actual quantity of channels is based upon what the underlying sound system reports about the hardware.

###DynamicSurroundings-1.11.2-3.2.7.0
**What's New**
* Light Level will be colored YELLOW if the current light level does not permit spawning, but will allow it when dark.
* Light Level will be colored BLUE for blocks where mobs cannot spawn due to height restrictions, type of blocks (slab, glass, etc.)
* Configuration option to display light level either vertically or layered on top of a block.
* Configuration option for lighter or darker color indicator set.
* Configuration option for hiding "safe" light levels (block light > spawn light level or blocks mob spawn)
* Barking dog sound in Villages
* Option to have square footprint rather than the shoe footprint

**Fixes**
* Screen should no longer black out when pressing F1 with light level active
* F5 will properly render light level based on perspective
* Dust particles will not disappear after editing/saving mod configuration.

**Changes**
* Light Level control changed!
    * The L key now toggles the light level HUD on/off
    * CTRL+L will toggle between block and block+skylight
    * SHIFT+L will toggle display of safe blocks

###DynamicSurroundings-1.11.2-3.2.6.0
**What's New!**
* Facade support for EnderIO/Chisel.  Sound and footsteps are based on the facade of conduits.  Will expand to other mods with facade capabilities over time.  (There isn't a 1.11 version of EnderIO yet, but the capability is in when it arrives.)
* Light Level HUD.  Bound to the "L" key by default:
    * First mode shows block light
    * Second mode shows combined block and sky light
    * Light level renders as a text number above the block.
    * Pressing L repeatedly will cycled the modes

**Fixes**
* Footsteps on snow blocks as well as snow layers.
* Micro variation of footstep Y to minimize z-fighting.  This reduces the flicker effect greatly for overlapping footsteps.

**Changes**
* Reworked fog system.  Should be smoother and handle transitions better.
* Reduced aurora transparency so they are more ephemeral.
* Water drop TLC:
    * Water drop into lava produces hiss
    * Lava drop into water produces hiss
    * Water drop into water produces drop sound
    * Drops from leaves are still ignored

###DynamicSurroundings-1.11.2-3.2.5.0
**What's New!**
* Added Coyote spot sound to a bunch of biomes.  Chance of playing at night when it isn't raining.
* Village sounds.  Sounds that can play when a player is in a village.  Requires server side mod installation for this feature to work.  (Minecraft village information is available server side, not client side.)
    * Rooster will crow during sunrise when the player is outside and it isn't raining.
    * Blacksmith hammer/anvil will play periodically during the day when the player is outside and it is not raining.
    * Sounds play in addition to biome sounds.  Village sounds are attached to the "player biome".
    * "In a village" defined as within a given village radius.

**Fixes**
* Destroy footprint particle when block beneath disappears.
* Tickable repeat sounds now fade in/out in a timely way.
* Fix intermittent sound handler crash reported via OpenEye (hopefully).

###DynamicSurroundings-1.11.2-3.2.4.1
**Changes**
* Created my own footstep particle:
    * Looks like footsteps with facing orientation
    * Lighting works so they shouldn't "black out" when looking at them at various angles
    * Shouldn't look like they are floating around when moving
* Option to disable footprint feature

###DynamicSurroundings-1.11.2-3.2.4.0
**What's New!**
* Use Minecraft's footprint particle to leave a footprint trail as the player walks.  The footprints will fade after a short period of time.  Soft blocks (sand, dirt, grass, etc) will have footprints; hard blocks (stone, sandstone, etc) will not.
* Started EnderIO block/item support.

**Fixes**
* Walking on EnderIO conduits should no longer crash the client.  Should fix walking on other blocks that do not use one of the standard Minecraft material types.

**Changes**
* Separate armor footwear sound overlay from main body.  There will now be two overlays: overlay from the chest/leggings slot, and the foot.  This will provide further variation if a player wears mixed sets of armor and allows for more footfall sound accents due to armor.
* Added new Medium armor class.  The Minecraft chainmail is in this class.
* Implemented config file versioning.  This will allow me to reset various options as needed.

###DynamicSurroundings-1.11.2-3.2.3.0
**What's New!**
* Relaxed load requirements so that the mod can run on 1.11.
* Plains biome now has a background sound.  Depending on mods installed prairies, grasslands, shrublands, etc. will also have the same sound.

**Fixes**
* I introduced an issue when tuning that caused biome spot sounds to be rare.  I renormalized the chance values so things should go back to normal.

**Changes**
* Redo of the Forest sounds.  New background sound that is more "background", and added several additional spot sounds of birds.  The goal was to minimize the repetitiveness of the prior sound clip.  The bird calls in that sound clip were pretty up-front and contributed to the repetitive impression.
* Updated underwater background sound for rivers, ocean, and deep ocean.  Sounds better IMO.
* Changed default footstep sound factor to 0.5 from 0.15.  Should make footstep and armor sounds a bit louder.  Note that this is a default meaning if you have an existing configuration the setting will remain at the currently defined value.
* Updated ru_RU.lang

###DynamicSurroundings-1.11.2-3.2.2.0
**What's New!**
* [Added armor overlay sounds](https://github.com/OreCruncher/DynamicSurroundings/wiki/Sound-Overlay).  When moving about additional sounds will be mixed in based on armor being worn by the player.

**Changes**
* Tweaks sound volumes and handling.  Affected sounds:
    * dsurround:jungle - decreased volume
    * dsurround:bookshelf - decreased volume
* Modified block effects.  Affected blocks:
    * bookshelf - triggers 25% less
    
###DynamicSurroundings-1.11.2-3.2.1.2
**What's New!**
* Added keybinding to toggle the block selection box on and off.  The selection box is the black fence you see outlining a block in the world when you have the cursor on it.  By default control is bound to the B key.

**Fixes**
* Rearranged some code to improve mod compatibility "out of the box".
* Don't allow AIR to be overwritten in the Acoustic registry.  This is what was causing the "iron block" sound when walking on the edge of a block.
* There were some reports of weather occurring underground in other places where it shouldn't.  I changed the implementation of suspect area of code. If anyone sees the problem file an issue and we can go from there.

###DynamicSurroundings-1.11.2-3.2.1.1
**What's New!**
* Support 1.11.2 Minecraft.  Same feature set as Dynamic Surroundings 1.10.2-3.2.1.1.

**Fixes**


**Changes**
* Removed support for Calendar API.  Will re-add when it comes to 1.11.2
