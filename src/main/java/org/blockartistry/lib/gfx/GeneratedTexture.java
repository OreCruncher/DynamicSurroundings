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

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;

/*
 * Simple handler class that wraps a shader that will be operating against a framebuffer
 * for later use.  Kinda like a dynamic texture.
 * 
 * Loosly based on:
 * https://github.com/Angry-Pixel/The-Betweenlands/blob/1.12/src/main/java/thebetweenlands/client/render/shader/postprocessing/PostProcessingEffect.java
 */
public abstract class GeneratedTexture {

	protected final DynamicTexture texture;
	protected final ResourceLocation resource;
	protected final int width;
	protected final int height;
	
	public GeneratedTexture(final String name, final int width, final int height) {
		this.width = width;
		this.height = height;
		this.texture = new DynamicTexture(this.width, this.height);
		this.resource = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(name, this.texture);
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public void bindTexture() {
		Minecraft.getMinecraft().getTextureManager().bindTexture(this.resource);
	}

	public void release() {
		this.texture.deleteGlTexture();
	}

	public void updateTexture() {
		// Save the MC frame buffer and bind ours
		final Framebuffer mcFrameBuffer = Minecraft.getMinecraft().getFramebuffer();
		final Framebuffer blit = new Framebuffer(this.width, this.height, false);
		blit.framebufferClear();
		blit.bindFramebuffer(true);

		// Backup attributes
		GL11.glPushAttrib(GL11.GL_MATRIX_MODE | GL11.GL_VIEWPORT_BIT | GL11.GL_TRANSFORM_BIT);

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
		GlStateManager.popMatrix();

		// Copy the data into our texture and update
	    blit.bindFramebufferTexture();
	    final IntBuffer pixelBuffer = BufferUtils.createIntBuffer(this.width * this.height);
	    GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
	    pixelBuffer.get(this.texture.getTextureData());
	    this.texture.updateDynamicTexture();
	    
		// Restore our state
	    blit.deleteFramebuffer();
		if (mcFrameBuffer != null)
			mcFrameBuffer.bindFramebuffer(true);
	}

	public abstract void render();

}
