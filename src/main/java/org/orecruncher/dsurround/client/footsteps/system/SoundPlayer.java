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
package org.orecruncher.dsurround.client.footsteps.system;

import java.util.Arrays;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.client.footsteps.implem.PendingSound;
import org.orecruncher.dsurround.client.footsteps.interfaces.EventType;
import org.orecruncher.dsurround.client.footsteps.interfaces.IAcoustic;
import org.orecruncher.dsurround.client.footsteps.interfaces.IOptions;
import org.orecruncher.dsurround.client.footsteps.interfaces.ISoundPlayer;
import org.orecruncher.dsurround.client.handlers.SoundEffectHandler;
import org.orecruncher.dsurround.client.sound.BasicSound;
import org.orecruncher.dsurround.client.sound.FootstepSound;
import org.orecruncher.dsurround.registry.Variator;
import org.orecruncher.lib.MCHelper;
import org.orecruncher.lib.TimeUtils;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SoundPlayer implements ISoundPlayer {

	protected final Random random = XorShiftRandom.current();
	protected final ObjectArray<PendingSound> pending = new ObjectArray<>();
	protected final Variator var;

	public SoundPlayer(@Nonnull final Variator var) {
		this.var = var;
	}

	public void playAcoustic(@Nonnull final EntityLivingBase entity, @Nonnull final Association assoc,
			@Nonnull final EventType event) {
		if (assoc.getNoAssociation()) {
			playStep(entity, assoc, event);
		} else {
			playAcoustic(entity, assoc.getData(), event, null);
		}
	}

	private void logAcousticPlay(@Nonnull final IAcoustic[] acoustics, @Nonnull final EventType event) {
		if (ModBase.log().isDebugging()) {
			final String txt = String.join(",",
					Arrays.stream(acoustics).map(IAcoustic::getAcousticName).toArray(String[]::new));
			ModBase.log().debug("Playing acoustic %s for event %s", txt, event.toString().toUpperCase());
		}
	}

	public void playAcoustic(@Nonnull final EntityLivingBase entity, @Nonnull final IAcoustic[] acoustics,
			@Nonnull final EventType event, @Nullable final IOptions inputOptions) {
		if (acoustics != null) {
			logAcousticPlay(acoustics, event);
			for (int i = 0; i < acoustics.length; i++) {
				acoustics[i].playSound(this, entity, event, inputOptions);
			}
		}
	}

	/**
	 * Used to play a block native sound because there is no acoustic profile
	 * defined for the block.
	 */
	protected void playStep(@Nonnull final EntityLivingBase entity, @Nonnull final Association assoc,
			@Nonnull final EventType event) {
		SoundType soundType = assoc.getSoundType();
		if (soundType != null && !assoc.isLiquid()) {
			final IBlockState upState = WorldUtils.getBlockState(entity.getEntityWorld(), assoc.getPos().up());
			if (upState.getBlock() == Blocks.SNOW_LAYER)
				soundType = MCHelper.getSoundType(upState);
			final SoundEvent se = event == EventType.LAND ? soundType.getFallSound() : soundType.getStepSound();
			actuallyPlaySound(entity, se, soundType.getVolume(), soundType.getPitch(), true);
		}
	}

	@Override
	public void playSound(@Nonnull final EntityLivingBase entity, @Nonnull final SoundEvent sound, final float volume,
			final float pitch, @Nullable final IOptions options) {
		// If it is a delayed sound queue it up. Otherwise play it.
		if (options != null && options.isDelayedSound()) {
			final long delay = TimeUtils.currentTimeMillis()
					+ randAB(this.random, options.getDelayMin(), options.getDelayMax());
			this.pending.add(new PendingSound(entity, sound, volume, pitch, delay, options.getDelayMax()));
		} else {
			actuallyPlaySound(entity, sound, volume, pitch, false);
		}
	}

	protected void actuallyPlaySound(@Nonnull final EntityLivingBase entity, @Nonnull final SoundEvent sound,
			final float volume, final float pitch, final boolean noScale) {
		try {
			final FootstepSound s = new FootstepSound(entity, sound).setVolume(volume * this.var.VOLUME_SCALE)
					.setPitch(pitch);
			if (noScale)
				s.setVolumeScale(BasicSound.DEFAULT_SCALE);
			SoundEffectHandler.INSTANCE.playSound(s);
		} catch (final Throwable t) {
			ModBase.log().error("Unable to play sound", t);
		}
	}

	@Override
	public Random getRNG() {
		return this.random;
	}

	public void think() {
		final long time = TimeUtils.currentTimeMillis();

		this.pending.removeIf(sound -> {
			if (sound.getTimeToPlay() <= time) {
				if (!sound.isLate(time))
					sound.playSound(this);
				return true;
			}
			return false;
		});
	}

	private long randAB(@Nonnull final Random rng, final long a, final long b) {
		return a >= b ? a : a + rng.nextInt((int) (b + 1));
	}

}
