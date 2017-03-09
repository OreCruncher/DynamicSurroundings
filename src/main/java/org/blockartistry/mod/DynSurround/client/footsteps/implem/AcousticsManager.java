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

package org.blockartistry.mod.DynSurround.client.footsteps.implem;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.footsteps.interfaces.EventType;
import org.blockartistry.mod.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.mod.DynSurround.client.footsteps.interfaces.IOptions;
import org.blockartistry.mod.DynSurround.client.footsteps.interfaces.ISoundPlayer;
import org.blockartistry.mod.DynSurround.client.footsteps.interfaces.IStepPlayer;
import org.blockartistry.mod.DynSurround.client.footsteps.interfaces.IOptions.Option;
import org.blockartistry.mod.DynSurround.client.footsteps.system.Association;
import org.blockartistry.mod.DynSurround.client.footsteps.system.Isolator;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleFootprint;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleFootprint.Style;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.util.BlockPosHelper;
import org.blockartistry.mod.DynSurround.util.MCHelper;
import org.blockartistry.mod.DynSurround.util.TimeUtils;
import org.blockartistry.mod.DynSurround.util.WorldUtils;

import gnu.trove.map.hash.THashMap;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A ILibrary that can also play sounds and default footsteps.
 * 
 * @author Hurry
 */
@SideOnly(Side.CLIENT)
public class AcousticsManager implements ISoundPlayer, IStepPlayer {

	private final Random RANDOM = new Random();

	private final THashMap<String, IAcoustic> acoustics = new THashMap<String, IAcoustic>();
	private final ArrayDeque<PendingSound> pending = new ArrayDeque<PendingSound>();
	private final Isolator isolator;
	private BlockPos.MutableBlockPos stepCheck = new BlockPos.MutableBlockPos();

	// Special sentinels for equating
	public static final IAcoustic[] EMPTY = {};
	public static final IAcoustic[] NOT_EMITTER = { new BasicAcoustic("NOT_EMITTER") };
	public static final IAcoustic[] MESSY_GROUND = { new BasicAcoustic("MESSY_GROUND") };
	public static IAcoustic[] SWIM;

	public AcousticsManager(@Nonnull final Isolator isolator) {
		this.isolator = isolator;
	}

	public void addAcoustic(@Nonnull final IAcoustic acoustic) {
		this.acoustics.put(acoustic.getAcousticName(), acoustic);
	}

	@Nullable
	public IAcoustic getAcoustic(@Nonnull final String name) {
		return this.acoustics.get(name);
	}

	public void playAcoustic(@Nonnull final Object location, @Nonnull final Association acousticName,
			@Nonnull final EventType event) {
		playAcoustic(location, acousticName.getData(), event, null);

		if (acousticName.hasFootstepImprint()) {
			final Vec3d stepLoc = acousticName.getStepLocation();
			if (stepLoc != null && WorldUtils.isSolidBlock(EnvironState.getWorld(),
					BlockPosHelper.setPos(this.stepCheck, stepLoc).move(EnumFacing.DOWN, 1))) {
				final Style style = Style.getStyle(ModOptions.footprintStyle);
				ParticleHelper.addParticle(new ParticleFootprint(EnvironState.getWorld(), stepLoc.xCoord,
						stepLoc.yCoord, stepLoc.zCoord, acousticName.getRotation(), acousticName.isRightFoot(), style));
			}
		}
	}

	public void playAcoustic(@Nonnull final Object location, @Nonnull final IAcoustic[] acoustics,
			@Nonnull final EventType event, @Nullable final IOptions inputOptions) {
		if (acoustics == null || acoustics.length == 0) {
			ModLog.debug("Attempt to play acoustic with no name");
			return;
		}

		if (ModLog.DEBUGGING) {
			final StringBuilder builder = new StringBuilder();
			boolean doComma = false;
			for (final IAcoustic acoustic : acoustics) {
				if (doComma)
					builder.append(",");
				else
					doComma = true;
				builder.append(acoustic.getAcousticName());
			}
			ModLog.debug("  Playing acoustic " + builder.toString() + " for event " + event.toString().toUpperCase());
		}

		for (final IAcoustic acoustic : acoustics) {
			acoustic.playSound(mySoundPlayer(), location, event, inputOptions);
		}
	}

	@Nonnull
	public IAcoustic[] compileAcoustics(@Nonnull final String acousticName) {
		if (acousticName.equals("NOT_EMITTER"))
			return NOT_EMITTER;
		else if (acousticName.equals("MESSY_GROUND"))
			return MESSY_GROUND;

		final List<IAcoustic> acoustics = new ArrayList<IAcoustic>();

		final String fragments[] = acousticName.split(",");
		for (final String fragment : fragments) {
			final IAcoustic acoustic = this.acoustics.get(fragment);
			if (acoustic == null) {
				ModLog.warn("Acoustic '%s' not found!", fragment);
			} else {
				acoustics.add(acoustic);
			}
		}

		return acoustics.size() == 0 ? EMPTY : acoustics.toArray(new IAcoustic[acoustics.size()]);
	}

	@Override
	public void playStep(@Nonnull final EntityLivingBase entity, @Nonnull final Association assos) {
		try {
			SoundType soundType = assos.getSoundType();
			if (!assos.isLiquid() && assos.getSoundType() != null) {

				if (EnvironState.getWorld().getBlockState(assos.getPos().up()).getBlock() == Blocks.SNOW_LAYER) {
					soundType = MCHelper.getSoundType(Blocks.SNOW_LAYER);
				}

				entity.playSound(soundType.getStepSound(), soundType.getVolume() * 0.15F, soundType.getPitch());
			}
		} catch (final Throwable t) {
			ModLog.error("Unable to play step sound", t);
		}
	}

	@Override
	public void playSound(@Nonnull final Object location, @Nonnull final SoundEvent sound, final float volume,
			final float pitch, @Nullable final IOptions options) {
		if (!(location instanceof Entity))
			return;

		try {
			if (options != null) {
				if (options.hasOption(Option.DELAY_MIN) && options.hasOption(Option.DELAY_MAX)) {
					final long delay = TimeUtils.currentTimeMillis()
							+ randAB(RANDOM, options.asLong(Option.DELAY_MIN), options.asLong(Option.DELAY_MAX));
					this.pending.add(new PendingSound(location, sound, volume, pitch, null, delay,
							options.hasOption(Option.SKIPPABLE) ? -1 : options.asLong(Option.DELAY_MAX)));
				} else {
					actuallyPlaySound((Entity) location, sound, volume, pitch);
				}
			} else {
				actuallyPlaySound((Entity) location, sound, volume, pitch);
			}
		} catch (final Throwable t) {
			ModLog.error("Unable to play sound", t);
		}
	}

	protected void actuallyPlaySound(@Nonnull final Entity location, @Nonnull final SoundEvent sound,
			final float volume, final float pitch) {
		if (ModLog.DEBUGGING)
			ModLog.debug("    Playing sound " + sound.getSoundName() + " ("
					+ String.format(Locale.ENGLISH, "v%.2f, p%.2f", volume, pitch) + ")");

		try {
			location.playSound(sound, volume, pitch);
		} catch (final Throwable t) {
			ModLog.error("Unable to play sound", t);
		}
	}

	private long randAB(@Nonnull final Random rng, final long a, final long b) {
		return a >= b ? a : a + rng.nextInt((int) (b + 1));
	}

	@Override
	@Nonnull
	public Random getRNG() {
		return RANDOM;
	}

	public void think() {

		final long time = TimeUtils.currentTimeMillis();

		while (!this.pending.isEmpty() && this.pending.peek().getTimeToPlay() <= time) {
			final PendingSound sound = this.pending.poll();
			if (!sound.isLate(time)) {
				sound.playSound(this);
			} else if (ModLog.DEBUGGING) {
				ModLog.debug("    Skipped late sound (late by " + sound.howLate(time) + "ms, tolerence is "
						+ sound.getLateTolerance() + "ms)");
			}
		}
	}

	@Nonnull
	protected ISoundPlayer mySoundPlayer() {
		return isolator.getSoundPlayer();
	}
}