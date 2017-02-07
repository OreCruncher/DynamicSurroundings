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
