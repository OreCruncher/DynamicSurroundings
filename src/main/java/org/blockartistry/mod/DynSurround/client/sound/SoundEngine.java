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

package org.blockartistry.mod.DynSurround.client.sound;

import java.nio.IntBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISoundEventListener;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.client.event.sound.SoundEvent.SoundSourceEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystemConfig;

@SideOnly(Side.CLIENT)
public class SoundEngine {

	private static final int SOUND_QUEUE_SLACK = 6;

	private static int normalChannelCount = 0;
	private static int streamChannelCount = 0;
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
		return this.manager.playingSounds.size();
	}

	public int maxSoundCount() {
		return normalChannelCount + streamChannelCount;
	}

	public boolean canFitSound() {
		return currentSoundCount() < (normalChannelCount - SOUND_QUEUE_SLACK);
	}

	public boolean isSoundPlaying(@Nonnull final ISound sound) {
		return this.manager.isSoundPlaying(sound) || this.manager.invPlayingSounds.containsKey(sound)
				|| this.manager.delayedSounds.containsKey(sound);
	}

	public boolean isSoundPlaying(@Nonnull final String soundId) {
		if (StringUtils.isEmpty(soundId))
			return false;
		return this.manager.playingSounds.containsKey(soundId);
	}
	
	private ISound currentSound;
	private String soundId;

	@Nullable
	public String playSound(@Nonnull final ISound sound) {
		if (ModOptions.enableDebugLogging)
			ModLog.debug("PLAYING: " + sound.toString());

		this.soundId = null;
		this.currentSound = sound;
		this.manager.playSound(sound);
		return this.soundId;
	}

	/**
	 * This event hook attempts to associate the internal UUID of the sound play
	 * event with a sound.
	 */
	@SubscribeEvent
	public void onSoundSourceEvent(@Nonnull final SoundSourceEvent event) {
		if (event.getSound() == this.currentSound)
			this.soundId = event.getUuid();
	}

	public static void configureSound() {
		int totalChannels = -1;

		try {
			final boolean create = !AL.isCreated();
			if (create)
				AL.create();
			final IntBuffer ib = BufferUtils.createIntBuffer(1);
			ALC10.alcGetInteger(AL.getDevice(), ALC11.ALC_MONO_SOURCES, ib);
			totalChannels = ib.get(0);
			if (create)
				AL.destroy();
		} catch (final Throwable e) {
			e.printStackTrace();
		}

		normalChannelCount = ModOptions.normalSoundChannelCount;
		streamChannelCount = ModOptions.streamingSoundChannelCount;

		if (ModOptions.autoConfigureChannels && totalChannels > 64) {
			totalChannels = ((totalChannels + 1) * 3) / 4;
			streamChannelCount = totalChannels / 5;
			normalChannelCount = totalChannels - streamChannelCount;
		}

		ModLog.info("Sound channels: %d normal, %d streaming (total avail: %s)", normalChannelCount, streamChannelCount,
				totalChannels == -1 ? "UNKNOWN" : Integer.toString(totalChannels));
		SoundSystemConfig.setNumberNormalChannels(normalChannelCount);
		SoundSystemConfig.setNumberStreamingChannels(streamChannelCount);
	}

}
