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

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.fx.particle.ParticleBreath;
import org.blockartistry.DynSurround.registry.EntityEffectInfo;
import org.blockartistry.lib.effects.EntityEffect;
import org.blockartistry.lib.effects.IEntityEffectFactory;
import org.blockartistry.lib.effects.IEntityEffectFactoryFilter;
import org.blockartistry.lib.effects.IEntityEffectHandlerState;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FrostBreathEffect extends EntityEffect {

	private static final int PRIME = 311;

	private int seed;

	@Override
	public String name() {
		return "Frost Breath";
	}

	@Override
	public void intitialize(@Nonnull final IEntityEffectHandlerState state) {
		super.intitialize(state);
		this.seed = state.subject().get().getEntityId() * PRIME;
	}

	@Override
	public void update(@Nonnull final Entity subject) {
		if (!ModOptions.player.showBreath)
			return;

		final int interval = (int) (((getState().getWorldTime() + this.seed) / 10) % 8);
		if (interval < 3 && isPossibleToShow(subject)) {
			final EntityPlayer player = getState().thePlayer();
			if ((subject == player) || (!subject.isInvisibleToPlayer(player) && player.canEntityBeSeen(subject))) {
				getState().addParticle(new ParticleBreath(subject));
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

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = (@Nonnull final Entity e,
			@Nonnull final EntityEffectInfo eei) -> eei.effects.contains("breath");

	public static class Factory implements IEntityEffectFactory {

		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity, @Nonnull final EntityEffectInfo eei) {
			return ImmutableList.of(new FrostBreathEffect());
		}
	}

}
