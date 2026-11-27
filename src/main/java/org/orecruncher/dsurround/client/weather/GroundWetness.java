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

package org.orecruncher.dsurround.client.weather;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.capabilities.CapabilitySeasonInfo;
import org.orecruncher.dsurround.capabilities.season.ISeasonInfo;
import org.orecruncher.dsurround.capabilities.season.PrecipitationType;
import org.orecruncher.dsurround.client.weather.wind.Shelter;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * How wet the ground is from the rain falling on it, as in the 26.x line: what footsteps share (and footprints, should
 * mud prints come over), so a step that squelches is on ground that would print mud. Read from the rain falling now:
 * dry the moment it stops.
 */
@SideOnly(Side.CLIENT)
public final class GroundWetness {

	/**
	 * From this wetness, ground that turns to mud (grass, dirt) is muddy: a squelching step
	 */
	public static final float MUDDY = 0.25F;

	private GroundWetness() {
	}

	/**
	 * How open a place is to rain: 0 under cover or where it doesn't rain (snow, a dry biome, dust), to 1 in the open
	 */
	public static float openToRain(@Nonnull final World world, @Nonnull final BlockPos pos) {
		final ISeasonInfo season = CapabilitySeasonInfo.getCapability(world);
		if (season == null || season.getPrecipitationType(pos, null) != PrecipitationType.RAIN)
			return 0F;
		return Shelter.exposure(world, pos);
	}

	/**
	 * How soaked the ground under a place is by the rain falling on it now, 0 to 1: how hard it rains, as far as the
	 * place is open to it
	 *
	 * @param pos the place above the ground: where feet are
	 */
	public static float at(@Nonnull final World world, @Nonnull final BlockPos pos) {
		final float falling = Weather.getIntensityLevel();
		return falling <= 0F ? 0F : Math.min(falling, 1F) * openToRain(world, pos);
	}
}
