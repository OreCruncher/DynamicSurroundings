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

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MoteWaterSpray extends MoteMotionBase {

	protected float scale;

	protected final float texU1, texU2;
	protected final float texV1, texV2;
	protected final float f4;

	public MoteWaterSpray(final World world, final double x, final double y, final double z, final double dX,
			final double dY, final double dZ) {

		super(world, x, y, z, dX, dY, dZ);

		this.maxAge = (int) (8.0D / (RANDOM.nextDouble() * 0.8D + 0.2D));
		this.scale = (RANDOM.nextFloat() * 0.5F + 0.5F) * 2.0F;

		final int textureIdx = RANDOM.nextInt(4);
		final int texX = textureIdx % 2;
		final int texY = textureIdx / 2;
		this.texU1 = texX * 0.5F;
		this.texU2 = this.texU1 + 0.5F;
		this.texV1 = texY * 0.5F;
		this.texV2 = this.texV1 + 0.5F;

		// Tweak the constant to change the size of the raindrop
		this.f4 = 0.07F * this.scale;

	}
	
	@Override
	public void configureColor() {
		final Color waterColor = MoteBase.getBiomeWaterColor(this.world, this.posX, this.posY, this.posZ);
		this.red = waterColor.red;
		this.green = waterColor.green;
		this.blue = waterColor.blue;
		this.alpha = 1F;
	}

	@Override
	public void renderParticle(VertexBuffer buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		final float x = renderX(partialTicks);
		final float y = renderY(partialTicks);
		final float z = renderZ(partialTicks);

		drawVertex(buffer, x + (-rotationX * f4 - rotationXY * f4), y + (-rotationZ * f4),
				z + (-rotationYZ * f4 - rotationXZ * f4), this.texU2, this.texV2);
		drawVertex(buffer, x + (-rotationX * f4 + rotationXY * f4), y + (rotationZ * f4),
				z + (-rotationYZ * f4 + rotationXZ * f4), this.texU2, this.texV1);
		drawVertex(buffer, x + (rotationX * f4 + rotationXY * f4), y + (rotationZ * f4),
				z + (rotationYZ * f4 + rotationXZ * f4), this.texU1, this.texV1);
		drawVertex(buffer, x + (rotationX * f4 - rotationXY * f4), y + (-rotationZ * f4),
				z + (rotationYZ * f4 - rotationXZ * f4), this.texU1, this.texV2);
	}

}
