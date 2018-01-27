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

package org.blockartistry.DynSurround.client.sound;

import java.lang.reflect.Field;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModEnvironment;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.event.DiagnosticEvent;
import org.blockartistry.lib.math.MathStuff;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.client.event.sound.SoundEvent.SoundSourceEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

@SideOnly(Side.CLIENT)
public class SoundManagerReplacement extends SoundManager {

	private static Field soundPhysicsGlobalVolume;

	static {
		try {
			final Class<?> soundPhysics = Class.forName("com.sonicether.soundphysics.SoundPhysics");
			soundPhysicsGlobalVolume = ReflectionHelper.findField(soundPhysics, "globalVolumeMultiplier");
		} catch (final Exception ex) {
			soundPhysicsGlobalVolume = null;
		}
	}

	private static final int MAX_STREAM_CHANNELS = 16;
	private final static float MUTE_VOLUME = 0.00001F;

	// Protection for bad behaved mods...
	private final Object mutex = new Object();
	private int violationCount = 0;

	public SoundManagerReplacement(final SoundHandler handler, final GameSettings settings) {
		super(handler, settings);
		MinecraftForge.EVENT_BUS.register(this);

		if (soundPhysicsGlobalVolume != null)
			DSurround.log().info("SoundPhysics present and using global volume multiplier");
	}

	private void checkForClientThread(final String method) {
		this.checkForClientThread(method, null);
	}

	private void checkForClientThread(final String method, final ISound sound) {
		if (ModOptions.enableDebugLogging) {
			final String name = Thread.currentThread().getName();
			if (!name.equals("Client thread")) {
				this.violationCount++;
				DSurround.log().warn("SoundManager.%s() access by non-client thread [%s]!", method, name);
				if (sound != null) {
					final ResourceLocation rl = sound.getSoundLocation();
					if (rl != null)
						DSurround.log().warn("The sound in question is: %s", rl.toString());
				}
			}
		}
	}

	private SoundSystem getSoundSystem() {
		return this.sndSystem;
	}

	private void setState(@Nonnull final ISound sound, @Nonnull final SoundState state) {
		if (sound instanceof BasicSound) {
			((BasicSound<?>) sound).setState(state);
		}
	}

	private void setStateIf(@Nonnull final ISound sound, @Nonnull final SoundState current,
			@Nonnull final SoundState state) {
		if (sound instanceof BasicSound) {
			final BasicSound<?> s = (BasicSound<?>) sound;
			if (s.getState() == current)
				s.setState(state);
		}
	}

	private void stopSound(@Nonnull final BasicSound<?> sound) {
		if (!StringUtils.isEmpty(sound.getId()) && getSoundSystem() != null) {
			getSoundSystem().stop(sound.getId());
		}
		super.stopSound(sound);
	}

	@Override
	public void stopSound(@Nonnull final ISound sound) {
		synchronized (this.mutex) {
			checkForClientThread("stopSound", sound);
			try {
				if (sound instanceof BasicSound<?>) {
					this.stopSound((BasicSound<?>) sound);
				} else {
					super.stopSound(sound);
				}
			} catch (final Throwable t) {
				// Stop more weirdness from a crashed sound system
			}
		}
	}

	private void playSound(@Nonnull final BasicSound<?> sound) {
		sound.setId(StringUtils.EMPTY);
		if (!ModEnvironment.ActualMusic.isLoaded() || sound.getCategory() != SoundCategory.MUSIC)
			super.playSound(sound);
		if (StringUtils.isEmpty(sound.getId()))
			sound.setState(SoundState.ERROR);
		else
			sound.setState(SoundState.PLAYING);
	}

	@Override
	public void playSound(@Nonnull final ISound sound) {
		if (sound != null) {
			try {
				synchronized (this.mutex) {
					checkForClientThread("playSound", sound);
					if (sound instanceof BasicSound<?>)
						this.playSound((BasicSound<?>) sound);
					else if (!ModEnvironment.ActualMusic.isLoaded() || sound.getCategory() != SoundCategory.MUSIC)
						super.playSound(sound);
				}

				// Flush - avoid that pesky missing sound issue due to async
				getSoundSystem().CommandQueue(null);
			} catch (final Throwable t) {
				// Stop more weirdness from a crashed sound system
			}
		}
	}

	private void playDelayedSound(@Nonnull final BasicSound<?> sound, final int delay) {
		sound.setId(StringUtils.EMPTY);
		super.playDelayedSound(sound, delay);
		sound.setState(SoundState.DELAYED);
	}

	@Override
	public void playDelayedSound(@Nonnull final ISound sound, final int delay) {
		if (sound != null) {
			try {
				synchronized (this.mutex) {
					checkForClientThread("playDelayedSound", sound);
					if (sound instanceof BasicSound<?>) {
						this.playDelayedSound((BasicSound<?>) sound, delay);
					} else {
						super.playDelayedSound(sound, delay);
					}
				}
			} catch (final Throwable t) {
				// Stop more weirdness from a crashed sound system
			}
		}
	}

	@Override
	public void stopAllSounds() {
		synchronized (this.mutex) {
			checkForClientThread("stopAllSounds");
			if (this.loaded) {
				// Need to make sure all our sounds have been marked
				// as DONE. Reason is the underlying routine just
				// wipes out all the lists.
				for (final ISound s : this.playingSounds.values())
					this.setState(s, SoundState.DONE);
				for (final ISound s : this.delayedSounds.keySet())
					this.setState(s, SoundState.DONE);
			}
			super.stopAllSounds();
		}
	}

	@Override
	public void pauseAllSounds() {
		synchronized (this.mutex) {
			checkForClientThread("pauseAllSounds");
			for (final ISound s : this.playingSounds.values())
				this.setStateIf(s, SoundState.PLAYING, SoundState.PAUSED);

			super.pauseAllSounds();
		}
	}

	@Override
	public void resumeAllSounds() {
		synchronized (this.mutex) {
			checkForClientThread("resumeAllSounds");
			for (final ISound s : this.playingSounds.values())
				this.setStateIf(s, SoundState.PAUSED, SoundState.PLAYING);

			super.resumeAllSounds();
		}
	}

	@Override
	public void updateAllSounds() {
		synchronized (this.mutex) {
			checkForClientThread("updateAllSounds");
			final SoundSystem sndSystem = getSoundSystem();

			++this.playTime;

			for (final ITickableSound itickablesound : this.tickableSounds) {
				itickablesound.update();

				if (itickablesound.isDonePlaying()) {
					this.stopSound(itickablesound);
				} else {
					final String s = this.invPlayingSounds.get(itickablesound);
					synchronized (SoundSystemConfig.THREAD_SYNC) {
						sndSystem.setVolume(s, this.getClampedVolume(itickablesound));
						sndSystem.setPitch(s, this.getClampedPitch(itickablesound));
						sndSystem.setPosition(s, itickablesound.getXPosF(), itickablesound.getYPosF(),
								itickablesound.getZPosF());
					}
				}
			}

			final Iterator<Entry<String, ISound>> iterator = this.playingSounds.entrySet().iterator();

			while (iterator.hasNext()) {

				final Entry<String, ISound> entry = iterator.next();
				final String s1 = entry.getKey();

				if (!sndSystem.playing(s1)) {
					final ISound isound = entry.getValue();
					final int j = isound.getRepeatDelay();
					final int minThresholdDelay = isound instanceof BasicSound ? 0 : 1;

					// Repeatable sound could have a delay of 0, meaning
					// don't delay a requeue.
					if (isound.canRepeat() && j >= minThresholdDelay) {
						this.playDelayedSound(isound, j);
					} else {
						this.setState(isound, SoundState.DONE);
					}

					iterator.remove();
					sndSystem.removeSource(s1);
					this.playingSoundsStopTime.remove(s1);

					try {
						this.categorySounds.remove(isound.getCategory(), s1);
					} catch (RuntimeException var8) {
						;
					}

					if (isound instanceof ITickableSound) {
						this.tickableSounds.remove(isound);
					}
				}
			}

			final Iterator<Entry<ISound, Integer>> iterator1 = this.delayedSounds.entrySet().iterator();

			while (iterator1.hasNext()) {
				final Entry<ISound, Integer> entry1 = iterator1.next();

				if (this.playTime >= entry1.getValue().intValue()) {
					final ISound isound1 = entry1.getKey();

					if (isound1 instanceof ITickableSound) {
						((ITickableSound) isound1).update();
					}

					this.playSound(isound1);
					iterator1.remove();
				}
			}
		}
	}

	@SubscribeEvent
	public void onSoundSourceEvent(@Nonnull final SoundSourceEvent event) {
		final ISound sound = event.getSound();
		if (sound instanceof BasicSound<?>) {
			((BasicSound<?>) sound).setId(event.getUuid());
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void diagnostics(final DiagnosticEvent.Gather event) {
		synchronized (this.mutex) {
			checkForClientThread("diagnostics");
			if (this.violationCount > 0)
				event.output
						.add(String.format(TextFormatting.RED + "SoundManager violations: %d", this.violationCount));

			final TObjectIntHashMap<String> counts = new TObjectIntHashMap<String>();

			final Iterator<Entry<String, ISound>> iterator = this.playingSounds.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, ISound> entry = iterator.next();
				ISound isound = entry.getValue();
				counts.adjustOrPutValue(isound.getSound().getSoundLocation().toString(), 1, 1);
			}

			final ArrayList<String> results = new ArrayList<String>();
			final TObjectIntIterator<String> itr = counts.iterator();
			while (itr.hasNext()) {
				itr.advance();
				results.add(String.format(TextFormatting.GOLD + "%s: %d", itr.key(), itr.value()));
			}
			Collections.sort(results);
			event.output.addAll(results);
		}

	}

	public boolean isMuted() {
		synchronized (this.mutex) {
			checkForClientThread("isMuted");
			return this.sndSystem != null && getSoundSystem().getMasterVolume() == MUTE_VOLUME;
		}
	}

	public void setMuted(final boolean flag) {
		// If not loaded return
		if (!this.loaded || this.sndSystem == null)
			return;

		synchronized (this.mutex) {
			checkForClientThread("setMuted");
			final SoundSystem ss = getSoundSystem();

			// OpenEye: Looks like the command thread is dead or not initialized.
			try {
				if (flag) {
					ss.setMasterVolume(MUTE_VOLUME);
				} else {
					final GameSettings options = Minecraft.getMinecraft().gameSettings;
					ss.setMasterVolume(options.getSoundLevel(SoundCategory.MASTER));
				}
			} catch (final Throwable t) {
				// Silent - at some point the thread will come back and can be
				// issued a mute.
				;
			}
		}
	}

	@Override
	public float getClampedVolume(@Nonnull final ISound sound) {
		final float volumeScale = ClientRegistry.SOUND.getVolumeScale(sound);
		final float volume = sound.getVolume() * getVolume(sound.getCategory()) * volumeScale;
		float result = MathStuff.clamp(volume, 0.0F, 1.0F);

		try {
			if (soundPhysicsGlobalVolume != null)
				return result * soundPhysicsGlobalVolume.getFloat(null);
		} catch (final Exception ex) {

		}
		return result;
	}

	private static void alErrorCheck() {
		final int error = AL10.alGetError();
		if (error != AL10.AL_NO_ERROR)
			DSurround.log().warn("OpenAL error: %d", error);
	}

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

		int normalChannelCount = ModOptions.normalSoundChannelCount;
		int streamChannelCount = ModOptions.streamingSoundChannelCount;

		if (ModOptions.autoConfigureChannels && totalChannels > 64) {
			totalChannels = ((totalChannels + 1) * 3) / 4;
			streamChannelCount = Math.min(totalChannels / 5, MAX_STREAM_CHANNELS);
			normalChannelCount = totalChannels - streamChannelCount;
		}

		DSurround.log().info("Sound channels: %d normal, %d streaming (total avail: %s)", normalChannelCount,
				streamChannelCount, totalChannels == -1 ? "UNKNOWN" : Integer.toString(totalChannels));
		SoundSystemConfig.setNumberNormalChannels(normalChannelCount);
		SoundSystemConfig.setNumberStreamingChannels(streamChannelCount);

		// Setup sound buffering
		if (ModOptions.streamBufferCount != 0)
			SoundSystemConfig.setNumberStreamingBuffers(ModOptions.streamBufferCount);
		if (ModOptions.streamBufferSize != 0)
			SoundSystemConfig.setStreamingBufferSize(ModOptions.streamBufferSize * 1024);
		DSurround.log().info("Stream buffers: %d x %d", SoundSystemConfig.getNumberStreamingBuffers(),
				SoundSystemConfig.getStreamingBufferSize());
	}

}
