/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
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

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.handlers.SoundEffectHandler;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.registry.SoundMetadata;
import org.blockartistry.lib.gui.RecordTitleEmitter;
import org.blockartistry.lib.random.XorShiftRandom;
import org.blockartistry.lib.sound.BasicSound;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

/*
 * Emitters are used to produce sounds that are continuous
 * or on repeat. They ensure that the sound is always queue
 * in the sound system even if the underlying sound system
 * cancels the sound.
 */
@SideOnly(Side.CLIENT)
public abstract class Emitter {

	protected final Random RANDOM = XorShiftRandom.current();

	protected final SoundEffect effect;
	@Nullable
	protected final RecordTitleEmitter titleEmitter;
	protected BasicSound<?> activeSound;
	protected boolean done = false;

	public Emitter(@Nonnull final SoundEffect sound) {
		this.effect = sound;

		final RecordTitleEmitter.ITimeKeeper timer = new RecordTitleEmitter.ITimeKeeper() {
			@Override
			public int getTickMark() {
				return EnvironState.getTickCounter();
			}
		};

		if (StringUtils.isEmpty(sound.getSoundTitle())) {
			final SoundMetadata data = ClientRegistry.SOUND.getSoundMetadata(this.effect.getSound().getRegistryName());
			if (data != null) {
				if (!StringUtils.isEmpty(data.getTitle())) {
					final StringBuilder builder = new StringBuilder();
					builder.append(data.getTitle());
					if (data.getCredits().size() > 0)
						builder.append(" by ").append(data.getCredits().get(0));
					this.titleEmitter = new RecordTitleEmitter(builder.toString(), timer);
				} else {
					this.titleEmitter = null;
				}
			} else {
				this.titleEmitter = null;
			}

		} else {
			this.titleEmitter = new RecordTitleEmitter(sound.getSoundTitle(), timer);
		}
	}

	protected abstract BasicSound<?> createSound();

	public void update() {
		if (this.titleEmitter != null)
			this.titleEmitter.update();

		// Allocate a new sound to send down if needed
		if (this.activeSound == null) {
			this.activeSound = createSound();
		} else if (this.activeSound.getState().isActive()) {
			return;
		} else if (this.isFading()) {
			// If we get here the sound is no longer playing and is in the
			// fading state. This is possible because the actual sound
			// volume down in the engine could have hit 0 but the tick
			// handler on the sound did not have a chance to get there
			// first.
			this.done = true;
			return;
		}

		try {
			// Don't play if the player can't hear
			if (this.activeSound.canSoundBeHeard(EnvironState.getPlayerPosition()))
				SoundEffectHandler.INSTANCE.playSound(this.activeSound);
		} catch (final Throwable t) {
			DSurround.log().error("Unable to play sound", t);
		}
	}

	public void setVolume(final float volume) {
		if (this.activeSound != null)
			this.activeSound.setVolume(volume);
	}

	public float getVolume() {
		return this.activeSound != null ? this.activeSound.getVolume() : 0.0F;
	}

	public void setVolumeThrottle(final float throttle) {
		if (this.activeSound != null)
			this.activeSound.setVolumeThrottle(throttle);
	}

	public void setPitch(final float pitch) {
		if (this.activeSound != null)
			this.activeSound.setPitch(pitch);
	}

	public float getPitch() {
		return this.activeSound != null ? this.activeSound.getPitch() : 0.0F;
	}

	public void fade() {
		if (this.activeSound != null) {
			DSurround.log().debug("FADE: %s", this.activeSound.toString());
			this.activeSound.fade();
		}
	}

	public boolean isFading() {
		if (this.activeSound != null) {
			return this.activeSound.isFading();
		}
		return false;
	}

	public void unfade() {
		if (this.activeSound != null) {
			DSurround.log().debug("UNFADE: %s", this.activeSound.toString());
			this.activeSound.unfade();
		}
	}

	public boolean isDonePlaying() {
		if (this.activeSound != null)
			return this.done || this.activeSound.isDonePlaying();
		return this.done;
	}

	public void stop() {
		if (this.activeSound != null) {
			this.activeSound.setRepeat(false);
			SoundEngine.instance().stopSound(this.activeSound);
		}
	}

	@Override
	public String toString() {
		return this.activeSound.toString();
	}

}
