/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.orecruncher.dsurround.client.weather.particle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Draws a {@link PrecipitationField}: rain as thin streaks along the way each drop falls, turned toward the camera,
 * and snow as flakes facing it, turning slowly as they fall. The streaks are a plain color that fades out toward their
 * tail; the flakes are cut from the game's own snow texture ({@link Snowflakes}). Both are lit by the lightmap and
 * fogged, like vanilla's weather. Ported from the 26.x line's PrecipitationRenderer, whose math this keeps: the rain
 * and snow are written in one pass over the drops into two buffers of their own, rain drawn untextured, then snow with
 * the snow texture. Written straight into memory in the particle vertex format rather than through BufferBuilder's
 * calls, which cost more than the math for thousands of quads, and handed to the tessellator in one copy. Render
 * thread only.
 */
@SideOnly(Side.CLIENT)
final class DropRenderer {

	// A rain streak's length, half width and alpha: for the smallest drop, and how much more for the largest
	static final float RAIN_MIN_LENGTH = 0.35F;
	static final float RAIN_LENGTH_RANGE = 0.65F;
	static final float RAIN_MIN_HALF_WIDTH = 0.015F;
	static final float RAIN_HALF_WIDTH_RANGE = 0.015F;
	static final float RAIN_MIN_ALPHA = 0.25F;
	static final float RAIN_ALPHA_RANGE = 0.25F;
	// A flake's half size: the smallest, and how much more the largest is. Most are small (see snowSize).
	static final float SNOW_MIN_HALF_SIZE = 0.05F;
	static final float SNOW_HALF_SIZE_RANGE = 0.07F;
	static final float SNOW_ALPHA = 0.9F;
	// How fast a flake turns, as a fraction of how fast it sways: about half a turn a second
	static final float SNOW_SPIN = 0.25F;
	// How fast a puff of dust turns, as a fraction of how fast it rises and falls (the field's dust isn't drawn here)
	static final float DUST_SPIN = 0.1F;
	/**
	 * How far behind the camera a drop can be and still reach into view, beyond the length of the longest streak or
	 * the largest flake's half size: room to spare
	 */
	static final float BEHIND_SPARE = 0.9F;
	/**
	 * Rain and snow far off are thinned out: all are drawn out to this share of the way to the edge of the cylinder,
	 * then a share falling off steadily to {@link #THIN_KEEP_AT_EDGE} at the edge
	 */
	static final float THIN_FROM_SHARE = 1F / 3F;
	static final float THIN_KEEP_AT_EDGE = 0.2F;
	// A drop being left out, or let back in, as the camera moves fades over this much of the share, rather than popping
	static final float THIN_FADE = 0.1F;
	// Drops this close to the cylinder's edge fade out, so they don't vanish all at once as the camera moves
	static final float EDGE_FADE = 2F;

	// The particle format: position (3 floats), texture (2 floats), color (4 bytes), lightmap (2 shorts), in ints
	private static final int VERTEX_INTS = 7;
	private static final int QUAD_INTS = VERTEX_INTS * 4;
	private static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

	/**
	 * Quads written into memory, to be handed to the tessellator
	 */
	static final class Quads {
		private ByteBuffer bytes = GLAllocation.createDirectByteBuffer(QUAD_INTS * 4 * 1024);
		private IntBuffer ints = this.bytes.asIntBuffer();
		private int at;

		void begin(final int most) {
			final int needed = most * QUAD_INTS * 4;
			if (this.bytes.capacity() < needed) {
				this.bytes = GLAllocation.createDirectByteBuffer(Math.max(needed, this.bytes.capacity() * 3 / 2));
				this.ints = this.bytes.asIntBuffer();
			}
			this.at = 0;
		}

		int count() {
			return this.at / QUAD_INTS;
		}

		void vertex(final float x, final float y, final float z, final float u, final float v, final int color,
				final int light) {
			final IntBuffer b = this.ints;
			final int i = this.at;
			b.put(i, Float.floatToRawIntBits(x));
			b.put(i + 1, Float.floatToRawIntBits(y));
			b.put(i + 2, Float.floatToRawIntBits(z));
			b.put(i + 3, Float.floatToRawIntBits(u));
			b.put(i + 4, Float.floatToRawIntBits(v));
			b.put(i + 5, color);
			b.put(i + 6, light);
			this.at = i + VERTEX_INTS;
		}

		/**
		 * Draws the quads with what is bound and set
		 */
		void draw() {
			if (this.at == 0)
				return;
			final Tessellator tessellator = Tessellator.getInstance();
			final BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
			this.bytes.position(0);
			this.bytes.limit(this.at * 4);
			buffer.putBulkData(this.bytes);
			this.bytes.clear();
			tessellator.draw();
		}
	}

	/**
	 * A color as the vertex format holds it: red, green, blue and alpha bytes, in that order in memory
	 */
	static int color(final float red, final float green, final float blue, final float alpha) {
		final int r = (int) (red * 255F) & 0xFF;
		final int g = (int) (green * 255F) & 0xFF;
		final int b = (int) (blue * 255F) & 0xFF;
		final int a = (int) (MathHelper.clamp(alpha, 0F, 1F) * 255F) & 0xFF;
		return LITTLE_ENDIAN ? a << 24 | b << 16 | g << 8 | r : r << 24 | g << 16 | b << 8 | a;
	}

	/**
	 * Packed light as the vertex format holds it: block light's short, then sky light's (as BufferBuilder.lightmap
	 * writes them), which on a little-endian machine is the packed light as it is
	 */
	static int light(final int packed) {
		return LITTLE_ENDIAN ? packed : (packed & 0xFFFF) << 16 | packed >>> 16;
	}

	/**
	 * Where the camera is and which way it faces, for building the drops
	 */
	static final class View {
		final double x, y, z;
		// The camera's left and up, as particles are turned, and the way it looks
		final float leftX, leftY, leftZ, upX, upY, upZ, lookX, lookY, lookZ;
		// How far behind the position the camera can be (third person), so drops between are kept
		final float behind;

		View(final double x, final double y, final double z, final float leftX, final float leftY, final float leftZ,
				final float upX, final float upY, final float upZ, final float behind) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.leftX = leftX;
			this.leftY = leftY;
			this.leftZ = leftZ;
			this.upX = upX;
			this.upY = upY;
			this.upZ = upZ;
			// Left across up is forward
			this.lookX = leftY * upZ - leftZ * upY;
			this.lookY = leftZ * upX - leftX * upZ;
			this.lookZ = leftX * upY - leftY * upX;
			this.behind = behind;
		}

		boolean sees(final float px, final float py, final float pz, final float margin) {
			return px * this.lookX + py * this.lookY + pz * this.lookZ >= -(margin + this.behind);
		}
	}

	private DropRenderer() {
	}

	/**
	 * Writes the falling rain into {@code rain} and the snow into {@code snow}, around the camera. Positions are
	 * relative to it, as vanilla's weather draws.
	 *
	 * @param snowflakes the flakes of the snow texture; empty to draw plain flakes (untextured, with the rain)
	 * @param largest    the largest of them, in pixels: the others are drawn in proportion
	 */
	static void build(@Nonnull final PrecipitationField field, @Nonnull final Quads rain, @Nonnull final Quads snow,
			@Nonnull final View view, final float partialTick, @Nonnull final List<Snowflakes.Flake> snowflakes,
			final int largest, @Nonnull final RainColor rainColor) {
		final PrecipitationField.Drops d = field.drops();
		final int falling = d.count - d.dustCount;
		rain.begin(falling);
		snow.begin(falling);
		if (falling <= 0)
			return;

		final float radius = field.radius();
		final float margin = Math.max(rainLength(1F), SNOW_MIN_HALF_SIZE + SNOW_HALF_SIZE_RANGE) + BEHIND_SPARE;
		// How much further each snowflake has turned since its last tick: the same for all, so worked out once
		final float snowTurn = partialTick * PrecipitationField.SNOW_SWAY_RATE * SNOW_SPIN;
		final float snowCos = MathHelper.cos(snowTurn), snowSin = MathHelper.sin(snowTurn);
		final float rainRed = rainColor.red, rainGreen = rainColor.green, rainBlue = rainColor.blue;
		// A streak's tail is clear
		final int rainTail = color(rainRed, rainGreen, rainBlue, 0F);
		final int white = color(1F, 1F, 1F, 1F) & (LITTLE_ENDIAN ? 0x00FFFFFF : 0xFFFFFF00);

		final PackedDrops packed = field.packed(view.x, view.y, view.z);
		final float[] r = packed.data;
		final float offsetX = (float) (packed.anchorX - view.x);
		final float offsetY = (float) (packed.anchorY - view.y);
		final float offsetZ = (float) (packed.anchorZ - view.z);
		final int end = packed.solid * PackedDrops.STRIDE;

		for (int at = 0; at < end; at += PackedDrops.STRIDE) {
			final float x = r[at + PackedDrops.PREV_X] + partialTick * r[at + PackedDrops.MOVE_X] + offsetX;
			final float y = r[at + PackedDrops.PREV_Y] + partialTick * r[at + PackedDrops.MOVE_Y] + offsetY;
			final float z = r[at + PackedDrops.PREV_Z] + partialTick * r[at + PackedDrops.MOVE_Z] + offsetZ;
			if (!view.sees(x, y, z, margin))
				continue;
			final byte kind = (byte) r[at + PackedDrops.KIND];
			final float distance = MathHelper.sqrt(x * x + z * z);
			final float fade = PrecipitationField.fade(kind, r[at + PackedDrops.AGE], r[at + PackedDrops.LIFETIME],
					r[at + PackedDrops.COVERED], partialTick) * MathHelper.clamp((radius - distance) / EDGE_FADE, 0F, 1F)
					* thinned(r[at + PackedDrops.THINNING], distance, radius);
			if (fade <= 0F)
				continue;
			final int light = light((int) r[at + PackedDrops.LIGHT]);
			final float size = r[at + PackedDrops.SIZE];

			if (kind == PrecipitationField.SNOW) {
				final int alpha = (int) (SNOW_ALPHA * fade * 255F) & 0xFF;
				final int c = LITTLE_ENDIAN ? white | alpha << 24 : white | alpha;
				final float spinCos = r[at + PackedDrops.A], spinSin = r[at + PackedDrops.B];
				final float cos = spinCos * snowCos - spinSin * snowSin;
				final float sin = spinSin * snowCos + spinCos * snowSin;
				float half = SNOW_MIN_HALF_SIZE + SNOW_HALF_SIZE_RANGE * size;
				if (snowflakes.isEmpty()) {
					// Plain flakes, drawn untextured with the rain
					billboard(rain, view, x, y, z, cos, sin, half, 0F, 0F, 0F, 0F, c, light);
				} else {
					// A flake of a pixel is drawn smaller than one of three, in proportion
					final Snowflakes.Flake flake = snowflakes
							.get(PackedDrops.variant(r[at + PackedDrops.PICK], snowflakes.size()));
					half *= flake.size / (float) largest;
					billboard(snow, view, x, y, z, cos, sin, half, flake.u0, flake.v0, flake.u1, flake.v1, c, light);
				}
			} else {
				final float length = rainLength(size);
				final float halfWidth = RAIN_MIN_HALF_WIDTH + RAIN_HALF_WIDTH_RANGE * size;
				// Along the way the drop is moving, its tail trailing up and upwind, turned about its own length to
				// face the camera
				final float vx = r[at + PackedDrops.A], vy = r[at + PackedDrops.B], vz = r[at + PackedDrops.C];
				// Across the streak: square to both its length and the line to the camera
				float sx = vy * z - vz * y, sy = vz * x - vx * z, sz = vx * y - vy * x;
				final float across = sx * sx + sy * sy + sz * sz;
				if (across > 1.0E-6F) {
					final float scale = halfWidth / MathHelper.sqrt(across);
					sx *= scale;
					sy *= scale;
					sz *= scale;
				} else {
					// Looking straight along the streak, where any turn will do
					sx = view.leftX * halfWidth;
					sy = view.leftY * halfWidth;
					sz = view.leftZ * halfWidth;
				}
				final float tx = x - vx * length, ty = y - vy * length, tz = z - vz * length;
				final float a = (RAIN_MIN_ALPHA + RAIN_ALPHA_RANGE * size) * fade;
				final int head = color(rainRed, rainGreen, rainBlue, a);
				rain.vertex(x - sx, y - sy, z - sz, 0F, 0F, head, light);
				rain.vertex(tx - sx, ty - sy, tz - sz, 0F, 0F, rainTail, light);
				rain.vertex(tx + sx, ty + sy, tz + sz, 0F, 0F, rainTail, light);
				rain.vertex(x + sx, y + sy, z + sz, 0F, 0F, head, light);
			}
		}
	}

	/**
	 * Writes the snow flecks into {@code quads}, around the camera, to be drawn as the falling snow is (the snow texture
	 * bound, or untextured if {@code snowflakes} is empty). Each lies flat on the face it landed on, the top of a block
	 * or a side it was blown into, as the flake it fell as, at the flake's size, fading and shrinking as it melts.
	 *
	 * @param snowflakes the flakes of the snow texture; empty to draw plain flecks
	 * @param largest    the largest of them, in pixels: the others are drawn in proportion
	 */
	static void build(@Nonnull final SnowFlecks flecks, @Nonnull final Quads quads, @Nonnull final View view,
			final float partialTick, @Nonnull final List<Snowflakes.Flake> snowflakes, final int largest) {
		quads.begin(flecks.count);
		for (int i = 0; i < flecks.count; i++) {
			final float remaining = flecks.remaining(i, partialTick);
			if (remaining <= 0F)
				continue;
			final float x = (float) (flecks.x[i] - view.x);
			final float y = (float) (flecks.y[i] - view.y);
			final float z = (float) (flecks.z[i] - view.z);
			float half = (SNOW_MIN_HALF_SIZE + SNOW_HALF_SIZE_RANGE * flecks.size[i]) * SnowFlecks.sizeFor(remaining);
			float u0 = 0F, v0 = 0F, u1 = 0F, v1 = 0F;
			if (!snowflakes.isEmpty()) {
				final Snowflakes.Flake flake = snowflakes.get(PackedDrops.variant(flecks.variant[i], snowflakes.size()));
				half *= flake.size / (float) largest;
				u0 = flake.u0;
				v0 = flake.v0;
				u1 = flake.u1;
				v1 = flake.v1;
			}
			final int color = color(1F, 1F, 1F, SNOW_ALPHA * remaining);
			final int light = light(flecks.light[i]);
			// The corners, each a quarter turn from the last, as far out as a falling flake's (see billboard), in the
			// plane of the face it lies on
			final float corner = half * SQRT_OF_TWO;
			final float cu = flecks.turnCos[i] * corner, cv = flecks.turnSin[i] * corner;
			final byte face = flecks.face[i];
			fleckCorner(quads, face, x, y, z, cu, cv, u0, v0, color, light);
			fleckCorner(quads, face, x, y, z, -cv, cu, u0, v1, color, light);
			fleckCorner(quads, face, x, y, z, -cu, -cv, u1, v1, color, light);
			fleckCorner(quads, face, x, y, z, cv, -cu, u1, v0, color, light);
		}
	}

	private static final float SQRT_OF_TWO = (float) Math.sqrt(2D);

	/**
	 * Adds a corner of a fleck lying on a face, offset from its center by (u, v) in the plane of the face: x and z on
	 * the top of a block, z and y on a west or east side, x and y on a north or south side
	 */
	private static void fleckCorner(final Quads quads, final byte face, final float x, final float y, final float z,
			final float u, final float v, final float texU, final float texV, final int color, final int light) {
		switch (face) {
		case SnowFlecks.FACE_WEST:
		case SnowFlecks.FACE_EAST:
			quads.vertex(x, y + v, z + u, texU, texV, color, light);
			break;
		case SnowFlecks.FACE_NORTH:
		case SnowFlecks.FACE_SOUTH:
			quads.vertex(x + u, y + v, z, texU, texV, color, light);
			break;
		default:
			quads.vertex(x + u, y, z + v, texU, texV, color, light);
		}
	}

	/**
	 * The share of the rain and snow drawn at a distance from the camera, horizontally: all of it near, falling off
	 * steadily to {@link #THIN_KEEP_AT_EDGE} at the edge of the cylinder
	 */
	static float shareDrawn(final float distance, final float radius) {
		final float from = radius * THIN_FROM_SHARE;
		final float across = MathHelper.clamp((distance - from) / (radius - from), 0F, 1F);
		return 1F + (THIN_KEEP_AT_EDGE - 1F) * across;
	}

	/**
	 * How much of a drop is drawn at a distance, 0 to 1: drops whose fixed random number is under the share drawn
	 * there are drawn whole, those over it not at all, and those near it fade
	 */
	static float thinned(final float thinning, final float distance, final float radius) {
		return MathHelper.clamp((shareDrawn(distance, radius) - thinning) / THIN_FADE + 1F, 0F, 1F);
	}

	/**
	 * A rain streak's length, for a drop of the given size, 0 to 1
	 */
	static float rainLength(final float dropSize) {
		return RAIN_MIN_LENGTH + RAIN_LENGTH_RANGE * dropSize;
	}

	/**
	 * A textured square facing the camera, turned in its plane by the angle whose cos and sin are given
	 */
	private static void billboard(final Quads quads, final View view, final float x, final float y, final float z,
			final float turnCos, final float turnSin, final float halfSize, final float u0, final float v0,
			final float u1, final float v1, final int color, final int light) {
		final float cos = turnCos * halfSize, sin = turnSin * halfSize;
		final float ax = view.leftX * cos + view.upX * sin, ay = view.leftY * cos + view.upY * sin,
				az = view.leftZ * cos + view.upZ * sin;
		final float bx = view.upX * cos - view.leftX * sin, by = view.upY * cos - view.leftY * sin,
				bz = view.upZ * cos - view.leftZ * sin;
		quads.vertex(x - ax - bx, y - ay - by, z - az - bz, u1, v1, color, light);
		quads.vertex(x - ax + bx, y - ay + by, z - az + bz, u1, v0, color, light);
		quads.vertex(x + ax + bx, y + ay + by, z + az + bz, u0, v0, color, light);
		quads.vertex(x + ax - bx, y + ay - by, z + az - bz, u0, v1, color, light);
	}
}
