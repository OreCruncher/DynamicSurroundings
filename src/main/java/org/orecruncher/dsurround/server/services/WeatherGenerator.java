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

package org.orecruncher.dsurround.server.services;

import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.capabilities.CapabilityDimensionInfo;
import org.orecruncher.dsurround.capabilities.dimension.IDimensionInfoEx;
import org.orecruncher.dsurround.network.Network;
import org.orecruncher.dsurround.network.PacketThunder;
import org.orecruncher.dsurround.network.PacketWeatherUpdate;
import org.orecruncher.lib.PlayerUtils;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

public class WeatherGenerator {

	protected final Random RANDOM = XorShiftRandom.current();
	protected final World world;
	protected final IDimensionInfoEx data;

	public WeatherGenerator(@Nonnull final World world) {
		this.world = world;
		this.data = (IDimensionInfoEx) CapabilityDimensionInfo.getCapability(world);
	}

	@Nonnull
	public String name() {
		return "STANDARD";
	}

	protected int nextThunderEvent(final float rainIntensity) {
		final float scale = 2.0F - rainIntensity;
		return this.RANDOM.nextInt((int) (450 * scale)) + 300;
	}

	protected boolean doFlash(final float rainIntensity) {
		final int randee = (int) (rainIntensity * 100.0F);
		return this.RANDOM.nextInt(150) <= randee;
	}

	protected WorldInfo worldInfo() {
		return this.world.getWorldInfo();
	}

	protected void preProcess() {
		// A hook for a sub-class to do some figuring before the main
		// routines execute.
	}

	protected void doRain() {
		// Track our rain intensity values
		if (worldInfo().isRaining()) {
			// If our intensity is 0 it means that we need to establish
			// a strength.
			if (this.data.getRainIntensity() == 0.0F) {
				this.data.randomizeRain();
				ModBase.log().debug("dim %d rain intensity set to %f, duration %d ticks", this.data.getId(),
						this.data.getRainIntensity(), worldInfo().getRainTime());
			}
			this.data.setCurrentRainIntensity(this.world.getRainStrength(1.0F));
		} else {

			if (this.world.getRainStrength(1.0F) > 0.0F) {
				// It's not raining and the world has strength. Means
				// that it is on the way out so reflect the worlds strength.
				this.data.setCurrentRainIntensity(this.world.getRainStrength(1.0F));
			} else if (this.data.getCurrentRainIntensity() > 0) {
				// We get here the world is not raining and there is no
				// strength, but our record indicates something. Means we
				// stopped.
				this.data.setRainIntensity(0);
				this.data.setCurrentRainIntensity(0);
				ModBase.log().debug("dim %d rain has stopped, next rain %d ticks", this.data.getId(),
						worldInfo().getRainTime());
			} else if (this.data.getRainIntensity() > 0) {
				this.data.setRainIntensity(0);
			}
		}

	}

	protected void doAmbientThunder() {

		// If not enabled, return
		if (!ModOptions.rain.allowBackgroundThunder)
			return;

		// Gather the intensity for rain
		final float intensity = this.data.getCurrentRainIntensity();

		// If it is thundering and the intensity exceeds our threshold...
		if (worldInfo().isThundering() && intensity >= ModOptions.rain.stormThunderThreshold) {
			int time = this.data.getThunderTimer() - 1;
			if (time <= 0) {
				// If it is 0 we just counted down to this. If it were
				// the first time through it would be -1.
				if (time == 0) {
					// Get a random player in the dimension - they will be the
					// locus of the event. Center it at build height above
					// their head.
					final EntityPlayer player = PlayerUtils.getRandomPlayer(this.world);
					final float theY = this.data.getSkyHeight();
					if (player != null) {
						final PacketThunder packet = new PacketThunder(this.data.getId(), doFlash(intensity),
								new BlockPos(player.posX, theY, player.posZ));
						Network.sendToDimension(this.data.getId(), packet);
					}
				}
				// set new time
				time = nextThunderEvent(intensity);
			}
			this.data.setThunderTimer(time);

		} else {
			// Clear out the timer data for the next storm
			this.data.setThunderTimer(0);
		}
	}

	protected void postProcess() {
		// Hook for sub-classes to do processing after the main routines
		// execute.
	}

	public final void update() {
		process();
		sendUpdate();
	}

	protected void process() {
		preProcess();
		doRain();
		doAmbientThunder();
		postProcess();
	}

	protected void sendUpdate() {
		// Send the weather update to all players in the dimension.
		if (this.world.playerEntities.size() > 0) {
			final PacketWeatherUpdate packet = new PacketWeatherUpdate(this.data.getId(),
					this.data.getCurrentRainIntensity(), this.data.getRainIntensity(), worldInfo().getRainTime(),
					this.world.getThunderStrength(1.0F), worldInfo().getThunderTime(), this.data.getThunderTimer());
			Network.sendToDimension(this.data.getId(), packet);
		}
	}
}
