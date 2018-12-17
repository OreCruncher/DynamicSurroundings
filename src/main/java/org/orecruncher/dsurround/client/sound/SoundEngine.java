/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher, Abastro
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.orecruncher.dsurround.client.sound;

import java.lang.reflect.Field;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.ModOptions.Trace;
import org.orecruncher.dsurround.event.DiagnosticEvent;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.lib.ThreadGuard;
import org.orecruncher.lib.ThreadGuard.Action;
import org.orecruncher.lib.math.MathStuff;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.sound.SoundEvent.SoundSourceEvent;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.Source;

@EventBusSubscriber(value = Side.CLIENT, modid = ModInfo.MOD_ID)
public final class SoundEngine {

	private static Field soundPhysicsGlobalVolume;

	private static final Field getSoundManager = ReflectionHelper.findField(SoundHandler.class, "sndManager",
			"field_147694_f");
	private static final Field getSoundRegistry = ReflectionHelper.findField(SoundHandler.class, "soundRegistry",
			"field_147697_e");
	private static final Field getSoundSystem = ReflectionHelper.findField(SoundManager.class, "sndSystem",
			"field_148620_e");
	private static final Field getPlayingSounds = ReflectionHelper.findField(SoundManager.class, "playingSounds",
			"field_148629_h");
	private static final Field getPlayingSoundsInv = ReflectionHelper.findField(SoundManager.class, "invPlayingSounds", "field_148630_i");
	private static final Field getDelayedSounds = ReflectionHelper.findField(SoundManager.class, "delayedSounds",
			"field_148626_m");
	private static final Field getSoundLibrary = ReflectionHelper.findField(SoundSystem.class, "soundLibrary");
	private static final Field removed = ReflectionHelper.findField(Source.class, "removed");

	private static final float MUTE_VOLUME = 0.00001F;
	private static final int MAX_STREAM_CHANNELS = 16;
	private static final int SOUND_QUEUE_SLACK = 6;

	// Maximum number of sound channels configured in the sound system
	private static int maxSounds = 0;
	private static SoundEngine instance_ = new SoundEngine();

	static {
		try {
			final Class<?> soundPhysics = Class.forName("com.sonicether.soundphysics.SoundPhysics");
			soundPhysicsGlobalVolume = ReflectionHelper.findField(soundPhysics, "globalVolumeMultiplier");
		} catch (final Exception ex) {
			soundPhysicsGlobalVolume = null;
		}
	}

	public static SoundEngine instance() {
		return instance_;
	}

	// Protection for bad behaved mods...
	private final ThreadGuard guard = new ThreadGuard(ModBase.log(), Side.CLIENT, "SoundManager")
			.setAction(ModBase.isDeveloperMode() ? Action.EXCEPTION
					: ModOptions.logging.enableDebugLogging ? Action.LOG : Action.NONE);

	private final Set<ISoundInstance> queuedSounds = new ReferenceOpenHashSet<>(256);

	private String playedSoundId = null;

	private SoundEngine() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	/**
	 * Obtains the SoundRegistry from the SoundHandler
	 *
	 * @return Reference to the SoundRegistry
	 */
	@Nonnull
	public SoundRegistry getSoundRegistry() {
		return resolve(getSoundRegistry, Minecraft.getMinecraft().getSoundHandler());
	}

	/**
	 * Obtains the reference to the SoundManager from SoundHandler
	 *
	 * @return Reference to the SoundManager
	 */
	@Nonnull
	public SoundManager getSoundManager() {
		return resolve(getSoundManager, Minecraft.getMinecraft().getSoundHandler());
	}

	@SuppressWarnings("unchecked")
	private <T> T resolve(@Nonnull final Field f, @Nonnull final Object obj) {
		try {
			return (T) f.get(obj);
		} catch (final Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	private int currentSoundCount() {
		try {
			synchronized (SoundSystemConfig.THREAD_SYNC) {
				return getSoundLibrary().getSources().size();
			}
		} catch (final Throwable t) {
			;
		}
		return 0;
	}

	private boolean canFitSound() {
		return currentSoundCount() < (maxSounds - SOUND_QUEUE_SLACK);
	}

	private void flushSoundQueue() {
		getSoundSystem().CommandQueue(null);
	}

	private SoundSystem getSoundSystem() {
		return resolve(getSoundSystem, getSoundManager());
	}

	private Library getSoundLibrary() {
		return resolve(getSoundLibrary, getSoundSystem());
	}

	private Map<String, ISound> getPlayingSounds() {
		return resolve(getPlayingSounds, getSoundManager());
	}
	
	private Map<ISound, String> getPlayingSoundsInv() {
		return resolve(getPlayingSoundsInv, getSoundManager());
	}

	private Map<ISound, Integer> getDelayedSounds() {
		return resolve(getDelayedSounds, getSoundManager());
	}

	/**
	 * Determines if the sound is currently playing within the sound system
	 *
	 * @param sound The sound to check
	 * @return true if the sound is currently playing, false otherwise
	 */
	public boolean isSoundPlaying(@Nonnull final ISoundInstance sound) {
		return sound.getState().isActive() && this.queuedSounds.contains(sound);
	}

	/**
	 * Stops the specified sound if it is playing.
	 *
	 * @param sound The sound to stop
	 */
	public void stopSound(@Nonnull final ISoundInstance sound) {
		getSoundManager().stopSound(sound);
	}

	/**
	 * Stops all playing and pending sounds. All lists and queues are dumped.
	 */
	public void stopAllSounds() {
		getSoundManager().stopAllSounds();
		flushSoundQueue();
		clearOrphans();
	}

	/**
	 * Submits the sound to the sound system to be played.
	 *
	 * @param sound Sound to play
	 * @return true if sound successfully queued; false if there was an error
	 */
	public boolean playSound(@Nonnull final ISoundInstance sound) {
		// If the sound is already queued return it's current active state.
		if (this.queuedSounds.contains(sound))
			return sound.getState().isActive();

		// Looks like a new sound.  Assume an error state until otherwise.
		sound.setState(SoundState.ERROR);

		// If the sound cannot fit then log and set the error state
		if (!canFitSound()) {
			ModBase.log().debug("> NO ROOM: [%s]", sound);
		} else {

			synchronized (SoundSystemConfig.THREAD_SYNC) {
				this.playedSoundId = null;
				try {
					getSoundManager().playSound(sound);
					if (this.playedSoundId != null)
						sound.setState(SoundState.PLAYING);
				} catch (@Nonnull final Throwable t) {
					final String txt = String.format("Unable to play sound [%s]", sound);
					ModBase.log().error(txt, t);
				}
			}

			// Add active sounds to the list for monitoring
			if (sound.getState().isActive()) {
				ModBase.log().debug(Trace.SOUND_PLAY, "> QUEUED: [%s]", sound);
				this.queuedSounds.add(sound);
			} else {
				ModBase.log().debug(Trace.SOUND_PLAY, "> NOT QUEUED: [%s]", sound);
			}
		}

		return sound.getState().isActive();
	}

	// Wipe out any orphans. Not sure exactly how this happens but it wouldn't
	// surprise me if there is a gap in thread processing in the sound engine.
	private void clearOrphans() {
		final Map<String, ISound> playingSounds = getPlayingSounds();
		final SoundSystem sndSystem = getSoundSystem();
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			final Map<String, Source> sounds = getSoundLibrary().getSources();
			final List<String> remove = sounds.entrySet().stream().filter(e -> !playingSounds.containsKey(e.getKey()))
					.map(e -> {
						final Source src = e.getValue();
						ModBase.log().debug("Killing orphaned sound [%s]",
								src.filenameURL != null ? src.filenameURL.getFilename() : "UNKNOWN");
						cleanupSource(src);
						return e.getKey();
					}).collect(Collectors.toList());
			remove.forEach(id -> sndSystem.removeSource(id));
		}
	}

	private static void cleanupSource(final Source source) {
		if (source.toStream) {
			try {
				removed.setBoolean(source, true);
			} catch (final IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			source.cleanup();
		}
	}

	/**
	 * Run down our active sound list checking that they are still active. If they
	 * aren't update the state accordingly.
	 *
	 * @param event Event that was raised
	 */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void clientTick(@Nonnull final TickEvent.ClientTickEvent event) {
		if (event.side == Side.CLIENT && event.phase == Phase.END) {
			final Map<ISound, Integer> delayedSounds = getDelayedSounds();
			final Map<ISound, String> playingInv = getPlayingSoundsInv();
			
			// Process our queued sounds to make sure the state is appropriate. A sound can
			// move between the playing sound list and the delayed sound list based on its
			// attributes so we need to make sure we detect that.
			//
			// Note that we cannot rely on isSoundPlaying().  It can return FALSE even
			// though the sound is in the internal playing lists.  We only want to transition
			// if the sound is in the playing lists or not.
			this.queuedSounds.removeIf(sound -> {
				switch (sound.getState()) {
				case DELAYED:
					if (!delayedSounds.containsKey(sound)) {
						if (playingInv.containsKey(sound))
							sound.setState(SoundState.PLAYING);
						else
							sound.setState(SoundState.DONE);
					}
					break;
				case PLAYING:
					if (!playingInv.containsKey(sound)) {
						if (delayedSounds.containsKey(sound))
							sound.setState(SoundState.DELAYED);
						else
							sound.setState(SoundState.DONE);
					}
					break;
				default:
					break;
				}
				return !sound.getState().isActive();
			});
		}
	}

	/**
	 * Checks to see if the sound system has been muted.
	 *
	 * @return true if it has been muted, false otherwise
	 */
	public boolean isMuted() {
		try {
			return getSoundSystem().getMasterVolume() == MUTE_VOLUME;
		} catch (final Throwable t) {
			;
		}
		return false;
	}

	/**
	 * Sets the mute state of the sound system based on the flag provided.
	 *
	 * @param flag true to mute the sound system, false to unmute
	 */
	public void setMuted(final boolean flag) {
		// OpenEye: Looks like the command thread is dead or not initialized.
		try {
			if (flag) {
				getSoundSystem().setMasterVolume(MUTE_VOLUME);
			} else {
				final GameSettings options = Minecraft.getMinecraft().gameSettings;
				if (options != null)
					getSoundSystem().setMasterVolume(options.getSoundLevel(SoundCategory.MASTER));
			}
		} catch (final Throwable t) {
			// Silent - at some point the thread will come back and can be
			// issued a mute.
			;
		}
	}

	/**
	 * Event raised when a sound is going to be played. Use this time to set the ID
	 * of an ISoundInstance as well as check whether the current thread is the
	 * client thread.
	 *
	 * @param event Incoming event that has been raised
	 */
	@SubscribeEvent
	public void onSoundSourceEvent(@Nonnull final SoundSourceEvent event) {
		this.guard.check("playSound");
		this.playedSoundId = event.getUuid();
	}

	/**
	 * Event handler for the diagnostic event.
	 *
	 * @param event Event that has been raised.
	 */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void diagnostics(final DiagnosticEvent.Gather event) {
		final int soundCount = currentSoundCount();
		final int maxCount = maxSounds;
		event.output.add("SoundSystem: " + soundCount + "/" + maxCount);

		final Object2IntOpenHashMap<ResourceLocation> counts = new Object2IntOpenHashMap<>();

		//@formatter:off
		getPlayingSounds().values().stream()
			.map(s -> s.getSound().getSoundLocation())
			.forEach(loc -> counts.addTo(loc, 1));

		final List<String> results =
			counts.object2IntEntrySet().stream()
				.map(e -> TextFormatting.GOLD + e.getKey().toString() + ": " + String.valueOf(e.getIntValue()))
				.sorted()
				.collect(Collectors.toList());
		//@formatter:on

		event.output.addAll(results);
	}

	public static float getVolume(@Nonnull final SoundCategory category) {
		if (category == null || category == SoundCategory.MASTER)
			return 1F;
		final GameSettings settings = Minecraft.getMinecraft().gameSettings;
		return settings != null ? settings.getSoundLevel(category) : 1.0F;
	}

	private static boolean fadeMusic(@Nonnull final ISound sound) {
		return (sound.getCategory() == SoundCategory.MUSIC && !(sound instanceof ConfigSoundInstance
				|| (sound instanceof TrackingSoundInstance && ModOptions.sound.enableBattleMusic)));
	}

	// SOUND may not be initialized if Forge did not initialized Minecraft fully.
	// That can happen if the environment does not meet it's dependency
	// requirements.
	private static float getVolumeScale(@Nonnull final ISound sound) {
		try {
			final float fade = fadeMusic(sound) ? MusicFader.getMusicScaling() : 1.0F;
			return RegistryManager.SOUND.getVolumeScale(sound) * fade;
		} catch (final Throwable t) {
			;
		}
		return 1F;
	}

	/**
	 * ASM redirects the SoundManager code to this method. Purpose is that the
	 * volume is scaled by additional configuration information.
	 *
	 * @param sound The sound object where volume is being calculated
	 * @return Clamped volume for playing the sound
	 */
	public static float getClampedVolume(@Nonnull final ISound sound) {
		final float volumeScale = getVolumeScale(sound);
		final float volume = sound.getVolume() * getVolume(sound.getCategory()) * volumeScale;
		final float result = MathStuff.clamp(volume, 0.0F, 1.0F);

		try {
			if (soundPhysicsGlobalVolume != null)
				return result * soundPhysicsGlobalVolume.getFloat(null);
		} catch (final Exception ex) {
			;
		}
		return result;
	}

	private static void alErrorCheck() {
		final int error = AL10.alGetError();
		if (error != AL10.AL_NO_ERROR)
			ModBase.log().warn("OpenAL error: %d", error);
	}

	/**
	 * Event handler for configuring the sound channels of the sound engine.
	 *
	 * @param event Event that has been raised
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void configureSound(@Nonnull final SoundSetupEvent event) {
		int totalChannels = -1;

		try {

			final boolean create = !AL.isCreated();
			if (create) {
				AL.create();
				alErrorCheck();
			}

			final IntBuffer ib = BufferUtils.createIntBuffer(1);
			ALC10.alcGetInteger(AL.getDevice(), ALC11.ALC_MONO_SOURCES, ib);
			alErrorCheck();
			totalChannels = ib.get(0);

			if (create)
				AL.destroy();

		} catch (final Throwable e) {
			e.printStackTrace();
		}

		int normalChannelCount = ModOptions.sound.normalSoundChannelCount;
		int streamChannelCount = ModOptions.sound.streamingSoundChannelCount;

		if (ModOptions.sound.autoConfigureChannels && totalChannels > 64) {
			totalChannels -= 32;
			streamChannelCount = Math.min(totalChannels / 8, MAX_STREAM_CHANNELS);
			normalChannelCount = totalChannels - streamChannelCount;
		}

		ModBase.log().info("Sound channels: %d normal, %d streaming (total avail: %s)", normalChannelCount,
				streamChannelCount, totalChannels == -1 ? "UNKNOWN" : Integer.toString(totalChannels));
		SoundSystemConfig.setNumberNormalChannels(normalChannelCount);
		SoundSystemConfig.setNumberStreamingChannels(streamChannelCount);

		maxSounds = SoundSystemConfig.getNumberNormalChannels() + SoundSystemConfig.getNumberStreamingChannels();

		// Setup sound buffering
		if (ModOptions.sound.streamBufferCount != 0)
			SoundSystemConfig.setNumberStreamingBuffers(ModOptions.sound.streamBufferCount);
		if (ModOptions.sound.streamBufferSize != 0)
			SoundSystemConfig.setStreamingBufferSize(ModOptions.sound.streamBufferSize * 1024);
		ModBase.log().info("Stream buffers: %d x %d", SoundSystemConfig.getNumberStreamingBuffers(),
				SoundSystemConfig.getStreamingBufferSize());
	}

}
