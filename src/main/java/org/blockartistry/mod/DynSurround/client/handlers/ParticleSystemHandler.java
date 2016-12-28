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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleSystem;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ParticleSystemHandler extends EffectHandlerBase {

	public static ParticleSystemHandler INSTANCE;

	private final Map<BlockPos, ParticleSystem> systems = new HashMap<BlockPos, ParticleSystem>();

	public ParticleSystemHandler() {
		INSTANCE = this;
	}

	@Override
	public String getHandlerName() {
		return "ParticleSystemHandler";
	}

	@Override
	public void process(final World world, final EntityPlayer player) {

		// Process the list looking for systems that can be removed.
		// They are removed if they are dead, or if they are out of
		// range of the player.
		final double range = ModOptions.specialEffectRange;
		final AxisAlignedBB box = player.getEntityBoundingBox().expand(range, range, range);
		Iterables.removeIf(this.systems.values(), new Predicate<ParticleSystem>() {
			@Override
			public boolean apply(@Nonnull final ParticleSystem input) {
				if (!input.isAlive())
					return true;
				if (!input.inBox(box)) {
					input.setExpired();
					return true;
				}
				return false;
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

	// Determines if it is OK to spawn a particle system at the specified
	// location. Generally only a single system can occupy a block.
	public boolean okToSpawn(@Nonnull final BlockPos pos) {
		return !this.systems.containsKey(pos);
	}

	public void addSystem(@Nonnull final ParticleSystem system) {
		final BlockPos pos = system.getPos();
		if (!okToSpawn(pos)) {
			// TODO: Best to not even get here...This is because
			// the trigger looks at one block when the partcile
			// spawns above.
			//ModLog.warn("More than one system in a block");
			return;
		}
		this.systems.put(pos, system);
		ParticleHelper.addParticle(system);
	}

}
