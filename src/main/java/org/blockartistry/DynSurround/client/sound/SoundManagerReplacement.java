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
import java.util.Iterator;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.registry.SoundRegistry;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.StreamThread;

@SideOnly(Side.CLIENT)
public class SoundManagerReplacement extends SoundManager {

	private static Field soundLibrary = null;
	private static Field streamThread = null;

	static {

		try {
			soundLibrary = ReflectionHelper.findField(SoundSystem.class, "soundLibrary");
			streamThread = ReflectionHelper.findField(Library.class, "streamThread");
		} catch (final Throwable t) {
			DSurround.log().warn("Cannot find sound manager fields; auto-restart not enabled");
			soundLibrary = null;
			streamThread = null;
		}

	}

	private final static float MUTE_VOLUME = 0.00001F;
	
	private SoundRegistry registry = null;

	public SoundManagerReplacement(final SoundHandler handler, final GameSettings settings) {
		super(handler, settings);
	}

	private void keepAlive() {
		if (streamThread == null)
			return;

		try {
			final Library l = (Library) soundLibrary.get(this.sndSystem);
			final StreamThread t = (StreamThread) streamThread.get(l);
			if (t != null && !t.isAlive()) {
				if (EnvironState.getPlayer() != null)
					EnvironState.getPlayer().sendMessage(new TextComponentString("Auto-restart of sound system!"));
				DSurround.log().warn("Auto-restart of sound system!");
				this.reloadSoundSystem();
			}
		} catch (final Throwable t) {
			;
		}
	}

	@Override
	public void updateAllSounds() {

		keepAlive();

		final SoundSystem sndSystem = this.sndSystem;

		++this.playTime;

		for (final ITickableSound itickablesound : this.tickableSounds) {
			itickablesound.update();

			if (itickablesound.isDonePlaying()) {
				this.stopSound(itickablesound);
			} else {
				final String s = this.invPlayingSounds.get(itickablesound);
				sndSystem.setVolume(s, this.getClampedVolume(itickablesound));
				sndSystem.setPitch(s, this.getClampedPitch(itickablesound));
				sndSystem.setPosition(s, itickablesound.getXPosF(), itickablesound.getYPosF(),
						itickablesound.getZPosF());
			}
		}

		final Iterator<Entry<String, ISound>> iterator = this.playingSounds.entrySet().iterator();

		while (iterator.hasNext()) {

			final Entry<String, ISound> entry = iterator.next();
			final String s1 = entry.getKey();

			if (!sndSystem.playing(s1)) {
				final int i = this.playingSoundsStopTime.get(s1).intValue();

				if (i <= this.playTime) {
					final ISound isound = entry.getValue();
					final int j = isound.getRepeatDelay();
					final int minThresholdDelay = isound instanceof BasicSound ? 0 : 1;

					// Repeatable sound could have a delay of 0, meaning
					// don't delay a requeue.
					if (isound.canRepeat() && j >= minThresholdDelay) {
						this.playDelayedSound(isound, j);
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
	
	public boolean isMuted() {
		return ((SoundSystem)this.sndSystem).getMasterVolume() == MUTE_VOLUME;
	}
	
	public void setMuted(final boolean flag) {
		// If not loaded return
		if(!this.loaded)
			return;
		
		final SoundSystem ss = (SoundSystem) this.sndSystem;

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

	@Override
	public float getClampedVolume(@Nonnull final ISound sound) {
		if (this.registry == null)
			this.registry = RegistryManager.get(RegistryType.SOUND);

		final float volumeScale = this.registry.getVolumeScale(sound);
		final float volume = sound.getVolume() * getVolume(sound.getCategory()) * volumeScale;
		return MathHelper.clamp(volume, 0.0F, 1.0F);
	}

}
