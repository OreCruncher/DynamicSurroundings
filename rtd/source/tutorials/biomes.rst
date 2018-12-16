..	role:: sectiontitle

Tutorial: Biomes
================
This tutorial assumes that you have already created a Json configuration file.  If you haven't
please read :ref:`tutorial-config-files`.

Internally Dynamic Surroundings has a Biome Registry where it finds information related to
background fog and density, fog color, whether the biome can spawn auroras, etc.  A modpack author
can override what Dynamic Surroundings uses in order to provide specific biome effects.

..	note::
	The ``/ds`` command has an option ``reload``.  This allows you to reload biome config
	files without having to exit Minecraft.  However, if you create a new one and add it to the
	``dsurround.cfg`` file you will still need to restart Minecraft.

The table below describes the various parameters that can be defined:

..	list-table:: Biome Configuration
   	:widths: auto
   	:align: center
   	:header-rows: 1

   	*	- Parameter
		- Value Type
		- Comment
	*	- conditions
		- String
		- The biome for which these options apply.  See :ref:`tutorial-condition-strings` for more information.
	*	- _comment
		- String
		- Just a string of text used for making a comment in Json.  Does not affect processing of the configuration file.
	*	- precipitation
		- Boolean
		- Supports rain/snow
	*	- dust
		- Boolean
		- Has dust particles when raining (deserts)
	*	- dustColor
		- String
		- RGB triple that describes dust fog color
	*	- aurora
		- Boolean
		- Can spawn auroras
	*	- fog
		- Boolean
		- Has background fog effect
	*	- fogColor
		- String
		- RGB triple that describes background fog color
	*	- fogDensity
		- Float
		- Number indicating fog density
	*	- sounds
		- List
		- List of sounds configured for the biome.  See below.
	*	- soundReset
		- Boolean
		- Tells Dynamic Surroundings to reset any existing sound list for the biome.
	*	- spotSoundChance
		- Integer
		- 1-in-N chance per tick of triggering spot sound.

In order to define biome options the first thing that needs to be done is to create a Json file in
the ``./minecraft/config/dsurround/`` configuration directory.  It is a normal text file but has a
specific Json syntax that must be followed.  Here is an example of such a file::

	{
		"biomes":[
			{
				"conditions": "TRUE",
				"dust": true,
				"dustColor": "204,185,102",
				"fog": true,
				"fogDensity": 0.04,
				"fogColor": "64,128,64"
			}
		]
	}

In the example above ``conditions`` is "TRUE".  This condition string will always evaluate true
for any biome.  The result of this configuration is that every biome registered with Minecraft
will have a dust effect when it is raining, and will have a background fog that is tinted a
greenish color.  If you wanted to match a specific biome, say "Swampland", the condition string
would be "biome.name == 'Swampland'".  Internally Dynamic Surroundings uses a Json config to set
the initial state of the biomes in the Biome Registry.  The file can be found here_ if you are
interested.

Not all parameter values have to be specified.  If they are not present in a config entry
Dynamic Surroundings will continue to use the existing setting.  Do note, though, the higher level
``entries`` parameter.  This *must* be present for Dynamic Surroundings to process the file.
As an example this is the minimal do-nothing configuration Json configuration::

	{
		"biomes":[
		
		]
	}

The color values are RGB triples.  A good reference chart can be found at RapidTables_ to help you
decide on colors.

The selection of a fog density can be a bit tricky.  There is no hard fast calculation that can be
performed to determine the best value to use in a given situation.  Selection of the value is
trial and error.  By default Dynamic Surroundings uses a fog density of ``0.04``.  Raising this
value increases density, were as reducing the value reduces density.  I suggest moving the density
by small amounts, like ``+/- 0.005`` until you get the desired effect.

It is possible to have multiple entries in the config file that match the same biome.  The entries
in the file are processed in order, so entries further down in the list can override the effects of
entries higher in the list.  This makes it possible to do things like "All biomes that look like
this have these parameters, except for this one because it is special".  It is possible to
accomplish the same effect with regex, but sometimes it is easier to spell it out.

:sectiontitle:`Examples`

Set a general background haze for all biomes.  Good for the wasteland type feel::

	{
		"entries":[
			{
				"conditions": "TRUE",
				"fog":true,
				"fogDensity":0.04,
				"fogColor":"64,64,64"
			}
		]
	}

Set a biome fog for magical biomes that is blue in color but less dense than swamps::

	{
		"biomes":[
			{
				"conditions":"matches('(?i).*magic.*', biome.name)",
				"fog":true,
				"fogDensity":0.02,
				"fogColor":"0,191,255"
			}
		]
	}

The example above matches a biome that has "magic" in it's name.  You could use Forge's biome
properties to match biomes that are marked as magic by doing the following::

	{
		"biomes":[
			{
				"conditions":"biome.isMAGICAL",
				"fog":true,
				"fogDensity":0.02,
				"fogColor":"0,191,255"
			}
		]
	}

Make it so auroras can trigger when a player is standing in a Plains biome::

	{
		"biomes":[
			{
				"conditions":"biome.name == 'Plains'",
				"aurora":true
			}
		]
	}

Precipitation sucks.  Turn off rain/snow textures and water splashes.  Good for wasteland maps.
This does not turn off the rain function in Minecraft - just the client side rendering of such
effects::

	{
		"biomes":[
			{
				"conditions": "TRUE",
				"precipitation":false
			}
		]
	}

:sectiontitle:`Biome Sounds`

A background sound can be played while a player is standing in a biome that is configured for sound.
A biome can be configured with several sound entries.  Dynamic Surroundings will make a sound
selection for a given biome based on current environmental conditions.  A sound will continue to
play until the player changes biomes or the conditions for the sound no longer apply.  It is
possible to have 1 or more selections made (example: playing wind in a forest if it is mountainous).

..	list-table:: Sound Configuration
   	:widths: auto
   	:align: center
   	:header-rows: 1
   	
   	*	- Parameter
   		- Value Type
   		- Comment
   	*	- sound
   		- String
   		- The name of the sound resource to play.
   	*	- conditions
   		- String
   		- A :ref:`condition string <tutorial-condition-strings>`  that match the condition for playing.
   	*	- volume
   		- Float
   		- The volume level at which to play the sound.
   	*	- pitch
   		- Float
   		- The pitch to use when playing the sound.
   	*	- soundType
   		- String
   		- Indicates the type of sound. Possible values are "background", "spot", and "periodic".  Defaults to "background" if not specified.
   	*	- repeatDelay
   		- Integer
   		- Number of ticks to delay when submitting sound when looping.
   	*	- repeatDelayRandom
   		- Integer
   		- Optional number of ticks to randomly delay; added to repeatDelay to get an effective delay amount.
   	*	- weight
   		- Integer
   		- Selection weight of the spot sound if more than one can be selected.

Before we get into the nitty gritty details here are some examples from the internal Dynamic
Surroundings configuration Json::

	{
		"conditions":"matches('(?i)(.*swamp.*)', biome.name)",
		"fog":true,
		"fogColor":"64,128,64",
		"fogDensity":0.04
	},
	{
		"biomeName":"matches('(?i)(?!.*dead.*)(.*swamp.*)', biome.name)",
		"sounds":[
			{
				"sound":"dsurround:crickets",
				"volume":0.1
			}
		]
	},

These two entries configure biomes that contain the character sequence ``swamp``.  The first entry
configures all biomes that contain the sequence ``swamp`` to have fog of a greenish tint and
density of ``0.04``.  The second entry defines a single sound that is to be played in all ``swamp``
biomes that do not have the character sequence of ``dead`` in the name (i.e. doesn't apply to a
Dead Swamp).

Here is another example for forest like biomes::

	{
		"conditions": "matches('(?i)(?!.*dead.*|.*fungi.*|.*frost.*|.*snow.*|.*kelp.*|.*wasteland.*)(.*forest.*|.*cherry.*|.*orchard.*|.*wood.*|.*wetland.*|.*grove.*|.*springs.*)', biome.name)",
		"spotSoundChance": 200,
		"sounds": [
			{
				"sound": "dsurround:forest",
				"conditions": "weather.isNotRaining && diurnal.isDay"
			},
			{
				"sound": "dsurround:bird",
				"conditions": "weather.isNotRaining && diurnal.isDay",
				"soundType": "spot"
			},
			{
				"sound": "dsurround:woodpecker",
				"conditions": "weather.isNotRaining && diurnal.isDay",
				"soundType": "spot"
			},
			{
				"sound": "dsurround:crickets",
				"conditions": "weather.isNotRaining && diurnal.isNight"
			},
			{
				"sound": "dsurround:owl",
				"conditions": "weather.isNotRaining && diurnal.isNight",
				"soundType": "spot"
			}
		]
	},

This rule matches all biomes that have ``forest``, ``cherry``, and ``orchard`` in their name
excepting those that have ``dead``, ``flower``, ``fungi``, or ``frost``.  Two sounds are configured,
one that plays ``dsurround:forest`` sound during the day if it is not raining, and the other is for
``dsurround:crickets`` if it is at night and not raining.  The sound entries in this list are
processed in order, so the first sound to match the specific conditions will be selected.

:sectiontitle:`Sound`

This value determines what sound to play.  It is in a ResourceString format.  As an example,
"dsurround:crickets" tells Minecraft to play the sound "crickets" from the mod "dsurround".  This
can be any valid sound reference, whether it is from Minecraft, Dynamic Surroundings, or another mod.  For example, if you want to play the Minecart movement sound you could use "minecraft:minecart.base", or want to use the Minecraft flame sound "minecraft:fire.fire".

:sectiontitle:`Conditions`

See :ref:`tutorial-condition-strings`.

:sectiontitle:`Volume`

Normally a sound will be played at a volume of 1.0F as a default.  Sometimes the supplied sound is
too loud so specifying a lower volume would be appropriate.  You will have to experiment to find
the right value for the sound you are playing.

:sectiontitle:`Pitch`

Pitch will raise or lower the pitch of the sound.  Typically lowering the pitch makes the sound
"deeper", and raising will make it more "shallow".  For example, Dynamic Surroundings uses the
regular beach wave noise for Deep Ocean by lowering the pitch to make it deeper to match the deep
water.

:sectiontitle:`SoundReset`

Sometimes a modpack author wants to reset the sound configuration for a biome before setting up
new ones.  To do this specified ``soundReset`` in the biome record before defining new sounds.
For example::

	{
		"biomes":[
			{
				"conditions": "TRUE",
				"soundReset": true,
				"sounds":[
					{
					    "sound": "dsurround:wind",
					    "volume": 0.3
					}
				]
			}
		]
	}

This entry will cause currently configured sound information to be removed from all biomes.  After
that, a new sound will be defined for each, in this case a "dsurround:wind" sound that will play at
a low volume regardless of the current conditions.

:sectiontitle:`SoundType`

Indicates the type of sound this entry represents.  The following are the possible sound types:

..	list-table:: Sound Types
   	:widths: auto
   	:align: center
   	:header-rows: 1
   	
   	*	- Sound Type
   		- Comment
   	*	- background
   		- Sound will play in a continuous loop until conditions change.
   	*	- periodic
   		- Sound will queue based on the repeatDelay and repeatDelayRandom settings.
   	*	- spot
   		- Sound is a spot sound and will play based on appropriate conditions and randmoness.

:sectiontitle:`RepeatDelay`

The number of ticks to delay between sound plays.  Sometimes there needs to be spacing when playing
a sound, such as the stomach rumble when a player is hungry.

:sectiontitle:`RepeatDelayRandom`

An additional random number of ticks that will be added to repeatDelay when calculating the number
of ticks to delay for the next play interval.  For example, if repeatDelay is 300, and
repeatDelayRandom is 1000, the effective delay amount will be 300-1299 ticks.

:sectiontitle:`Weight`

Specifies the relative weight of a particular sound when a random selection can be made.
The higher the weight the higher the likelyhood of selection.  Selection behavior of a sound is
similar to the weighted selections from Minecraft's loot tables.  If a weight is not
specified a value of 10 is assumed.

:sectiontitle:`Spot Sounds`

A spot sound is a non-repeating sound that has a random chance of playing while a player is
present in a biome.  While a biome sound can be thought of as background audible ambiance/theme
for a biome, a spot sound is more like punctuation.  For example a Jungle could have a biome sound
that gives the sense of leaves moving in the breeze and the scurrying/noise of small creatures
within the leaf canopy.  To accent this experience jaguar growls could be introduced as spot sound.
The growl would randomly play while the player is present in a Jungle, but it is not part of the
sound track.  Another example is an owl hooting in a Forest at night while the crickets chirp.

:sectiontitle:`Fake Biomes`

A fake biome is similar to a fake player in that they really don't exist, but serve as a proxy for
getting things done.  In the case of fake biomes Dynamic Surroundings will use them to better refine
the players locale for the purposes of configuring environmental effects.  Fake biomes do not show
up in the regular Minecraft/Forge biome listings - they are strictly internal to Dynamic
Surroundings.

..	list-table:: Fake Biomes
   	:widths: auto
   	:align: center
   	:header-rows: 1

	*	- Name
		- Comment
	*	- Underground
		- Biome for when a player's Y value is several blocks below the defined sea level for the dimension.
	*	- UnderOCN
		- Underwater in an Ocean biome. ("(?i)(?!.*deep.*)(.*ocean.*|.*kelp.*|.*coral.*)")
	*	- UnderDOCN
		- Underwater in a Deep Ocean biome. ("(?i).*deep.*ocean.*|.*abyss.*")
	*	- UnderRVR
		- Underwater in a River biome. ("(?i).*river.*")
	*	- Underwater
		- Underwater and the player is not in an Ocean, Deep Ocean, or River biome.
	*	- Player
		- Special biome that is used to configure sounds for the player themselves.
	*	- Clouds
		- Biome for when the player's Y exceeds the cloud height setting for the dimension.
	*	- OuterSpace
		- Biome for when the player's Y exceeds the space height setting for the dimension.
	*	- Village
		- Biome for when the player is within a village radius.
	*	- BattleMusic
		- Special biome for attaching battle music background tracks.

..	_here: https://github.com/OreCruncher/DynamicSurroundings/blob/master/src/main/resources/assets/dsurround/dsurround/data/mcp.json
..	_RapidTables: http://www.rapidtables.com/web/color/RGB_Color.htm
