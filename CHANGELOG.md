###DynamicSurroundings-1.10.2-3.4.1.0
**What's New**
* Aurora spawn is now 100% client side!  This means auroras will display in polar biomes even though a server may not have Dynamic Surroundings installed.
    * The "seed" for the aurora is based on the current Minecraft day of the dimension in question
    * Auroras will be a fixed X,Z distance from the player regardless of the direction of movement (i.e. you can no longer fly up into them)
    * Aurora Y will obey the settings in the configuration file
    * They will fade once you move out of a polar biome
    * Because aurora processing is 100% client side there is no server side code
    
**Fixes**
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

###DynamicSurroundings-1.10.2-3.4.0.0
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

###DynamicSurroundings-1.10.2-3.3.8.0
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

###DynamicSurroundings-1.10.2-3.3.7.0
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

###DynamicSurroundings-1.10.2-3.3.6.1
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

###DynamicSurroundings-1.10.2-3.3.6.0
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

###DynamicSurroundings-1.10.2-3.3.5.2
**Fixes**
* Strange rendering caused by client side block event handler throwing an NPE due to race condition

###DynamicSurroundings-1.10.2-3.3.5.1
**Fixes**
* LWJGL having difficulties digesting default sound device name

**Changes**
* Updated Tough as Nails support (v1.9.4-1.1.1)

###DynamicSurroundings-1.10.2-3.3.5.0
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
* Built against Forge 12.18.3.2281
* Fancified compass and debug hud with text panels
* Disabling desert and biome fog effects will turn off fog processing within Dynamic Surroundings.  Intended to address compatibility with certain mod configurations (DS + BOP + OptFine).
* Default footprint style is now square
* Fireflies will be out at sunset as well as night
* Changed a dry biome definition from being no rainfall to having a rainfall of < 0.2F
* Code cleanup and performance tweaks

###DynamicSurroundings-1.10.2-3.3.4.1
**Fixes**
* Exception modifying immutable collection when config reloads after editing
* Simply Jetpacks sound would not stop when it should

**Changes**
* Updated/Improved/Fancified the Dynamic Surroundings mod information screen
* Debug HUD that describes blocks in the world when in creative mode and holding a stack of nether stars.  Helps a modpack/resource pack developers in modifying Dynamic Surroundings configuration files.
* Show debug information in an item's tooltip if Dynamic Surroundings debugging is enabled.
* No longer integrate with Waila for display of debug information.

###DynamicSurroundings-1.10.2-3.3.4.0
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
* Built against Forge 12.18.3.2254.
* Replace Minecraft SoundManager with slightly modified one to improve playing of looping sound clips and simplify volume scale hook
* Use Item Java class hierarchy to identify sound to play when player swings an item.  This allows for a modded item to be "detected" and handled appropriately if derived from an existing known class (i.e. a modded hoe that derives from ItemHoe will get the swing sound of a hoe.)
* Reworked waterfall splash and dust mote particle rendering to squeeze more performance.
* Capped number of streaming sound channels at 16.
* Increased firefly spawn rate quite a bit, and smoothed out the Y trajectory.  Fireflies spawn around `red_flower`, `yellow_flower`, and `double_plant`.  It does NOT include `tallgrass`.  (The two block tall grass is actually a `double_plant`.)
* Randomize the initial sound play for waterfall splash.  This should even out the sound effect of large falls a bit.
* General performance tweaks related to internal data tables.

###DynamicSurroundings-1.10.2-3.3.3.1
**What's New**
* Mod Support: Railcraft

**Changes**
* Aurora rendering was moved to a different rendering phase so that it can render properly even when rain/snow rendering is replaced by other mods (Weather2).
* Use different RNG; improves area scanning performance a bit

###DynamicSurroundings-1.10.2-3.3.3.0
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

###DynamicSurroundings-1.10.2-3.3.2.1
**What's New**
* Mod support for Ars Magica 2, Rockhounding: Surface, Rockhounding: Ore Tiers

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

###DynamicSurroundings-1.10.2-3.3.2.0
**What's New**
* Nether dimension background sound (may need some tweaking)
* Added another compass rose texture (style 6).
* Mod support (FTB Beyond Modpack):
    * Actually Additions
    * Thermal Expansion

**Changes**
* Take into account Wasteland Forest ([Wasteland Mod](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2721105-wasteland-mod-the-lost)) when applying biome rules
* Initialize sound channels when SoundSetupEvent is raised

###DynamicSurroundings-1.10.2-3.3.1.1
**Fixes**
* Repeated "java.lang.IllegalArgumentException: value already present" when playing sound.

###DynamicSurroundings-1.10.2-3.3.1.0
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

###DynamicSurroundings-1.10.2-3.3.0.0
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

###DynamicSurroundings-1.10.2-3.2.8.1
**What's New**
* Config option to override default Minecraft thunder volume of 10000.

**Changes**
* Reduced the amount of "clipping" the light level frustum produced.  Light level textures will show at the boundry of the player field of view.

###DynamicSurroundings-1.10.2-3.2.8.0
**What's New!**
* Added new Light Level HUD style - surface with rotation.  Displays the light level on the block surface and rotated toward the player.  Snaps to the NWSE directions.
* Light Level HUD display mode is now configurable so you can set your favorite default.
* Underwater background sound.  This is for when the player's head is in water, but not in a watery biome.

**Fixes**
* Incorrect default fog colors for the Nether and The End were being used.

**Changes**
* Increased the default number of sound channels.  Modern sound systems generally have 255 channels, and the update will use 196 of them for sound (38 of which will be streaming channels).  The actual quantity of channels is based upon what the underlying sound system reports about the hardware.

###DynamicSurroundings-1.10.2-3.2.7.0
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

###DynamicSurroundings-1.10.2-3.2.6.0
**What's New!**
* Facade support for EnderIO/Chisel.  Sound and footsteps are based on the facade of conduits.  Will expand to other mods with facade capabilities over time.
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

###DynamicSurroundings-1.10.2-3.2.5.0
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

###DynamicSurroundings-1.10.2-3.2.4.1
**Changes**
* Created my own footstep particle:
    * Looks like footsteps with facing orientation
    * Lighting works so they shouldn't "black out" when looking at them at various angles
* Option to disable footprint feature

###DynamicSurroundings-1.10.2-3.2.4.0
**What's New!**
* Use Minecraft's footprint particle to leave a footprint trail as the player walks.  The footprints will fade after a short period of time.  Soft blocks (sand, dirt, grass, etc) will have footprints; hard blocks (stone, sandstone, etc) will not.
* Started EnderIO block/item support.

**Fixes**
* Walking on EnderIO conduits should no longer crash the client.  Should fix walking on other blocks that do not use one of the standard Minecraft material types.

**Changes**
* Separate armor footwear sound overlay from main body.  There will now be two overlays: overlay from the chest/leggings slot, and the foot.  This will provide further variation if a player wears mixed sets of armor and allows for more footfall sound accents due to armor.
* Added new Medium armor class.  The Minecraft chainmail is in this class.
* Implemented config file versioning.  This will allow me to reset various options as needed.

###DynamicSurroundings-1.10.2-3.2.3.0
**What's New!**
* Plains biome now has a background sound.  Depending on mods installed prairies, grasslands, shrublands, etc. will also have the same sound.

**Fixes**
* I introduced an issue when tuning that caused biome spot sounds to be rare.  I renormalized the chance values so things should go back to normal.

**Changes**
* Redo of the Forest sounds.  New background sound that is more "background", and added several additional spot sounds of birds.  The goal was to minimize the repetitiveness of the prior sound clip.  The bird calls in that sound clip were pretty up-front and contributed to the repetitive impression.
* Updated underwater background sound for rivers, ocean, and deep ocean.  Sounds better IMO.
* Changed default footstep sound factor to 0.5 from 0.15.  Should make footstep and armor sounds a bit louder.  Note that this is a default meaning if you have an existing configuration the setting will remain at the currently defined value.
* Updated ru_RU.lang

###DynamicSurroundings-1.10.2-3.2.2.0
**What's New!**
* [Added armor overlay sounds](https://github.com/OreCruncher/DynamicSurroundings/wiki/Sound-Overlay).  When moving about additional sounds will be mixed in based on armor being worn by the player.

**Changes**
* Tweaks sound volumes and handling.  Affected sounds:
    * dsurround:jungle - decreased volume
    * dsurround:bookshelf - decreased volume
* Modified block effects.  Affected blocks:
    * bookshelf - triggers 25% less

###DynamicSurroundings-1.10.2-3.2.1.2
**What's New!**
* Added keybinding to toggle the block selection box on and off.  The selection box is the black fence you see outlining a block in the world when you have the cursor on it.  By default control is bound to the B key.

**Fixes**
* Rearranged some code to improve mod compatibility "out of the box".
* Don't allow AIR to be overwritten in the Acoustic registry.  This is what was causing the "iron block" sound when walking on the edge of a block.
* There were some reports of weather occurring underground in other places where it shouldn't.  I changed the implementation of suspect area of code. If anyone sees the problem file an issue and we can go from there.

###DynamicSurroundings-1.10.2-3.2.1.1
**Fixes**
* Fix Nullpointer exception when playing sound because of a bonehead mistake on my end.  You may not have seen the problem depending on the mix of mods in your pack.

**Changes**
* Updated and building against Forge 12.18.3.2202.  Should continue to be compatible with older Forge versions.
* Updated ru_RU.lang

###DynamicSurroundings-3.2.1.0
**What's New!**
* Added a not ! operator to the conditional string grammar.  Does the same thing as the function NOT but without all the extra verbiage.
* Removed regex support for conditional strings.  Must use the new scripting syntax.
* Added weapon/tool sound support for TConstruct things.  Now you can swoosh! with style with that cleaver.  (Thanks lukiono!)
* Added some debris enhancements to explosions.  Just light some TNT and watch.  Or use flint and steel on a creeper.  Let me know what you think.
* Embellished the Debug Dialog with additional tabs.

**Fixes**
* Don't initialize state on a Netty thread. :\
* Fixed a delayed acoustic bug that's been around for a while.  Landing on blocks should sound better.
* Blending of biome sounds at biome boundaries would sometimes result in lower volumes.  Fixed that.

**Changes**
* Changes focused on performance and memory utilization.
* Increased default special effect range from 16 to 24.
* Improved fade in/out of biome sounds when moving between biomes.  Shouldn't sound as abrupt.

###DynamicSurroundings-3.2.0.0BETA
**What's New!**
* [Background thunder during storms!](https://github.com/OreCruncher/DynamicSurroundings/wiki/Let's-Talk-Rain)
* Refreshed sounds; Minecraft thunder replacement sounds.
* For those wanting to develop their own sound/block configuration files there is a new configuration option "Enable Debug Dialog".  Setting to "true" will cause a Java dialog window to appear displaying the state of all the script variables and their current values.  Dialog will only be active when a world is loaded.  The dialog will dynamically update as you move through the world.
* Config options for disabling ASM transformations.  Dynamic Surroundings weather effects can be turned off completely using these options.  Useful if using shader packs and the weather just doesn't work right.
* Rain/snow/dust will be auto-disabled if Localized Weather and Stormfronts (Weather2) is installed.  Weather2 does a lot with weather and rendering, and Dynamic Surroundings would interfere with all that.
* Config GUI can now be translated!  Russian translations provided by Xottab-DUTY!  Thanks!

**Fixes**
* [Updated README with new attributions for sounds.](https://github.com/OreCruncher/DynamicSurroundings/blob/master/README.md)  Depending on the attributions monetizing streaming channels may be a challenge.

**Changes**
* Speech Bubbles, Entity Chat, and Entity Emojis disabled by default.  These can be turned back on in the configuration.
* Speech bubble range reduced to 16 as a default range.  Affects player chat as well as entity talk.
* Speech bubbles will only render if the player can see the entity.  No more "X-ray" of mobs and the like.
* A ParticleWaterSplash instance can have only one outstanding sound playing.  Reduces pressure on the Minecraft sound engine.  (Each block you see splashes around is an instance.)
* Adjusted the ParticleWaterSplash creation so that it will not spawn in simple water flows, like dumping a bucket of water on the ground.  Intent is to reduce client and rendering impacts with large falls.
* Created a new waterfall OGG sound rather than reuse the rain drop.
* Additional checks for bad sounds when debug logging is enabled.  Minecraft does do checks at startup but I have seen some bad ones during runtime that are currently not explained.
* Move default location of the Potion HUD to be the upper left.  Main reason is that a lot of mapping mods put their maps in the upper right and the intent is to reduce the amount of configuration the player has to do.
* Changed how the configuration options are used for feature control.  Most option changes will not require a restart of the client.
* The sky will darken when it starts raining rather than having some sort of variable gray sky.  This is because I refactored how rain intensity affects Minecraft to eliminate ASM.  Goal was to simplify, work more within the Forge framework, and minimize issues related to mod interactions.
* Implemented area scanner for "always on" particle effects such as water splashes and steam jets.

**Notes**
* It's recommended you make a backup copy of your configuration file and let Dynamic Surroundings create a new one.  There were changes made to default settings that would affect things like sound.

###DynamicSurroundings-3.1.4.1BETA
**Fixes**
* Improved error checking around the Forge version check information.
* Fixed syntax error in entity chat data file.

###DynamicSurroundings-3.1.4.0BETA
**What's New!**
* Better support for Tough as Nails.  Dynamic Surroundings will use the temperature information from TAN to determine weather rendering effects as well as player temperature settings.
* Tap into Forge's version check capability to check for Dynamic Surrounding updates.  Starting with this version of the mod, any new version will cause Forge to indicate new mod availability.  Disabling version checking in the Dynamic Surroundings config will cause the chat notice to be suppressed but will not block Forge online checking.
* Waterfall splash and sound effect.  Will occur when water is flowing down and hits a solid or liquid surface.  Can be sound and particle intensive depending on the size of the waterfall.  I recommend blocking the sound "minecraft:block.water.ambient" if you have the waterfall splash enabled.
* Witches and skeletons have speech text.
* Started making a "public" API for mod integration.

**Fixes**
* The default sounds for sound culling were incorrect.  Updated to match the new Minecraft sound names.  It is recommended that you delete the option in the config file and let Dynamic Surroundings regenerate the default settings for the option.
* The Forge event that the culling/blocking logic was using changed so culling/blocking didn't take place. :\

**Changes**
* Scripting now uses the apostrophe ' to mark a string.  Using the quote became a pain especially since it had to be escaped in the Json config file.
* Tweaked "inside" detection algorithm.  Gives more weight to blocks above and around the player than those further out. Helps a bit with heavy forests though there are cases where the player will still be considered "inside".  Usually occurs when the tree has lots of logs in the canopy.
* Modified the "underground" background ambient sound to only play when the player's Y is <= 32 (i.e. going deeper).  The rock slide and monster growl sounds are unaffected.

###DynamicSurroundings-3.1.3.0BETA
**What's New!**
* Want to know what mobs are thinking?  Look at the emojis!  This is WIP and subject to change.  Entities will have emoji particles orbiting their head indicating what they are thinking.  Works in SP and SMP where the server has Dynamic Surroundings installed.
* [Enhanced condition strings for configuration files](https://github.com/OreCruncher/DynamicSurroundings/wiki/Tutorial:-Condition-Strings).

**Fixes**
* Should no longer "buzz" when player health gets low.

**Changes**
* Speech bubbles are rendered as particles.  Address some rendering quirks and improves performance a little bit.

###DynamicSurroundings-3.1.2.0BETA
**What's New!**
* Natura and Agricraft support built in.
* Russian translations.  Thanks Xottab-DUTY!
* [Villagers can be chatty](https://github.com/OreCruncher/DynamicSurroundings/wiki/Entity-Chat).  Need feedback!
* [Configuration Json files will be loaded from a resource pack if provided](https://github.com/OreCruncher/DynamicSurroundings/wiki/Tutorial:-Resource-Packs).

**Fixes**
* Beetroots have 4 growth stages, not 7 like other crops.
* Fixes for concurrency issues related to network packet handling.
* Fixed Speech Bubbles rendering for incorrect entity.
* Fixed reference to Minecraft ambient fire sound.
* Updated Footstep primitive map based on new Minecraft sound names.  Should improve footstep sounds for modded blocks that do not have direct support within Dynamic Surroundings.  Example, if a modded block has a step sound of SoundType.WOOD Footsteps will apply the "wood" acoustic to the block.

**Changes**
* Additional information in Waila HUD when debug is enabled.  Helps out when trying to figure out what a block is called and what Footsteps acoustics are associated.
* Removed moonlight alpha scaling when rendering Auroras.  I may introduce back when I come up with a better
scheme to dim the aurora when the moon is bright.
* Added more Aurora color pairs, and disabled fog when rendering.  Auroras should stand out pretty well now.
* Converted sounds from stero to mono.  Works better with point sounds and reduces overall size of JAR.

###DynamicSurroundings-3.1.1.0BETA
**What's New!**
* The configuration system received a major overhaul.  Biome and dimension tweak files have been combined into a single file.  Additionally, Footstep and Forge mapping can be done in the same file. [See the wiki for details.](https://github.com/OreCruncher/DynamicSurroundings/wiki/Tutorial:-Configuration-Files)
* Biomes O'Plenty, Tinkers Construct, Forestry, and Pam's HarvestCraft support built in.  There were changes in these mods for 1.10.2 that required some maintenance.
* German translations - thanks Hendrik!

**Fixes**
* Fixed up Minecraft sound names that changed since the prior versions.  Ladder climbing and swimming should now work.
* There were some issues with the meta data sensitive block registry. 

**Changes**
* Some of the longer playing sounds have been tagged as "stream".  This should reduce/eliminate perceived lag spikes when crossing biome boundaries.

###DynamicSurroundings-3.1.0.0BETA
**What's New!**
* Minecraft 1.10.2 support!  Yay!  Works in SP as well as SMP.
* Speech Bubbles will display player chat messages above their heads.
* Firefly block effect that generate fireflies at night in biomes that are temperate or warm.

I am looking for feedback on the firefly and speech bubble features.  Feel free to send me a message direct with comments.  If you have an issue make an entry in the the [issue tracker](https://github.com/OreCruncher/DynamicSurroundings/issues) so it won't get lost.

I want to thank Abastro for stepping up and providing unofficial builds for those of you that moved on to Minecraft 1.9.x+.  I had no intention of dropping the mod but sometimes real life things work against you.  No fear, though, things are back to normal - or what passes for normal in the US now a days.

**Fixes**
* Block effects (effects and footsteps) are now sensitive to block metadata values.  This means that dust motes will no longer drop from the polished versions of stone.
* Updated support for the 1.10.2 release of Biomes O'Plenty.  Checkout the firefly effects in a BoP world.
* Updated Dynamic Surroundings particle sheet.  Particles for the End Rod and Minecraft's hit splat hearts will now render correctly.

**Changes**
* The mod is built and tested against Forge 1.10.2-12.18.2185.
* Added options to quickly disable various block effects without having to resort to configuration files.  Don't like the new fireflies?  Turn them off.
* Removed fancy cloud handling.  It was causing more compatibility issues with mods than what was gained.  Dynamic Surroundings needs to work out of the box.
* Since Minecraft now has a weapon swoosh sound that plays when whacking a mob, the Dynamic Surroundings feature was changed to produce a noise when a tool or weapon is swung but not hitting a mob.  This allows you to impress villagers with your technique by swinging the tool all around!  Well, not really...
* Since Minecraft now has a potion HUD Dynamic Surroundings has been changed to replace it out out of the box.  The potion indicators now line up in the upper right corner.  If you like the Minecraft HUD better you can turn off the Dynamic Surroundings potion HUD and behavior will revert.
* Thunder storms in Minecraft became rare because of how the weather mechanics in Minecraft work and Dynamic Surroundings interacted.  Additional configuration options are now available that allow a player to customize when thunder occurs.  By default the rain intensity has to be greater than 50 for there to be a chance of a storm (thunder).
* The default range for **Dynamic Surroundings** particle effects has been changed from 16 to 32.  A configuration option can be tweaked so that you can reduce it back to 16, or increase it all the way up to 64.  Note that longer ranges will require more CPU so pay attention to client side tick lag.
* Tweaked popoff particles to always render on top.  Sometimes the particle was buried in a mob and you wouldn't see it.  Oh, added more catch phrases to the critical hit particle.
* Biome background sounds will fade in rather than starting abruptly.  Makes biome transitions more natural.
* Increased the default volume scale factor for footstep sounds so that they play louder.
* The `/rain` command is now `/ds`.  The options and functions have changed.  Detailed information can be found [here](https://github.com/OreCruncher/DynamicSurroundings/wiki/Commands).