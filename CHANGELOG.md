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
