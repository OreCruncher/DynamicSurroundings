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

import java.util.Iterator;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.SoundRegistry;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystem;

@SideOnly(Side.CLIENT)
public class SoundManagerReplacement extends SoundManager {

	public SoundManagerReplacement(final SoundHandler handler, final GameSettings settings) {
		super(handler, settings);
	}

	@Nullable
	private static SoundSystem getSoundSystem(final SoundManager mgr) {
		return (SoundSystem) mgr.sndSystem;
	}

	@Override
	public void updateAllSounds() {
		final SoundSystem sndSystem = getSoundSystem(this);

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

					// Repeatable sound could have a delay of 0, meaning
					// don't delay a requeue.
					if (isound.canRepeat() && j >= 0) {
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

	@Override
	public float getClampedVolume(@Nonnull final ISound sound) {
		float result = 0.0F;
		if (sound != null) {
			final ResourceLocation location = sound.getSoundLocation();
			if (location != null) {
				final SoundRegistry registry = RegistryManager.get(RegistryType.SOUND);
				final float volumeScale = registry.getVolumeScale(location.toString());
				result = (float) MathHelper.clamp(
						(double) sound.getVolume() * (double) getSoundCategoryVolume(sound.getCategory()) * volumeScale,
						0.0D, 1.0D);
			}
		}
		return result;
	}

	private static float getSoundCategoryVolume(@Nullable final SoundCategory category) {
		return category != null && category != SoundCategory.MASTER
				? Minecraft.getMinecraft().gameSettings.getSoundLevel(category) : 1.0F;
	}

}
