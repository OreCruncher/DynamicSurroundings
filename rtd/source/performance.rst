Performance
===========
I have spent quite a bit of time tuning and tweaking Dynamic Surroundings to have as small an impact
on client side processing as possible.  Because of the nature of what the mod does (particles, sound,
etc.) players automatically assume it is some sort of performance hog.  Though I cannot say that
the mod will be a speed demon in all situations I think it will be performant for the majority of
players that use something other than a potato for a computer.

Without a doubt the most intensive processing occurs when rendering the aurora shader.  It is a complex
graphical beast and does require a system that has some CPU and GPU power to it.  If for some reason
your system does not perform well rendering the shader you can configure Dynamic Surroundings to use
the classic aurora render mechanism, or you can turn the feature entirely off.

The second most impactful feature is the waterfall splash effect.  And it is more than likely due to
sound lag.  For very large falls many point sources of sound are created to give the sensory feel of
being near a waterfall.  Large quantities of sounds down in the sound engine could cause the engine to
stutter a bit, and thus cause Minecraft to stutter during sound tick processing.  If you encounter
large falls and it causes problems you can disable the watersplash effect.

Dynamic Surroundings does take timings the various processes that it performs during a client tick.
You can see the information that it collects by turning on Dynamic Surrounding debug logging, and
hitting F3 to see the debug display.  Here is picture of a what I see on my system running a debug
build after about 4 minutes since client start:

.. image:: images/debug.png
   :align: center
   
At the time of the screen capture a multiband aurora was being rendered.  On average it takes about
0.27ms between client ticks to render the aurora.  Note that I have an older generation i7 and a
GTX 980 so YMMV.

Also worthy of note is that it takes about 0.58ms of a client tick to do the processing Dynamic
Surroundings needed to do, with 0.23ms of that time related to gathering the diagnostic information
to render to the screen.  Effectively, when the debug dialog is dismissed the average time for
processing would be about 0.35ms of the client tick.  For reference, a client tick is about 50ms.

For clarity here are some additional points to consider:

- I was standing in one place.  Some of the processing has stabilized (most notably the cuboid scanner responsible for effects such as waterfalls).  If I were to move the amount of time processing would go up because the new blocks that enter within the special effect range would need to be scanned.
- The JVM was not entirely warmed up.  The JIT process hasn't really taken full effect.  There is a point at which JIT kicks in and you can see the measurements drop.
- The time measurement calculation is an exponential moving average with a period of 100 ticks (5 seconds).  This smooths out the highs/lows and gives a better impression as to tick impact.
- This is v3.5 of Dynamic Surroundings.  It has additional performance improvements to reduce lookups and improve caching over and beyond what previous versions had.

If you suspect that Dynamic Surroundings is having a performance issue enable debug tracing
and hit F3.  I would like to see the timing results and any information you have related to the issue
you are experiencing.

-----

If you are stuck with a potato, or even a squash, for a computer there are several things you can
tweak to try and improve the situation.

**Disable the shader version of Auroras**

This really only applies if you hang around areas where they render.  As mentioned above they can be
rather intensive in terms of processing *when rendering*.

**Disable water suspend particle effect**

Suspend particles are those little black square like particles that generate in the water.  If you
spend time out by large bodies of water this can reduce your particle load quite a bit.

**Reduce the Special Effect rendering range**

By default the special effects rendering of Dynamic Surroundings will be out to 24 blocks (about
45x45x45 blocks centered on the player).  Lowering this value will reduce the amount of blocks
that Dynamic Surroundings will have to scan as well as reduce the number of entity effects that
have to be handled.  Conversely, increasing this value will increase workload so be careful.

**Change Minecraft's Particle Setting**

Changing the particle setting to decreased or minimum will reduce the number of particles being
rendered.  This change could have other side effects, such as a change in the sound sense with
falling rain.  (Minecraft sound generation for rain is tied to particle generation - go figure.)

**Increase the Waterfall Cutoff**

If you stay around large waterfalls you could experience sound lag.  Increasing the cutoff value
will reduce the overall amount of sounds that are played by the waterfall effects.  By default
the value is 0 meaning that all sound points will play.  Increasing to 1 or 2 can significantly
cut back on the number of sound points.
