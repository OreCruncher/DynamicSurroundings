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