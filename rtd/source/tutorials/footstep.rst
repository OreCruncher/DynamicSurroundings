Tutorial: Footsteps
===================
This tutorial assumes that you have already created a Json configuration file.  If you haven't please
read `Tutorial: External Configuration Files <config_files.html>`__.

If you are not familiar with Dynamic Surroundings block state specification methods please read
`Tutorial: BlockState Specification <blockstate.html>`_.  It will save you a lot of heartache.

The "footsteps" entry in the configuration file is used to specify what sounds to play when a player
is walking on or through a block::

	"footsteps": {
		"minecraft:barrier": "NOT_EMITTER",
		"minecraft:stone": "stone",
		"minecraft:grass": "grass",
		"minecraft:grass_path": "grass"
	}

The list is comprised of value pairs.  The value on the right is the `acoustic <acoustic_profile.html>`_
to apply to the block that is specified on the left side.  Above, ``minecraft:barrier`` is not an
emitter (doesn't produce sound) so it is set to ``NOT_EMITTER``.  The block ``minecraft:stone`` is
set to have the acoustic profile of ``stone``.

In the above example the block specification would apply to a block that is a singleton (i.e.
``minecraft:barrier`` has no variants), or if the block had variants it would apply to *all* variants
of that block.  If you wanted to specify an acoustic for a specific variant you need to have an
entry specifically for it.  For example::

	"minecraft:sponge[wet=false]": "organic_dry",
	"minecraft:sponge[wet=true]": "mud"

The sponge has two variants - one that is dry, and one that has absorbed water.  The dry version
will have the acoustic of ``organic_dry``, and the wet one will have the acoustic of ``mud``.

A more complicated example is that of ``minecraft:double_plant``::

	"minecraft:double_plant": "brush",
	"minecraft:double_plant+messy": "MESSY_GROUND",
	"minecraft:double_plant[variant=syringa]+foliage": "brush_straw_transition",
	"minecraft:double_plant[variant=double_grass]+foliage": "brush",
	"minecraft:double_plant[variant=double_fern]+foliage": "brush_straw_transition",
	"minecraft:double_plant[variant=double_rose]+foliage": "brush_straw_transition",
	"minecraft:double_plant[variant=paeonia]+foliage": "brush_straw_transition",
	"minecraft:double_plant[variant=sunflower]+foliage": "brush_straw_transition",

The first thing to note is that the block specifications have either a "+messy" or "+foliage" added
to the entry.  These are called substrates.

- **messy** This is the acoustic that is played when a player moves *through* a block like reeds.
- **foliage** This is the acoustic that is played when Dynamic Surroundings checks for foliage above the block the player is standing on.  Typically this acoustic is played in addition to the acoustics of the block beneath the player.

Other possible substrates are:

- **carpet** This acoustic is for when Dynamic Surroundings does a carpet check.  Obviously carpets fall into this category, but other things that use this acoustic style are pressure plates and snow layers.
- **bigger** This acoustic is special to things like fences.  The bounding box of a fence extends a half block above thus it is considered "bigger".  (For fences it is preferential to use the acoustic "#fence" because it simplifies configuration.)

It is worth pointing out that multiple acoustics can be combined for a block.  For example, this is
what the Minecraft standing banner looks like::

	"minecraft:standing_banner": "rug,straw,squeakywood",
	"minecraft:standing_banner+foliage": "rug,straw,squeakywood",

The standing banner, both from a block perspective and a foliage perspective, will play three
acoustics when triggered: rug, straw, and squeakywood.  Combining acoustics is a good way to get a
different sound.

Macros
^^^^^^
Dynamic Surroundings has a set of built in macros to simplify configuration.  In a lot of cases blocks
have similar acoustic profiles and it makes sense to refer to those common profiles in a simple way.
An example of this is a wooden fence.  Normally a wooden fence would be described thusly::

	"minecraft:acacia_fence": "NOT_EMITTER",
	"minecraft:acacia_fence+bigger": "bluntwood"
	
This pattern is pretty straight forward and can be tedious to replicate across configurations.
Dynamic Surroundings has a built in macro ``#fence`` that simplifies the configuration::

	"minecraft:acacia_fence": "#fence"
	
During startup Dynamic Surroundings will automagically expand the ``#fence`` entry into the two listed
prior.

The other benefit to using macros is that it standardizes sounds across all blocks of a certain class.
If Dynamic Surroundings were to redefine the acoustic profile of ``#fence`` all blocks that refer to
``#fence`` will be affected.

The following table lists the built in macros available.

..	list-table:: Block Macros
   	:header-rows: 1
   	:widths: 20 30 60
   	:stub-columns: 1

   	*	- Macro Name
   		- Example Block
   		- Description
  	*	- #sapling
		- Tree Sapling
		- Sound made when walked through
	*	- #reed
		- Sugar Cane/Reeds
		- Sound made when walked through
	*	- #plant
		- Tall Grass
		- Sound made when walked through
	*	- #bush
		- Double Tall plants
		- Sound made when walked through
	*	- #vine
		- Vine
		- Sound is made when climbed or walked through
	*	- #moss
		- Twilight Forest Moss
		- Sound made when walked on
	*	- #fence
		- Wooden Fence
		- Sound made when walked on
	*	- #wheat
		- Wheat growth profile
		- Strawlike sounds based on 7 growth level
	*	- #crop
		- Carrot growth profile
		- Plantlike sounds based on 7 growth level
	*	- #beets
		- Beet growth profile
		- Plantlike sounds based on 4 growth levels
