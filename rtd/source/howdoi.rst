..	role:: question

How Do I....
============
This page contains a list of frequently asked "How do I..." questions related to configuring
Dynamic Surroundings.  If you have something that is not covered on this list check out the
CurseForge_ page for the mod or shoot me a PM.

- :ref:`How do I turn off dust storm effects? <stormEffects>`
- :ref:`I have Sound Physics installed and the sound is too loud/low.  How do I fix it? <soundPhysics>`
- :ref:`How do I turn off the Nether weather effects? <weatherEffects>`
- :ref:`How do I enable Auroras for all biomes? <aurorasAllBiomes>`

..	_stormEffects:

:question:`How do I turn off dust storm effects?`

Some players using shaders do not like the combined effect of Dynamic Surroundings dust storms and
the shaders.  To turn off dust storms you will need to create an external configuration file that
contains the following::

	{
	    "biomes": [
	        {
	            "conditions": "TRUE",
	            "_comment": "DS v3.4.1.0+ Turns off dust effects for all biomes",
	            "dust": false
	        }
	    ]
	}

..	_soundPhysics:

:question:`I have Sound Physics installed and the sound is too loud/low.  How do I fix it?`

Sound Physics is written with Vanilla in mind.  In my experience it does not work well with
Dynamic Surroundings.  I personally use `Sound Filters <https://minecraft.curseforge.com/projects/sound-filters?gameCategorySlug=mc-mods&projectID=222789>`_
and haven't had any issues.

..	_weatherEffects:

:question:`How do I turn off the Nether weather effects?`

To turn off Nether weather effects you will need to create an external configuration file that
contains the following::

	{
	    "dimensions": [
	        {
	            "dimId": -1,
	            "weather": false,
	            "fog": false,
	            "haze": false
	        }
	    ]
	}

..	_aurorasAllBiomes:

:question:`How do I enable Auroras for all biomes?`

It is pretty easy to enable Auroras for all biomes.  To do this will require creating an external
configuration file that contains the following::

	{
	    "biomes": [
	        {
	            "conditions": "TRUE",
	            "_comment": "Always display aurora",
	            "aurora": true
	        }
	    ]
	}

..	_CurseForge: https://minecraft.curseforge.com/projects/dynamic-surroundings
