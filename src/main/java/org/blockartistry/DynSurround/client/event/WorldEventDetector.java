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

package org.blockartistry.DynSurround.client.event;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.fx.particle.ExplosionHelper;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Listener that looks for server events and turns them into Dynamic Surroundings
 * client events.  This is the way it can pick up on things like waterflow changes
 * and liquid being picked up/placed.
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class WorldEventDetector implements IWorldEventListener {
	
	protected final World world;
	
	public WorldEventDetector(@Nonnull final World world) {
		this.world = world;
	}

	@Override
	public void notifyBlockUpdate(@Nonnull final World worldIn, @Nonnull final BlockPos pos,
			@Nonnull final IBlockState oldState, @Nonnull final IBlockState newState, final int flags) {

		if (worldIn.provider.getDimension() == EnvironState.getDimensionId()) {
			final BlockUpdateEvent event = new BlockUpdateEvent(worldIn, pos, oldState, newState, flags);
			MinecraftForge.EVENT_BUS.post(event);
		}
	}

	@Override
	public void notifyLightSet(@Nonnull final BlockPos pos) {

	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {

	}

	@Override
	public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x,
			double y, double z, float volume, float pitch) {

	}

	@Override
	public void playRecord(SoundEvent soundIn, BlockPos pos) {

	}

	@Override
	public void spawnParticle(int particleID, boolean ignoreRange, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed, int... parameters) {
		
		if(!ModOptions.enableExplosionEnhancement)
			return;
		
		if(EnumParticleTypes.EXPLOSION_LARGE.getParticleID() == particleID) {
			ExplosionHelper.doExplosion(this.world, x, y, z);
		}
	}

	@Override
	public void onEntityAdded(Entity entityIn) {

	}

	@Override
	public void onEntityRemoved(Entity entityIn) {

	}

	@Override
	public void broadcastSound(int soundID, BlockPos pos, int data) {

	}

	@Override
	public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {

	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

	}

	@SubscribeEvent(receiveCanceled = false)
	public static void onWorldLoad(final WorldEvent.Load event) {
		// Only want client side world things
		if (!event.getWorld().isRemote)
			return;
		event.getWorld().addEventListener(new WorldEventDetector(event.getWorld()));
	}
}
