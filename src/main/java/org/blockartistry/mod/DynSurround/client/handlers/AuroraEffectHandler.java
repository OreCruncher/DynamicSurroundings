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

package org.blockartistry.mod.DynSurround.client.handlers;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.api.events.AuroraSpawnEvent;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.weather.Aurora;
import org.blockartistry.mod.DynSurround.data.AuroraData;
import org.blockartistry.mod.DynSurround.util.DiurnalUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public final class AuroraEffectHandler extends EffectHandlerBase {

	// Aurora information
	private static int auroraDimension = 0;
	private static final Set<AuroraData> auroras = new HashSet<AuroraData>();
	private static Aurora current;

	@Nullable
	public static Aurora getCurrentAurora() {
		return current;
	}

	@Nullable
	private Aurora getClosestAurora(@Nonnull final World world) {
		if (auroras.size() == 0)
			return current;

		AuroraData ad = null;

		final EntityPlayer player = EnvironState.getPlayer();
		final int playerX = (int) player.posX;
		final int playerZ = (int) player.posZ;
		boolean started = false;
		int distanceSq = 0;
		for (final AuroraData data : auroras) {
			final int deltaX = data.posX - playerX;
			final int deltaZ = data.posZ - playerZ;
			final int d = deltaX * deltaX + deltaZ * deltaZ;
			if (!started || distanceSq > d) {
				started = true;
				distanceSq = d;
				ad = data;
			}
		}

		if (ad == null) {
			current = null;
		} else if (current == null || (current.posX != ad.posX && current.posZ != ad.posZ)) {
			ModLog.debug("New aurora: " + ad.toString());
			current = new Aurora(ad);
		}

		return current;
	}

	@Override
	public void onConnect() {
		auroraDimension = 0;
		current = null;
		auroras.clear();
	}

	@Override
	public void onDisconnect() {
		auroraDimension = 0;
		current = null;
		auroras.clear();
	}

	@Override
	@Nonnull
	public String getHandlerName() {
		return "AuroraEffectHandler";
	}

	protected void scrubAuroraList(@Nonnull final World world) {
		if (EnvironState.getDimensionId() != auroraDimension) {
			auroras.clear();
			current = null;
			auroraDimension = EnvironState.getDimensionId();
		} else if (DiurnalUtils.isAuroraInvisible(world)) {
			auroras.clear();
		}

		if (current != null && current.isComplete()) {
			current = null;
		}
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {
		
		scrubAuroraList(world);
		final Aurora aurora = getClosestAurora(world);
		if (aurora != null) {
			aurora.update();
			if (aurora.isAlive() && DiurnalUtils.isAuroraInvisible(world)) {
				ModLog.debug("Aurora fade...");
				aurora.die();
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
	public void onAuroraSpawnEvent(final AuroraSpawnEvent event) {
		if (!ModOptions.auroraEnable)
			return;

		if (EnvironState.getDimensionId() == event.world.provider.getDimension()) {
			auroras.add(new AuroraData(event));
		}
	}
}
