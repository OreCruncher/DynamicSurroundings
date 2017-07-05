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

package org.blockartistry.DynSurround.client.handlers;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.api.events.FootstepEvent;
import org.blockartistry.DynSurround.client.fx.ParticleCollections;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.registry.BlockRegistry;
import org.blockartistry.DynSurround.registry.FootstepsRegistry;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.lib.WorldUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FootstepsHandler extends EffectHandlerBase {

	protected final FootstepsRegistry footsteps;
	protected final BlockRegistry registry;

	public FootstepsHandler() {
		super("FootstepsHandler");

		this.footsteps = RegistryManager.get(RegistryType.FOOTSTEPS);
		this.registry = RegistryManager.<BlockRegistry>get(RegistryType.BLOCK);
}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {
		this.footsteps.process(world, player);
		
		if (EnvironState.isPlayerOnGround() && EnvironState.isPlayerMoving()) {
			final BlockPos pos = EnvironState.getPlayerPosition().down(1);
			final IBlockState state = WorldUtils.getBlockState(world, pos);
			final SoundEffect sound = this.registry.getStepSoundToPlay(state, RANDOM);
			if (sound != null)
				sound.doEffect(WorldUtils.getDefaultBlockStateProvider(), state, pos, RANDOM);
		}

	}

	@SubscribeEvent
	public void onDisplayFootstep(@Nonnull final FootstepEvent.Display event) {
		if (ModOptions.enableFootprints) {
			final Vec3d stepLoc = event.location;
			ParticleCollections.addFootprint(EnvironState.getWorld(), stepLoc.x, stepLoc.y, stepLoc.z,
					event.rotation, event.isRightFoot);
		}

	}
}
