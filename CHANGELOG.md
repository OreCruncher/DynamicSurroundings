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