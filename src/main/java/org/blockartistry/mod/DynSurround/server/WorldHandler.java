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

package org.blockartistry.mod.DynSurround.server;

import java.util.Random;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.data.DimensionEffectData;
import org.blockartistry.mod.DynSurround.data.DimensionEffectDataFile;
import org.blockartistry.mod.DynSurround.util.XorShiftRandom;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

public class WorldHandler {

	private static final float STRENGTH_ADJUST = 0.01F;
	private static final Random RANDOM = new XorShiftRandom();

	private static int nextThunderInterval(final boolean isThundering) {
		return RANDOM.nextInt(isThundering ? 12000 : 168000) + (isThundering ? 3600 : 12000);
	}

	private static int nextRainInterval(final boolean isRaining) {
		return RANDOM.nextInt(isRaining ? 12000 : 168000) + 12000;
	}

	public static void updateWeatherBody(final World world) {

		// If it is the client, or it has no sky, return.
		if (world.isRemote)
			return;

		final int dimensionId = world.provider.getDimension();
		final DimensionEffectData data = DimensionEffectDataFile.get(world);
		final WorldInfo info = world.getWorldInfo();

		// Interesting bit added to 1.8.9
		int i = info.getCleanWeatherTime();

		if (i > 0) {
			--i;
			info.setCleanWeatherTime(i);
			info.setThunderTime(info.isThundering() ? 1 : 2);
			info.setRainTime(info.isRaining() ? 1 : 2);
		}

		i = info.getThunderTime();

		if (i <= 0) {
			info.setThunderTime(nextThunderInterval(info.isThundering()));
		} else {
			--i;
			info.setThunderTime(i);

			if (i <= 0) {
				info.setThundering(!info.isThundering());
			}
		}

		world.prevThunderingStrength = world.thunderingStrength;
		world.thunderingStrength += info.isThundering() ? STRENGTH_ADJUST : -STRENGTH_ADJUST;
		world.thunderingStrength = MathHelper.clamp_float(world.thunderingStrength, 0.0F, 1.0F);

		int j = info.getRainTime();

		if (j <= 0) {
			info.setRainTime(nextRainInterval(info.isRaining()));
		} else {
			--j;
			info.setRainTime(j);

			if (j <= 0) {
				info.setRaining(!info.isRaining());
			}
		}

		if (info.isRaining() && data.getRainIntensity() == 0.0F) {
			data.randomizeRain();
			ModLog.debug(String.format("dim %d rain strength set to %f", dimensionId, data.getRainIntensity()));
		}

		world.prevRainingStrength = world.rainingStrength;
		if (info.isRaining()) {
			if (world.rainingStrength > data.getRainIntensity()) {
				world.rainingStrength -= STRENGTH_ADJUST;
				if (world.rainingStrength < 0.0F)
					world.rainingStrength = 0.0F;
			} else if (world.rainingStrength < data.getRainIntensity()) {
				world.rainingStrength += STRENGTH_ADJUST;
				if (world.rainingStrength > data.getRainIntensity())
					world.rainingStrength = data.getRainIntensity();
			}
		} else if (world.rainingStrength > 0.0F) {
			world.rainingStrength -= STRENGTH_ADJUST;
			if (world.rainingStrength < 0.0F)
				world.rainingStrength = 0.0F;
		} else if (data.getRainIntensity() > 0.0F) {
			data.setRainIntensity(0.0F);
			ModLog.debug(String.format("dim %d rain has stopped", dimensionId));
		}
	}

	public static boolean isRaining(final World world) {
		return (double) world.getRainStrength(1.0F) > 0.2D;
	}

	public static boolean isThundering(final World world) {
		final double strength = (double) world.getThunderStrength(1.0F);
		return world.isRemote ? strength > 0.9D : strength > DimensionEffectDataFile.get(world).getThunderThreshold();
	}

}
