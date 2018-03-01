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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.lib.sound.ITrackedSound;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.client.event.sound.SoundEvent.SoundSourceEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = DSurround.MOD_ID)
public final class SoundEngine {

	private static final Field soundSystem = ReflectionHelper.findField(SoundManager.class, "sndSystem",
			"field_148620_e");

	private final static float MUTE_VOLUME = 0.00001F;
	private final static int MAX_STREAM_CHANNELS = 16;

	public static final SoundEngine INSTANCE = new SoundEngine();

	private SoundEngine() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	private static SoundManager getManager() {
		return (SoundManagerReplacement) Minecraft.getMinecraft().getSoundHandler().sndManager;
	}

	private static SoundSystem getSoundSystem() {
		try {
			return (SoundSystem) soundSystem.get(getManager());
		} catch (final Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	public boolean isSoundPlaying(@Nonnull final ITrackedSound sound) {
		return getManager().isSoundPlaying(sound);
	}

	public void stopSound(@Nonnull final ITrackedSound sound) {
		getManager().stopSound(sound);
	}

	public void stopAllSounds() {
		getManager().stopAllSounds();
	}

	@Nullable
	public String playSound(@Nonnull final ITrackedSound sound) {
		getManager().playSound(sound);
		return sound.getId();
	}

	public boolean isMuted() {
		return getSoundSystem().getMasterVolume() == MUTE_VOLUME;
	}

	public void setMuted(final boolean flag) {
		// OpenEye: Looks like the command thread is dead or not initialized.
		try {
			if (flag) {
				getSoundSystem().setMasterVolume(MUTE_VOLUME);
			} else {
				final GameSettings options = Minecraft.getMinecraft().gameSettings;
				getSoundSystem().setMasterVolume(options.getSoundLevel(SoundCategory.MASTER));
			}
		} catch (final Throwable t) {
			// Silent - at some point the thread will come back and can be
			// issued a mute.
			;
		}
	}

	@SubscribeEvent
	public static void onSoundSourceEvent(@Nonnull final SoundSourceEvent event) {
		final ISound sound = event.getSound();
		if (sound instanceof ITrackedSound) {
			((ITrackedSound) sound).setId(event.getUuid());
		}
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

		int normalChannelCount = ModOptions.sound.normalSoundChannelCount;
		int streamChannelCount = ModOptions.sound.streamingSoundChannelCount;

		if (ModOptions.sound.autoConfigureChannels && totalChannels > 64) {
			totalChannels = ((totalChannels + 1) * 3) / 4;
			streamChannelCount = Math.min(totalChannels / 5, MAX_STREAM_CHANNELS);
			normalChannelCount = totalChannels - streamChannelCount;
		}

		DSurround.log().info("Sound channels: %d normal, %d streaming (total avail: %s)", normalChannelCount,
				streamChannelCount, totalChannels == -1 ? "UNKNOWN" : Integer.toString(totalChannels));
		SoundSystemConfig.setNumberNormalChannels(normalChannelCount);
		SoundSystemConfig.setNumberStreamingChannels(streamChannelCount);

		// Setup sound buffering
		if (ModOptions.sound.streamBufferCount != 0)
			SoundSystemConfig.setNumberStreamingBuffers(ModOptions.sound.streamBufferCount);
		if (ModOptions.sound.streamBufferSize != 0)
			SoundSystemConfig.setStreamingBufferSize(ModOptions.sound.streamBufferSize * 1024);
		DSurround.log().info("Stream buffers: %d x %d", SoundSystemConfig.getNumberStreamingBuffers(),
				SoundSystemConfig.getStreamingBufferSize());
	}

}
