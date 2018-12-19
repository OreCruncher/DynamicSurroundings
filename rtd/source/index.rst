.. |br| raw:: html

   <br />
   
Welcome
-------
Dynamic Surroundings is a Minecraft Forge mod that alters the player's **visual** and **audible**
experience without changing game mechanics.  The player has a high degree of control over their
experience, and modpack authors can customize biome and block effects based on their need.

This set of documentation applies to Dynamic Surroundings v3.5+ for Minecraft 1.12.2+.
Documentation for prior versions can be found here_.

The design of Dynamic Surroundings permits it to run on a client without having the mod installed on
the server.  However, there will be limitations:

- Player speech bubbles will not be displayed.
- Village biome sounds will not play.
- Weather render effects will not be coordinated across clients.
- Battle Music will be limited (if configured)

..	versionadded:: 3.5.4.0

	Starting with version 3.5.4.0 it is possible to have the mod installed on a server, but not on the client.
	This will allow pack authors to include the mod in packs, and give players the ability to remove the mod
	based on their desires.

Downloads: `CurseForge <http://minecraft.curseforge.com/projects/dynamic-surroundings>`_ |br|
Source Code: `GitHub <https://github.com/OreCruncher/DynamicSurroundings>`_ |br|
Issue Tracker: `GitHub Issues <https://github.com/OreCruncher/DynamicSurroundings/issues>`_

-----

..	list-table::
	:widths: auto
	:align: center
   	:header-rows: 0

	*	-	..	toctree::
				:titlesonly:
				:maxdepth: 1
			
				license
				credits
				compatibility
				howdoi
				performance
				commands/calc
				commands/ds
		-	..	toctree::
				:titlesonly:
				:maxdepth: 1
				:glob:
				
				features/*
		-	.. 	toctree::
				:titlesonly:
				:maxdepth: 1
				:glob:
		
				tutorials/*

.. _here: https://github.com/OreCruncher/DynamicSurroundings/wiki

