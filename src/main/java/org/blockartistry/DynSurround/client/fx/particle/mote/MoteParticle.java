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

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.ClientChunkCache;
import org.blockartistry.lib.random.XorShiftRandom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A particle that stays fixed at a certain point of the world.
 */
@SideOnly(Side.CLIENT)
public abstract class MoteParticle implements IParticleMote {

	protected static final Random RANDOM = XorShiftRandom.current();
	protected static final RenderManager RENDERER = Minecraft.getMinecraft().getRenderManager();

	protected final World world;

	protected boolean isAlive = true;
	protected double posX;
	protected double posY;
	protected double posZ;
	protected final BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();

	protected int slX16;
	protected int blX16;

	protected float red;
	protected float green;
	protected float blue;
	protected float alpha;

	public MoteParticle(@Nonnull final World world, final double x, final double y, final double z) {
		this.world = world;
		setPosition(x, y, z);
		configureColor();
	}

	public void setPosition(final double posX, final double posY, final double posZ) {
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		this.position.setPos(posX, posY, posZ);
	}

	public void configureColor() {
		this.red = this.green = this.blue = this.alpha = 1F;
	}

	@Override
	public boolean isAlive() {
		return this.isAlive;
	}

	protected void kill() {
		this.isAlive = false;
	}

	@Override
	public void onUpdate() {
		if (!isAlive())
			return;

		update();

		// The update() may have killed the mote
		if (isAlive())
			updateBrightness();
	}

	protected void update() {

	}

	public void updateBrightness() {
		final int combinedLight = getBrightnessForRender(0);
		this.slX16 = combinedLight >> 16 & 65535;
		this.blX16 = combinedLight & 65535;
	}

	protected final double interpX() {
		return RENDERER.viewerPosX;
	}

	protected final double interpY() {
		return RENDERER.viewerPosY;
	}

	protected final double interpZ() {
		return RENDERER.viewerPosZ;
	}

	protected float renderX(final float partialTicks) {
		return (float) (this.posX - interpX());
	}

	protected float renderY(final float partialTicks) {
		return (float) (this.posY - interpY());
	}

	protected float renderZ(final float partialTicks) {
		return (float) (this.posZ - interpZ());
	}

	protected void applyColor(@Nonnull final BufferBuilder buffer) {
		buffer.color(this.red, this.green, this.blue, this.alpha);
	}

	protected void applyLightmap(@Nonnull final BufferBuilder buffer) {
		buffer.lightmap(this.slX16, this.blX16);
	}

	protected void drawVertex(final BufferBuilder buffer, final double x, final double y, final double z,
			final double u, final double v) {
		buffer.pos(x, y, z).tex(u, v);
		applyColor(buffer);
		applyLightmap(buffer);
		buffer.endVertex();
	}

	@Override
	public abstract void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotX,
			float rotZ, float rotYZ, float rotXY, float rotXZ);

	public int getBrightnessForRender(final float partialTicks) {
		return ClientChunkCache.INSTANCE.getCombinedLight(this.position, 0);
	}

}
