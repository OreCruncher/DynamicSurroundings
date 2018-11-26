Tutorial: Blocks
================
This tutorial assumes that you have already created a Json configuration file.  If you haven't
please read `Tutorial: External Configuration Files <config_files.html>`__.

Internally Dynamic Surroundings has a Block Registry where it finds information related to spot
sounds that can be played at a block as well as special particle effects such as dropping dust
motes.  Using Json configuration files an author can modify default behavior or add new blocks to
the system.

The table below describes the various parameters that can be defined in the Json configuration file:

..	list-table:: Biome Configuration
   	:header-rows: 1
   	:widths: 20 30 60
   	:stub-columns: 1

	*	- Parameter
		- Value Type
		- Comment
	*	- blocks
		- String List
		- The block states for which the configuration options apply. See `Tutorial: BlockState Specification <blockstate.html>`_ for more information.
	*	- conditions
		- String
		- `Tutorial: Condition Strings <condition_strings.html>`_.
	*	- soundReset
		- Boolean
		- Clears existing sound configuration for the blocks.
	*	- effectReset
		- Boolean
		- Clears existing effects for the blocks.
	*	- chance
		- Integer
		- 1-in-N chance that a spot sound will play.
	*	- sounds
		- List
		- List of spot sounds configured for the block.  See below.
	*	- effects
		- List
		- List of effects configured for the block.  See below.

In order to define block options the first thing that needs to be done is to create a Json file in
the ``./minecraft/config/dsurround/`` configuration directory.  It is a normal text file but has a
specific Json syntax that must be followed.  Here is an example of such a file::

	{
		"blocks":[
			{
				"blocks": [
					"minecraft:water"
				],
				"effects": [
					{
						"effect": "steam",
						"chance": 10
					},
					{
						"effect": "bubble",
						"chance": 1800
					}
				]
			},
			{
				"blocks": [
					"minecraft:ice",
					"minecraft:packed_ice"
				],
				"chance": 10000,
				"sounds": [
					{
						"sound": "dsurround:ice",
						"volume": 0.3,
						"pitch": 1.0,
						"variable": false
					}
				]
			},
			{
				"blocks": [
					"minecraft:red_flower",
					"minecraft:yellow_flower",
					"minecraft:double_plant"
				],
				"effects": [
					{
						"effect": "firefly",
						"conditions": "weather.isNotRaining && (diurnal.isNight || diurnal.isSunset) && !(biome.isHot || biome.isCold || biome.isSnowy)",
						"chance": 50
					}
				]
			}
		]
	}

Not all parameter values have to be specified.  If they are not present in a config entry Dynamic
Surroundings will continue to use the existing setting.  Do note, though, the higher level ``entries``
parameter.  This **must** be present for Dynamic Surroundings to process the file.  As an example
this is the minimal do-nothing configuration Json configuration::

	{
		"blocks":[
		]
	}

It is possible to have multiple entries in the config file that match the same block.  The entries
in the file are processed in order, so entries further down in the list can override the effects
of entries higher in the list.

Sounds
^^^^^^
The sound configuration for blocks is similar to that of biomes.  There are some parameters that
will be different, such as "variable".

..	list-table:: Sound Configuration
   	:header-rows: 1
   	:widths: 20 30 60
   	:stub-columns: 1

	*	- Parameter
		- Value Type
		- Comment
	*	- sound
		- String
		- The name of the sound resource to play.
	*	- conditions
		- String
		- See `Tutorial: Condition Strings <condition_strings.html>`_
	*	- volume
		- Float
		- The volume level at which to play the sound.
	*	- pitch
		- Float
		- The pitch to use when playing the sound.
	*	- variable
		- Boolean
		- The pitch will vary slightly for each play.
	*	- weight
		- Integer
		- Selection weight of the spot sound if more than one can be selected.

Sound
^^^^^
This value determines what sound to play.  It is in a ResourceString format.  As an example,
"dsurround:crickets" tells Minecraft to play the sound "crickets" from the mod "dsurround".
This can be any valid sound reference, whether it is from Minecraft, Dynamic Surroundings, or
another mod.  For example, if you want to play the Minecart movement sound you could use
"minecraft:minecart.base", or want to use the Minecraft flame sound "minecraft:fire.fire".

Conditions
^^^^^^^^^^
See `Tutorial: Condition Strings <condition_strings.html>`_.

Volume
^^^^^^
Normally a sound will be played at a volume of 1.0F as a default.  Sometimes the supplied sound is
too loud so specifying a lower volume would be appropriate.  You will have to experiment to find
the right value for the sound you are playing.

Pitch
^^^^^
Pitch will raise or lower the pitch of the sound.  Typically lowering the pitch makes the sound
"deeper", and raising will make it more "shallow".  For example, Dynamic Surroundings uses the
regular beach wave noise for Deep Ocean by lowering the pitch to make it deeper to match the deep
water.

Variable
^^^^^^^^
Sometimes you have a single source sound, but want to vary the pitch when played.  An example of
this is the frog croak of the water lily.  The croak is a single sound within the mod, but by
varying the pitch it can give the impression of a small frog (higher pitch), or a bigger frog
(lower pitch).

Weight
^^^^^^
Specifies the relative weight of a particular sound when a random selection can be made.  The higher
the weight the higher the likelyhood of selection.  Selection behavior of a sound is similar to
the weighted selections from Minecraft's loot tables.  If a weight is not specified a value of 10
is assumed.

Effects
^^^^^^^

..	list-table:: Effect Configuration
   	:header-rows: 1
   	:widths: 20 30 60
   	:stub-columns: 1

	*	- Parameter
		- Value Type
		- Comment
	*	- effect
		- String
		- The name of the effect to spawn.
	*	- chance
		- Integer
		- 1-in-N chance that the effect will spawn.

The possible effects are:

..	list-table:: Effect Types
   	:header-rows: 1
   	:widths: 20 60
   	:stub-columns: 1

	*	- Effect
		- Comment
	*	- steam
		- Will display steam jet if lava blocks are near by.  Duration is based on the number of lava blocks nearby.
	*	- fire
		- Will display a fire jet.  Size and duration is based on the count of similar blocks underneath.
	*	- bubble
		- Will display bubbles rising upwards.  Duration is based on the count of similar blocks above.
	*	- dust
		- Will drop dust motes under the block.  Texture of the particles will be that of the block.
	*	- fountain
		- Will display a fountain of particles shooting up from the block.  Texture of the particles will be that of the block.
	*	- firefly
		- Will display firefly motes that travel in various directions.  Starting point is the center of the block.  Typically applied to plants.

Using this system it is possible to do things like have a fire jet spawn on top of a dirt block.
The size and duration of the jet would be based on the count of dirt blocks underneath the source
block.  Steam, however, has the requirement of needing lava blocks nearby.