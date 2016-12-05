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

package org.blockartistry.mod.DynSurround.client;

import java.util.HashSet;
import java.util.Set;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.aurora.Aurora;
import org.blockartistry.mod.DynSurround.data.AuroraData;
import org.blockartistry.mod.DynSurround.util.DiurnalUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public final class AuroraEffectHandler implements IClientEffectHandler {

	// Aurora information
	private static int auroraDimension = 0;
	private static final Set<AuroraData> auroras = new HashSet<AuroraData>();
	public static Aurora currentAurora;

	public static void addAurora(final AuroraData data) {
		if (!ModOptions.auroraEnable)
			return;

		synchronized (auroras) {
			if (auroraDimension != data.dimensionId || EnvironState.getDimensionId() != data.dimensionId) {
				auroras.clear();
				currentAurora = null;
				auroraDimension = data.dimensionId;
			}
			auroras.add(data);
		}
	}

	public AuroraEffectHandler() {
	}

	private Aurora getClosestAurora(final World world) {

		AuroraData ad = null;

		synchronized (auroras) {
			if (auroraDimension != EnvironState.getDimensionId() || DiurnalUtils.isDaytime(world)) {
				auroras.clear();
			}

			if (auroras.size() == 0) {
				currentAurora = null;
				return null;
			}

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
		}

		if (ad == null) {
			currentAurora = null;
		} else if (currentAurora == null || (currentAurora.posX != ad.posX && currentAurora.posZ != ad.posZ)) {
			ModLog.debug("New aurora: " + ad.toString());
			currentAurora = new Aurora(ad);
		}

		return currentAurora;
	}

	@Override
	public boolean hasEvents() {
		return false;
	}

	/*
	 * Need to get called every tick to process the dust fade timer as well as
	 * aurora processing.
	 */
	@Override
	public void process(final World world, final EntityPlayer player) {
		final Aurora aurora = getClosestAurora(world);
		if (aurora != null) {
			aurora.update();
			if (aurora.isAlive() && DiurnalUtils.isSunrise(world)) {
				ModLog.debug("Aurora fade...");
				aurora.die();
			}
		}
	}
}
