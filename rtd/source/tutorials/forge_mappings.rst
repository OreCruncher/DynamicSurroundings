Tutorial: Forge Mappings
========================
This tutorial assumes that you have already created a Json configuration file.  If you haven't please
read `Tutorial: External Configuration Files <config_files.html>`__.

Forge mappings is a way to apply an `acoustic <acoustic_profiles.html>`__ to all blocks that are
contained in a specified Forge Ore Dictionary group.  Configuration is pretty straight forward.
Here is an example from the internal Minecraft configuration_ file::

	"forgeMappings": [
		{
			"accousticProfile": "ore",
			"dictionaryEntries": [
				"oreIron",
				"oreGold",
				"oreCopper",
				"oreTin",
				"oreSilver",

This example shows that members of "oreIron", "oreGold", etc. are given the acoustic "ore".

The configuration for Minecraft is quite extensive so the need of specifying these entries is pretty
small.  However, a mod may add new Forge Ore Dictionary groups that are used and it would be easier
to configure all members within a group rather than making detailed entries in the "footsteps"
configuration.

..	_configuration: https://github.com/OreCruncher/DynamicSurroundings/blob/master/src/main/resources/assets/dsurround/dsurround/data/mcp.json
