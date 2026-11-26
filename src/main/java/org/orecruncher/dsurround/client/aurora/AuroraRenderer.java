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

import java.util.Random;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.client.shader.Shaders;
import org.orecruncher.lib.gfx.OpenGlState;
import org.orecruncher.lib.gfx.shaders.ShaderProgram;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Draws an aurora into the sky: each curtain a strip of quads, its lower edge following the curtain's path and its top
 * above that, filled in by the aurora shader (shaders/aurora.vert, aurora.frag). Rebuilt every frame, since the
 * curtains ripple; at a few hundred vertices that costs next to nothing. Ported from the 26.x line, where it costs
 * about 0.05 ms of GPU per million pixels it covers.
 * <p>
 * Drawn after the world, around the camera as the sky is, on the far plane: tested less-or-equal against the depth
 * buffer, it shows only where nothing but the sky was drawn, so terrain and clouds cover it. Light adds to the sky
 * behind it; curtains are seen from both sides. Without the shader there is no aurora. The render thread only.
 */
@SideOnly(Side.CLIENT)
public final class AuroraRenderer {

	/**
	 * The noise texture's size, in texels, which it repeats at. Wider than a curtain's finest noise: 23 cells to a unit
	 * along a curtain up to 10 across, and folded up to 0.675 either way, about 260; any fewer and the rays would
	 * repeat within one.
	 */
	static final int NOISE_SIZE = 512;
	private static final long NOISE_SEED = 0x6175726FL;

	// Quads along each curtain
	private static final int SEGMENTS = 96;

	private static final float[] BOTTOM = new float[3];
	private static final float[] TOP = new float[3];

	// Made on first use, on the render thread
	private static DynamicTexture noise;
	// Set once drawing fails: no aurora is drawn again until the game restarts, and the failure is logged once
	private static boolean failed;

	private AuroraRenderer() {
	}

	/**
	 * Whether auroras can be drawn: the shader built, and drawing hasn't failed since
	 */
	public static boolean isAvailable() {
		return !failed && Shaders.areShadersSupported() && Shaders.AURORA != null;
	}

	/**
	 * Draws the aurora
	 *
	 * @param aurora what to draw
	 * @param alpha  how strongly, 0 to 1
	 * @param time   seconds, for the animation
	 */
	public static void render(@Nonnull final Aurora aurora, final float alpha, final float time) {
		if (alpha <= 0F || !isAvailable())
			return;

		final ShaderProgram program = Shaders.AURORA;
		final OpenGlState glState = OpenGlState.push();
		final boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
		final int shadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
		try {
			GlStateManager.disableFog();
			GlStateManager.disableLighting();
			GlStateManager.disableAlpha();
			GlStateManager.disableCull();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
			GlStateManager.enableDepth();
			GlStateManager.depthFunc(GL11.GL_LEQUAL);
			GlStateManager.depthMask(false);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
			GlStateManager.enableTexture2D();
			GlStateManager.bindTexture(noise().getGlTextureId());

			final float strength = Math.max(0F, Math.min(1F, alpha * aurora.brightness));
			final AuroraPalette palette = aurora.palette;
			program.use(shader -> {
				shader.set("BottomColor", palette.bottom.red, palette.bottom.green, palette.bottom.blue);
				shader.set("MiddleColor", palette.middle.red, palette.middle.green, palette.middle.blue);
				shader.set("TopColor", palette.top.red, palette.top.green, palette.top.blue);
				shader.set("AuroraTime", time);
				shader.set("Alpha", strength);
				shader.set("NoiseSampler", 0);
			});
			try {
				drawCurtains(aurora, time);
			} finally {
				program.unUse();
			}
		} catch (final Exception e) {
			failed = true;
			ModBase.log().error("The aurora couldn't be drawn; auroras are off until the game restarts", e);
		} finally {
			GlStateManager.shadeModel(shadeModel);
			if (fog)
				GlStateManager.enableFog();
			OpenGlState.pop(glState);
		}
	}

	private static void drawCurtains(@Nonnull final Aurora aurora, final float time) {
		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		// Farthest curtain first; with additive blending the order doesn't change the result, but it's tidy
		for (int band = aurora.bands - 1; band >= 0; band--) {
			final float bandAlpha = aurora.bandBrightness(band);
			float prevS = -1F;
			float prevU = aurora.textureU(band, prevS);
			float prevA = Aurora.taper(prevS) * bandAlpha;
			aurora.bottom(band, prevS, time, BOTTOM);
			aurora.top(band, BOTTOM, TOP);
			float pbx = BOTTOM[0], pby = BOTTOM[1], pbz = BOTTOM[2];
			float ptx = TOP[0], pty = TOP[1], ptz = TOP[2];

			for (int i = 1; i <= SEGMENTS; i++) {
				final float s = -1F + 2F * i / SEGMENTS;
				final float u = aurora.textureU(band, s);
				final float a = Aurora.taper(s) * bandAlpha;
				aurora.bottom(band, s, time, BOTTOM);
				aurora.top(band, BOTTOM, TOP);

				vertex(buffer, pbx, pby, pbz, prevU, 0F, prevA);
				vertex(buffer, BOTTOM[0], BOTTOM[1], BOTTOM[2], u, 0F, a);
				vertex(buffer, TOP[0], TOP[1], TOP[2], u, 1F, a);
				vertex(buffer, ptx, pty, ptz, prevU, 1F, prevA);

				pbx = BOTTOM[0];
				pby = BOTTOM[1];
				pbz = BOTTOM[2];
				ptx = TOP[0];
				pty = TOP[1];
				ptz = TOP[2];
				prevU = u;
				prevA = a;
			}
		}
		tessellator.draw();
	}

	private static void vertex(@Nonnull final BufferBuilder buffer, final float x, final float y, final float z,
			final float u, final float v, final float alpha) {
		buffer.pos(x * Aurora.SCALE, y * Aurora.SCALE, z * Aurora.SCALE).tex(u, v).color(1F, 1F, 1F, alpha)
				.endVertex();
	}

	/**
	 * A random value a texel in red (and another in green), read smoothly between texels and repeating: the shader
	 * reads smooth value noise from it with a single read
	 */
	@Nonnull
	private static DynamicTexture noise() {
		if (noise == null) {
			noise = new DynamicTexture(NOISE_SIZE, NOISE_SIZE);
			final int[] pixels = noise.getTextureData();
			final Random reds = new XorShiftRandom(NOISE_SEED);
			final Random greens = new XorShiftRandom(NOISE_SEED ^ 0x9E3779B97F4A7C15L);
			for (int i = 0; i < pixels.length; i++)
				pixels[i] = 0xFF000000 | reds.nextInt(256) << 16 | greens.nextInt(256) << 8;
			noise.updateDynamicTexture();
			// Uploading leaves it nearest and repeating; the shader needs it read smoothly between texels
			GlStateManager.bindTexture(noise.getGlTextureId());
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		}
		return noise;
	}
}
