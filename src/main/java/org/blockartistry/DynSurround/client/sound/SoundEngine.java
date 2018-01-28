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
import net.minecraft.client.audio.SoundManager;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystem;

@SideOnly(Side.CLIENT)
public class SoundEngine {

	private static final int SOUND_QUEUE_SLACK = 6;

	private static SoundEngine instance = null;

	public static SoundEngine instance() {
		if (instance == null)
			instance = new SoundEngine();

		return instance;
	}

	private SoundEngine() {

	}

	private static SoundManagerReplacement getManager() {
		return (SoundManagerReplacement) Minecraft.getMinecraft().getSoundHandler().sndManager;
	}

	public int currentSoundCount() {
		return getManager().currentSoundCount();
	}

	public int maxSoundCount() {
		return getManager().maxSoundCount();
	}

	private boolean canFitSound() {
		return currentSoundCount() < (getManager().numberOfNormalChannels() - SOUND_QUEUE_SLACK);
	}

	public boolean isSoundPlaying(@Nonnull final BasicSound<?> sound) {
		return getManager().isSoundPlaying(sound);
	}

	public boolean isSoundPlaying(@Nonnull final String soundId) {
		return getManager().isSoundPlaying(soundId);
	}

	public void stopSound(@Nonnull final String sound) {
		this.stopSound(sound, null);
	}

	public void stopSound(@Nonnull final String sound, @Nonnull final SoundCategory cat) {
		getManager().stop(sound, cat);
	}

	public void stopSound(@Nonnull final BasicSound<?> sound) {
		getManager().stopSound(sound);
	}

	public void stopAllSounds() {
		getManager().stopAllSounds();
	}

	@Nullable
	public String playSound(@Nonnull final BasicSound<?> sound) {
		if (!canFitSound()) {
			if (ModOptions.enableDebugLogging)
				DSurround.log().debug("> NO ROOM: [%s]", sound.toString());
			return null;
		}

		final SoundManager manager = getManager();

		if (!StringUtils.isEmpty(sound.getId()))
			manager.stopSound(sound);

		manager.playSound(sound);

		if (ModOptions.enableDebugLogging) {
			if (StringUtils.isEmpty(sound.getId())) {
				DSurround.log().debug("> NOT QUEUED: [%s]", sound.toString());
			} else {
				final StringBuilder builder = new StringBuilder();
				builder.append("> QUEUED: [").append(sound.toString()).append(']');
				if (DSurround.log().testTrace(ModOptions.Trace.TRUE_SOUND_VOLUME)) {
					final SoundSystem ss = manager.sndSystem;
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

}
