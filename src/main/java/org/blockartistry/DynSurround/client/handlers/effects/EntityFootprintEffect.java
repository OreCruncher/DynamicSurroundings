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
import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.footsteps.system.Generator;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.registry.BlockRegistry;
import org.blockartistry.DynSurround.registry.FootstepsRegistry;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.effects.EntityEffect;
import org.blockartistry.lib.effects.IEntityEffectFactory;
import org.blockartistry.lib.effects.IEntityEffectFactoryFilter;
import org.blockartistry.lib.effects.IEntityEffectHandlerState;
import org.blockartistry.lib.random.XorShiftRandom;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityFootprintEffect extends EntityEffect {

	protected static final Random RANDOM = XorShiftRandom.current();

	protected final FootstepsRegistry footsteps = RegistryManager.<FootstepsRegistry>get(RegistryType.FOOTSTEPS);
	protected final BlockRegistry registry = RegistryManager.<BlockRegistry>get(RegistryType.BLOCK);;

	protected Generator generator;

	public EntityFootprintEffect() {

	}

	public void intitialize(@Nonnull final IEntityEffectHandlerState state) {
		super.intitialize(state);

		this.generator = this.footsteps.createGenerator((EntityLivingBase) state.subject().get());
	}

	protected boolean isMoving(@Nonnull final Entity entity) {
		return entity.distanceWalkedModified != entity.prevDistanceWalkedModified;
	}

	@Override
	public void update() {
		final EntityPlayer player = this.getState().thePlayer().get();
		if (player == null)
			return;

		this.generator.generateFootsteps(player);

		if (player.onGround && isMoving(player)) {
			final BlockPos pos = player.getPosition().down(1);
			final IBlockState bs = WorldUtils.getBlockState(player.worldObj, pos);
			final SoundEffect sound = this.registry.getStepSoundToPlay(bs, RANDOM);
			if (sound != null)
				sound.doEffect(WorldUtils.getDefaultBlockStateProvider(), bs, pos, RANDOM);
		}
	}

	// Currently restricted to the active player. Have stuff to unwind in the
	// footprint code.
	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = new IEntityEffectFactoryFilter() {
		@Override
		public boolean applies(@Nonnull final Entity e) {
			return EnvironState.isPlayer(e);
		}
	};

	public static class Factory implements IEntityEffectFactory {

		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity) {
			return ImmutableList.of(new EntityFootprintEffect());
		}
	}

}
