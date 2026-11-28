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

package org.orecruncher.dsurround.client.fx.light;

import java.nio.FloatBuffer;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.ARBShaderObjects;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.shader.Shaders;
import org.orecruncher.dsurround.client.weather.particle.ParticleWeather;
import org.orecruncher.dsurround.lib.profiling.RenderTimer;
import org.orecruncher.dsurround.profiling.RenderTimers;
import org.orecruncher.lib.gfx.shaders.ShaderProgram;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Small point lights, lighting the grass, leaves and ground near them: around fireflies. From the 26.x line. Each is
 * drawn with a shader that works out from the scene's depth where each pixel near the light is, and adds light by how
 * close it is and whether it faces the light. Being added on top of the scene, it doesn't change Minecraft's lighting,
 * and it can't cast shadows.
 * <p>
 * Particles {@link #add} their light as they are drawn, and the lights are all drawn together at the end of the
 * world's drawing (after the translucent blocks and the weather), from a copy of the depth made then. Translucent blocks
 * leave no depth in 1.12.2, so the light falls on what is under water or behind glass.
 * <p>
 * Without shaders, under an OptiFine shader pack, or after a failure to draw them, there are no lights. Render thread
 * only.
 */
@SideOnly(Side.CLIENT)
public final class PointLights {

	/**
	 * How far a firefly's light reaches, in blocks.
	 */
	public static final float FIREFLY_RADIUS = 1.75F;
	/**
	 * Strength of a firefly's light at its brightest, 0 to 1.
	 */
	public static final float FIREFLY_STRENGTH = 0.45F;

	// Lights recorded longer ago than this weren't from this frame (nothing lit was drawn), so are dropped
	private static final long STALE_NANOS = 250_000_000L;

	private static final RenderTimer RENDER_TIMER = RenderTimers.add(new RenderTimer("Point Lights"));
	private static final PointLightList LIGHTS = new PointLightList();
	private static long lastAdded;
	private static boolean failed;
	private static int drawn;

	// The copy of the scene's depth the shader reads, made each frame lights are drawn
	private static int depthTexture = -1;
	private static int depthWidth;
	private static int depthHeight;

	private PointLights() {
	}

	/**
	 * Whether lights can be drawn: shaders work here, the shader compiled, drawing hasn't failed, and no shader pack is
	 * in use. Each kind of light also has its own option.
	 */
	public static boolean isAvailable() {
		return !failed && Shaders.areShadersSupported() && Shaders.POINT_LIGHT != null
				&& !ParticleWeather.shaderPackInUse();
	}

	/**
	 * Whether fireflies light their surroundings
	 */
	public static boolean fireflyLightsOn() {
		return ModOptions.effects.enableFireflyLight && isAvailable();
	}

	/**
	 * How many lights were drawn in the last frame, for the diagnostics
	 */
	public static int drawn() {
		return drawn;
	}

	/**
	 * Records a light to draw this frame, its position relative to the camera, which is where it is drawn from: far
	 * from the world's middle, a float can't hold a position in the world finely enough, and the lights would jitter.
	 *
	 * @param strength 0 to 1
	 * @param radius   how far it reaches, in blocks
	 */
	public static void add(final float x, final float y, final float z, final float red, final float green,
			final float blue, final float strength, final float radius) {
		LIGHTS.add(x, y, z, red, green, blue, strength, radius);
		lastAdded = System.nanoTime();
	}

	@SubscribeEvent
	public static void onRenderWorldLast(@Nonnull final RenderWorldLastEvent event) {
		final int n = LIGHTS.size();
		drawn = 0;
		if (n == 0)
			return;
		try {
			if (System.nanoTime() - lastAdded <= STALE_NANOS && isAvailable())
				draw();
		} finally {
			LIGHTS.clear();
		}
	}

	private static void draw() {
		final ShaderProgram program = Shaders.POINT_LIGHT;
		final Framebuffer framebuffer = Minecraft.getMinecraft().getFramebuffer();
		final int width = framebuffer.framebufferWidth;
		final int height = framebuffer.framebufferHeight;
		RENDER_TIMER.begin();
		try {
			final int count = LIGHTS.prepare();
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
			copyDepth(width, height);

			// Only where the scene is nearer than the far side of the light's reach (see point_light.vert)
			GlStateManager.enableDepth();
			GlStateManager.depthFunc(GL11.GL_GREATER);
			GlStateManager.depthMask(false);
			GlStateManager.disableAlpha();
			GlStateManager.disableCull();
			GlStateManager.disableLighting();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE,
					GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);

			program.use();
			program.set("DepthSampler", 0);
			program.set("ScreenSize", (float) width, (float) height);

			final Tessellator tessellator = Tessellator.getInstance();
			final BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			final float[] lights = LIGHTS.lights;
			for (int l = 0; l < count; l++) {
				final int i = l * PointLightList.STRIDE;
				final float a = lights[i + 6] * PointLightList.rankFade(l);
				if (a < PointLightList.MIN_STRENGTH)
					continue;
				final float x = lights[i], y = lights[i + 1], z = lights[i + 2];
				final float r = lights[i + 3], g = lights[i + 4], b = lights[i + 5];
				// All four at the center; the vertex shader spreads them out. Each corner is given at the light's
				// radius, which is how the shader knows it
				final float radius = lights[i + 7];
				buffer.pos(x, y, z).tex(-radius, -radius).color(r, g, b, a).endVertex();
				buffer.pos(x, y, z).tex(radius, -radius).color(r, g, b, a).endVertex();
				buffer.pos(x, y, z).tex(radius, radius).color(r, g, b, a).endVertex();
				buffer.pos(x, y, z).tex(-radius, radius).color(r, g, b, a).endVertex();
				drawn++;
			}
			tessellator.draw();
			program.unUse();
		} catch (final Exception e) {
			failed = true;
			ARBShaderObjects.glUseProgramObjectARB(0);
			ModBase.log().error("Point lights couldn't be drawn; fireflies won't light their surroundings", e);
		} finally {
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.enableCull();
			GlStateManager.depthFunc(GL11.GL_LEQUAL);
			GlStateManager.depthMask(true);
			GlStateManager.enableDepth();
			GlStateManager.bindTexture(0);
			RENDER_TIMER.end();
		}
	}

	/**
	 * Copies the depth of the framebuffer being drawn into, bound for reading, into the depth texture, left bound to
	 * the current texture unit. (A blit into a texture of the framebuffer's own format measured no faster.)
	 */
	private static void copyDepth(final int width, final int height) {
		if (depthTexture < 0)
			depthTexture = GlStateManager.generateTexture();
		GlStateManager.bindTexture(depthTexture);
		if (width != depthWidth || height != depthHeight) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL11.GL_NONE);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0,
					GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (FloatBuffer) null);
			depthWidth = width;
			depthHeight = height;
		}
		GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
	}
}
