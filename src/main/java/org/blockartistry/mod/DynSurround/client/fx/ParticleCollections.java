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

package org.blockartistry.mod.DynSurround.client.fx;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.fx.particle.IParticleMote;
import org.blockartistry.mod.DynSurround.client.fx.particle.MoteWaterRipple;
import org.blockartistry.mod.DynSurround.client.fx.particle.MoteWaterSpray;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleCollection;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.mod.DynSurround.client.fx.particle.WaterRippleCollection;
import org.blockartistry.mod.DynSurround.client.fx.particle.WaterSprayCollection;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ParticleCollections {

	private abstract static class CollectionHelper<T extends ParticleCollection> {
		private T collection;
		
		public CollectionHelper() {
			
		}
		
		protected abstract T create(@Nonnull final World world);
		
		public T get() {
			if(this.collection == null || this.collection.shouldDie()) {
				this.collection = create(EnvironState.getWorld());
				ParticleHelper.addParticle(this.collection);
			}
			return this.collection;
		}
	}
	
	private final static CollectionHelper<WaterRippleCollection> theRipples = new CollectionHelper<WaterRippleCollection>() {
		@Override
		protected WaterRippleCollection create(@Nonnull final World world) {
			return new WaterRippleCollection(world);
		}
	};
	
	private final static CollectionHelper<WaterSprayCollection> theSprays = new CollectionHelper<WaterSprayCollection>() {
		@Override
		protected WaterSprayCollection create(@Nonnull final World world) {
			return new WaterSprayCollection(world);
		}
	};
	
	public static IParticleMote addWaterRipple(@Nonnull final World world, final double x, final double y, final double z) {
		final IParticleMote mote = new MoteWaterRipple(world, x, y, z);
		theRipples.get().addParticle(mote);
		return mote;
	}

	public static IParticleMote addWaterSpray(@Nonnull final World world, final double x, final double y, final double z, final double dX, final double dY, final double dZ) {
		final IParticleMote mote = new MoteWaterSpray(world, x, y, z, dX, dY, dZ);
		theSprays.get().addParticle(mote);
		return mote;
	}

}
