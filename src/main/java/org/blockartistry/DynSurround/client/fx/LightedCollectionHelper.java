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

import org.blockartistry.DynSurround.client.fx.particle.mote.IParticleMote;
import org.blockartistry.DynSurround.client.fx.particle.mote.ParticleCollection;
import org.blockartistry.DynSurround.client.fx.particle.mote.ParticleCollection.ICollectionFactory;
import org.blockartistry.lib.collections.ObjectArray;

import elucent.albedo.event.GatherLightsEvent;
import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
