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

package org.blockartistry.DynSurround.client.fx.particle.mote;

import java.util.function.Predicate;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.fx.particle.ParticleBase;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.collections.ObjectArray;
import org.blockartistry.lib.compat.ModEnvironment;
import org.blockartistry.lib.gfx.OpenGlState;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleCollection extends ParticleBase {

	/**
	 * Predicate used to update a mote and return whether it is dead or not.
	 */
	private static final Predicate<IParticleMote> UPDATE_REMOVE = mote -> {
		mote.onUpdate();
		return !mote.isAlive();
	};

	protected static final int MAX_PARTICLES = 4000;
	protected static final int ALLOCATION_SIZE = 128;
	protected static final int TICK_GRACE = 2;

	protected final ObjectArray<IParticleMote> myParticles = new ObjectArray<>(ALLOCATION_SIZE);
	protected final ResourceLocation texture;

	protected int lastTickUpdate;
	protected OpenGlState glState;

	public ParticleCollection(@Nonnull final World world, @Nonnull final ResourceLocation tex) {
		super(world, 0, 0, 0);

		this.canCollide = false;
		this.texture = tex;
		this.lastTickUpdate = EnvironState.getTickCounter();
	}

	public boolean canFit() {
		return this.myParticles.size() < MAX_PARTICLES;
	}

	public boolean addParticle(@Nonnull final IParticleMote mote) {

		if (canFit()) {
			this.myParticles.add(mote);
			return true;
		}
		return false;
	}

	public ObjectArray<IParticleMote> getParticles() {
		return this.myParticles;
	}

	public int size() {
		return this.myParticles.size();
	}

	public boolean shouldDie() {
		final boolean timeout = (EnvironState.getTickCounter() - this.lastTickUpdate) > TICK_GRACE;
		return timeout || size() == 0 || this.world != EnvironState.getWorld();
	}

	@Override
	public void onUpdate() {
		if (!isAlive())
			return;

		this.lastTickUpdate = EnvironState.getTickCounter();

		// Update state and remove the dead ones
		this.myParticles.removeIf(UPDATE_REMOVE);

		if (shouldDie()) {
			setExpired();
		}
	}

	@Nonnull
	protected VertexFormat getVertexFormat() {
		return DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP;
	}

	@Override
	public void renderParticle(final BufferBuilder buffer, final Entity entityIn, final float partialTicks,
			final float rotX, final float rotZ, final float rotYZ, final float rotXY, final float rotXZ) {

		if (this.myParticles.size() == 0)
			return;

		bindTexture(this.texture);
		preRender();

		buffer.begin(GL11.GL_QUADS, getVertexFormat());
		for (int i = 0; i < this.myParticles.size(); i++)
			this.myParticles.get(i).renderParticle(buffer, entityIn, partialTicks, rotX, rotZ, rotYZ, rotXY, rotXZ);
		Tessellator.getInstance().draw();

		postRender();
	}

	protected boolean enableLighting() {
		return ModEnvironment.Albedo.isLoaded();
	}

	protected void preRender() {
		this.glState = OpenGlState.push();
		if (enableLighting())
			GlStateManager.enableLighting();
		else
			GlStateManager.disableLighting();
	}

	protected void postRender() {
		OpenGlState.pop(this.glState);
		this.glState = null;
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	/**
	 * Factory interface for creating particle collection instances. Used by the
	 * ParticleCollections manager.
	 */
	public static interface ICollectionFactory {
		ParticleCollection create(@Nonnull final World world, @Nonnull final ResourceLocation texture);
	}

	public static final ICollectionFactory FACTORY = (world, texture) -> {
		return new ParticleCollection(world, texture);
	};

}
