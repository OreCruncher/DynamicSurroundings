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

package org.blockartistry.DynSurround.client.fx;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.DynSurround.client.fx.particle.mote.IParticleMote;
import org.blockartistry.DynSurround.client.fx.particle.mote.MoteEmoji;
import org.blockartistry.DynSurround.client.fx.particle.mote.MoteFireFly;
import org.blockartistry.DynSurround.client.fx.particle.mote.MoteFootprint;
import org.blockartistry.DynSurround.client.fx.particle.mote.MoteRainSplash;
import org.blockartistry.DynSurround.client.fx.particle.mote.MoteWaterRipple;
import org.blockartistry.DynSurround.client.fx.particle.mote.MoteWaterSpray;
import org.blockartistry.DynSurround.client.fx.particle.mote.ParticleCollection;
import org.blockartistry.DynSurround.client.fx.particle.mote.ParticleCollectionFireFly;
import org.blockartistry.DynSurround.client.fx.particle.mote.ParticleCollectionFootprint;
import org.blockartistry.DynSurround.client.fx.particle.mote.ParticleCollectionRipples;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.collections.ObjectArray;

import elucent.albedo.event.GatherLightsEvent;
import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SideOnly(Side.CLIENT)
public final class ParticleCollections {

	private static class CollectionHelper {

		private final Class<? extends ParticleCollection> factory;
		private final ResourceLocation texture;

		private ParticleCollection collection;

		public CollectionHelper(@Nonnull final ResourceLocation texture) {
			this(ParticleCollection.class, texture);
		}

		public CollectionHelper(@Nonnull final Class<? extends ParticleCollection> clazz,
				@Nonnull final ResourceLocation texture) {
			this.texture = texture;
			this.factory = clazz;
		}

		public ParticleCollection get() {
			if (this.collection == null || this.collection.shouldDie()) {
				try {
					this.collection = this.factory.getConstructor(World.class, ResourceLocation.class)
							.newInstance(EnvironState.getWorld(), this.texture);
				} catch (final Throwable t) {
					throw new RuntimeException("Unknown ParticleCollection type!");
				}
				ParticleHelper.addParticle(this.collection);
			}
			return this.collection;
		}
	}

	private static class LightedCollectionHelper extends CollectionHelper {

		@SuppressWarnings("unused")
		public LightedCollectionHelper(@Nonnull final ResourceLocation texture) {
			this(ParticleCollection.class, texture);
		}

		public LightedCollectionHelper(@Nonnull final Class<? extends ParticleCollection> clazz,
				@Nonnull final ResourceLocation texture) {
			super(clazz, texture);
			MinecraftForge.EVENT_BUS.register(this);
		}

		@Optional.Method(modid = "albedo")
		@SubscribeEvent
		public void onGatherLight(@Nonnull final GatherLightsEvent event) {
			final ObjectArray<IParticleMote> motes = this.get().getParticles();
			if (motes == null || motes.size() == 0)
				return;

			for (int i = 0; i < motes.size(); i++) {
				final IParticleMote m = motes.get(i);
				if (m instanceof ILightProvider) {
					final ILightProvider provider = (ILightProvider) m;
					final Light l = provider.provideLight();
					if (l != null) {
						event.getLightList().add(l);
					}
				}
			}
		}

	}

	private static final ResourceLocation RIPPLE_TEXTURE = new ResourceLocation(DSurround.RESOURCE_ID,
			"textures/particles/ripple.png");
	private static final ResourceLocation SPRAY_TEXTURE = new ResourceLocation(DSurround.RESOURCE_ID,
			"textures/particles/rainsplash.png");
	private static final ResourceLocation EMOJI_TEXTURE = new ResourceLocation(DSurround.RESOURCE_ID,
			"textures/particles/emojis.png");
	private static final ResourceLocation FOOTPRINT_TEXTURE = new ResourceLocation(DSurround.RESOURCE_ID,
			"textures/particles/footprint.png");
	private static final ResourceLocation FIREFLY_TEXTURE = new ResourceLocation("textures/particle/particles.png");

	private final static CollectionHelper theRipples = new CollectionHelper(ParticleCollectionRipples.class,
			RIPPLE_TEXTURE);
	private final static CollectionHelper theSprays = new CollectionHelper(SPRAY_TEXTURE);
	private final static CollectionHelper theEmojis = new CollectionHelper(EMOJI_TEXTURE);
	private final static CollectionHelper thePrints = new CollectionHelper(ParticleCollectionFootprint.class,
			FOOTPRINT_TEXTURE);
	private final static CollectionHelper theFireFlies = new LightedCollectionHelper(ParticleCollectionFireFly.class,
			FIREFLY_TEXTURE);

	@Nullable
	public static IParticleMote addWaterRipple(@Nonnull final World world, final double x, final double y,
			final double z) {
		IParticleMote mote = null;
		if (theRipples.get().canFit()) {
			mote = new MoteWaterRipple(world, x, y, z);
			theRipples.get().addParticle(mote);
		}
		return mote;
	}

	public static IParticleMote addWaterSpray(@Nonnull final World world, final double x, final double y,
			final double z, final double dX, final double dY, final double dZ) {
		IParticleMote mote = null;
		if (theSprays.get().canFit()) {
			mote = new MoteWaterSpray(world, x, y, z, dX, dY, dZ);
			theSprays.get().addParticle(mote);
		}
		return mote;
	}

	public static IParticleMote addRainSplash(@Nonnull final World world, final double x, final double y,
			final double z) {
		IParticleMote mote = null;
		if (theSprays.get().canFit()) {
			mote = new MoteRainSplash(world, x, y, z);
			theSprays.get().addParticle(mote);
		}
		return mote;
	}

	public static IParticleMote addEmoji(@Nonnull final Entity entity) {
		IParticleMote mote = null;
		if (theEmojis.get().canFit()) {
			mote = new MoteEmoji(entity);
			theEmojis.get().addParticle(mote);
		}
		return mote;
	}

	public static IParticleMote addFootprint(@Nonnull final World world, final double x, final double y, final double z,
			final float rot, final boolean isRight) {
		IParticleMote mote = null;
		if (thePrints.get().canFit()) {
			mote = new MoteFootprint(world, x, y, z, rot, isRight);
			thePrints.get().addParticle(mote);
		}
		return mote;
	}

	public static IParticleMote addFireFly(@Nonnull final World world, final double x, final double y, final double z) {
		IParticleMote mote = null;
		if (theFireFlies.get().canFit()) {
			mote = new MoteFireFly(world, x, y, z);
			theFireFlies.get().addParticle(mote);
		}
		return mote;
	}
}
