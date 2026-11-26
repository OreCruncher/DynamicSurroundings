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
import org.orecruncher.dsurround.client.weather.wind.SceneWind;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.particle.Particle;

/**
 * Carries wind blown particles downwind (ported from the 26.x line). The drift is added to how far the particle moves,
 * not to its speed: a particle's drag can't be relied on, and going through its move keeps it stopping at walls.
 * Particles that place themselves rather than move aren't touched, nor any not marked (see {@link IWindBlown#blow}).
 */
@Mixin(Particle.class)
public abstract class MixinParticle implements IWindBlown {

	@Shadow
	protected double posX;
	@Shadow
	protected double posZ;

	// 0 for particles the wind doesn't move: all but those marked
	@Unique
	private float dsurround$windExposure;
	@Unique
	private float dsurround$driftX;
	@Unique
	private float dsurround$driftZ;

	@Override
	public void dsurround$blowInWind(final float exposure) {
		this.dsurround$windExposure = exposure;
	}

	@ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private double dsurround$driftX(final double dx) {
		if (this.dsurround$windExposure == 0F)
			return dx;
		this.dsurround$driftX = SceneWind.drift(this.dsurround$driftX, SceneWind.x(this.posX, this.posZ),
				this.dsurround$windExposure);
		return dx + this.dsurround$driftX;
	}

	@ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), argsOnly = true, ordinal = 2)
	private double dsurround$driftZ(final double dz) {
		if (this.dsurround$windExposure == 0F)
			return dz;
		this.dsurround$driftZ = SceneWind.drift(this.dsurround$driftZ, SceneWind.z(this.posX, this.posZ),
				this.dsurround$windExposure);
		return dz + this.dsurround$driftZ;
	}
}
