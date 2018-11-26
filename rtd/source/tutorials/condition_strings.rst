Condition Strings
=================
When creating a configuration file some of the elements have a condition string option.  The
condition string tells Dynamic Surroundings the circumstances under which a sound or effect can
occur.  For example, attached the the PLAYER biome is the heartbeat sound that will play when
the player is considered "hurt" and will not play at any other time.

There are no boolean types in the language.  TRUE and FALSE are based the classic C approach:
0 means FALSE, and TRUE is not FALSE.  An expression like "10 \* FALSE" will evaluate to "0".

The expression must generate a TRUE or FALSE result.  Any other result will cause an error.

The expression evaluation engine understands the basic set of operators found in most languages. A
summary of the operators can be found in the following table.

..	list-table:: Built-in Operators
   	:header-rows: 1
   	:widths: 20 30 60
   	:stub-columns: 1

   	*	- Operator
		- Name
		- Example
	*	- \+
		- Plus
		- 5 + 6
	*	- \-
		- Minus
		- 6 - 5
	*	- \*
		- Multiply
		- 4 * 5
	*	- /
		- Divide
		- 9 / 4
	*	- %
		- Modulus
		- 8 % 4
	*	- &&
		- Logical And
		- weather.isNotRaining && diurnal.isDay
	*	- ||
		- Logical Or
		- player.temperature == 'icy' || player.temperature == 'cold'
	*	- >
		- Greather Than
		- 9 > 8
	*	- >=
		- Greater Than Equal
		- 10 >= 11
	*	- <
		- Less Than
		- 8 < 9
	*	- <=
		- Less Than Equal
		- 6 <= 6
	*	- =
		- Equal
		- 8 = 9
	*	- ==
		- Equivalent
		- 'this' == 'that'
	*	- !=
		- Not Equal
		- 9 != 10
	*	- <>
		- Not Equal
		- 9 <> 10
	*	- !
		- Not
		- Inverts a logical value (See NOT function below)

The expression engine has a set of built-in functions that can be used to simplify expressions.

..	list-table:: Built-in Functions
   	:header-rows: 1
   	:widths: 40 80
   	:stub-columns: 1

   	*	- Function
		- Description
	*	- MATCH(regex, input)
		- Performs a regular expression match
	*	- NOT(expression)
		- Performs a logical not on an expression.  A value of 0 becomes 1, and a value of non-zero becomes 0.
	*	- IF(condition, trueExp, falseExp)
		- Performs one of two evaluations based on a condition
	*	- MAX(exp1,exp2,...)
		- Determines the max value from a selection of expressions
	*	- MIN(exp1,exp2,...)
		- Determines the min value from a selection of expressions
	*	- ABS(exp)
		- Determines the absolute value of an expression
	*	- ROUND(exp)
		- Rounds a number to the closest integer value
	*	- FLOOR(exp)
		- Returns the largest integer less than or equal to exp.
	*	- CEILING(exp)
		- Returns the smallest integer greater than or equal to exp.
	*	- SQRT(exp)
		- Calculates the square root of an expression
	*	- CLAMP(exp,min,max)
		- Ensures that an expression is within the specified bounds
	*	- ONEOF(exp,v1,...)
		- Determines if the result of the expression matches any of the specified values

A few constant variables are provided if they are needed.

..	list-table:: Constant Variables
   	:header-rows: 1
   	:widths: 20 20 60
   	:stub-columns: 1
   	
   	*	- Variable
   		- Type
   		- Description
	*	- TRUE
		- boolean
		- Indicates true.  Has a value of 1.
	*	- FALSE
		- boolean
		- Indicates false.  Has a value of 0.

Dimension variables describe the dimension that the player is in.

..	list-table:: Dimension Variables
   	:header-rows: 1
   	:widths: 20 20 60
   	:stub-columns: 1

   	*	- Variable
   		- Type
   		- Description
	*	- dim.id
		- integer
		- ID of the player's dimension (example: 0 is Overworld)
	*	- dim.name
		- string
		- Name of the player's dimension (example: "Overworld")
	*	- dim.hasSky
		- boolean
		- Indicates whether the player's dimension has a sky.

Diurnal variables describe the characteristics of the day/night cycle.

..	list-table:: Diurnal Variables
   	:header-rows: 1
   	:widths: 20 20 60
   	:stub-columns: 1

   	*	- Variable
   		- Type
   		- Description
	*	- diurnal.isDay
		- boolean
		- Indicates if it is currently daytime.
	*	- diurnal.isNight
		- booean
		- Indicates if it is currently nighttime.
	*	- diurnal.isSunrise
		- boolean
		- Indicates if the time of day is sunrise.
	*	- diurnal.isSunset
		- boolean
		- Indicates if the time of day is sunset.
	*	- diurnal.isAuroraVisible
		- boolean
		- Indicates if an aurora is currently visible.
	*	- diurnal.moonPhaseFactor
		- float
		- Indicates the current phase of the moon.

Weather variables describe the current state of weather in the player's dimension.

..	list-table:: Weather Variables
   	:header-rows: 1
   	:widths: 20 20 60
   	:stub-columns: 1

   	*	- Variable
   		- Type
   		- Description
	*	- weather.isRaining
		- boolean
		- Whether it is currently raining or not.
	*	- weather.isNotRaining
		- boolean
		- Inverse of weather.isRaining.
	*	- weather.isThundering
		- boolean
		- Whether the current storm is a thunder storm.
	*	- weather.isNotThundering
		- boolean
		- Inverse of weather.isThundering.
	*	- weather.rainfall
		- float
		- Current intensity of rainfall, 0.0 - 1.0.
	*	- weather.temperatureValue
		- float
		- Temperature of the biome at the player's current location.
	*	- weather.temperature
		- string
		- String indicating the general temperature of the player location ('icy', 'cool', 'mild', 'warm, 'hot').

If Serene Seasons is installed, the season variables will provide information about the current
season.  If Serene Seasons is not installed "noseason" will be reported.

..	list-table:: Season Variables
   	:header-rows: 1
   	:widths: 20 20 60
   	:stub-columns: 1

   	*	- Variable
   		- Type
   		- Description
	*	- season.season
		- string
		- Text description of the current season.
	*	- season.type
		- string
		- The current general season ('noseason', 'spring', 'summer', 'autumn', 'winter').
	*	- season.subType
		- string
		- The current subseason ('nosubtype', 'early', 'mid', 'late').

Player variables describe some of the attributes of the player at the given point in time.

..	list-table:: Player Variables
   	:header-rows: 1
   	:widths: 20 20 60
   	:stub-columns: 1

   	*	- Variable
   		- Type
   		- Description
	*	- player.isDead
		- boolean
		- Indicates if the player is dead.
	*	- player.isHurt
		- boolean
		- Indicates if the player has reached the configured hurt threshold.
	*	- player.isHungry
		- boolean
		- Indicates fi the player has reached the configured hunger threshold.
	*	- player.isBurning
		- boolean
		- Indicates if the player is on fire.
	*	- player.isSuffocating
		- boolean
		- Indicates if the player is suffocating (head in a dirt block type of thing).
	*	- player.isFlying
		- boolean
		- Indicates if the player is flying.
	*	- player.isSprinting
		- boolean
		- Indicates if the player is sprinting.
	*	- player.isInLava
		- boolean
		- Indicates if the player is in lava.
	*	- player.isInvisible
		- boolean
		- Indicates if the player is invisible.
	*	- player.isBlind
		- boolean
		- Indicates if the player is currently blind.
	*	- player.isInWater
		- boolean
		- Indicates if the player is in water.
	*	- player.isWet
		- boolean
		- Indicates if the player is wet.
	*	- player.isUnderwater
		- boolean
		- Indicates if the player is underwater.
	*	- player.isRiding
		- boolean
		- Indicates if the player is currently riding an entity.
	*	- player.inBoat
		- boolean
		- Indicates if the player is currently in a boat.
	*	- player.isOnGround
		- boolean
		- Indicates if the player is currently standing on the ground.
	*	- player.isMoving
		- boolean
		- Indicates if the player is in motion.
	*	- player.isInside
		- boolean
		- Indicates if the player is considered inside a structure.
	*	- player.isUnderground
		- boolean
		- Indicates if the player is considered underground.
	*	- player.isInSpace
		- boolean
		- Indicates if the player is considered to be in space.
	*	- player.isInClouds
		- boolean
		- Indicates if the player is considered to be in the clouds.
	*	- player.temperature
		- float
		- The players temperature.
	*	- player.X
		- float
		- The player's X coordinate.
	*	- player.Y
		- float
		- The player's Y coordinate.
	*	- player.Z
		- float
		- The player's Z coordinate.
	*	- player.health
		- integer
		- The player's current health.
	*	- player.maxHealth
		- integer
		- The player's maximum health.
	*	- player.luck
		- float
		- The player's current luck level.
	*	- player.canRainOn
		- boolean
		- Indicates if the player can be hit by falling rain.
	*	- player.canSeeSky
		- boolean
		- Indicates if the player is in a position to see the sky.
	*	- player.lightlevel
		- integer
		- Current light level at the player location.
	*	- player.inVillage
		- boolean
		- Indicates if the player is within a village radius.
	*	- player.food.saturation
		- integer
		- The player's food saturation level.
	*	- player.food.level
		- integer
		- The player's current food level.

Biome Type variables describe the player's biome in terms of traits.  These traits are defined by
Forge.

..	list-table:: Biome Type Variables
   	:header-rows: 1
   	:widths: 20 20 60
   	:stub-columns: 1

   	*	- Variable
   		- Type
   		- Description
	*	- biome.isBEACH
		- boolean
		- The current player biome has the BEACH trait
	*	- biome.isCOLD
		- boolean
		- The current player biome has the COLD trait
	*	- biome.isCONIFEROUS
		- boolean
		- The current player biome has the CONIFEROUS trait
	*	- biome.isDEAD
		- boolean
		- The current player biome has the DEAD trait
	*	- biome.isDENSE
		- boolean
		- The current player biome has the DENSE trait
	*	- biome.isDRY
		- boolean
		- The current player biome has the DRY trait
	*	- biome.isEND
		- boolean
		- The current player biome has the END trait
	*	- biome.isFOREST
		- boolean
		- The current player biome has the FOREST trait
	*	- biome.isHILLS
		- boolean
		- The current player biome has the HILLS trait
	*	- biome.isHOT
		- boolean
		- The current player biome has the HOT trait
	*	- biome.isJUNGLE
		- boolean
		- The current player biome has the JUNGLE trait
	*	- biome.isLUSH
		- boolean
		- The current player biome has the LUSH trait
	*	- biome.isMAGICAL
		- boolean
		- The current player biome has the MAGICAL trait
	*	- biome.isMESA
		- boolean
		- The current player biome has the MESA trait
	*	- biome.isMOUNTAIN
		- boolean
		- The current player biome has the MOUNTAIN trait
	*	- biome.isMUSHROOM
		- boolean
		- The current player biome has the MUSHROOM trait
	*	- biome.isNETHER
		- boolean
		- The current player biome has the NETHER trait
	*	- biome.isOCEAN
		- boolean
		- The current player biome has the OCEAN trait
	*	- biome.isPLAINS
		- boolean
		- The current player biome has the PLAINS trait
	*	- biome.isRARE
		- boolean
		- The current player biome has the RARE trait
	*	- biome.isRIVER
		- boolean
		- The current player biome has the RARE trait
	*	- biome.isSANDY
		- boolean
		- The current player biome has the SANDY trait
	*	- biome.isSAVANNA
		- boolean
		- The current player biome has the SAVANNA trait
	*	- biome.isSNOWY
		- boolean
		- The current player biome has the SNOWY trait
	*	- biome.isSPARSE
		- boolean
		- The current player biome has the SPARSE trait
	*	- biome.isSPOOKY
		- boolean
		- The current player biome has the SPOOKY trait
	*	- biome.isSWAMP
		- boolean
		- The current player biome has the SWAMP trait
	*	- biome.isVOID
		- boolean
		- The current player biome has the VOID trait
	*	- biome.isWASTELAND
		- boolean
		- The current player biome has the WASTELAND trait
	*	- biome.isWATER
		- boolean
		- The current player biome has the WATER trait
	*	- biome.isWET
		- boolean
		- The current player biome has the WET trait

Biome variables provide meta information about the player biome.

..	list-table:: Biome Variables
   	:header-rows: 1
   	:widths: 20 20 60
   	:stub-columns: 1

   	*	- Variable
   		- Type
   		- Description
	*	- biome.name
		- string
		- Name of the player biome (example: "Plains")
	*	- biome.id
		- string
		- Resource ID of the biome (example: "minecraft:plains")
	*	- biome.modid
		- string
		- The mod to which this biome belongs (example: "minecraft")
	*	- biome.rainfall
		- float
		- The rainfall rating of the player biome
	*	- biome.temperature
		- string
		- The temperature rating of the player biome ('icy', 'cold', 'mild', 'warm', 'hot')
	*	- biome.temperatureValue
		- float
		- The temperature value of the player biome

Battle variables provide information related to battles taking place around the player.  Battle music
must be enabled to have these variables populated.

..	list-table:: Battle Variables
   	:header-rows: 1
   	:widths: 20 20 60
   	:stub-columns: 1

   	*	- Variable
   		- Type
   		- Description
	*	- battle.inBattle
		- boolean
		- Indicates if there is a battle nearby the player
	*	- battle.isBoss
		- boolean
		- Indicates if the battle is a boss fight
	*	- battle.isWither
		- boolean
		- Indicates if the battle is with a Wither
	*	- battle.isDragon
		- boolean
		- Indicates if the battle is with an Ender Dragon

-------

Examples
^^^^^^^^
::

	player.health <= 8
	
This is essentially ``player.isHurt`` based on a default configuration.  This is fragile, howevever, because a modpack author cannot tune the threshold.

::

	biome.name == 'Plains'

Returns TRUE if the player's current biome name is 'Plains'.

::

	MATCH('(?i)(.*plains.*)', biome.name)

Uses a regular expression to evaluate the player's current biome name.  If the name contains 'plains' regardless of case it will return TRUE.

::

	IF(player.dimension == 0, player.isHurt, player.health <= 16)

If the player is currently in Overworld (dimension 0) it will return whether the player is hurt or not.  If it is not Overworld then it will return whether the player's current health is less than or equal to 16.

::

	ONEOF(biome.temperature, 'icy', 'cold', 'mild')

Returns TRUE if the ``biome.temperature`` is 'icy', 'cold', or 'mild'.  This is a more compact form of chaining a bunch of '==' expressions with '||'.

::

	ONEOF(player.dimension, 0, -1)

Returns TRUE if the player dimension is either Overworld or Nether.

------

**Notes**

- Strings are denoted in a script by using the apostrophe '.  This is to minimize errors introduced in the Json config because of escaping.  Example of the word 'Overworld' below:

::

	"conditions":"match('(?i)(.*taiga.*|.*snow.*forest.*)', biome.name)",
	"sounds":[
	{
		"sound":"dsurround:owl",
		"conditions":"player.dimensionName == 'Overworld' && weather.isNotRaining && diurnal.isNight",
		"soundType":"spot",
		"volume":0.3
	}
	
- In a lot of cases 'player.temperature' and 'biome.temperature' will be the same.  However, other mods can change dynamics of what the player experiences and what actually exists in the biome.  A good example is the mod Tough As Nails.  My recommendation is to use 'player.temperature' when dealing with PLAYER biome effects, and 'biome.temperature' for things attached to Biomes.