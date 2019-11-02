### DynamicSurroundings-1.12.2-3.6.0.1
**Requirements**
* Forge 2779+
* OreLib-1.12.2-3.6.0.0+; **Do not use the deobf version of OreLib**; it's provided to developers for mod development
* If upgrading from version 3.4.x or earlier:
     * Delete your dsurround.cfg and let it regenerate
     * Check your mods/1.12.2 folder and ensure there are no Dynamic Surroundings files within

**What's New**
* Changed from having a separate coremod to using the Sponge Mixin framework.  (Why didn't I do this sooner?)
     * Change should be transparent.  If you have a problem open an issue in the tracker.
* Eliminated AT transforms.
     * Change should be transparent.  If you have a problem open an issue in the tracker.
* Option to turn off the potion icon display to the left of the inventory screen.  You will have to enable.  Option is under the ASM options in the config.
* Fire Jet effect can be applied to solid blocks.  The height of the effect is currently fixed.
* Option to disable the lava particle effect when generating rainsplash on netherrack and magma blocks

**Fixes**
* NPE when a mod creates a fake world before mod initialization is complete
* Default splash ripple effect on water should be pixelated
* Sound Physics updates from djpadbit.  Apparently some stuff got lost along the way.
* Compatibility changes for Cubic Chunks and Nether to help avoid extreme lag spikes on client.
* [OpenEye] Fixed some edge cases in server side network code which caused concurrency issues.
* [OpenEye] Several speculative fixes for NPEs when receiving capability data when client not entirely ready.
* [OpenEye] Speculative fix for concurrency exception initializing biome registry.
* [OpenEye] Speculative fix for Eclipse Open J9 compatibility when processing entity data server side.  If anyone uses Open J9 I would appreciate feedback.

**Changes**
* Changed ASM options no longer require world/client restart.  Yay Mixins!
* Cleaned up Animania badging a bit.  As a result badges will not display when the animal is sleeping.
* Blocked the following sounds as a default: elephant, gnatt, insectbuzz, hiss, and rattlesnake.  These are some of the most disliked sounds in the mod, and most folks aren't aware that they can be blocked in the configuration.  If you want these sounds you will need to turn them on.
* Gave auroras some TLC:
     * Render optimizations both in code and shader - big improvements.
     * Added config option to cap number of bands.  The default is max of 3 bands but can be turned down to 1 for potatoes.
     * I have a GTX 980 and I can drive a 3 band aurora at 120FPS.
* Updates for biome sounds to clean up some high frequency noise (thanks TechnoMysterio!)
     * Savanna Day, Forest, Bog, and Wind
* Adusted footstep volume scaling for non-EntityPlayer mobs so they are not as loud

### DynamicSurroundings-1.12.2-3.6.0.0
**Requirements**
* Forge 2779+
* OreLib-1.12.2-3.6.0.0+; **Do not use the deobf version of OreLib**; it's provided to developers for mod development
* If upgrading from version 3.4.x or earlier:
     * Delete your dsurround.cfg and let it regenerate
     * Check your mods/1.12.2 folder and ensure there are no Dynamic Surroundings files within

**What's New**
* Changed from having a separate coremod to using the Sponge Mixin framework.  (Why didn't I do this sooner?)
     * Change should be transparent.  If you have a problem open an issue in the tracker.
* Eliminated AT transforms.
     * Change should be transparent.  If you have a problem open an issue in the tracker.
* Option to turn off the potion icon display to the left of the inventory screen.  You will have to enable.  Option is under the ASM options in the config.
* Fire Jet effect can be applied to solid blocks.  The height of the effect is currently fixed.
* Option to disable the lava particle effect when generating rainsplash on netherrack and magma blocks

**Fixes**
* Default splash ripple effect on water should be pixelated
* Sound Physics updates from djpadbit.  Apparently some stuff got lost along the way.
* Compatibility changes for Cubic Chunks and Nether to help avoid extreme lag spikes on client.
* [OpenEye] Fixed some edge cases in server side network code which caused concurrency issues.
* [OpenEye] Several speculative fixes for NPEs when receiving capability data when client not entirely ready.
* [OpenEye] Speculative fix for concurrency exception initializing biome registry.
* [OpenEye] Speculative fix for Eclipse Open J9 compatibility when processing entity data server side.  If anyone uses Open J9 I would appreciate feedback.

**Changes**
* Changed ASM options no longer require world/client restart.  Yay Mixins!
* Cleaned up Animania badging a bit.  As a result badges will not display when the animal is sleeping.
* Blocked the following sounds as a default: elephant, gnatt, insectbuzz, hiss, and rattlesnake.  These are some of the most disliked sounds in the mod, and most folks aren't aware that they can be blocked in the configuration.  If you want these sounds you will need to turn them on.
* Gave auroras some TLC:
     * Render optimizations both in code and shader - big improvements.
     * Added config option to cap number of bands.  The default is max of 3 bands but can be turned down to 1 for potatoes.
     * I have a GTX 980 and I can drive a 3 band aurora at 120FPS.
* Updates for biome sounds to clean up some high frequency noise (thanks TechnoMysterio!)
     * Savanna Day, Forest, Bog, and Wind
* Adusted footstep volume scaling for non-EntityPlayer mobs so they are not as loud

### DynamicSurroundings-1.12.2-3.5.5.0
**Requirements**
* Forge 2779+
* OreLib-1.12.2-3.5.2.3+
* If upgrading from version 3.4.x or earlier:
    * Delete your dsurround.cfg and let it regenerate
    * Check your mods/1.12.2 folder and ensure there are no Dynamic Surroundings files within

**Fixes**
* Waterfall splash sound should no longer linger after the waterfall effect is gone.  Yay!
* No breath effect in spectator mode.
* No footstep effects in spectator mode.
* Fixed AbyssalCraft config.  Should no longer play Nether sounds in regular biomes.  (It should play in Omothol.)
* Fixed non-positive random parameter to Random in fountain jet effects.

**Changes**
* Update to support Serene Seasons 1.12.2-1.2.18.  If you use Serene Seasons you must update otherwise Dynamic Surroundings will not start!
* Update to support Animania 1.12.2-1.7.2.  If you use Animania you must update otherwise Dynamic Surroundings will not start!
* Player jump sound is disabled by default.

### DynamicSurroundings-1.12.2-3.5.4.3
**Requirements**
* Forge 2768+
* OreLib-1.12.2-3.5.2.1+
* If upgrading from version 3.4.x or earlier:
    * Delete your dsurround.cfg and let it regenerate
    * Check your mods/1.12.2 folder and ensure there are no Dynamic Surroundings files within

**Fixes**
* Fix block state matching RE: BoP overgrown blocks

### DynamicSurroundings-1.12.2-3.5.4.2
**Requirements**
* Forge 2768+
* OreLib-1.12.2-3.5.2.1+
* If upgrading from version 3.4.x or earlier you should delete your dsurround.cfg and let it regenerate

**Fixes**
* Patch up IBlockState data on the fly.  For some reason it was not present during load complete event processing
* More defensive code for reentrant ASM processing

**Changes**
* Use Serene Seasons dimension whitelist to determine what dims to apply Serene Seasons handling to
    * If you use Serene Seasons it must be at least version 1.2.13
    * Information comes from local config

### DynamicSurroundings-1.12.2-3.5.4.1
**What's New**
* Bottle slosh sound when equipping a potion (or anything derived from ItemPotion).

**Requirements**
* OreLib-1.12.2-3.5.1.3+
* If upgrading from version 3.4.x or earlier you should delete your dsurround.cfg and let it regenerate.	

**Fixes**
* Defensive code for invalid ItemStacks in ore dictionary
* Individual sound scaling should work again
* Workaround re-entrant ASM transform of SoundCategory

**Changes**
* Performance enhancements for waterfalls and other effects
    * On my i7/GTX 980 rig near a large fall generating 4000 particles and 200+ sounds I had 110 - 120FPS
* Other internal changes to squeeze out more performance per tick (improved map indexing, etc.)

### DynamicSurroundings-1.12.2-3.5.4.0
**Requires OreLib-1.12.2-3.5.1.3!**

**What's New**
* Allow the mod to be installed on the server but not on the client.  This allows a player to remove DS from their client in the case of having a potato for CPU/GPU.  For this to work the server would have to have v3.5.4.0 installed.
* Bubbles breath effect when under water.  Small bubble trail leaves an entities mouth and travels upwards a short ways.  Bigger less frequent bubbles when drowning.
* The following HUDs have been moved into a separate mod titled Dynamic Surroundings: HUDs (original, huh?)
    * Compass
    * Clock
    * Season
    * Potion
    * Light Level

### DynamicSurroundings-1.12.2-3.5.3.1
**Fixes**
* Reapply sorting index to FML ASM transformer.  Depending on modmix the names may not have been deobsfuscated to a point where it could be understood by Dynamic Surroundings.

### DynamicSurroundings-1.12.2-3.5.3.0
**Fixes**
* Fixed compatibility with Vivecraft
* Fixed occasional waterfall sound errors

**Changes**
* Removed ASM for sound timing.
* Reworked some of the internal sound handling.
* Change the default sound system buffer size from 128K (system default) to 16K.  Need to reset manually in your dsurround.cfg if the file already exists.  Not overly important to do so.

### DynamicSurroundings-1.12.2-3.5.2.0
**What's New**
* Added "alwaysOutside" dimension config attribute.  This tells Dynamic Surroundings to always consider the player outside while in that dimension.  Affects biome sound selection.
* Improved Erebus support.  Biome sounds while in the Erebus.  (See first bullet.)
* ru_ru translations for Entity Chat.  (Thanks VoltoREv!)

**Fixes**
* OpenEye: Guard against non WorldClient world in mood processing.
* OpenEye: Guard against modded client side worlds crashing when registering world listener.
* Fixed corrupted ru_ru translations.

**Changes**
* Rustic Ironwood tree root footstep sound as wood.
* Removed ASM for flushing sounds in Minecraft's sound engine.  Let me know if anything weird is encountered.
* Removed ASM for sound caching.  With other changes this feature appears to be marginal.
* Updated ru_ru translations.  (Thanks VoltoREv!)
* Default rain ripple is the pixelated version

### DynamicSurroundings-1.12.2-3.5.1.1
**What's New**
* ElectroBlob's Wizardry config support
* Books now have page flip sounds when equipped, etc.

**Fixes**
* Immersive Engineering item equip sounds.
* Reinitialize the DS ItemRegistry on the fly because some mod modified the Forge item registry after Dynamic Surroundings scanned them.
* Abstract method error loading resources from resource pack.
* Defensive code when processing ore dictionary entries where it encounters a block that is not registered.

**Changes**
* Move DS registry initialization to load complete rather than post init.
* Reworked built-in profiles a bit.  You will need to reconfigure if you used them prior.
* Added "Auroras in all Biomes" built-in profile.

### DynamicSurroundings-1.12.2-3.5.1.0
**What's New**
* Added script variable diurnal.celestialAngle
* [WIP] Added pixelated water ripple rain effect.  You will need to set the config to see it.  (Thank you Forstride for the textures!)
    * Goal is to make the ripple more Minecraft like
* If Serene Seasons is installed morning fog may be more/less foggier during certain seasons.

**Changes**
* Only render rain splash particles if world weather renderer is hooked by Dynamic Surroundings
    * Otherwise vanilla will render the splash particles
    * Should improve compatibility with other mods that replace the weather renderer
* Added additional transitions to precipitation textures to make it a bit smoother
* Recolor rain/splash textures to use vanilla rain pixel colors
* Rework drip effect to generate steam when hitting hot blocks, like magma.

### DynamicSurroundings-1.12.2-3.5.0.5
* Quick hotfix to address mock EntityItems.

### DynamicSurroundings-1.12.2-3.5.0.4
**Fixes**
* Yet another OTG related NPE fix for when a player randomly teleports.
* Entity chat was staying around too long.

**Changes**
* Improve season processing related to temperature

### DynamicSurroundings-1.12.2-3.5.0.3
**Fixes**
* NPE when encountering an OTG Biome
* Compatibility with Serene Seasons 1.2.12+.  Dependency enforcement is in place and you will get a Forge notification if you are using an older version of Serene Seasons.

**Changes**
* Stained hardened clay now has the same step sound as regular hardened clay (sandstone)

### DynamicSurroundings-1.12.2-3.5.0.2
**What's New**
* Added weather.canWaterFreeze script variable
* The various Forest and Plains background biome sound track will not play if water is capable of freezing.  For example, with Serene Seasons installed and it being in the middle of winter things will be fairly quiet in Forests and Plains.
    * This shouldn't affect things if you do not use Serene Seasons
    * It also affects things like frog croaks and fireflies

**Fixes**
* Footstep sound on thin LittleTile block was incorrect.

**Changes**
* Cap hurt threshold percentage at 50% of max health 

### DynamicSurroundings-1.12.2-3.5.0.1
**Fixes**
* Fixed NPE processing blockstate bounding box
* Fixed compatbility issue with World Downloader mod that caused it to crash
* Fixed NPE getting data from BlockStateRegistry 
* Compatibility with latest Serene Seasons.  If you use Serene Seasons it must be at least version 1.2.10!

### DynamicSurroundings-1.12.2-3.5.0.0
This release is a major update to Dynamic Surroundings.  Make sure you make backups of your Minecraft
instance.  You will need to reset your Dynamic Surroundings config file and re-apply your changes.

The format of the external Json configuration file has changed enough where you will need to rework
it to be compatible.  Tutorials on the changes can be found on the wiki.

With this release there is a dependency on an external library JAR.  Make sure that is installed
as well.

**What's New**
* New wiki can be found at https://dynamicsurroundings.readthedocs.io
* New deep forest day and night biome sounds.  Applies to Roofed Forest as well as some Birch Forest variants.
* Support Tetra mod.
* Footstep sounds on LittleTile blocks.
* Footstep sounds on ForgeMultipart blocks.
* Cosmetic Armor Reworked compatibility.  Use rendered armor model as the basis for selecting armor acoustic accents when moving.
* Improved compatibility with Just Enough Dimensions.
* Moved Battle Music into a resource pack.  As a result resource packs can be made for Dynamic Surroundings.
* Moved critical hit power words into an external file so they are now translatable.
* Added 2 new sound categories for controlling sound volume of biome and footstep sounds.  These will show up in the sound volume control dialog.
* Cleaned up mod configuration file.
* Cleaned up expression variables.

**Fixes**
* Client should no longer crash if there is a mod dependency issue.
* Render of modded potion icons in potion HUD display.
* Reload biome registry whenever a world loads.  Should address issues with OTG/Biome Bundle.
* Eliminated MusicTicker replacement.  Should improve improve compatibility with other mods that deal with the ticker.

**Changes**
* Removed reset rain on sleep configuration option.  It was little used and broadly improves compatibility because it eliminates some ASM.
* Removed rain control parameters.  These options were seldom used and materially impacted gameplay.
* Removed Albedo support.  Looking for a library replacement.
* Removed sound routing.  Crafting sounds for other players will not be heard.
* Removed Entity Emojis.  Little used feature.
* Removed explosion enhancements.  Caused too much lag with large explosions.
* Removed Presets!  If there is enough demand I can roll it into a separate mod.

**Other**
* Split library functions into a separate JAR (OreLib).
* There were a lot of internal changes made to restructure the mod and to support block variants rather than block metadata.
* Moved away from Trove collections to FastUtil collections.
* Improved validation of Json configuration files during load.

