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

import java.lang.ref.WeakReference;
import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.DynSurround.client.fx.particle.mote.ParticleCollection;
import org.blockartistry.DynSurround.client.fx.particle.mote.ParticleCollection.ICollectionFactory;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CollectionHelper {

	private final String name;
	private final ICollectionFactory factory;
	private final ResourceLocation texture;

	// Weak reference because the particle could be evicted from Minecraft's
	// particle manager for some reason.
	private WeakReference<ParticleCollection> collection;

	public CollectionHelper(@Nonnull final String name, @Nonnull final ResourceLocation texture) {
		this(name, ParticleCollection.FACTORY, texture);
	}

	public CollectionHelper(@Nonnull final String name, @Nonnull final ICollectionFactory factory,
			@Nonnull final ResourceLocation texture) {
		this.name = name;
		this.texture = texture;
		this.factory = factory;
	}

	@Nonnull
	public String name() {
		return this.name;
	}

	@Nonnull
	public ParticleCollection get() {
		ParticleCollection pc = this.collection != null ? this.collection.get() : null;
		if (pc == null || !pc.isAlive() || pc.shouldDie()) {
			pc = this.factory.create(EnvironState.getWorld(), this.texture);
			this.collection = new WeakReference<ParticleCollection>(pc);
			ParticleHelper.addParticle(pc);
		}
		return pc;
	}

	public void clear() {
		ParticleCollection pc = this.collection != null ? this.collection.get() : null;
		if (pc != null) {
			pc.setExpired();
			this.collection = null;
		}
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(this.name).append('=');
		ParticleCollection pc = this.collection != null ? this.collection.get() : null;
		if (pc == null)
			builder.append("No Collection");
		else if (!pc.isAlive())
			builder.append("Expired");
		else if (pc.shouldDie())
			builder.append("Should Die");
		else
			builder.append(pc.size());
		return builder.toString();
	}
}
