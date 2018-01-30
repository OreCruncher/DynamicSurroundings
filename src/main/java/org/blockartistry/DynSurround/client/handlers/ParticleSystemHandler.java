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
import org.blockartistry.DynSurround.api.effects.BlockEffectType;
import org.blockartistry.DynSurround.api.events.BlockEffectEvent;
import org.blockartistry.DynSurround.client.fx.particle.system.ParticleSystem;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.BlockPosHelper;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSystemHandler extends EffectHandlerBase {

	public static ParticleSystemHandler INSTANCE;

	private final TLongObjectHashMap<ParticleSystem> systems = new TLongObjectHashMap<ParticleSystem>();

	public ParticleSystemHandler() {
		super("ParticleSystemHandler");
		INSTANCE = this;
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {

		if (this.systems.size() == 0)
			return;

		final double range = ModOptions.general.specialEffectRange;
		final BlockPos min = EnvironState.getPlayerPosition().add(-range, -range, -range);
		final BlockPos max = EnvironState.getPlayerPosition().add(range, range, range);

		final TLongObjectIterator<ParticleSystem> itr = this.systems.iterator();
		while (itr.hasNext()) {
			itr.advance();

			// If it is out of range expire, else update
			if (!BlockPosHelper.contains(itr.value().getPos(), min, max)) {
				itr.value().setExpired();
			} else {
				itr.value().onUpdate();
			}

			// If it's dead remove from the list
			if (!itr.value().isAlive())
				itr.remove();
		}
	}

	@Override
	public void onConnect() {
		this.systems.clear();
	}

	@Override
	public void onDisconnect() {
		this.systems.clear();
	}

	private static boolean interestingEvent(final BlockEffectEvent event) {
		return event.effect != BlockEffectType.FIREFLY;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onBlockEffectEvent(@Nonnull final BlockEffectEvent event) {
		if (interestingEvent(event) && !okToSpawn(event.location))
			event.setCanceled(true);
	}

	// Determines if it is OK to spawn a particle system at the specified
	// location. Generally only a single system can occupy a block.
	private boolean okToSpawn(@Nonnull final BlockPos pos) {
		return !this.systems.containsKey(pos.toLong());
	}

	public void addSystem(@Nonnull final ParticleSystem system) {
		this.systems.put(system.getPos().toLong(), system);
	}

}
