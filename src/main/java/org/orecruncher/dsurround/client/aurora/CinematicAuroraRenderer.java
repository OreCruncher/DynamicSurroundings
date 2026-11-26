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

package org.orecruncher.dsurround.client.aurora;

import java.nio.FloatBuffer;

import javax.annotation.Nonnull;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.client.shader.Shaders;
import org.orecruncher.lib.gfx.OpenGlState;
import org.orecruncher.lib.gfx.OpenGlUtil;
import org.orecruncher.lib.gfx.shaders.ShaderProgram;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Draws a {@link CinematicAurora}: a pass over the whole screen that follows each pixel's line of sight up through the
 * layer of the sky the ribbons hang in, adding the light of each ribbon it passes through (shaders/aurora_cinematic.*).
 * Ported from the 26.x line.
 * <p>
 * Fancy draws it straight onto the screen, on the far plane, tested less-or-equal against the depth buffer, so it shows
 * only where nothing but the sky was drawn. Fast draws it at half the screen's size each way, to a framebuffer of its
 * own, then lays it over the screen with the composite shader the same way: a quarter of the pixels to march. Under
 * OptiFine, whose framebuffers aren't safe to use, Fast draws as Fancy does. The render thread only.
 */
@SideOnly(Side.CLIENT)
public final class CinematicAuroraRenderer {

	private static final FloatBuffer MATRIX = BufferUtils.createFloatBuffer(16);
	private static final FloatBuffer VECTORS = BufferUtils.createFloatBuffer(CinematicAurora.MAX_RIBBONS * 4);

	// Where Fast draws it, made on first use
	private static Framebuffer reduced;
	// Set once drawing fails: no cinematic aurora is drawn again until the game restarts (the classic one is)
	private static boolean failed;

	private CinematicAuroraRenderer() {
	}

	/**
	 * Whether the cinematic aurora can be drawn: its shaders built, and drawing hasn't failed since
	 */
	public static boolean isAvailable() {
		return !failed && Shaders.areShadersSupported() && Shaders.AURORA_CINEMATIC != null
				&& Shaders.AURORA_COMPOSITE != null;
	}

	/**
	 * Draws the aurora
	 *
	 * @param moment how active the night is now
	 * @param motion where its folds, wave and rays have moved to
	 * @param alpha  how strongly, 0 to 1
	 * @param fast   whether to draw it at half size
	 * @param time   seconds, for the animation
	 */
	public static void render(@Nonnull final CinematicAurora aurora, @Nonnull final AuroraNight.Moment moment,
			@Nonnull final CinematicAuroraMotion motion, final float alpha, final boolean fast, final float time) {
		if (alpha <= 0F || !isAvailable())
			return;

		final Minecraft mc = Minecraft.getMinecraft();
		final boolean half = fast && OpenGlUtil.areFrameBuffersSafe() && OpenGlHelper.isFramebufferEnabled();
		final int width = half ? Math.max(1, mc.displayWidth / 2) : mc.displayWidth;
		final int height = half ? Math.max(1, mc.displayHeight / 2) : mc.displayHeight;

		// From the screen back to directions: the inverse of the projection and the view as the world was drawn (view
		// bobbing and all)
		final Matrix4f projection = matrix(GL11.GL_PROJECTION_MATRIX);
		final Matrix4f view = matrix(GL11.GL_MODELVIEW_MATRIX);
		final Matrix4f inverse = Matrix4f.mul(projection, view, null);
		if (inverse.invert() == null)
			return;
		final CinematicAuroraFrame frame = CinematicAuroraFrame.of(aurora, moment, motion, alpha, projection.m11, time,
				height);

		final OpenGlState glState = OpenGlState.push();
		final boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
		try {
			GlStateManager.disableFog();
			GlStateManager.disableLighting();
			GlStateManager.disableAlpha();
			GlStateManager.disableCull();
			GlStateManager.enableBlend();
			// Light adds to what is there: the sky, or (at Fast) the cleared framebuffer
			GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
			GlStateManager.depthMask(false);
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
			GlStateManager.enableTexture2D();

			if (half) {
				final Framebuffer target = reduced(width, height);
				target.framebufferClear();
				target.bindFramebuffer(true);
				GlStateManager.disableDepth();
				march(frame, inverse);
				mc.getFramebuffer().bindFramebuffer(true);
				GlStateManager.enableDepth();
				GlStateManager.depthFunc(GL11.GL_LEQUAL);
				composite(target);
			} else {
				GlStateManager.enableDepth();
				GlStateManager.depthFunc(GL11.GL_LEQUAL);
				march(frame, inverse);
			}
		} catch (final Exception e) {
			failed = true;
			ModBase.log().error("The cinematic aurora couldn't be drawn; the classic one is drawn instead", e);
			if (half)
				mc.getFramebuffer().bindFramebuffer(true);
		} finally {
			if (fog)
				GlStateManager.enableFog();
			OpenGlState.pop(glState);
		}
	}

	// The pass over the whole of what is bound: the ribbons' light, marched along each pixel's line of sight
	private static void march(@Nonnull final CinematicAuroraFrame frame, @Nonnull final Matrix4f inverse)
			throws Exception {
		final ShaderProgram program = Shaders.AURORA_CINEMATIC;
		GlStateManager.bindTexture(AuroraRenderer.noiseTexture().getGlTextureId());
		program.use();
		try {
			final int id = program.getProgramId();
			MATRIX.clear();
			inverse.store(MATRIX);
			MATRIX.flip();
			ARBShaderObjects.glUniformMatrix4ARB(location(id, "InvViewProj"), false, MATRIX);
			vectors(id, "Frame", frame.header, 0, 1);
			vectors(id, "Look", frame.header, 1, 1);
			vectors(id, "Edge", frame.header, 2, 1);
			vectors(id, "RibbonAxis", frame.axis, 0, CinematicAurora.MAX_RIBBONS);
			vectors(id, "RibbonFold", frame.fold, 0, CinematicAurora.MAX_RIBBONS);
			vectors(id, "RibbonWave", frame.wave, 0, CinematicAurora.MAX_RIBBONS);
			vectors(id, "RibbonLight", frame.light, 0, CinematicAurora.MAX_RIBBONS);
			vectors(id, "RibbonCurl", frame.curl, 0, CinematicAurora.MAX_RIBBONS);
			program.set("NoiseSampler", 0);
			quad();
		} finally {
			program.unUse();
		}
	}

	// Lays what Fast drew over the screen
	private static void composite(@Nonnull final Framebuffer target) throws Exception {
		final ShaderProgram program = Shaders.AURORA_COMPOSITE;
		target.bindFramebufferTexture();
		program.use();
		try {
			program.set("AuroraSampler", 0);
			program.set("HalfPixel", 0.5F / target.framebufferTextureWidth, 0.5F / target.framebufferTextureHeight);
			quad();
		} finally {
			program.unUse();
		}
	}

	// A quad over the whole of what is bound, its corners in clip space (the shaders ignore the matrices)
	private static void quad() {
		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		buffer.pos(-1, -1, 0).endVertex();
		buffer.pos(1, -1, 0).endVertex();
		buffer.pos(1, 1, 0).endVertex();
		buffer.pos(-1, 1, 0).endVertex();
		tessellator.draw();
	}

	@Nonnull
	private static Framebuffer reduced(final int width, final int height) {
		if (reduced == null) {
			reduced = new Framebuffer(width, height, false);
			reduced.setFramebufferColor(0F, 0F, 0F, 0F);
			reduced.setFramebufferFilter(GL11.GL_LINEAR);
		} else if (reduced.framebufferWidth != width || reduced.framebufferHeight != height) {
			reduced.createBindFramebuffer(width, height);
			reduced.setFramebufferFilter(GL11.GL_LINEAR);
		}
		return reduced;
	}

	@Nonnull
	private static Matrix4f matrix(final int which) {
		MATRIX.clear();
		GL11.glGetFloat(which, MATRIX);
		final Matrix4f m = new Matrix4f();
		m.load(MATRIX);
		return m;
	}

	private static int location(final int program, @Nonnull final String name) {
		return ARBShaderObjects.glGetUniformLocationARB(program, name);
	}

	// Uploads count vec4s of values, from the vector at first, to a uniform (an array's first element for an array)
	private static void vectors(final int program, @Nonnull final String name, @Nonnull final float[] values,
			final int first, final int count) {
		final int location = location(program, name);
		if (location < 0)
			return;
		VECTORS.clear();
		VECTORS.put(values, first * 4, count * 4);
		VECTORS.flip();
		ARBShaderObjects.glUniform4ARB(location, VECTORS);
	}
}
