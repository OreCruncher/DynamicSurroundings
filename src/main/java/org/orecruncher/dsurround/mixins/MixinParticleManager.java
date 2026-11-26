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

package org.orecruncher.dsurround.mixins;

import org.orecruncher.dsurround.client.weather.wind.IWindBlown;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

/**
 * Marks the vanilla particles the wind carries as they are made: this is the only place a particle's type is known.
 * Smoke, large smoke, clouds and the poof of an explosion or a death, as the 26.x line's wind blown tag has them.
 * Particles handed straight to {@code addEffect} (the mod's own) have none, so are never marked here.
 */
@Mixin(ParticleManager.class)
public abstract class MixinParticleManager {

	@Shadow
	protected World world;

	@Inject(method = "spawnEffectParticle(IDDDDDD[I)Lnet/minecraft/client/particle/Particle;", at = @At("RETURN"))
	private void dsurround$markWindBlown(final int particleId, final double x, final double y, final double z,
			final double dx, final double dy, final double dz, final int[] parameters,
			final CallbackInfoReturnable<Particle> cir) {
		final Particle particle = cir.getReturnValue();
		if (particle != null && this.world != null && isWindBlown(particleId))
			IWindBlown.blow(particle, this.world, x, y, z);
	}

	@Unique
	private static boolean isWindBlown(final int particleId) {
		return particleId == EnumParticleTypes.SMOKE_NORMAL.getParticleID()
				|| particleId == EnumParticleTypes.SMOKE_LARGE.getParticleID()
				|| particleId == EnumParticleTypes.CLOUD.getParticleID()
				|| particleId == EnumParticleTypes.EXPLOSION_NORMAL.getParticleID();
	}
}
