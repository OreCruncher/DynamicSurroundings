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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

@SideOnly(Side.CLIENT)
public class SoundEngine {

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
		if (sound != null) {
			this.manager.stopSound(sound);
		}
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

		if (!StringUtils.isEmpty(sound.getId()))
			this.manager.stopSound(sound);

		this.manager.playSound(sound);

		if (ModOptions.enableDebugLogging) {
			if (StringUtils.isEmpty(sound.getId())) {
				DSurround.log().debug("> NOT QUEUED: [%s]", sound.toString());
			} else {
				final StringBuilder builder = new StringBuilder();
				builder.append("> QUEUED: [").append(sound.toString()).append(']');
				if (DSurround.log().testTrace(ModOptions.Trace.TRUE_SOUND_VOLUME)) {
					final SoundSystem ss = this.manager.sndSystem;
					// Force a flush of all commands so we can get
					// the actual volume and pitch used within the
					// sound library.
					ss.CommandQueue(null);
					final float v = ss.getVolume(sound.getId());
					final float p = ss.getPitch(sound.getId());
					builder.append("; v: ").append(v).append(", p: ").append(p);
				}
				DSurround.log().debug(builder.toString());
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

}
