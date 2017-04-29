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

import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.mod.DynSurround.client.fx.particle.mote.IParticleMote;
import org.blockartistry.mod.DynSurround.client.fx.particle.mote.MoteRainSplash;
import org.blockartistry.mod.DynSurround.client.fx.particle.mote.MoteWaterRipple;
import org.blockartistry.mod.DynSurround.client.fx.particle.mote.MoteWaterSpray;
import org.blockartistry.mod.DynSurround.client.fx.particle.mote.ParticleCollection;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ParticleCollections {

	private static class CollectionHelper {
		
		private final ResourceLocation texture;
		private ParticleCollection collection;
		
		public CollectionHelper(@Nonnull final ResourceLocation texture) {
			this.texture = texture;
		}
		
		public ParticleCollection get() {
			if(this.collection == null || this.collection.shouldDie()) {
				this.collection = new ParticleCollection(EnvironState.getWorld(), this.texture);
				ParticleHelper.addParticle(this.collection);
			}
			return this.collection;
		}
	}
	
	private static final ResourceLocation RIPPLE_TEXTURE = new ResourceLocation(DSurround.RESOURCE_ID,
			"textures/particles/ripple.png");
	private static final ResourceLocation SPRAY_TEXTURE = new ResourceLocation(DSurround.RESOURCE_ID,
			"textures/particles/rainsplash.png");

	private final static CollectionHelper theRipples = new CollectionHelper(RIPPLE_TEXTURE);
	private final static CollectionHelper theSprays = new CollectionHelper(SPRAY_TEXTURE);
	
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
	
	public static IParticleMote addRainSplash(@Nonnull final World world, final double x, final double y, final double z) {
		final IParticleMote mote = new MoteRainSplash(world, x, y, z);
		theSprays.get().addParticle(mote);
		return mote;
	}

}
