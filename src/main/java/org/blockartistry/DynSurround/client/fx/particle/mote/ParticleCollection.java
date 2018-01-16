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

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.OpenGlState;
import org.blockartistry.lib.collections.ObjectArray;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleCollection extends Particle {

	protected static final int MAX_PARTICLES = 4000;
	protected static final int ALLOCATION_SIZE = 1024;

	protected final ObjectArray<IParticleMote> myParticles = new ObjectArray<IParticleMote>(ALLOCATION_SIZE);
	protected final ResourceLocation texture;

	protected OpenGlState glState;

	public ParticleCollection(@Nonnull final World world, @Nonnull final ResourceLocation tex) {
		super(world, 0, 0, 0);

		this.canCollide = false;
		this.texture = tex;
	}

	protected void bindTexture(@Nonnull final ResourceLocation resource) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(resource);
	}

	public boolean canFit() {
		return this.myParticles.size() < MAX_PARTICLES;
	}

	public boolean addParticle(@Nonnull final IParticleMote mote) {

		if (this.canFit()) {
			this.myParticles.add(mote);
			return true;
		}
		return false;
	}

	public ObjectArray<IParticleMote> getParticles() {
		return this.myParticles;
	}

	public boolean shouldDie() {
		return this.myParticles.size() == 0 || this.worldObj != EnvironState.getWorld();
	}

	@Override
	public void onUpdate() {
		if (!this.isAlive())
			return;

		// Update state and remove the dead ones
		this.myParticles.removeIf(IParticleMote.UPDATE_REMOVE);

		if (this.shouldDie()) {
			this.setExpired();
		}
	}

	@Nonnull
	protected VertexFormat getVertexFormat() {
		return DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP;
	}

	@Override
	public void renderParticle(final VertexBuffer buffer, final Entity entityIn, final float partialTicks,
			final float rotX, final float rotZ, final float rotYZ, final float rotXY, final float rotXZ) {

		this.bindTexture(this.texture);
		this.preRender();

		buffer.begin(GL11.GL_QUADS, getVertexFormat());
		for (int i = 0; i < this.myParticles.size(); i++)
			this.myParticles.get(i).renderParticle(buffer, entityIn, partialTicks, rotX, rotZ, rotYZ, rotXY, rotXZ);
		Tessellator.getInstance().draw();

		this.postRender();
	}

	protected boolean enableLighting() {
		return false;
	}

	protected void preRender() {
		this.glState = OpenGlState.push();
		if (this.enableLighting())
			GlStateManager.enableLighting();
		else
			GlStateManager.disableLighting();
	}

	protected void postRender() {
		OpenGlState.pop(this.glState);
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

}
