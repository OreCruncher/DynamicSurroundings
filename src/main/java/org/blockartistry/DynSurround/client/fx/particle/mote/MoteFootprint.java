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

import org.blockartistry.lib.WorldUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MoteFootprint extends MoteBase {

	private static float zFighter = 0F;

	protected boolean isSnowLayer;
	protected BlockPos downPos;

	protected final float rotation;
	protected final float texU1, texU2;
	protected final float texV1, texV2;
	protected final float width = 0.125F;
	protected final float length = this.width * 2.0F;

	public MoteFootprint(@Nonnull final World world, final double x, final double y, final double z,
			final float rotation, final boolean isRight) {
		super(world, x, y, z);

		this.maxAge = 200;

		if (++zFighter > 20)
			zFighter = 1;

		this.posY += zFighter * 0.001F;

		// If the block is a snow layer block need to adjust the
		// y up so the footprint rides on top.
		final IBlockState state = world.getBlockState(this.position);
		this.isSnowLayer = state.getBlock() == Blocks.SNOW_LAYER;
		if (this.isSnowLayer) {
			this.posY += 0.125F;
		}

		this.downPos = this.position.down();
		this.rotation = -rotation + 180;

		this.texU1 = isRight ? 0.5F : 0F;
		this.texU2 = isRight ? 1.0F : 0.5F;
		this.texV1 = 0F;
		this.texV2 = 1F;
	}

	@Override
	protected void update() {
		if (!WorldUtils.isSolidBlock(this.world, this.downPos)) {
			this.isAlive = false;
		} else if (this.isSnowLayer
				&& WorldUtils.getBlockState(this.world, this.position).getBlock() != Blocks.SNOW_LAYER) {
			this.isAlive = false;
		}
	}

	@Override
	public void renderParticle(VertexBuffer buffer, Entity entityIn, float partialTicks, float rotX, float rotZ,
			float rotYZ, float rotXY, float rotXZ) {

		float f = ((float) this.age + partialTicks) / ((float) this.maxAge + 1);
		f = f * f;
		this.alpha = 2.0F - f * 2.0F;

		if (this.alpha > 1.0F) {
			this.alpha = 1.0F;
		}

		// Sets the alpha
		this.alpha = this.alpha * 0.4F;

		final double x = renderX(partialTicks);
		final double y = renderY(partialTicks);
		final double z = renderZ(partialTicks);

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.rotate(this.rotation, 0F, 1F, 0F);

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		drawVertex(buffer, -this.width, 0, +this.length, this.texU1, this.texV2);
		drawVertex(buffer, +this.width, 0, +this.length, this.texU2, this.texV2);
		drawVertex(buffer, +this.width, 0, -this.length, this.texU2, this.texV1);
		drawVertex(buffer, -this.width, 0, -this.length, this.texU1, this.texV1);
		Tessellator.getInstance().draw();

		GlStateManager.popMatrix();
	}

}
