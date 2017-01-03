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

package org.blockartistry.mod.DynSurround.server.services;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.ModEnvironment;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.data.DimensionEffectData;
import org.blockartistry.mod.DynSurround.network.Network;
import org.blockartistry.mod.DynSurround.registry.DimensionRegistry;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.mod.DynSurround.util.PlayerUtils;
import org.blockartistry.mod.DynSurround.util.XorShiftRandom;

public final class AtmosphereService extends Service {

	private static final Random RANDOM = new XorShiftRandom();

	private static int nextThunderInterval(final boolean isThundering) {
		final int base = isThundering ? ModOptions.stormActiveTimeConst : ModOptions.stormInactiveTimeConst;
		return base + RANDOM
				.nextInt(isThundering ? ModOptions.stormActiveTimeVariable : ModOptions.stormInactiveTimeVariable);
	}

	private static int nextRainInterval(final boolean isRaining) {
		final int base = isRaining ? ModOptions.rainActiveTimeConst : ModOptions.rainInactiveTimeConst;
		return base
				+ RANDOM.nextInt(isRaining ? ModOptions.rainActiveTimeVariable : ModOptions.rainInactiveTimeVariable);
	}

	private static int nextThunderEvent(final float rainIntensity) {
		final float scale = 2.0F - rainIntensity;
		return RANDOM.nextInt((int) (450 * scale)) + 300;
	}

	private static boolean doFlash(final float rainIntensity) {
		final int randee = (int)(rainIntensity * 100.0F);
		return RANDOM.nextInt(150) <= randee;
	}

	private final DimensionRegistry dimensions = RegistryManager.get(RegistryType.DIMENSION);

	AtmosphereService() {
		super("AtmosphereService");
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void tickEvent(@Nonnull final TickEvent.WorldTickEvent event) {

		if (!ModOptions.enableWeatherASM || ModEnvironment.Weather2.isLoaded())
			return;

		if (event.side != Side.SERVER || event.phase == Phase.START)
			return;

		final World world = event.world;
		final DimensionEffectData data = DimensionEffectData.get(world);

		if (!this.dimensions.hasWeather(world))
			return;

		// If we get here and the world has no sky we have a dimension
		// like the Nether. We need to turn the crank manually to get
		// Minecraft to do what we need.
		if (world.provider.getHasNoSky()) {
			world.provider.hasNoSky = false;
			try {
				world.updateWeatherBody();
			} catch (final Throwable t) {
				;
			}
			world.provider.hasNoSky = true;
		}

		final WorldInfo info = world.getWorldInfo();

		// Tackle the rain and thunder timers. We toggle on remaining
		// time of 2 since updateWeatherBody() triggers on 0. We
		// want to control the timers.

		final int rain = info.getRainTime();
		if (rain <= 2) {
			info.setRaining(!info.isRaining());
			info.setRainTime(nextRainInterval(info.isRaining()));
		}

		final int thunder = info.getThunderTime();
		if (thunder <= 2) {
			info.setThundering(!info.isThundering());
			info.setThunderTime(nextThunderInterval(info.isThundering()));
		}

		// Track our rain intensity values
		if (info.isRaining()) {
			// If our intensity is 0 it means that we need to establish
			// a strength.
			if (data.getRainIntensity() == 0.0F) {
				data.randomizeRain();
				info.setRainTime(nextRainInterval(true));
				ModLog.debug("dim %d rain intensity set to %f, duration %d ticks", data.getDimensionId(),
						data.getRainIntensity(), info.getRainTime());
			}
			data.setCurrentRainIntensity(world.getRainStrength(1.0F));
		} else if (world.getRainStrength(1.0F) > 0.0F) {
			// It's not raining and the world has strength. Means that it is on
			// the way out so reflect the worlds strength.
			data.setCurrentRainIntensity(world.getRainStrength(1.0F));
		} else if (data.getCurrentRainIntensity() > 0) {
			// We get here the world is not raining and there is no strength,
			// but our record indicates something. Means we stopped.
			data.setRainIntensity(0);
			data.setCurrentRainIntensity(0);
			info.setRainTime(nextRainInterval(false));
			ModLog.debug("dim %d rain has stopped, next rain %d ticks", data.getDimensionId(),
					world.getWorldInfo().getRainTime());
		}

		// Set the rain rainIntensity for all players in the current
		// dimension.
		Network.sendRainIntensity(data.getCurrentRainIntensity(), data.getRainIntensity(), data.getDimensionId());

		if (info.isThundering() && data.getCurrentRainIntensity() >= ModOptions.stormThunderThreshold) {

			int time = data.getThunderTimer() - 1;
			if (time <= 0) {
				// If it is 0 we just counted down to this. If it were
				// the first time through it would be -1.
				if (time == 0) {
					// Get a random player in the dimension - they will be the
					// locus of the event.  Center it at build height above
					// their head.
					final EntityPlayer player = PlayerUtils.getRandomPlayer(world);
					final float theY = this.dimensions.getSkyHeight(world);
					if (player != null) {
						Network.sendThunder(data.getDimensionId(), doFlash(data.getCurrentRainIntensity()),
								(float) player.posX, (float) theY, (float) player.posZ);
					}
				}
				// set new time
				time = nextThunderEvent(data.getCurrentRainIntensity());
			}
			data.setThunderTimer(time);

		} else {
			// Clear out the timer data for the next storm
			data.setThunderTimer(0);
		}
	}

}
