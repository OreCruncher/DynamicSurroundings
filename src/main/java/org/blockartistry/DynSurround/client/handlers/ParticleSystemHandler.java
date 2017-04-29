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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.api.effects.BlockEffectType;
import org.blockartistry.DynSurround.api.events.BlockEffectEvent;
import org.blockartistry.DynSurround.client.fx.particle.system.ParticleSystem;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.BlockPosHelper;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSystemHandler extends EffectHandlerBase {

	public static ParticleSystemHandler INSTANCE;

	private final Map<BlockPos, ParticleSystem> systems = new HashMap<BlockPos, ParticleSystem>();

	public ParticleSystemHandler() {
		INSTANCE = this;
	}

	@Override
	@Nonnull
	public String getHandlerName() {
		return "ParticleSystemHandler";
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {

		if(this.systems.size() == 0)
			return;

		// Process the system list.  Remove entries that have expired
		// or gone out of range; trigger and update() on remaining ones.
		// Note that particles will be updated via the Minecraft
		// particle manager or a ParticleCollection.
		final double range = ModOptions.specialEffectRange;
		final BlockPos min = EnvironState.getPlayerPosition().add(-range, -range, -range);
		final BlockPos max = EnvironState.getPlayerPosition().add(range, range, range);

		Iterables.removeIf(this.systems.values(), new Predicate<ParticleSystem>() {
			@Override
			public boolean apply(@Nonnull final ParticleSystem input) {
				if (!input.isAlive())
					return true;
				if (!BlockPosHelper.contains(input.getPos(), min, max)) {
					input.setExpired();
					return true;
				}
				input.onUpdate();
				return !input.isAlive();
			}
		});
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
		return !this.systems.containsKey(pos);
	}

	public void addSystem(@Nonnull final ParticleSystem system) {
		this.systems.put(system.getPos(), system);
	}

}
