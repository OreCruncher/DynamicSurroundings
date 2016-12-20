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

package org.blockartistry.mod.DynSurround.server.services;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.data.AuroraData;
import org.blockartistry.mod.DynSurround.data.AuroraPreset;
import org.blockartistry.mod.DynSurround.data.ColorPair;
import org.blockartistry.mod.DynSurround.data.DimensionEffectData;
import org.blockartistry.mod.DynSurround.network.Network;
import org.blockartistry.mod.DynSurround.registry.DimensionRegistry;
import org.blockartistry.mod.DynSurround.util.DiurnalUtils;
import org.blockartistry.mod.DynSurround.util.PlayerUtils;

import gnu.trove.map.hash.TIntIntHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public final class AuroraService {

	// Minimum distance between auroras, squared
	private static final long MIN_AURORA_DISTANCE_SQ = 400 * 400;

	public static void initialize() {
		if (ModOptions.auroraEnable)
			MinecraftForge.EVENT_BUS.register(new AuroraService());
	}

	@SubscribeEvent
	public void tickEvent(@Nonnull final TickEvent.WorldTickEvent event) {
		if (event.phase == Phase.END)
			processAuroras(event);
	}

	private boolean isAuroraInRange(@Nonnull final EntityPlayerMP player, @Nonnull final Set<AuroraData> data) {
		for (final AuroraData aurora : data) {
			if (aurora.distanceSq(player, -ModOptions.auroraSpawnOffset) <= MIN_AURORA_DISTANCE_SQ)
				return true;
		}

		return false;
	}

	/*
	 * Only OK to spawn an aurora when it is night time and the moon brightness
	 * is less than half full.
	 */
	private boolean okToSpawnAurora(@Nonnull final World world) {
		return DiurnalUtils.isNighttime(world);
	}

	private static final int CHECK_INTERVAL = 100; // Ticks
	private final TIntIntHashMap tickCounters = new TIntIntHashMap();

	protected void processAuroras(@Nonnull final TickEvent.WorldTickEvent event) {

		final World world = event.world;
		if (world == null || !DimensionRegistry.hasAuroras(world))
			return;

		final Set<AuroraData> data = DimensionEffectData.get(world).getAuroraList();

		// Daylight hours clear the aurora list
		if (DiurnalUtils.isDaytime(world)) {
			data.clear();
		} else {
			final int tickCount = tickCounters.get(world.provider.getDimension()) + 1;
			tickCounters.put(world.provider.getDimension(), tickCount);
			if (tickCount % CHECK_INTERVAL == 0) {
				if (okToSpawnAurora(world)) {

					final List<EntityPlayerMP> players = event.world.getMinecraftServer().getPlayerList()
							.getPlayerList();

					for (final EntityPlayerMP player : players) {
						if (!PlayerUtils.getPlayerBiome(player, false).getHasAurora())
							continue;
						if (isAuroraInRange(player, data))
							continue;

						final int colorSet = ColorPair.randomId();
						final int preset = AuroraPreset.randomId();
						// final int colorSet = ColorPair.testId();
						// final int preset = AuroraPreset.testId();
						final AuroraData aurora = new AuroraData(player, -ModOptions.auroraSpawnOffset, colorSet,
								preset);
						if (data.add(aurora)) {
							ModLog.debug("Spawned new aurora: " + aurora.toString());
						}
					}
				}

				for (final AuroraData a : data) {
					Network.sendAurora(a, world.provider.getDimension());
				}
			}
		}
	}

	@Nonnull
	public static String getAuroraData(@Nonnull final EntityPlayer player) {
		final StringBuilder builder = new StringBuilder();
		final Set<AuroraData> data = DimensionEffectData.get(player.getEntityWorld()).getAuroraList();
		if (data.size() == 0) {
			builder.append("NO AURORAS");
		} else {
			for (final AuroraData ad : data) {
				builder.append(ad.toString()).append("\n");
			}
		}
		return builder.toString();
	}
}
