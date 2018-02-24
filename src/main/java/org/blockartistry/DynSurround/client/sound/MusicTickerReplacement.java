/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

import java.lang.reflect.Field;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.client.gui.ConfigSound;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.compat.ModEnvironment;
import org.blockartistry.lib.math.MathStuff;
import org.blockartistry.lib.sound.BasicSound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * Replacement for the Vanilla MusicTicker.  This version queues up sounds that
 * have their volumes dynamically scaled.  Purpose is to reduce normal music
 * volume so that battle music can play.  Once battle is over volume of the
 * sound will return to normal.
 *
 * The music sound is queued as an ITickableSound so that the sound engine
 * will update the volume dynamically.
 */
@SideOnly(Side.CLIENT)
public class MusicTickerReplacement extends MusicTicker {

	private static final float MIN_VOLUME_SCALE = 0.001F;
	private static final float FADE_AMOUNT = 0.02F;

	private float currentScale = 1.0F;

	private final BasicSound.ISoundScale MUSIC_SCALER = () -> {
		return MusicTickerReplacement.this.currentScale;
	};

	public MusicTickerReplacement(@Nonnull final Minecraft mcIn) {
		super(mcIn);
	}

	@Override
	public void update() {
		// Adjust volume scale based on battle state
		if (EnvironState.getBattleScanner().inBattle()) {
			this.currentScale -= FADE_AMOUNT * 2;
		} else {
			this.currentScale += FADE_AMOUNT;
		}

		// Make sure it is properly bounded
		this.currentScale = MathStuff.clamp(this.currentScale, MIN_VOLUME_SCALE, 1.0F);

		if (this.currentMusic instanceof ConfigSound) {
			if (!SoundEngine.isSoundPlaying((BasicSound<?>) this.currentMusic)) {
				this.currentMusic = null;
				this.timeUntilNextMusic = 60;
				super.update();
			}
		} else {
			super.update();
		}
	}

	public void setPlaying(@Nonnull final ConfigSound sound) {
		stopMusic();
		this.currentMusic = sound;
		SoundEngine.playSound((BasicSound<?>) this.currentMusic);
	}

	@Override
	public void playMusic(@Nonnull final MusicTicker.MusicType requestedMusicType) {
		this.currentMusic = new MusicSound(requestedMusicType.getMusicLocation()).setVolumeScale(this.MUSIC_SCALER);
		SoundEngine.playSound((BasicSound<?>) this.currentMusic);
		this.timeUntilNextMusic = Integer.MAX_VALUE;
	}

	@Override
	public void stopMusic() {
		if (this.currentMusic != null) {
			SoundEngine.stopSound((BasicSound<?>) this.currentMusic);
			this.currentMusic = null;
			this.timeUntilNextMusic = 0;
		}
	}

	public static void initialize() {
		if (ModEnvironment.ActualMusic.isLoaded()) {
			DSurround.log().info("ActualMusic is installed; MusicTicker is NOT being replaced!");
		} else {
			try {
				final Field ticker = ReflectionHelper.findField(Minecraft.class, "mcMusicTicker", "field_147126_aw");
				if (ticker != null) {
					final Minecraft mc = Minecraft.getMinecraft();
					ticker.set(mc, new MusicTickerReplacement(mc));
				}
			} catch (final Throwable t) {
				DSurround.log().error("Unable to replace MusicTicker!", t);
			}
		}
	}

}
