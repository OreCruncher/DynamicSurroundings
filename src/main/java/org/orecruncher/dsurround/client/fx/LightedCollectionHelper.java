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
package org.orecruncher.dsurround.client.fx;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.client.fx.particle.mote.IIlluminatedMote;
import org.orecruncher.dsurround.client.fx.particle.mote.IParticleMote;
import org.orecruncher.dsurround.client.fx.particle.mote.ParticleCollection;
import org.orecruncher.dsurround.client.fx.particle.mote.ParticleCollection.ICollectionFactory;
import org.orecruncher.lib.Color;
import org.orecruncher.lib.collections.ObjectArray;

import elucent.albedo.event.GatherLightsEvent;
import elucent.albedo.lighting.Light;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.Optional;

@SideOnly(Side.CLIENT)
public class LightedCollectionHelper extends CollectionHelper {

	public LightedCollectionHelper(@Nonnull final String name, @Nonnull final ResourceLocation texture) {
		this(name, ParticleCollection.FACTORY, texture);
	}

	public LightedCollectionHelper(@Nonnull final String name, @Nonnull final ICollectionFactory factory,
			@Nonnull final ResourceLocation texture) {
		super(name, factory, texture);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Optional.Method(modid = "albedo")
	@SubscribeEvent
	public void onGatherLight(@Nonnull final GatherLightsEvent event) {
		final ParticleCollection pc = this.collection != null ? this.collection.get() : null;
		if (pc != null) {
			final ObjectArray<IParticleMote> motes = pc.getParticles();
			if (motes == null || motes.size() == 0)
				return;

			for (int i = 0; i < motes.size(); i++) {
				final IParticleMote m = motes.get(i);
				if (m instanceof IIlluminatedMote) {
					final IIlluminatedMote provider = (IIlluminatedMote) m;
					Vec3d pos = provider.getPosition();
					Color color = provider.getColor();
					float alpha = provider.getAlpha();
					float radius = provider.getRadius();
					final Light l = Light.builder().pos(pos).color(color.red, color.green, color.blue, alpha).radius(radius).build();
					event.add(l);
				}
			}
		}
	}
}
