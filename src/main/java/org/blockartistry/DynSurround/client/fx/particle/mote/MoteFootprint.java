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

import org.blockartistry.lib.MathStuff;
import org.blockartistry.lib.WorldUtils;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MoteFootprint extends MoteBase {

	// Basic layout of the footprint
	private static final float WIDTH = 0.125F;
	private static final float LENGTH = WIDTH * 2.0F;
	private static final Vec3d FIRST_POINT = new Vec3d(-WIDTH, 0, LENGTH);
	private static final Vec3d SECOND_POINT = new Vec3d(WIDTH, 0, LENGTH);
	private static final Vec3d THIRD_POINT = new Vec3d(WIDTH, 0, -LENGTH);
	private static final Vec3d FOURTH_POINT = new Vec3d(-WIDTH, 0, -LENGTH);

	// Micro Y adjuster to avoid z-fighting when rendering
	// multiple overlapping prints.
	private static float zFighter = 0F;

	protected final boolean isSnowLayer;
	protected final BlockPos downPos;

	protected final float texU1, texU2;
	protected final float texV1, texV2;

	protected final Vec3d firstPoint;
	protected final Vec3d secondPoint;
	protected final Vec3d thirdPoint;
	protected final Vec3d fourthPoint;

	public MoteFootprint(@Nonnull final World world, final double x, final double y, final double z,
			final float rotation, final boolean isRight) {
		super(world, x, y, z);

		this.maxAge = 200;

		if (++zFighter > 20)
			zFighter = 1;

		this.posY += zFighter * 0.001F;

		// If the block is a snow layer block need to adjust the
		// y up so the footprint rides on top.
		this.isSnowLayer = WorldUtils.getBlockState(this.world, this.position).getBlock() == Blocks.SNOW_LAYER;
		if (this.isSnowLayer) {
			this.posY += 0.125F;
		}

		this.downPos = this.position.down();

		this.texU1 = isRight ? 0.5F : 0F;
		this.texU2 = isRight ? 1.0F : 0.5F;
		this.texV1 = 0F;
		this.texV2 = 1F;

		// Rotate our vertex coordinates.  Since prints are static
		// doing the rotation on the vertex points during
		// constructions makes for a much more efficient render
		// process.
		final float theRotation = MathStuff.toRadians(-rotation + 180);
		this.firstPoint = FIRST_POINT.rotateYaw(theRotation);
		this.secondPoint = SECOND_POINT.rotateYaw(theRotation);
		this.thirdPoint = THIRD_POINT.rotateYaw(theRotation);
		this.fourthPoint = FOURTH_POINT.rotateYaw(theRotation);
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

		drawVertex(buffer, x + this.firstPoint.xCoord, y, z + this.firstPoint.zCoord, this.texU1, this.texV2);
		drawVertex(buffer, x + this.secondPoint.xCoord, y, z + this.secondPoint.zCoord, this.texU2, this.texV2);
		drawVertex(buffer, x + this.thirdPoint.xCoord, y, z + this.thirdPoint.zCoord, this.texU2, this.texV1);
		drawVertex(buffer, x + this.fourthPoint.xCoord, y, z + this.fourthPoint.zCoord, this.texU1, this.texV1);
	}

}
