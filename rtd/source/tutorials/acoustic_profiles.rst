Tutorial: Acoustic Profiles
===========================
An acoustic profile is a collection of footstep sound effect data that can be applied when an entity
foot strike hits a block.  These are built-in and can be tagged to any block. The table below lists
the block classes available and as well as a Vanilla block that can be used to check out the sound
in the game.

You will noticed that some of the entries have comma separated values. These are composites, meaning 
that multiple acoustic profiles will be combined to give a synthetic effect.  For example, Anvils
have a class of "metalcompressed,hardmetal" which causes Dynamic Surroundings to combine the acoustic
profiles of "metalcompressed" and "hardmetal".

..	list-table:: Builtin Acoustic Profiles
   	:header-rows: 1
   	:widths: 20 30 60
   	:stub-columns: 1

   	*	- Acoustic
		- Example Block
		- Comment
   	*  	- NOT_EMITTER
   		- Flowers
   		- Block does not emit a sound
   	*	- MESSY_GROUND
   		- Grass
   		-
   	*	- _SWIM
   		-
   		- Player swimming in water
   	*	- straw
   		- Fully grown wheat
   		-
   	*	- brush_straw_transition
   		- Rose Bush bottom block
   		-
   	*	- brush
   		- Tall grass
   		-
   	*	- fire
   		- Fire
   		-
   	*	- rails
   		- Tracks
   		-
   	*	- grass
   		- Grass
   		-
   	*	- organic_solid
   		- Pumpkin
   		-
   	*	- organic_dry
   		- Cocoa
   		-
   	*	- dirt
   		- Dirt
   		-
   	*	- stone
   		- Cobblestone
   		-
   	*	- wood
   		- Wooden Planks
   		- Also a normal extended piston head
   	*	- log
   		- Wooden Logs
   		-
   	*	- bedrock
   		- Bedrock
   		-
   	*	- sand
   		- Sand
   		-
   	*	- gravel
   		- Gravel
   		-
   	*	- ore
   		- Iron Ore
   		-
   	*	- leaves
   		- Leaves
   		-
   	*	- glass
   		- Glass
   		-
   	*	- mud
   		- Sponge
   		-
   	*	- glowstone
   		- Glowstone
   		-
   	*	- stonemachine
   		- Dispenser
   		-
   	*	- composite
   		- Diamond Block
   		-
   	*	- sandstone
   		- Sandstone
   		-
   	*	- rug
   		- Carpet/Wool
   		-
   	*	- wood_sticky
   		- Sticky Piston Head
   		- When head is extended
   	*	- hardmetal
   		- Iron Block
   		-
   	*	- brickstone
   		- Stone Bricks
   		-
   	*	- marble
   		- Quartz Block
   		-
   	*	- equipment
   		- TnT
   		-
   	*	- obsidian
   		- Obsidian
   		-
   	*	- metalbar
   		- Iron Bars
   		-
   	*	- squeakywood
   		- Wooden Chest
   		-
   	*	- bluntwood
   		- Wooden Door
   		-
   	*	- ladder
   		- Ladder
   		-
   	*	- ladder_default
		-
		-
	*	- snow
		- Snow
		-
	*	- organic
		- Nether Wart Block
		-
	*	- quicksand
		- Soul Sand
		-
	*	- waterfine
		- Lily Pad
		-
	*	- stoneutility
		- Enchanting Table
		-
	*	- metalcompressed
		- Anvil
		-
	*	- metalsubparts
		- Iron Door
		-
	*	- metalcompressed,hardmetal
		- Anvil
		- Composite sound
	*	- stoneutility,glass
		- Daylight Detector
		- Composite sound
	*	- stone,snow
		- Packed Ice
		- Composite sound
	*	- rug,straw,squeakywood
		- Standing Banner
		- Composite sound
	*	- rug,straw
		- Wall Banner
		- Composite sound
