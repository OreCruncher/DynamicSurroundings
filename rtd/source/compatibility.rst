..	role:: question

Compatibility
=============
This page contains compatibility information with other popular environment changing mods.  If you
come across something in your travels that needs to be updated leave an entry on the Issues page.

If you run into a mod conflict the first thing to do is make sure that your mods are all
up-to-date.  Sometimes these conflicts have already been discovered and fixed.  If you still run into
issues notify the mod authors in question and provide as much detail as possible.

:question:`Weather 2`

When using defaults Weather 2 replaces Minecraft's rain/snow rendering routines.  This is a conflict
with Dynamic Surroundings because it does the same thing.  To have Dynamic Surroundings take
over rendering change Weather 2's advanced setting ``Particle_RainSnow`` to ``false``.

:question:`StellarSky`

Dynamic Surroundings uses the celestial angle of the Minecraft sun to determine day/night.  If
StellarSky is being used this means that the length of the day/night cycle can vary over time.

:question:`Shaders`

Shaders can sometimes interfere with Dynamic Surroundings weather and effect rendering.  In a lot of
cases you cannot see the renderings, or they don't get rendered at all.  It would depend on which
shader pack you use.

At the time of this writing Dynamic Surroundings works "out of the box" with OptiFine.  So if you
want the benefits of OptiFine without a shader pack you are good to go.  If you do use a shader pack
you will more than likely experience the following:

- **Can't see auroras**  The cloud renderings produced by the shader pack will wash out/replace the aurora rendering.  In some cases I could see the aurora but it would be feint and in gray scale.  You can turn off aurora processing.
- **Can't see damage pop offs too well**  Damage popoffs are particles that are text written to the display.  The shaders I have worked with will wash them out or turn them black.  You can see the same effect by using a Minecraft name tag on a mob - the name will not render correctly.  You can turn off the damage popoffs.
- **Can't see speech bubble text too well**  Same problem as damage pop-offs and name tag rendering in Minecraft.  You can turn off speech bubbles.
