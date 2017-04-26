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

package org.blockartistry.mod.DynSurround.client.fx.particle;

import java.util.Random;

import org.blockartistry.mod.DynSurround.util.random.XorShiftRandom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MoteWaterRipple implements IParticleMote {

	private static final float TEX_SIZE_HALF = 0.5F;

	protected final RenderManager manager = Minecraft.getMinecraft().getRenderManager();
	protected final World world;

	protected boolean isAlive = true;
	protected final int maxAge;
	protected int age;
	protected final float growthRate;

	protected double posX;
	protected double posY;
	protected double posZ;
	protected final BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();

	protected int slX16;
	protected int blX16;

	protected float alpha = 1F;
	protected float scale;

	public MoteWaterRipple(final World world, final double x, final double y, final double z) {

		final Random random = XorShiftRandom.current();

		this.world = world;
		this.age = 0;
		this.maxAge = 12 + random.nextInt(8);
		this.growthRate = this.maxAge / 500F;

		this.position.setPos(x, y, z);
		this.posX = x;
		this.posY = y - 0.2D;
		this.posZ = z;

		this.scale = this.growthRate;
	}

	@Override
	public boolean isAlive() {
		return this.isAlive;
	}

	@Override
	public void onUpdate() {

		if (this.age++ >= this.maxAge) {
			this.isAlive = false;
		} else {
			this.scale += this.growthRate;
			this.alpha = (float) (this.maxAge - this.age) / (float) (this.maxAge + 3);

			final int combinedLight = this.getBrightnessForRender(0);
			this.slX16 = combinedLight >> 16 & 65535;
			this.blX16 = combinedLight & 65535;
		}
	}

	@Override
	public boolean moveParticleOnExpire() {
		return false;
	}

	@Override
	public Particle getParticle() {
		throw new RuntimeException("Cannot move a mote!");
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	protected double interpX() {
		return this.manager.viewerPosX;
	}

	protected double interpY() {
		return this.manager.viewerPosY;
	}

	protected double interpZ() {
		return this.manager.viewerPosZ;
	}

	protected void drawVertex(final VertexBuffer buffer, final double x, final double y, final double z, final double u,
			final double v, final float brightness) {
		buffer.pos(x, y, z).tex(u, v).color(brightness, brightness, brightness, this.alpha)
				.lightmap(this.slX16, this.blX16).endVertex();
	}

	@Override
	public void renderParticle(VertexBuffer buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		final float x = (float) (this.posX - interpX());
		final float y = (float) (this.posY - interpY());
		final float z = (float) (this.posZ - interpZ());
		final float scaledWidth = TEX_SIZE_HALF * this.scale;

		final float bright = 1F;
		drawVertex(buffer, -scaledWidth + x, y, scaledWidth + z, 0, 1, bright);
		drawVertex(buffer, scaledWidth + x, y, scaledWidth + z, 1, 1, bright);
		drawVertex(buffer, scaledWidth + x, y, -scaledWidth + z, 1, 0, bright);
		drawVertex(buffer, -scaledWidth + x, y, -scaledWidth + z, 0, 0, bright);
	}

	public int getBrightnessForRender(final float partialTicks) {
		return this.world.getCombinedLight(this.position, 0);
	}

}
