Tutorial: Item Configuration
============================
The item configuration section allows a modpack author to define the various item sounds that can
occur when a player manipulates an item.  Assignment to a particular type will define what the equip,
swing, use, and foot step accent (in case of armor) to use.  An example Json for the option is as
follows:

::

	"items": {
		"SHIELD": [
			"net.minecraft.item.ItemShield"
		],
		"LEATHER": [
			"minecraft:leather_boots"
		],
		"CHAIN": [
			"minecraft:chainmail_chestplate"
		],
		"CRYSTAL": [
			"minecraft:diamond_helmet"
		],
		"PLATE": [
			"minecraft:golden_leggings"
		],
		"AXE": [
			"net.minecraft.item.ItemPickaxe"
		],
		"BOW": [
			"net.minecraft.item.ItemBow"
		],
		"SWORD": [
			"net.minecraft.item.ItemSword"
		],
		"TOOL": [
			"net.minecraft.item.ItemFishingRod"
		],
		"FOOD": [
			"net.minecraft.item.ItemFood"
		]
	}

You will notice that entries above come in two forms:

- Java class name of the item in question (i.e. net.minecraft.item.ItemShield).  This tells Dynamic Surroundings that any item that derives from ItemShield will also belong to this group.  This helps with "out of the box" support for modded items, assuming they derive from the Minecraft classes.
- By item name (i.e. minecraft:diamond_helmet).  This is highly specific and only the item of that type will be added to the category.

The first 5 entries in the list are considered armor categories.  Armor that is assigned to these
categories will also have the corresponding accents applied when determining the footstep acoustic
to play when the player moves.  The remaining 5 are considered item categories.

It it is possible that an item can be assigned to an armor category (such as assigning a leather
belt item to LEATHER).  This will affect the items equip, use, and swing sounds, and since these
non-armor items cannot be equipped in armor slots no armor sounds will play.
 