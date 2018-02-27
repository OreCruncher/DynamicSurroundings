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
package org.blockartistry.DynSurround.client;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.lib.chunk.DynamicChunkCache;
import org.blockartistry.lib.chunk.IBlockAccessEx;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

/**
 * The ClientChunkCache caches chunks within a certain range the the player. The
 * cache is updated once per client tick. If the chunk region changes the cache
 * is updated with the appropriate chunks. The cache will be wiped when a player
 * disconnects or unloads a session.
 *
 * NOTE: Fog scanning uses a range of 20, so if the ranges get adjusted in this
 * cache make sure it is checked with the fog calculators.
 */
@Mod.EventBusSubscriber(value = Side.CLIENT)
public final class ClientChunkCache {

	public static final IBlockAccessEx INSTANCE = new DynamicChunkCache();

	// Figure the block range from the player. The area scanners are up to 32
	// blocks, but the player may have a configured effect range that is
	// greater. Add a couple blocks because some of the effects look at
	// neighbor blocks.
	private static int range() {
		int r = Math.max(32, ModOptions.general.specialEffectRange);
		r = Math.max(r, ModOptions.lightlevel.llBlockRange);
		return r + 2;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void clientTick(@Nonnull final TickEvent.ClientTickEvent event) {
		if (event.side == Side.CLIENT && event.phase == Phase.START) {
			final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			if (player != null) {
				final int range = range();
				final BlockPos pos = new BlockPos(player.getPosition());
				final BlockPos min = pos.add(-range, -range, -range);
				final BlockPos max = pos.add(range, range, range);
				((DynamicChunkCache) INSTANCE).update(player.getEntityWorld(), min, max);
			} else {
				// If there is no player reference wipe the cache to ensure resources
				// are freed.
				((DynamicChunkCache) ClientChunkCache.INSTANCE).clear();
			}
		}

	}
}
