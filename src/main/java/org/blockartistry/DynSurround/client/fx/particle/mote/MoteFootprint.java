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

import org.blockartistry.DynSurround.client.weather.Weather;
import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.math.MathStuff;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MoteFootprint extends MoteAgeable {

	// Basic layout of the footprint
	private static final float WIDTH = 0.125F;
	private static final float LENGTH = WIDTH * 2.0F;
	private static final Vec2f FIRST_POINT = new Vec2f(-WIDTH, LENGTH);
	private static final Vec2f SECOND_POINT = new Vec2f(WIDTH, LENGTH);
	private static final Vec2f THIRD_POINT = new Vec2f(WIDTH, -LENGTH);
	private static final Vec2f FOURTH_POINT = new Vec2f(-WIDTH, -LENGTH);

	// Micro Y adjuster to avoid z-fighting when rendering
	// multiple overlapping prints.
	private static float zFighter = 0F;

	protected final boolean isSnowLayer;
	protected final BlockPos downPos;

	protected final float texU1, texU2;
	protected final float texV1, texV2;
	protected final float scale;

	protected final Vec2f firstPoint;
	protected final Vec2f secondPoint;
	protected final Vec2f thirdPoint;
	protected final Vec2f fourthPoint;

	public MoteFootprint(@Nonnull final World world, final double x, final double y, final double z,
			final float rotation, final float scale, final boolean isRight) {
		super(world, x, y, z);

		this.maxAge = 200;

		if (++zFighter > 20)
			zFighter = 1;

		final IBlockState state = WorldUtils.getBlockState(this.world, this.position);
		this.isSnowLayer = state.getBlock() == Blocks.SNOW_LAYER;

		this.posY += zFighter * 0.001F;

		// Make sure that the down position is calculated from the display position!
		this.downPos = new BlockPos(this.posX, this.posY, this.posZ).down();

		this.texU1 = isRight ? 0.5F : 0F;
		this.texU2 = isRight ? 1.0F : 0.5F;
		this.texV1 = 0F;
		this.texV2 = 1F;
		this.scale = scale;

		// Rotate our vertex coordinates. Since prints are static
		// doing the rotation on the vertex points during
		// constructions makes for a much more efficient render
		// process.
		final float theRotation = MathStuff.toRadians(-rotation + 180);
		this.firstPoint = MathStuff.rotateScale(FIRST_POINT, theRotation, this.scale);
		this.secondPoint = MathStuff.rotateScale(SECOND_POINT, theRotation, this.scale);
		this.thirdPoint = MathStuff.rotateScale(THIRD_POINT, theRotation, this.scale);
		this.fourthPoint = MathStuff.rotateScale(FOURTH_POINT, theRotation, this.scale);
	}

	@Override
	protected boolean advanceAge() {
		// Footprints age faster when raining
		if (Weather.isRaining())
			this.age += (Weather.getIntensityLevel() * 100F) / 25;
		return super.advanceAge();
	}

	@Override
	protected void update() {
		if (!WorldUtils.isSolidBlock(this.world, this.downPos)) {
			this.kill();
		} else if (this.isSnowLayer
				&& WorldUtils.getBlockState(this.world, this.position).getBlock() != Blocks.SNOW_LAYER) {
			this.kill();
		}
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotX, float rotZ,
			float rotYZ, float rotXY, float rotXZ) {

		float f = ((float) this.age + partialTicks) / ((float) this.maxAge + 1);
		f = f * f;
		this.alpha = MathStuff.clamp(1.0F - f, 0F, 1F);

		// Sets the alpha
		this.alpha = this.alpha * 0.4F;

		final double x = renderX(partialTicks);
		final double y = renderY(partialTicks);
		final double z = renderZ(partialTicks);

		drawVertex(buffer, x + this.firstPoint.x, y, z + this.firstPoint.y, this.texU1, this.texV2);
		drawVertex(buffer, x + this.secondPoint.x, y, z + this.secondPoint.y, this.texU2, this.texV2);
		drawVertex(buffer, x + this.thirdPoint.x, y, z + this.thirdPoint.y, this.texU2, this.texV1);
		drawVertex(buffer, x + this.fourthPoint.x, y, z + this.fourthPoint.y, this.texU1, this.texV1);
	}

}
