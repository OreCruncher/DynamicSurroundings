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
import java.nio.FloatBuffer;
import java.util.List;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.orecruncher.dsurround.client.shader.Shaders;
import org.orecruncher.lib.gfx.shaders.ShaderProgram;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Rain and snow with their quads made on the GPU: the drops are uploaded into a vertex buffer when they change, once a
 * tick, four vertices a drop, each with the drop's packed record ({@link PackedDrops}) and which corner it is; the
 * vertex shader (shaders/drops.vert) makes each frame's quad from them for the frame's camera and partial tick, as
 * {@link DropRenderer#build} does on the CPU. Nothing is built each frame, and nothing is culled: the GPU clips what
 * is off screen.
 * <p>
 * OpenGL 2.1 only, as this line keeps to: a plain vertex buffer and vertex attributes, a vertex repeated for each
 * corner. The 26.x line reads one record a drop from a texel buffer by the vertex's index, which needs OpenGL 3.1.
 * Render thread only.
 */
@SideOnly(Side.CLIENT)
final class GpuDrops {

	// A vertex's floats: Prev (3), Move (3), Direction (3), Info (4), Life (4), TexLight (4)
	private static final int VERTEX_FLOATS = 21;
	private static final int STRIDE = VERTEX_FLOATS * 4;
	private static final String[] ATTRIBUTES = { "Prev", "Move", "Direction", "Info", "Life", "TexLight" };
	private static final int[] SIZES = { 3, 3, 3, 4, 4, 4 };
	// A texture coordinate the shader takes as "no texture"
	private static final float NO_TEXTURE = -1F;

	private ByteBuffer bytes = GLAllocation.createDirectByteBuffer(STRIDE * 4 * 1024);
	private FloatBuffer floats = this.bytes.asFloatBuffer();
	private int buffer = -1;
	private int vertices;
	// Where the uploaded drops were packed around
	private double anchorX, anchorY, anchorZ;
	// The program's attribute and uniform locations, looked up on first use
	private int[] attributes;
	private int offset, left, up, field, rainColor, fog, sampler, lightmap;
	private boolean failed;

	/**
	 * Whether drops can be made on the GPU: shaders, and the program compiled. Set off for good if drawing fails.
	 */
	boolean isAvailable() {
		return !this.failed && Shaders.areShadersSupported() && Shaders.DROPS != null;
	}

	/**
	 * Uploads the drops drawn, packed around the camera at the given position
	 *
	 * @param snowflakes the flakes of the snow texture; empty to draw plain flakes
	 * @param largest    the largest of them, in pixels
	 */
	void upload(@Nonnull final PrecipitationField field, final double cameraX, final double cameraY,
			final double cameraZ, @Nonnull final List<Snowflakes.Flake> snowflakes, final int largest) {
		final PackedDrops packed = field.packed(cameraX, cameraY, cameraZ);
		final int drops = packed.solid;
		this.vertices = drops * 4;
		this.anchorX = packed.anchorX;
		this.anchorY = packed.anchorY;
		this.anchorZ = packed.anchorZ;
		if (drops == 0)
			return;

		final int needed = this.vertices * STRIDE;
		if (this.bytes.capacity() < needed) {
			this.bytes = GLAllocation.createDirectByteBuffer(Math.max(needed, this.bytes.capacity() * 3 / 2));
			this.floats = this.bytes.asFloatBuffer();
		}
		final FloatBuffer out = this.floats;
		out.clear();
		final float[] r = packed.data;
		final float[] vertex = new float[VERTEX_FLOATS];
		for (int i = 0; i < drops; i++) {
			final int at = i * PackedDrops.STRIDE;
			final boolean snow = (byte) r[at + PackedDrops.KIND] == PrecipitationField.SNOW;
			final int light = (int) r[at + PackedDrops.LIGHT];
			float u0 = NO_TEXTURE, v0 = NO_TEXTURE, u1 = NO_TEXTURE, v1 = NO_TEXTURE, flakeScale = 1F;
			if (snow && !snowflakes.isEmpty()) {
				final Snowflakes.Flake flake = snowflakes
						.get(PackedDrops.variant(r[at + PackedDrops.PICK], snowflakes.size()));
				u0 = flake.u0;
				v0 = flake.v0;
				u1 = flake.u1;
				v1 = flake.v1;
				flakeScale = flake.size / (float) largest;
			}
			vertex[0] = r[at + PackedDrops.PREV_X];
			vertex[1] = r[at + PackedDrops.PREV_Y];
			vertex[2] = r[at + PackedDrops.PREV_Z];
			vertex[3] = r[at + PackedDrops.MOVE_X];
			vertex[4] = r[at + PackedDrops.MOVE_Y];
			vertex[5] = r[at + PackedDrops.MOVE_Z];
			vertex[6] = r[at + PackedDrops.A];
			vertex[7] = r[at + PackedDrops.B];
			vertex[8] = r[at + PackedDrops.C];
			vertex[9] = snow ? 1F : 0F;
			vertex[11] = r[at + PackedDrops.SIZE];
			vertex[12] = r[at + PackedDrops.THINNING];
			vertex[13] = r[at + PackedDrops.AGE];
			vertex[14] = r[at + PackedDrops.COVERED];
			vertex[15] = r[at + PackedDrops.LIFETIME];
			vertex[16] = flakeScale;
			vertex[19] = light & 0xFFFF;
			vertex[20] = light >>> 16;
			// The corners as DropRenderer writes them: a flake's texture turned with it
			for (int corner = 0; corner < 4; corner++) {
				vertex[10] = corner;
				vertex[17] = corner < 2 ? u1 : u0;
				vertex[18] = corner == 0 || corner == 3 ? v1 : v0;
				out.put(vertex);
			}
		}
		this.bytes.position(0);
		this.bytes.limit(needed);
		if (this.buffer == -1)
			this.buffer = OpenGlHelper.glGenBuffers();
		OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, this.buffer);
		OpenGlHelper.glBufferData(OpenGlHelper.GL_ARRAY_BUFFER, this.bytes, GL15.GL_STREAM_DRAW);
		OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
		this.bytes.clear();
	}

	/**
	 * Draws the drops last uploaded, with the snow texture bound to the first texture unit and the lightmap to the
	 * second, and the blend set
	 *
	 * @return false if it failed, and is off from now on
	 */
	boolean draw(final double cameraX, final double cameraY, final double cameraZ, final float partialTick,
			@Nonnull final DropRenderer.View view, final float radius, @Nonnull final RainColor color) {
		if (this.vertices == 0)
			return true;
		final ShaderProgram program = Shaders.DROPS;
		try {
			program.use();
			if (this.attributes == null)
				locate(program.getProgramId());
			ARBShaderObjects.glUniform4fARB(this.offset, (float) (this.anchorX - cameraX),
					(float) (this.anchorY - cameraY), (float) (this.anchorZ - cameraZ), partialTick);
			ARBShaderObjects.glUniform3fARB(this.left, view.leftX, view.leftY, view.leftZ);
			ARBShaderObjects.glUniform3fARB(this.up, view.upX, view.upY, view.upZ);
			ARBShaderObjects.glUniform4fARB(this.field, radius, 0F, 0F, 0F);
			ARBShaderObjects.glUniform3fARB(this.rainColor, color.red, color.green, color.blue);
			ARBShaderObjects.glUniform4fARB(this.fog, fogMode(), 0F, 0F, 0F);
			ARBShaderObjects.glUniform1iARB(this.sampler, 0);
			ARBShaderObjects.glUniform1iARB(this.lightmap, 1);

			OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, this.buffer);
			int at = 0;
			for (int i = 0; i < this.attributes.length; i++) {
				final int location = this.attributes[i];
				if (location >= 0) {
					GL20.glEnableVertexAttribArray(location);
					GL20.glVertexAttribPointer(location, SIZES[i], GL11.GL_FLOAT, false, STRIDE, at * 4L);
				}
				at += SIZES[i];
			}
			GL11.glDrawArrays(GL11.GL_QUADS, 0, this.vertices);
			for (final int location : this.attributes)
				if (location >= 0)
					GL20.glDisableVertexAttribArray(location);
			OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
			program.unUse();
			return true;
		} catch (final Exception e) {
			this.failed = true;
			ARBShaderObjects.glUseProgramObjectARB(0);
			org.orecruncher.dsurround.ModBase.log()
					.error("Particle weather couldn't be drawn on the GPU; it is built on the CPU from now on", e);
			return false;
		}
	}

	private void locate(final int program) {
		this.attributes = new int[ATTRIBUTES.length];
		for (int i = 0; i < ATTRIBUTES.length; i++)
			this.attributes[i] = GL20.glGetAttribLocation(program, ATTRIBUTES[i]);
		this.offset = ARBShaderObjects.glGetUniformLocationARB(program, "Offset");
		this.left = ARBShaderObjects.glGetUniformLocationARB(program, "Left");
		this.up = ARBShaderObjects.glGetUniformLocationARB(program, "Up");
		this.field = ARBShaderObjects.glGetUniformLocationARB(program, "Field");
		this.rainColor = ARBShaderObjects.glGetUniformLocationARB(program, "RainColor");
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
}
