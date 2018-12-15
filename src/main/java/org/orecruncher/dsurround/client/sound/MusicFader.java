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

package org.orecruncher.dsurround.client.sound;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.lib.math.MathStuff;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

/**
 * This guy is responsible for manipulating the volume state of the Music
 * SoundCategory. This is so normal playing music can fade in/out whenever
 * BattleMusic is being played, or when a player hits the "play" button in the
 * sound configuration.
 */
@EventBusSubscriber(value = Side.CLIENT, modid = ModInfo.MOD_ID)
public final class MusicFader {

	private static final float MIN_VOLUME_SCALE = 0.001F;
	private static final float FADE_AMOUNT = 0.02F;

	private static float currentScale = 1.0F;

	private static ConfigSoundInstance playingConfigSound;

	public static float getMusicScaling() {
		return currentScale;
	}

	@SubscribeEvent
	public static void onTick(@Nonnull final TickEvent.ClientTickEvent event) {

		if (event.phase == Phase.END)
			return;

		final float oldScale = currentScale;

		if (playingConfigSound != null) {
			if (playingConfigSound.isDonePlaying())
				stopConfigSound(playingConfigSound);
		}

		if (playingConfigSound == null) {
			// Adjust volume scale based on battle state
			if (EnvironState.getBattleScanner().inBattle()) {
				currentScale -= FADE_AMOUNT * 2;
			} else {
				currentScale += FADE_AMOUNT;
			}
		}

		// Make sure it is properly bounded
		currentScale = MathStuff.clamp(currentScale, MIN_VOLUME_SCALE, 1.0F);

		// If there is a change in scale tell the SoundManager
		if (Float.compare(oldScale, currentScale) != 0) {
			// Have to tickle the sound engine because the scaling changed. Just set it to
			// the current value which causes the SoundManager to reevaluate the volume
			// settings. Since we hooked the volume routine with ASM we can apply the
			// effect of scaling there.
			final float mcScale = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MUSIC);
			SoundEngine.instance().getSoundManager().setVolume(SoundCategory.MUSIC, mcScale);
		}

	}

	/**
	 * Used by the configuration system to short circuit the playing music in order
	 * to play a sound sample. The system will automagically put things back the way
	 * they were before the interruption.
	 *
	 * @param sound
	 */
	public static void playConfigSound(@Nonnull final ConfigSoundInstance sound) {
		if (sound != null) {
			SoundEngine.instance().stopAllSounds();
			currentScale = MIN_VOLUME_SCALE;
			playingConfigSound = sound;
			SoundEngine.instance().playSound(sound);
		}
	}

	/**
	 * Used by the configuration system to stop playing a configure sound. Sounds
	 * that were muted prior will fade back in.
	 *
	 * @param sound
	 */
	public static void stopConfigSound(@Nonnull final ConfigSoundInstance sound) {
		if (playingConfigSound != null) {
			if (playingConfigSound != sound)
				ModBase.log().warn("Inconsistent sound in MusicFader");
			SoundEngine.instance().stopSound(playingConfigSound);
			playingConfigSound = null;
		}
	}

}
