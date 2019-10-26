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
package org.orecruncher.dsurround.client.handlers.effects;

import java.util.List;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.capabilities.CapabilitySeasonInfo;
import org.orecruncher.dsurround.client.effects.EntityEffect;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactory;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactoryFilter;
import org.orecruncher.dsurround.client.effects.IEntityEffectHandlerState;
import org.orecruncher.dsurround.client.fx.particle.ParticleBubbleBreath;
import org.orecruncher.dsurround.client.fx.particle.ParticleFrostBreath;
import org.orecruncher.dsurround.registry.effect.EntityEffectInfo;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BreathEffect extends EntityEffect {

	private static final int PRIME = 311;

	private int seed;

	@Override
	public String name() {
		return "Breath";
	}

	@Override
	public void intitialize(@Nonnull final IEntityEffectHandlerState state) {
		super.intitialize(state);
		this.seed = state.subject().getEntityId() * PRIME;
	}

	@Override
	public void update(@Nonnull final Entity subject) {
		if (!ModOptions.effects.showBreath)
			return;

		if (isBreathVisible(subject)) {
			final int c = (int) ((getState().getWorldTime() + this.seed));
			final IBlockState state = getHeadBlock(subject);
			if (showWaterBubbles(subject, state)) {
				final int air = subject.getAir();
				if (air > 0) {
					final int interval = c % 3;
					if (interval == 0) {
						final Particle particle = new ParticleBubbleBreath(subject);
						getState().addParticle(particle);
					}
				} else if (air == 0) {
					// Need to generate a bunch of bubbles due to drowning
					for (int i = 0; i < 8; i++) {
						final Particle particle = new ParticleBubbleBreath(subject, true);
						getState().addParticle(particle);
					}
				}
			} else {
				final int interval = (c / 10) % 8;
				if (interval < 3 && showFrostBreath(subject, state)) {
					getState().addParticle(new ParticleFrostBreath(subject));
				}
			}
		}
	}

	protected boolean isBreathVisible(@Nonnull final Entity entity) {
		final EntityPlayer player = getState().thePlayer();
		if (entity == player) {
			return !player.isSpectator();
		}
		return !entity.isInvisibleToPlayer(player) && player.canEntityBeSeen(entity);
	}

	protected IBlockState getHeadBlock(final Entity entity) {
		final double d0 = entity.posY + entity.getEyeHeight();
		final BlockPos blockpos = new BlockPos(entity.posX, d0, entity.posZ);
		return entity.getEntityWorld().getBlockState(blockpos);
	}

	protected boolean showWaterBubbles(final Entity entity, @Nonnull final IBlockState headBlock) {
		return headBlock.getMaterial().isLiquid();
	}

	protected boolean showFrostBreath(final Entity entity, @Nonnull final IBlockState headBlock) {
		if (headBlock.getMaterial() == Material.AIR) {
			final World world = entity.getEntityWorld();
			final BlockPos entityPos = entity.getPosition();
			return CapabilitySeasonInfo.getCapability(world).showFrostBreath(entityPos);
		}
		return false;
	}

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = (@Nonnull final Entity e,
			@Nonnull final EntityEffectInfo eei) -> eei.effects.contains("breath");

	public static class Factory implements IEntityEffectFactory {

		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity, @Nonnull final EntityEffectInfo eei) {
			return ImmutableList.of(new BreathEffect());
		}
	}

}
