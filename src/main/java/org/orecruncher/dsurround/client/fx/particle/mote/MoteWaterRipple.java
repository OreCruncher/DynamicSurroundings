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

package org.orecruncher.dsurround.client.fx.particle.mote;

import org.orecruncher.dsurround.registry.biome.BiomeUtil;
import org.orecruncher.lib.Color;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MoteWaterRipple extends MoteAgeable {

	private static final float TEX_SIZE_HALF = 0.5F;

	protected final float growthRate;
	protected float scale;
	protected float scaledWidth;

	protected float texU1, texU2;
	protected float texV1, texV2;

	public MoteWaterRipple(final World world, final double x, final double y, final double z) {
		super(world, x, y, z);

		final RippleStyle style = RippleStyle.get();

		this.maxAge = style.getMaxAge();

		if (style.doScaling()) {
			this.growthRate = this.maxAge / 500F;
			this.scale = this.growthRate;
			this.scaledWidth = this.scale * TEX_SIZE_HALF;
		} else {
			this.growthRate = 0F;
			this.scale = 0F;
			this.scaledWidth = 0.5F;
		}

		this.posY -= 0.2D;

		final Color waterColor = BiomeUtil.getColorForLiquid(world, this.position);

		this.red = (int) (waterColor.red * 255);
		this.green = (int) (waterColor.green * 255);
		this.blue = (int) (waterColor.blue * 255);

		this.texU1 = style.getU1(this.age);
		this.texU2 = style.getU2(this.age);
		this.texV1 = style.getV1(this.age);
		this.texV2 = style.getV2(this.age);
	}

	@Override
	public void update() {
		final RippleStyle style = RippleStyle.get();
		if (style.doScaling()) {
			this.scale += this.growthRate;
			this.scaledWidth = this.scale * TEX_SIZE_HALF;
		}

		if (style.doAlpha()) {
			this.alpha = (int) ((float) (this.maxAge - this.age) / (float) (this.maxAge + 3) * 255);
		}

		this.texU1 = style.getU1(this.age);
		this.texU2 = style.getU2(this.age);
		this.texV1 = style.getV1(this.age);
		this.texV2 = style.getV2(this.age);
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		final float x = renderX(partialTicks);
		final float y = renderY(partialTicks);
		final float z = renderZ(partialTicks);

		drawVertex(buffer, -this.scaledWidth + x, y, this.scaledWidth + z, this.texU2, this.texV2);
		drawVertex(buffer, this.scaledWidth + x, y, this.scaledWidth + z, this.texU2, this.texV1);
		drawVertex(buffer, this.scaledWidth + x, y, -this.scaledWidth + z, this.texU1, this.texV1);
		drawVertex(buffer, -this.scaledWidth + x, y, -this.scaledWidth + z, this.texU1, this.texV2);
	}

}
