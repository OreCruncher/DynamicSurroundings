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

package org.orecruncher.dsurround.client.weather.frost;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.orecruncher.dsurround.client.shader.Shaders;
import org.orecruncher.lib.gfx.shaders.ShaderProgram;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Draws the frost on windows (ported from the 26.x line): each frosted face as a grid of small quads just off the
 * glass, with the frost texture laid across it in the world's coordinates, so a window of many blocks shows one
 * pattern. Each corner of the grid carries how far the frost has grown there, in its alpha: more near the window's
 * edges, where frost starts, and its bottom, where it gathers, than in its middle. The shader ({@code frost.vert} and
 * {@code frost.frag}) shows the texels of the {@link FrostTexture} that have frosted by then. Positions are relative to
 * the camera. Render thread only.
 */
public final class FrostRenderer {

	// The size of a cell of the grid a face is drawn as, in blocks
	static final float CELL = 0.25F;
	// How many blocks the texture covers each way
	static final float TEXTURE_BLOCKS = 4F;
	// How far frost grows past what the face has, at a window's edge, and how far short of it at its middle
	static final float EDGE_AHEAD = 0.6F;
	static final float MIDDLE_BEHIND = 0.6F;
	// The bottom edge counts as nearer than it is: frost gathers at the bottom of a window
	static final float BOTTOM_NEARER = 0.6F;
	// Faces with less frost than this aren't drawn
	static final float MIN_SHOWN = 0.01F;
	// The frost's color, before light: a little blue
	static final float RED = 0.88F, GREEN = 0.94F, BLUE = 1F;

	private boolean failed;
	private int fog = -1, sampler, lightmap;

	/**
	 * Whether the frost can be drawn: shaders work here, its shader compiled, and drawing it hasn't failed
	 */
	public boolean isAvailable() {
		return !this.failed && Shaders.areShadersSupported() && Shaders.FROST != null;
	}

	/**
	 * Draws the frost around the camera, with the frost texture bound to the first texture unit, the lightmap to the
	 * second, and the blend and depth set
	 *
	 * @return how many faces were drawn
	 */
	@SideOnly(Side.CLIENT)
	int draw(@Nonnull final FrostField field, final double camX, final double camY, final double camZ) {
		final ShaderProgram program = Shaders.FROST;
		try {
			program.use();
			if (this.fog < 0)
				locate(program.getProgramId());
			ARBShaderObjects.glUniform4fARB(this.fog, fogMode(), 0F, 0F, 0F);
			ARBShaderObjects.glUniform1iARB(this.sampler, 0);
			ARBShaderObjects.glUniform1iARB(this.lightmap, 1);
			final Tessellator tessellator = Tessellator.getInstance();
			final BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
			final int drawn = build(field, buffer, camX, camY, camZ);
			tessellator.draw();
			program.unUse();
			return drawn;
		} catch (final Exception e) {
			this.failed = true;
			ARBShaderObjects.glUseProgramObjectARB(0);
			org.orecruncher.dsurround.ModBase.log().error("Window frost couldn't be drawn; it is off from now on", e);
			return 0;
		}
	}

	private void locate(final int program) {
		this.fog = ARBShaderObjects.glGetUniformLocationARB(program, "Fog");
		this.sampler = ARBShaderObjects.glGetUniformLocationARB(program, "Sampler0");
		this.lightmap = ARBShaderObjects.glGetUniformLocationARB(program, "Lightmap");
	}

	/**
	 * The fog as the fixed-function pipeline has it now: 0 none, 1 linear, 2 exp, 3 exp2
	 */
	private static float fogMode() {
		if (!GL11.glIsEnabled(GL11.GL_FOG))
			return 0F;
		switch (GL11.glGetInteger(GL11.GL_FOG_MODE)) {
		case GL11.GL_EXP:
			return 2F;
		case GL11.GL_EXP2:
			return 3F;
		default:
			return 1F;
		}
	}

	/**
	 * Writes the frost's quads around the camera, in the particle format, to be drawn with the frost's shader and
	 * texture
	 *
	 * @return how many faces were written
	 */
	static int build(@Nonnull final FrostField field, @Nonnull final BufferBuilder buffer, final double camX,
			final double camY, final double camZ) {
		final float range = FrostField.RANGE;
		final float rangeSq = range * range;
		final int[] drawn = { 0 };
		field.forEachFace(face -> {
			if (face.shown < MIN_SHOWN)
				return;
			final double dx = face.x + 0.5D - camX, dy = face.y + 0.5D - camY, dz = face.z + 0.5D - camZ;
			if (dx * dx + dy * dy + dz * dz > rangeSq)
				return;
			drawFace(buffer, face, camX, camY, camZ);
			drawn[0]++;
		});
		return drawn[0];
	}

	private static void drawFace(@Nonnull final BufferBuilder buffer, @Nonnull final FrostFace face,
			final double camX, final double camY, final double camZ) {
		// Fully frosted, the frost is the same all over, so one quad does: a window frosts fully on cold nights
		final boolean uniform = isUniform(face);
		final int cellsU = uniform ? 1 : Math.max(1, MathHelper.ceil((float) (face.u1 - face.u0) / CELL));
		final int cellsV = uniform ? 1 : Math.max(1, MathHelper.ceil((face.v1 - face.v0) / CELL));
		final double stepU = (face.u1 - face.u0) / cellsU;
		final float stepV = (face.v1 - face.v0) / cellsV;
		// The texture's origin: on a whole number of its repeats, and moved along by an amount of the window's own, so
		// windows side by side don't show the same frost
		final boolean alongX = face.side.getAxis() == EnumFacing.Axis.Z;
		final int blockU = alongX ? face.x : face.z;
		final float originU = TEXTURE_BLOCKS * MathHelper.floor(blockU / TEXTURE_BLOCKS);
		final float originV = TEXTURE_BLOCKS * MathHelper.floor(face.y / TEXTURE_BLOCKS);
		final int planeBlock = alongX ? face.z : face.x;
		final float offsetU = shift(planeBlock, face.side.getHorizontalIndex());
		final float offsetV = shift(planeBlock, face.side.getHorizontalIndex() + 4);
		final int sky = face.light >> 16 & 0xFFFF, block = face.light & 0xFFFF;

		for (int j = 0; j < cellsV; j++) {
			final float va = face.v0 + j * stepV, vb = va + stepV;
			for (int i = 0; i < cellsU; i++) {
				final double ua = face.u0 + i * stepU, ub = ua + stepU;
				corner(buffer, face, alongX, ua, va, originU, originV, offsetU, offsetV, camX, camY, camZ, sky, block);
				corner(buffer, face, alongX, ub, va, originU, originV, offsetU, offsetV, camX, camY, camZ, sky, block);
				corner(buffer, face, alongX, ub, vb, originU, originV, offsetU, offsetV, camX, camY, camZ, sky, block);
				corner(buffer, face, alongX, ua, vb, originU, originV, offsetU, offsetV, camX, camY, camZ, sky, block);
			}
		}
	}

	private static void corner(@Nonnull final BufferBuilder buffer, @Nonnull final FrostFace face,
			final boolean alongX, final double u, final float v, final float originU, final float originV,
			final float offsetU, final float offsetV, final double camX, final double camY, final double camZ,
			final int sky, final int block) {
		final double x = (alongX ? u : face.plane) - camX;
		final double y = v - camY;
		final double z = (alongX ? face.plane : u) - camZ;
		final float texU = (float) (u - originU) / TEXTURE_BLOCKS + offsetU;
		final float texV = (originV - v) / TEXTURE_BLOCKS + offsetV;
		buffer.pos(x, y, z).tex(texU, texV).color(RED, GREEN, BLUE, grown(face, u, v)).lightmap(sky, block)
				.endVertex();
	}

	/**
	 * Whether the frost has grown as far as it goes everywhere on a face (see {@link #grown})
	 */
	static boolean isUniform(@Nonnull final FrostFace face) {
		return face.shown >= 1F;
	}

	/**
	 * How far the frost has grown at a point on a face, 0 to 1: the frost the face has, ahead of that at the window's
	 * edges and behind it in its middle
	 */
	static float grown(@Nonnull final FrostFace face, final double u, final float v) {
		final float across = (float) Math.min(u - face.edgeU0, face.edgeU1 - u);
		final float edge = Math.min(across, Math.min((v - face.edgeV0) * BOTTOM_NEARER, face.edgeV1 - v));
		final float edgeness = 1F - MathHelper.clamp(edge / FrostFaces.EDGE_REACH, 0F, 1F);
		return MathHelper.clamp(
				face.shown * (1F + EDGE_AHEAD * edgeness) - MIDDLE_BEHIND * (1F - edgeness) * (1F - face.shown), 0F,
				1F);
	}

	/**
	 * An amount, 0 to 1, of a window's own: the same for every face in the plane of the glass
	 */
	private static float shift(final int plane, final int salt) {
		long h = (plane * 0x9E3779B97F4A7C15L) ^ (salt * 0xC2B2AE3D27D4EB4FL);
		h ^= h >>> 29;
		h *= 0xBF58476D1CE4E5B9L;
		h ^= h >>> 32;
		return (h & 0xFFFF) / 65536F;
	}
}
