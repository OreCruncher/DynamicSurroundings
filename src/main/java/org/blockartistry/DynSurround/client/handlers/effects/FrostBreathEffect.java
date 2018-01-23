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
package org.blockartistry.DynSurround.client.handlers.effects;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.fx.particle.ParticleBreath;
import org.blockartistry.lib.effects.IEffect;
import org.blockartistry.lib.effects.IEffectFactory;
import org.blockartistry.lib.effects.IEffectHandlerState;
import org.blockartistry.lib.effects.IFactoryFilter;
import org.blockartistry.lib.math.MathStuff;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FrostBreathEffect implements IEffect {

	@Override
	public void update(@Nonnull final IEffectHandlerState state) {
		if (!ModOptions.showBreath)
			return;

		final Optional<Entity> e = state.subject();
		if (e.isPresent()) {
			final Entity entity = e.get();
			final int interval = ((state.getCurrentTick() + MathStuff.abs(entity.getPersistentID().hashCode())) / 10)
					% 8;
			if (interval < 3 && isPossibleToShow(entity)) {
				final EntityPlayer player = state.thePlayer().get();
				if (!entity.isInvisibleToPlayer(player) && player.canEntityBeSeen(entity)) {
					state.addParticle(new ParticleBreath(entity));
				}
			}
		}
	}

	protected boolean isPossibleToShow(final Entity entity) {
		if (entity.isInsideOfMaterial(Material.AIR)) {
			final BlockPos entityPos = entity.getPosition();
			final float temp = entity.getEntityWorld().getBiome(entityPos).getFloatTemperature(entityPos);
			return temp < 0.2F;
		}
		return false;
	}

	public static final IFactoryFilter DEFAULT_FILTER = new IFactoryFilter() {
		@Override
		public boolean applies(@Nonnull final Entity e) {
			return e instanceof EntityPlayer || e instanceof EntityVillager;
		}
	};

	public static class Factory implements IEffectFactory {

		// Since the effect has no state a singleton can be used.
		private static final Optional<IEffect> singleton = Optional.of(new FrostBreathEffect());

		@Override
		public Optional<IEffect> create(@Nonnull final Entity entity) {
			return singleton;
		}
	}

}
