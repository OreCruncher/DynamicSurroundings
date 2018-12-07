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

