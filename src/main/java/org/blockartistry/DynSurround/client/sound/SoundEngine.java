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

import java.nio.IntBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISoundEventListener;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.sound.SoundEvent.SoundSourceEvent;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

@Mod.EventBusSubscriber(Side.CLIENT)
public class SoundEngine {

	private static final int MAX_STREAM_CHANNELS = 16;
	private static final int SOUND_QUEUE_SLACK = 6;

	private static SoundEngine instance = null;

	public static SoundEngine instance() {
		if (instance == null)
			instance = new SoundEngine();

		return instance;
	}

	private SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
	private SoundManager manager = this.handler.sndManager;

	private SoundEngine() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void addListender(@Nonnull final ISoundEventListener listener) {
		this.manager.addListener(listener);
	}

	public void removeListener(@Nonnull final ISoundEventListener listener) {
		this.manager.removeListener(listener);
	}

	public int currentSoundCount() {
		return this.manager.playingSoundsStopTime.size();
	}

	public int maxSoundCount() {
		return SoundSystemConfig.getNumberNormalChannels() + SoundSystemConfig.getNumberStreamingChannels();
	}

	private boolean canFitSound() {
		return currentSoundCount() < (SoundSystemConfig.getNumberNormalChannels() - SOUND_QUEUE_SLACK);
	}

	public boolean isSoundPlaying(@Nonnull final BasicSound<?> sound) {
		return this.manager.isSoundPlaying(sound) || this.manager.invPlayingSounds.containsKey(sound)
				|| this.manager.delayedSounds.containsKey(sound);
	}

	public boolean isSoundPlaying(@Nonnull final String soundId) {
		if (StringUtils.isEmpty(soundId))
			return false;
		return this.manager.playingSounds.containsKey(soundId);
	}

	public void stopSound(@Nonnull final String sound, @Nonnull final SoundCategory cat) {
		if (sound != null)
			this.manager.stop(sound, cat);
	}

	public void stopSound(@Nonnull final BasicSound<?> sound) {
		if (sound != null)
			this.manager.stopSound(sound);
	}

	public void stopAllSounds() {
		this.manager.stopAllSounds();
	}

	@Nullable
	public String playSound(@Nonnull final BasicSound<?> sound) {
		if (!canFitSound()) {
			if (ModOptions.enableDebugLogging)
				DSurround.log().debug("> NO ROOM: [%s]", sound.toString());
			return null;
		}

		if(!StringUtils.isEmpty(sound.getId()))
			this.manager.stopSound(sound);
		
		sound.setId(StringUtils.EMPTY);
		this.manager.playSound(sound);

		if (ModOptions.enableDebugLogging) {
			if (StringUtils.isEmpty(sound.getId())) {
				DSurround.log().debug("> NOT QUEUED: [%s]", sound.toString());
			} else {
				final SoundSystem ss = this.manager.sndSystem;
				// Force a flush of all commands so we can get
				// the actual volume and pitch used within the
				// sound library.
				ss.CommandQueue(null);
				final float v = ss.getVolume(sound.getId());
				final float p = ss.getPitch(sound.getId());
				DSurround.log().debug("> QUEUED: [%s]; v: %f, p: %f", sound.toString(), v, p);
			}
		}

		return sound.getId();
	}

	@Nullable
	public String playSound(@Nonnull final BlockPos pos, @Nonnull final SoundEvent soundIn,
			@Nonnull final SoundCategory category, final float volume, final float pitch) {
		final BasicSound<?> sound = new AdhocSound(soundIn, category);
		sound.setVolume(volume).setPitch(pitch).setPosition(pos);
		return this.playSound(sound);
	}
	
	/**
	 * This event hook attempts to associate the internal UUID of the sound play
	 * event with a sound.
	 */
	@SubscribeEvent
	public void onSoundSourceEvent(@Nonnull final SoundSourceEvent event) {
		final ISound sound = event.getSound();
		if (sound instanceof BasicSound<?>) {
			((BasicSound<?>) sound).setId(event.getUuid());
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onSoundSetup(@Nonnull final SoundSetupEvent event) {
		configureSound();
	}

	private static void alErrorCheck() {
		final int error = AL10.alGetError();
		if (error != AL10.AL_NO_ERROR)
			DSurround.log().warn("OpenAL error: %d", error);
	}

	private static void configureSound() {
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
	}

}
