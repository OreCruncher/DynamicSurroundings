..	_update-3.5:
..	role:: sectiontitle

The 3.5 Update
==============

:sectiontitle:`Requirements`

* Forge 2768+
* OreLib-1.12.2-3.5.2.1+
* If upgrading from version 3.4.x or earlier:
    * Delete your dsurround.cfg and let it regenerate.  Features were removed and the config options moved around.  Make sure to have a backup copy in case you need to refer to it.
    * Check your mods/1.12.2 folder and ensure there are no Dynamic Surroundings files within.

:sectiontitle:`What's New`

* Dynamic Surroundings can be removed from a client but still be installed on the server.  This will allow Dynamic Surroundings to be put into a published modpack but let an individual player remove the mod.
* HUDs have been moved into a separate mod (Dynamic Surroundings: HUDs).
* Removed a bunch of ASM.
* Deep forest day and night biome sounds.  Applies to Roofed Forest as well as some Birch Forest variants.
* Additional bird sounds (nightingale and loon) for various forest biomes.
* Air bubbles when player head is underwater.
* Liquid slosh sound equipping potions.
* Page flip sound equipping books.
* Pixelated rain splash on water - fits better with Minecraft look/feel.
* Moved Battle Music into a resource pack.  As a result resource packs can be made for Dynamic Surroundings.
* Moved critical hit power words into an external file so they can be translated.
* Added 2 new sound categories for controlling sound volume of biome and footstep sounds.  These will show up in the sound volume control dialog.
* Increased/decreased morning fog based on season if Serene Seasons is installed.
* Cosmetic Armor Reworked compatibility.  Use rendered armor model as the basis for selecting armor acoustic accents when moving.
* Improved compatibility with Just Enough Dimensions.
* Support Tetra, Electroblob's Wizardry, Ancient Warfare 2, and Minecolonies mods.
* Footstep sounds on LittleTile and ForgeMultipart blocks.
* Improved support for Erebus mod.
* Improved "Out of the Box" footstep support for IBlockStates that do not have a specific configuration.
* Cleaned up mod configuration file.
* Cleaned up expression variables.

:sectiontitle:`Fixes`

* Client should no longer crash if there is a mod dependency issue.
* Eliminated MusicTicker replacement.  Should improve compatibility with other mods that deal with the ticker.
* Improved client performance, especially around large waterfalls.
* Reduced memory footprint.
* Temperature driven precipitation rendering - provides better compat with Serene Seasons.

:sectiontitle:`Changes`

* Removed reset rain on sleep configuration option.  It was little used and broadly improves compatibility because it eliminates some ASM.
* Removed rain control parameters.  These options were seldom used and materially impacted gameplay.
* Removed Albedo support.  Looking for a library replacement.
* Removed sound routing.  Crafting sounds for other players will not be heard.
* Removed Entity Emojis.  Little used feature.
* Removed explosion enhancements.  Caused too much lag with large explosions.
* Removed Presets!  If there is enough demand I can roll it into a separate mod.

:sectiontitle:`Other`

* There were a lot of internal changes made to restructure the mod and to support block variants rather than block metadata.  This is required to support MC 1.13.
* Moved away from Trove collections to FastUtil collections.
* Improved validation of Json configuration files during load.
