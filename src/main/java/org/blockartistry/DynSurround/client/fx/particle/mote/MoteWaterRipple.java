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

import org.blockartistry.lib.Color;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MoteWaterRipple extends MoteBase {

	private static final float TEX_SIZE_HALF = 0.5F;

	protected final float growthRate;
	protected float scale;
	protected float scaledWidth;

	public MoteWaterRipple(final World world, final double x, final double y, final double z) {
		super(world, x, y, z);

		this.maxAge = 12 + RANDOM.nextInt(8);
		this.growthRate = this.maxAge / 500F;
		this.scale = this.growthRate;
		this.scaledWidth = this.scale * TEX_SIZE_HALF;
		this.posY -= 0.2D;
	
		final Color waterColor = MoteBase.getBiomeWaterColor(world, x, y, z);
		this.red = waterColor.red;
		this.green = waterColor.green;
		this.blue = waterColor.blue;
	}

	@Override
	public void update() {
		this.scale += this.growthRate;
		this.scaledWidth = this.scale * TEX_SIZE_HALF;
		this.alpha = (float) (this.maxAge - this.age) / (float) (this.maxAge + 3);
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		final float x = renderX(partialTicks);
		final float y = renderY(partialTicks);
		final float z = renderZ(partialTicks);

		drawVertex(buffer, -this.scaledWidth + x, y, this.scaledWidth + z, 0, 1);
		drawVertex(buffer, this.scaledWidth + x, y, this.scaledWidth + z, 1, 1);
		drawVertex(buffer, this.scaledWidth + x, y, -this.scaledWidth + z, 1, 0);
		drawVertex(buffer, -this.scaledWidth + x, y, -this.scaledWidth + z, 0, 0);
	}

}
