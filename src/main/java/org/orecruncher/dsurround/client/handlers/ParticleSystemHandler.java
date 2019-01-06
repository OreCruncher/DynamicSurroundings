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

package org.orecruncher.dsurround.client.handlers;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.fx.particle.system.ParticleSystem;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.lib.BlockPosHelper;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSystemHandler extends EffectHandlerBase {

	private static ParticleSystemHandler _instance = null;

	private final Object2ObjectOpenHashMap<BlockPos, ParticleSystem> systems = new Object2ObjectOpenHashMap<>();

	public ParticleSystemHandler() {
		super("Particle Systems");
	}

	@Override
	public boolean doTick(final int tick) {
		return !this.systems.isEmpty();
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {
		final double range = ModOptions.effects.specialEffectRange;
		final BlockPos min = EnvironState.getPlayerPosition().add(-range, -range, -range);
		final BlockPos max = EnvironState.getPlayerPosition().add(range, range, range);

		this.systems.object2ObjectEntrySet().removeIf(entry -> {
			final ParticleSystem system = entry.getValue();
			if (BlockPosHelper.notContains(system.getPos(), min, max)) {
				system.setExpired();
			} else {
				system.onUpdate();
			}
			return !system.isAlive();
		});
	}

	@Override
	public void onConnect() {
		_instance = this;
		this.systems.clear();
	}

	@Override
	public void onDisconnect() {
		this.systems.clear();
		_instance = null;
	}

	// Determines if it is OK to spawn a particle system at the specified
	// location. Generally only a single system can occupy a block.
	public static boolean okToSpawn(@Nonnull final BlockPos pos) {
		return !_instance.systems.containsKey(pos);
	}

	public static void addSystem(@Nonnull final ParticleSystem system) {
		_instance.systems.put(system.getPos().toImmutable(), system);
	}

}
