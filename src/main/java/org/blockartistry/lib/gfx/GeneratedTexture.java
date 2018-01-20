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

package org.blockartistry.lib.gfx;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;

/*
 * Simple handler class that wraps a shader that will be operating against a framebuffer
 * for later use.  Kinda like a dynamic texture.
 * 
 * Loosly based on:
 * https://github.com/Angry-Pixel/The-Betweenlands/blob/1.12/src/main/java/thebetweenlands/client/render/shader/postprocessing/PostProcessingEffect.java
 */
public abstract class GeneratedTexture {

	private static final FloatBuffer CLEAR_COLOR_BUFFER = GLAllocation.createDirectFloatBuffer(16);

	protected Framebuffer blit = null;

	public GeneratedTexture(final int width, final int height) {
		this.setSize(width, height);
	}

	public void setSize(final int width, final int height) {
		if (this.blit != null && (this.blit.framebufferWidth != width || this.blit.framebufferHeight != height)) {
			this.release();
		}

		if (this.blit == null) {
			this.blit = new Framebuffer(width, height, false);
			this.blit.setFramebufferColor(0F, 0F, 0F, 0F);
		}
	}

	public int getWidth() {
		return this.blit.framebufferWidth;
	}

	public int getHeight() {
		return this.blit.framebufferHeight;
	}

	public void bindTexture() {
		if (this.blit == null)
			throw new RuntimeException("Texture not available");

		this.blit.bindFramebufferTexture();
	}

	public void release() {
		if (this.blit != null) {
			this.blit.deleteFramebuffer();
			this.blit = null;
		}
	}

	public void updateTexture() {

		if (this.blit == null)
			throw new RuntimeException("Framebuffer not initialized");

		this.blit.framebufferClear();

		// Save the MC frame buffer and bind ours
		final Framebuffer mcFrameBuffer = Minecraft.getMinecraft().getFramebuffer();
		this.blit.bindFramebuffer(true);

		// Backup attributes
		GL11.glPushAttrib(GL11.GL_MATRIX_MODE | GL11.GL_VIEWPORT_BIT | GL11.GL_TRANSFORM_BIT);
		GL11.glGetFloat(GL11.GL_COLOR_CLEAR_VALUE, CLEAR_COLOR_BUFFER);

		// Backup matrices
		GlStateManager.pushMatrix();
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.pushMatrix();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.pushMatrix();

		// Set up 2D matrices
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.loadIdentity();
		GlStateManager.ortho(0.0D, this.getWidth(), this.getHeight(), 0.0D, 1000.0D, 3000.0D);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.loadIdentity();
		GlStateManager.translate(0.0F, 0.0F, -2000.0F);

		try {
			// Do the render supplied by a derived class
			render();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		// Restore matrices
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.popMatrix();

		// Restore attributes
		GlStateManager.popAttrib();
		GlStateManager.clearColor(CLEAR_COLOR_BUFFER.get(0), CLEAR_COLOR_BUFFER.get(1), CLEAR_COLOR_BUFFER.get(2),
				CLEAR_COLOR_BUFFER.get(3));

		// Restore matrices
		GlStateManager.popMatrix();

		// Bind back the MC framebuffer
		if (mcFrameBuffer != null)
			mcFrameBuffer.bindFramebuffer(true);
	}

	public abstract void render();

}
