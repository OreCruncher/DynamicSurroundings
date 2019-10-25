..	role:: question

Debugging
=========
Putting together a modpack and have it run efficiently can be a challenge.  Sometimes the problems are flat out conflicts that cannot be resolved,
other times it is due to a mod misbehaving.  It can be a real challenge trying to figure out what is going on.

The following is a list of things that I have encountered while developing Dynamic Surroundings.  I place them here in hopes that it will improve any
pack you are working with.

- :ref:`Non-client thread submitting sounds to the sound engine <serversidesound>`

..	_serversidesound:

:question:`Non-client thread submitting sounds to the sound engine`

There is a protocol where server side code can cause a sound to play on the client.  This involved sending a message from the server thread to the
client thread.  Sometimes a mod author bypasses this protocol out of ignorance, or there is a bug where it does not work the way they expect.  The reason
that this is of concern is that the client and server portions of code run in different threads.  It is very bad to have a the server thread interact
with data on the client side, and vice versa.  This will lead to the client and/or server crashing with concurrency related exceptions, or worse.

The following is such an example of what I am talking about::

	java.util.ConcurrentModificationException 
	
	    at com.google.common.collect.HashBiMap$Itr.hasNext(HashBiMap.java:401)
	    at net.minecraft.client.audio.SoundManager.stopAllSounds(SoundManager.java:205)
    
To help diagnose which mod is the offender you can run your Minecraft client with an additional command line parameter::

	-Ddsurround.devMode=true
	
This will cause Dynamic Surroundings to do additional checks during sound handling events to ensure that it is the client thread that is executing.
If it detects any other thread, whether it is the server thread or network handling thread, it will throw an exception generating a call stack.  This
call stack will contain the source of the sound event and will hopefully lead to an issue tracker where a bug can be filed.

..	note::

	It is also possible to see these types of concurrency exceptions in Minecraft's ParticleManager.  Similar to sounds, this is mostly due to the
	server thread trying to generate a particle effect client side.  I have only seen this happen once.  I mention it in the off chance that you may be
	encountering such a problem.  Unfortunately Dynamic Surroundings does not have a check in the ParticleManager because that would require ASM. 