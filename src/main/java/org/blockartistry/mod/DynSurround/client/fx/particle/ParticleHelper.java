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

package org.blockartistry.mod.DynSurround.client.fx.particle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.EnumParticleTypes;

public final class ParticleHelper {

	protected ParticleHelper() {

	}

	@Nullable
	public static Particle spawnParticle(@Nonnull final EnumParticleTypes particleId, double xCoord, double yCoord,
			double zCoord, double xSpeed, double ySpeed, double zSpeed) {
		return Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(particleId.getParticleID(), xCoord, yCoord,
				zCoord, xSpeed, ySpeed, zSpeed);
	}

	@Nullable
	public static Particle spawnParticle(@Nonnull final EnumParticleTypes particleId, double xCoord, double yCoord,
			double zCoord) {
		return spawnParticle(particleId, xCoord, yCoord, zCoord, 0, 0, 0);
	}

	@Nullable
	public static Particle spawnParticle(@Nonnull final EnumParticleTypes particleId, double xCoord, double yCoord,
			double zCoord, double ySpeed) {
		return spawnParticle(particleId, xCoord, yCoord, zCoord, 0, ySpeed, 0);
	}

	public static void addParticle(@Nonnull final Particle particle) {
		Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}

}
