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

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.fx.particle.ParticleBreath;
import org.blockartistry.lib.effects.EntityEffect;
import org.blockartistry.lib.effects.IEntityEffectFactory;
import org.blockartistry.lib.effects.IEntityEffectFactoryFilter;
import org.blockartistry.lib.math.MathStuff;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FrostBreathEffect extends EntityEffect {

	@Override
	public void update() {
		if (!ModOptions.showBreath)
			return;

		final Optional<Entity> e = this.getState().subject();
		if (e.isPresent()) {
			final Entity entity = e.get();
			final int interval = (int) (((this.getState().getWorldTime()
					+ MathStuff.abs(entity.getPersistentID().hashCode())) / 10) % 8);
			if (interval < 3 && isPossibleToShow(entity)) {
				final EntityPlayer player = this.getState().thePlayer().get();
				if (!entity.isInvisibleToPlayer(player) && player.canEntityBeSeen(entity)) {
					this.getState().addParticle(new ParticleBreath(entity));
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

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = new IEntityEffectFactoryFilter() {
		@Override
		public boolean applies(@Nonnull final Entity e) {
			return e instanceof EntityPlayer || e instanceof EntityVillager;
		}
	};

	public static class Factory implements IEntityEffectFactory {

		// Since the effect has no state a singleton can be used.
		private static final List<EntityEffect> singleton = ImmutableList.of(new FrostBreathEffect());

		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity) {
			return singleton;
		}
	}

}
