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

package org.orecruncher.dsurround.client.weather.wind;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A particle the wind can carry: every particle, through MixinParticle, which adds the drift to how far it moves.
 * Only those marked with {@link #dsurround$blowInWind} drift. Ported from the 26.x line.
 */
public interface IWindBlown {

	/**
	 * Marks the particle as one the wind carries
	 *
	 * @param exposure how open to the weather it is where it was made, from {@link Shelter#exposure}
	 */
	void dsurround$blowInWind(float exposure);

	/**
	 * Marks a particle as one the wind carries, as open to the weather as it is where it starts: indoors or in a cave
	 * it stays as it is. Nothing is marked with the option off, or off the client thread (Shelter's scratch state is
	 * the client thread's).
	 */
	static void blow(@Nonnull final Particle particle, @Nonnull final World world, final double x, final double y,
			final double z) {
		if (!ModOptions.effects.windBlowsParticles || !Minecraft.getMinecraft().isCallingFromMinecraftThread())
			return;
		final float exposure = Shelter.exposure(world, new BlockPos(x, y, z));
		if (exposure > 0F)
			((IWindBlown) particle).dsurround$blowInWind(exposure);
	}
}
