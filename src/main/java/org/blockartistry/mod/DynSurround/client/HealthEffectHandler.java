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

package org.blockartistry.mod.DynSurround.client;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleCriticalPopOff;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleDamagePopOff;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleHealPopOff;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.mod.DynSurround.data.HealthData;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class HealthEffectHandler {

	private static final double DISTANCE_THRESHOLD_SQ = 32 * 32;

	private HealthEffectHandler() {
	}

	public static void handleEvent(final HealthData data) {
		if (!ModOptions.enableDamagePopoffs)
			return;

		// Don't want to display if too far away.
		final double distance = EnvironState.distanceToPlayer(data.posX, data.posY, data.posZ);
		if (distance >= DISTANCE_THRESHOLD_SQ)
			return;

		// Don't show the players pop-offs
		if (EnvironState.isPlayer(data.entityId))
			return;

		final World world = EnvironState.getWorld();

		if (data.isCritical) {
			ParticleHelper.addParticle(new ParticleCriticalPopOff(world, data.posX, data.posY, data.posZ));
		}

		if (data.amount > 0) {
			ParticleHelper.addParticle(new ParticleDamagePopOff(world, data.posX, data.posY, data.posZ, data.amount));
		} else if (data.amount < 0) {
			ParticleHelper.addParticle(
					new ParticleHealPopOff(world, data.posX, data.posY, data.posZ, MathHelper.abs_int(data.amount)));
		}
	}
}
