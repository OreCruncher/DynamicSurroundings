Tutorial: Dimensions
====================
This tutorial assumes that you have already created a Json configuration file.  If you haven't
please read :ref:`tutorial-config-files`.

Internally Dynamic Surroundings has a Dimension Registry where it keeps information related to how it
should operate for a dimension. A modpack author can override what Dynamic Surroundings uses in order
to provide specific dimension behaviors.

The table below describes the various parameters that can be defined:

..	list-table:: Dimension Configuration
   	:header-rows: 1
   	:widths: auto

   	*	- Parameter
		- Value Type
		- Comment
	*	- dimId
		- Integer
		- The ID of the dimension the parameters apply.
	*	- name
		- String
		- Name of the dimension.  If used dimId does not have to be specified.  Match is based on dimension name.
	*	- seaLevel
		- Integer
		- The Y block coordinate that defines sea level.  Only affects Dynamic Surroundings!
	*	- skyHeight
		- Integer
		- The Y block coordinate that defines larges Y for building.  Only affects Dynamic Surroundings!
	*	- cloudHeight
		- Integer
		- The Y block coordinate that is considered the cloud layer.
	*	- haze
		- Boolean
		- Whether to allow elevation haze effect in the dimension.
	*	- aurora
		- Boolean
		- Whether to allow auroras in the dimension.
	*	- weather
		- Boolean
		- Whether to allow weather effects in the dimension.
	*	- fog
		- Boolean
		- Whether the dimension has fog effects.

..	note::
	Either dimId or name must be specified so that Dynamic Surroundings can match up the configuration
	to the right dimension.  If neither are specified it will be ignored.

In order to define dimension options the first thing that needs to be done is to create a Json file
in the ``./minecraft/config/dsurround/`` configuration directory.  It is a normal text file but has
a specific Json syntax that must be followed.  Here is an example of such a file:

::

	{
		"dimensions":[
			{
				"dimId": -1,
				"weather": true,
				"haze": false,
				"aurora": false,
				"seaLevel": 0
			}
		]
	}

If you already have a configuration Json available to you the information can be added to that file
instead.  There is no specific reason to maintain it as a separate file.

In this example, the dimension that is being configured is -1 (The Nether).  Weather effects are
turned on, elevation haze is disabled, auroras are disabled, and seaLevel is defined to be 0 so
that the dust weather effect can render to make it foggy when it is raining.