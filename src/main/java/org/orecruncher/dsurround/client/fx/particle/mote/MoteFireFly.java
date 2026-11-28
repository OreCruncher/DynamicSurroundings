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

import java.nio.ByteOrder;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.client.fx.firefly.FireflyFlash;
import org.orecruncher.dsurround.client.fx.firefly.WanderingFlight;
import org.orecruncher.dsurround.client.fx.light.PointLights;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A firefly, from the 26.x line: dark most of the time, flashing in the rhythm of its kind ({@link FireflyFlash}) as
 * it wanders about ({@link WanderingFlight}), steering around blocks rather than bumping into them. While lit it has a
 * soft halo and lights its surroundings (see {@link ParticleCollectionFireFly}); while dark nothing of it is drawn.
 */
@SideOnly(Side.CLIENT)
public class MoteFireFly extends MoteMotionBase {

	// A slow, meandering flight, climbing by up to its lift in the "J" stroke (see FireflyFlash.climb)
	private static final WanderingFlight.Settings FLIGHT = new WanderingFlight.Settings(0.012F, // minSpeed
			0.03F, // maxSpeed
			0.025F, // turnWander
			0.9F, // turnDamping
			0.12F, // steer
			0.018F, // lift
			0.004F, // bob
			0.2F, // bobRate
			6); // lookAhead

	// The halo: a soft glow around the firefly, this many times its size, at this strength
	private static final float HALO_SCALE = 5F;
	private static final float HALO_ALPHA = 0.35F;

	// A firefly's light: yellow-green, about 560nm
	static final float LIGHT_RED = 0.72F;
	static final float LIGHT_GREEN = 1F;
	static final float LIGHT_BLUE = 0.22F;
	// Dimmer than this, nothing is drawn
	private static final float MIN_VISIBLE = 0.004F;

	// Vanilla's end rod sparkle in particles.png: 8 frames, from the last down to this one as it ages
	private static final int TEXTURE_INDEX = 176;
	private static final int TEXTURE_FRAMES = 8;
	private static final float TEXTURE_CELL = 0.0624375F;

	private final FireflyFlash flash;
	private final WanderingFlight flight;
	private final WanderingFlight.Obstacles obstacles;
	// Half the size of its square, in blocks
	private final float size;

	public MoteFireFly(@Nonnull final World world, final double x, final double y, final double z) {
		super(world, x, y, z, 0, 0, 0);

		this.gravity = 0D;
		// Lives for a few flashes; dark in between
		this.flash = new FireflyFlash(RANDOM);
		this.maxAge = this.flash.lifetime();
		// Already under way when it appears
		this.flight = new WanderingFlight(FLIGHT, RANDOM);
		this.obstacles = WanderingFlight.blocksIn(world);
		this.motionX = this.flight.xd();
		this.motionY = this.flight.yd();
		this.motionZ = this.flight.zd();
		// Vanilla's particle size, scaled as the 26.x line's
		final float scale = Math.max(0.05F, 0.20F + (float) (RANDOM.nextGaussian() * 0.1F));
		this.size = 0.1F * (RANDOM.nextFloat() * 0.5F + 0.5F) * 2F * scale;

		this.red = (int) (LIGHT_RED * 255F);
		this.green = (int) (LIGHT_GREEN * 255F);
		this.blue = (int) (LIGHT_BLUE * 255F);
		updateBrightness();
	}

	@Override
	protected void update() {
		// The "J" stroke: dipping just before each flash, rising while lit
		this.flight.tick(this.posX, this.posY, this.posZ, this.age, this.flash.climb(this.age), this.obstacles);
		this.motionX = this.flight.xd();
		this.motionY = this.flight.yd();
		this.motionZ = this.flight.zd();

		// No collisions: it steers around blocks instead
		this.prevX = this.posX;
		this.prevY = this.posY;
		this.prevZ = this.posZ;
		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;
		this.position.setPos(this.posX, this.posY, this.posZ);
	}

	/**
	 * How brightly it is lit now, 0 to 1
	 */
	public float glow(final float partialTicks) {
		return this.flash.brightness(this.age + partialTicks);
	}

	/**
	 * Draws the firefly itself, lit by its own light while it flashes, however dark it is around it
	 */
	@Override
	public void renderParticle(final BufferBuilder buffer, final Entity entityIn, final float partialTicks,
			final float rotX, final float rotZ, final float rotYZ, final float rotXY, final float rotXZ) {
		final float glow = glow(partialTicks);
		if (glow < MIN_VISIBLE)
			return;
		final int frame = TEXTURE_INDEX
				+ Math.max(0, TEXTURE_FRAMES - 1 - this.age * TEXTURE_FRAMES / Math.max(1, this.maxAge));
		final float u1 = (frame % 16) / 16F;
		final float v1 = (frame / 16) / 16F;
		final int block = Math.max(this.blX16, (int) (glow * 240F));
		quad(buffer, partialTicks, this.size, u1, u1 + TEXTURE_CELL, v1, v1 + TEXTURE_CELL, glow, block, rotX, rotZ,
				rotYZ, rotXY, rotXZ);
	}

	/**
	 * Draws its halo, with the halo's texture bound
	 */
	public void renderHalo(final BufferBuilder buffer, final float partialTicks, final float rotX, final float rotZ,
			final float rotYZ, final float rotXY, final float rotXZ) {
		final float glow = glow(partialTicks);
		if (glow < MIN_VISIBLE)
			return;
		final int block = Math.max(this.blX16, (int) (glow * 240F));
		quad(buffer, partialTicks, this.size * HALO_SCALE, 0F, 1F, 0F, 1F, HALO_ALPHA * glow, block, rotX, rotZ, rotYZ,
				rotXY, rotXZ);
	}

	/**
	 * Adds the light it casts on its surroundings this frame
	 */
	public void addLight(final float partialTicks) {
		final float glow = glow(partialTicks);
		if (glow < MIN_VISIBLE)
			return;
		PointLights.add(renderX(partialTicks), renderY(partialTicks), renderZ(partialTicks), LIGHT_RED, LIGHT_GREEN,
				LIGHT_BLUE, PointLights.FIREFLY_STRENGTH * glow, PointLights.FIREFLY_RADIUS);
	}

	// A quad in the particle vertex format (position, texture, color, lightmap: 7 ints a vertex), written to the
	// buffer whole: setting each element through BufferBuilder cost several times as much. Render thread only
	private static final int[] QUAD = new int[28];
	private static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

	private void quad(final BufferBuilder buffer, final float partialTicks, final float scale, final float u1,
			final float u2, final float v1, final float v2, final float alpha, final int block, final float rotX,
			final float rotZ, final float rotYZ, final float rotXY, final float rotXZ) {
		final float x = renderX(partialTicks);
		final float y = renderY(partialTicks);
		final float z = renderZ(partialTicks);
		final int a = (int) (alpha * 255F);
		final int color = LITTLE_ENDIAN ? this.red | this.green << 8 | this.blue << 16 | a << 24
				: this.red << 24 | this.green << 16 | this.blue << 8 | a;
		// BufferBuilder.lightmap's shorts: the block light, then the sky's
		final int light = LITTLE_ENDIAN ? (block & 0xFFFF) | this.slX16 << 16 : block << 16 | (this.slX16 & 0xFFFF);
		final float rx = rotX * scale, rz = rotZ * scale, ryz = rotYZ * scale, rxy = rotXY * scale,
				rxz = rotXZ * scale;
		vertex(0, x - rx - rxy, y - rz, z - ryz - rxz, u2, v2, color, light);
		vertex(7, x - rx + rxy, y + rz, z - ryz + rxz, u2, v1, color, light);
		vertex(14, x + rx + rxy, y + rz, z + ryz + rxz, u1, v1, color, light);
		vertex(21, x + rx - rxy, y - rz, z + ryz - rxz, u1, v2, color, light);
		buffer.addVertexData(QUAD);
	}

	private static void vertex(final int i, final float x, final float y, final float z, final float u,
			final float v, final int color, final int light) {
		QUAD[i] = Float.floatToRawIntBits(x);
		QUAD[i + 1] = Float.floatToRawIntBits(y);
		QUAD[i + 2] = Float.floatToRawIntBits(z);
		QUAD[i + 3] = Float.floatToRawIntBits(u);
		QUAD[i + 4] = Float.floatToRawIntBits(v);
		QUAD[i + 5] = color;
		QUAD[i + 6] = light;
	}
}
