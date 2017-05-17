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

package org.blockartistry.DynSurround.server.services;

import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.network.Network;
import org.blockartistry.DynSurround.network.PacketEnvironment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.village.VillageCollection;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

public final class EnvironmentService extends Service {

	EnvironmentService() {
		super("EnvironmentService");
	}

	@SubscribeEvent
	public void tickEvent(@Nonnull final TickEvent.PlayerTickEvent event) {
		if (event.phase == Phase.END && event.side == Side.SERVER) {
			final EntityPlayer player = event.player;
			final VillageCollection villageCollection = player.getEntityWorld().getVillageCollection();
			boolean inVillage = false;

			if (villageCollection != null) {
				final List<Village> villages = villageCollection.getVillageList();
				if (villages != null && villages.size() > 0) {
					final BlockPos pos = player.getPosition();
					for (final Village v : villages)
						if (v.isBlockPosWithinSqVillageRadius(pos)) {
							inVillage = true;
							break;
						}
				}
			}

			final PacketEnvironment packet = new PacketEnvironment(inVillage);
			Network.sendToPlayer((EntityPlayerMP) player, packet);
		}
	}

}
