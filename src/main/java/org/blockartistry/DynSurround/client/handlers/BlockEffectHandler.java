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
import org.blockartistry.DynSurround.client.event.BlockUpdateEvent;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.handlers.scanners.AlwaysOnBlockEffectScanner;
import org.blockartistry.DynSurround.client.handlers.scanners.RandomBlockEffectScanner;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.lib.WorldUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/*
 * Based on doVoidParticles().
 */
@SideOnly(Side.CLIENT)
public class BlockEffectHandler extends EffectHandlerBase {

	protected final RandomBlockEffectScanner effects = new RandomBlockEffectScanner(ModOptions.specialEffectRange);
	protected final AlwaysOnBlockEffectScanner alwaysOn = new AlwaysOnBlockEffectScanner(ModOptions.specialEffectRange);

	@Override
	public String getHandlerName() {
		return "BlockEffectHandler";
	}

	@Override
	public void process(final World world, final EntityPlayer player) {
		this.effects.update();
		this.alwaysOn.update();

		if (EnvironState.isPlayerOnGround() && EnvironState.isPlayerMoving()) {
			final BlockPos pos = EnvironState.getPlayerPosition().down(1);
			final IBlockState state = WorldUtils.getBlockState(world, pos);
			final SoundEffect sound = getBlockRegistry().getStepSoundToPlay(state, RANDOM);
			if (sound != null)
				sound.doEffect(state, world, pos, RANDOM);
		}
	}

	@SubscribeEvent(receiveCanceled = false)
	public void onBlockUpdate(@Nonnull final BlockUpdateEvent event) {
		// Notify the always on cuboid scanner that a block has changed
		this.alwaysOn.onBlockUpdate(event.oldState, event.newState, event.pos, event.flags);
	}

	@Override
	public void onConnect() {
		MinecraftForge.EVENT_BUS.register(this.alwaysOn);
	}

	@Override
	public void onDisconnect() {
		MinecraftForge.EVENT_BUS.unregister(this.alwaysOn);
	}

}
