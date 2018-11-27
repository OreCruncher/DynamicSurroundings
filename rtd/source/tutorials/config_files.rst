..	_tutorial-config-files:

Tutorial: Configuration Files
=============================
It is assumed that you are familiar with Json.  If you wish to learn some internet searching on
"json tutorials" will give a wealth of information.

Feel free to experiment with the configuration.  You can't break anything and it is pretty easy
to try new and different acoustic combinations.

--------

Dynamic Surroundings will load configurations from three general locations:

- Internal configurations.  These are Json configuration files that are contained with the Dynamic Surroundings JAR archive file.  These configurations provide `support for various mods <https://github.com/OreCruncher/DynamicSurroundings/tree/master/src/main/resources/assets/dsurround/dsurround/data>`_.
- Resource Packs.  These are the packs that you can download and install that can customize things like graphics and sound.
- External Configuration Files.  These are configuration files contained in the ``./minecraft/config/dsurround`` directory and are typically provided by other players or modpack authors to customize the experience based on their vision.

Loading of these configurations occur in the order listed above.  It is possible for a given
configuration file to override/modify the settings of prior configuration files.  This means that
a modpack author can fully redefine the configuration based on what is needed.

A configuration file can define as much or as little as needed.  For example, if Biomes are not being
configured the "biomes" tag in the Json file can be omitted.  Browse the Github repository referenced
above to see what I have done to support other mods.

Including a Configuration File
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**STEP 1**  Create a text file in the ``./minecraft/config/dsurround`` directory that will contain your
settings.  Make sure it has the file extension of ".json".  For the purpose of this tutorial call it
"tutorial.json".

**STEP 2** Edit the content of the file so that it looks like the following and save it.

::

	{
		"biomes":[
		],
		"blocks":[
		],
		"items": {
		},
		"dimensions":[
		],
		"acoustics": {
		},
		"primitiveAcoustics": {
		},
		"footsteps":{
		},
		"forgeMappings":[
		],
		"variators": {
		},
		"entities": {
		}
	}

Please note the placement of the curly braces { } and brackets [ ].  These tokens are part of the Json
grammar and have meaning.  They are not interchangeable!

**STEP 3** Edit the ``./minecraft/config/dsurround/dsurround.cfg`` configuration file.  Locate and
modify the "External Configuration Files" under "general". Put in the "tutorial.json" file name.
If there is more than one configuration file each name must be on a separate line.
NO BLANK LINES!

::

	# Configuration files for customization [default: ]
	S:"External Configuration Files" <
	    tutorial.json
	 >

**STEP 4** Startup up your Minecraft client.  If you did things right you should see something
like the following show up in the trace log:

::

[08:01:43] [Client thread/INFO] [dsurround/dsurround]: Executing script for mod [E:\minecraft\config\dsurround\tutorial.json]

**STEP 5** Edit the ``tutorial.json`` file with the changes you wish to make.  You can reload the
configuration without exiting the client by issuing the ``/ds reload`` command.  Note that you will
have to be in creative mode to execute the command.

As mentioned prior, Dynamic Surroundings uses the same style configuration file to provide support
for Vanilla blocks as well as the common Forge ore dictionary names.  You can look at that file here_.

.. _here: https://github.com/OreCruncher/DynamicSurroundings/blob/master/src/main/resources/assets/dsurround/data/mcp.json
