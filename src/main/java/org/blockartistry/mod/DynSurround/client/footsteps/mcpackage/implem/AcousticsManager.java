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

package org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.implem;

import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Random;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.client.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.implem.AcousticsLibrary;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.IOptions;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.IOptions.Option;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.ISoundPlayer;
import org.blockartistry.mod.DynSurround.client.footsteps.game.system.Association;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IStepPlayer;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IIsolator;
import org.blockartistry.mod.DynSurround.compat.MCHelper;
import org.blockartistry.mod.DynSurround.util.XorShiftRandom;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A ILibrary that can also play sounds and default footsteps.
 * 
 * @author Hurry
 */
@SideOnly(Side.CLIENT)
public class AcousticsManager extends AcousticsLibrary implements ISoundPlayer, IStepPlayer {

	private static final Random RANDOM = new XorShiftRandom();

	private final PriorityQueue<PendingSound> pending = new PriorityQueue<PendingSound>();
	private final IIsolator isolator;

	public AcousticsManager(final IIsolator isolator) {
		this.isolator = isolator;
	}

	@Override
	public void playStep(final EntityLivingBase entity, final Association assos) {
		final IBlockState state = assos.getState();
		SoundType soundType = MCHelper.getSoundType(state);
		if (!state.getMaterial().isLiquid() && soundType != null) {

			if (EnvironState.getWorld().getBlockState(assos.getPos().up()).getBlock() == Blocks.SNOW_LAYER) {
				soundType = MCHelper.getSoundType(Blocks.SNOW_LAYER);
			}

			entity.playSound(soundType.getStepSound(), soundType.getVolume() * 0.15F, soundType.getPitch());
		}
	}

	@Override
	public void playSound(final Object location, final SoundEvent sound, final float volume, final float pitch,
			final IOptions options) {
		if (!(location instanceof Entity))
			return;

		if (options != null) {
			if (options.hasOption(Option.DELAY_MIN) && options.hasOption(Option.DELAY_MAX)) {
				long delay = randAB(RANDOM, (Long) options.getOption(Option.DELAY_MIN),
						(Long) options.getOption(Option.DELAY_MAX));

				pending.add(new PendingSound(location, sound, volume, pitch, null, System.currentTimeMillis() + delay,
						options.hasOption(Option.SKIPPABLE) ? -1 : (Long) options.getOption(Option.DELAY_MAX)));
			} else {
				actuallyPlaySound((Entity) location, sound, volume, pitch);
			}
		} else {
			actuallyPlaySound((Entity) location, sound, volume, pitch);
		}
	}

	protected void actuallyPlaySound(final Entity location, final SoundEvent sound, final float volume,
			final float pitch) {
		if (ModLog.DEBUGGING)
			ModLog.debug("    Playing sound " + sound.getSoundName() + " ("
					+ String.format(Locale.ENGLISH, "v%.2f, p%.2f", volume, pitch) + ")");
		location.playSound(sound, volume, pitch);
	}

	private long randAB(final Random rng, final long a, final long b) {
		return a >= b ? a : a + rng.nextInt((int) b + 1);
	}

	@Override
	public Random getRNG() {
		return RANDOM;
	}

	@Override
	public void think() {

		final long time = System.currentTimeMillis();

		while (!pending.isEmpty() && pending.peek().getTimeToPlay() <= time) {
			final PendingSound sound = pending.poll();
			if (!sound.isLate(time)) {
				sound.playSound(this);
			} else if (ModLog.DEBUGGING) {
				ModLog.debug("    Skipped late sound (late by " + sound.howLate(time) + "ms, tolerence is "
						+ sound.getLateTolerance() + "ms)");
			}
		}
	}

	@Override
	protected ISoundPlayer mySoundPlayer() {
		return isolator.getSoundPlayer();
	}
}