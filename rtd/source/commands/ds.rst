..	role:: question

Command: ds
===========
Dynamic Surroundings has a single command, ``/ds``.  It is usable by an OP and can do several things:

:question:`/ds help`

Displays help about the command.

:question:`/ds reload`

Reloads the Dynamic Surroundings configuration files.  This is handy when working on your own
configuration files because you do not have to restart the client every time you make a tweak.

:question:`/ds config`

Displays the dimension configuration information for the dimension the player is currently in.

:question:`/ds reset`

Resets Minecraft's current rain and storm settings by turning them off.  The effect is as if all
the players slept and a new day comes.

:question:`/ds status <rain|thunder|aurora>`

Displays the current operational status of either rain, thunder storms, or auroras for the dimension.

:question:`/ds settime <rain|thunder> 0.0 - 1000.0`

Sets the trigger time for rain or thunder to the specified number of minutes.  If it is currently
not raining this will be the time it starts; if it is raining this will be the time it stops.
Ditto for thunder.

:question:`/ds setstr rain 0-100`

Sets the current rain strength to the specified value.

:question:`/ds setmin rain 0-100`

Sets the minimum rain strength for the dimension.

:question:`/ds setmax rain 0-100`

Sets the maximum rain strength for the dimension.

:question:`/ds setthreshold thunder 0-100`

Sets the rain intensity threshold at which a storm (thunder) can occur.

Note that rain is started and stopped by using Minecraft's ``/toggledownfall``.