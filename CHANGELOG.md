###DynamicSurroundings-1.10.2-3.2.3.0
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

###DynamicSurroundings-1.10.2-3.2.2.0
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
