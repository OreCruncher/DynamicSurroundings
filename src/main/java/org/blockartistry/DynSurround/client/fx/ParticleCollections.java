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
import org.blockartistry.DynSurround.client.footsteps.interfaces.FootprintStyle;
import org.blockartistry.DynSurround.client.fx.particle.mote.IParticleMote;
import org.blockartistry.DynSurround.client.fx.particle.mote.MoteEmoji;
import org.blockartistry.DynSurround.client.fx.particle.mote.MoteFireFly;
import org.blockartistry.DynSurround.client.fx.particle.mote.MoteFootprint;
import org.blockartistry.DynSurround.client.fx.particle.mote.MoteRainSplash;
import org.blockartistry.DynSurround.client.fx.particle.mote.MoteWaterRipple;
import org.blockartistry.DynSurround.client.fx.particle.mote.MoteWaterSpray;
import org.blockartistry.DynSurround.client.fx.particle.mote.ParticleCollectionFireFly;
import org.blockartistry.DynSurround.client.fx.particle.mote.ParticleCollectionFootprint;
import org.blockartistry.DynSurround.client.fx.particle.mote.ParticleCollectionRipples;
import org.blockartistry.DynSurround.event.DiagnosticEvent;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ParticleCollections {

	private static final ResourceLocation RIPPLE_TEXTURE = new ResourceLocation(DSurround.RESOURCE_ID,
			"textures/particles/ripple.png");
	private static final ResourceLocation SPRAY_TEXTURE = new ResourceLocation(DSurround.RESOURCE_ID,
			"textures/particles/rainsplash.png");
	private static final ResourceLocation EMOJI_TEXTURE = new ResourceLocation(DSurround.RESOURCE_ID,
			"textures/particles/emojis.png");
	private static final ResourceLocation FOOTPRINT_TEXTURE = new ResourceLocation(DSurround.RESOURCE_ID,
			"textures/particles/footprint.png");
	private static final ResourceLocation FIREFLY_TEXTURE = new ResourceLocation("textures/particle/particles.png");

	private final static CollectionHelper theRipples = new CollectionHelper("Rain Ripples",
			ParticleCollectionRipples.FACTORY, RIPPLE_TEXTURE);
	private final static CollectionHelper theSprays = new CollectionHelper("Water Spray", SPRAY_TEXTURE);
	private final static CollectionHelper theEmojis = new CollectionHelper("Emojis", EMOJI_TEXTURE);
	private final static CollectionHelper thePrints = new CollectionHelper("Footprints",
			ParticleCollectionFootprint.FACTORY, FOOTPRINT_TEXTURE);
	private final static CollectionHelper theFireFlies = new LightedCollectionHelper("Fireflies",
			ParticleCollectionFireFly.FACTORY, FIREFLY_TEXTURE);

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

	@Nullable
	public static IParticleMote addWaterSpray(@Nonnull final World world, final double x, final double y,
			final double z, final double dX, final double dY, final double dZ) {
		IParticleMote mote = null;
		if (theSprays.get().canFit()) {
			mote = new MoteWaterSpray(world, x, y, z, dX, dY, dZ);
			theSprays.get().addParticle(mote);
		}
		return mote;
	}

	public static boolean canFitWaterSpray() {
		return theSprays.get().canFit();
	}

	@Nullable
	public static IParticleMote addRainSplash(@Nonnull final World world, final double x, final double y,
			final double z) {
		IParticleMote mote = null;
		if (theSprays.get().canFit()) {
			mote = new MoteRainSplash(world, x, y, z);
			theSprays.get().addParticle(mote);
		}
		return mote;
	}

	@Nullable
	public static IParticleMote addEmoji(@Nonnull final Entity entity) {
		IParticleMote mote = null;
		if (theEmojis.get().canFit()) {
			mote = new MoteEmoji(entity);
			theEmojis.get().addParticle(mote);
		}
		return mote;
	}

	@Nullable
	public static IParticleMote addFootprint(@Nonnull final FootprintStyle style, @Nonnull final World world,
			final Vec3d loc, final float rot, final float scale, final boolean isRight) {
		IParticleMote mote = null;
		if (thePrints.get().canFit()) {
			mote = new MoteFootprint(style, world, loc.x, loc.y, loc.z, rot, scale, isRight);
			thePrints.get().addParticle(mote);
		}
		return mote;
	}

	@Nullable
	public static IParticleMote addFireFly(@Nonnull final World world, final double x, final double y, final double z) {
		IParticleMote mote = null;
		if (theFireFlies.get().canFit()) {
			mote = new MoteFireFly(world, x, y, z);
			theFireFlies.get().addParticle(mote);
		}
		return mote;
	}

	@SubscribeEvent
	public static void onWorldUnload(@Nonnull final WorldEvent.Unload event) {
		if (event.getWorld() instanceof WorldClient) {
			DSurround.log().debug("World [%s] unloading, clearing particle collections",
					event.getWorld().provider.getDimensionType().getName());
			theRipples.clear();
			theSprays.clear();
			theEmojis.clear();
			thePrints.clear();
			theFireFlies.clear();
		}
	}

	@SubscribeEvent
	public static void diagnostics(@Nonnull final DiagnosticEvent.Gather event) {
		event.output.add(TextFormatting.AQUA + thePrints.toString());
		event.output.add(TextFormatting.AQUA + theRipples.toString());
		event.output.add(TextFormatting.AQUA + theSprays.toString());
		event.output.add(TextFormatting.AQUA + theFireFlies.toString());
		event.output.add(TextFormatting.AQUA + theEmojis.toString());
	}
}
