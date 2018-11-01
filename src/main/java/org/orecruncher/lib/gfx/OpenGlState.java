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

package org.orecruncher.lib.gfx;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import net.minecraft.client.renderer.GlStateManager;

public final class OpenGlState {

	private final boolean enableBlend;
	private final int blendSource;
	private final int blendDest;
	private final int blendEquation;

	private final boolean enableAlphaTest;
	private final int alphaTestFunc;
	private final float alphaTestRef;

	private final boolean depthTest;
	private final int depthFunc;

	private final boolean cull;
	private GlStateManager.CullFace cullMode;

	private final boolean lighting;
	private final boolean depthMask;
	private final boolean normal;
	private final boolean rescaleNormal;
	private final boolean texture2D;

	private static int getInteger(final int parm) {
		return GL11.glGetInteger(parm);
	}

	private static float getFloat(final int parm) {
		return GL11.glGetFloat(parm);
	}

	private static boolean isSet(final int parm) {
		return getInteger(parm) == GL11.GL_TRUE;
	}

	private OpenGlState() {
		this.enableBlend = isSet(GL11.GL_BLEND);
		this.blendSource = getInteger(GL11.GL_BLEND_SRC);
		this.blendDest = getInteger(GL11.GL_BLEND_DST);
		this.blendEquation = getInteger(GL14.GL_BLEND_EQUATION);

		this.enableAlphaTest = isSet(GL11.GL_ALPHA_TEST);
		this.alphaTestFunc = getInteger(GL11.GL_ALPHA_TEST_FUNC);
		this.alphaTestRef = getFloat(GL11.GL_ALPHA_TEST_REF);

		this.depthTest = isSet(GL11.GL_DEPTH_TEST);
		this.depthFunc = getInteger(GL11.GL_DEPTH_FUNC);

		this.cull = isSet(GL11.GL_CULL_FACE);
		switch (getInteger(GL11.GL_CULL_FACE_MODE)) {
		case 1029:
			this.cullMode = GlStateManager.CullFace.BACK;
			break;
		case 1032:
			this.cullMode = GlStateManager.CullFace.FRONT_AND_BACK;
			break;
		case 1028:
		default:
			this.cullMode = GlStateManager.CullFace.FRONT;
		}

		this.lighting = isSet(GL11.GL_LIGHTING);
		this.depthMask = isSet(GL11.GL_DEPTH_WRITEMASK);
		this.normal = isSet(GL11.GL_NORMALIZE);
		this.rescaleNormal = isSet(GL12.GL_RESCALE_NORMAL);
		this.texture2D = isSet(GL11.GL_TEXTURE_2D);

		GlStateManager.pushMatrix();
	}

	public static OpenGlState push() {
		return new OpenGlState();
	}

	public static void pop(final OpenGlState state) {
		GlStateManager.popMatrix();

		if (state.enableBlend)
			GlStateManager.enableBlend();
		else
			GlStateManager.disableBlend();
		GlStateManager.blendFunc(state.blendSource, state.blendDest);
		GlStateManager.glBlendEquation(state.blendEquation);

		if (state.enableAlphaTest)
			GlStateManager.enableAlpha();
		else
			GlStateManager.disableAlpha();
		GlStateManager.alphaFunc(state.alphaTestFunc, state.alphaTestRef);

		if (state.depthTest)
			GlStateManager.enableDepth();
		else
			GlStateManager.disableDepth();
		GlStateManager.depthFunc(state.depthFunc);

		if (state.cull)
			GlStateManager.enableCull();
		else
			GlStateManager.disableCull();
		GlStateManager.cullFace(state.cullMode);

		if (state.lighting)
			GlStateManager.enableLighting();
		else
			GlStateManager.disableLighting();

		if (state.normal)
			GlStateManager.enableNormalize();
		else
			GlStateManager.disableNormalize();

		if (state.rescaleNormal)
			GlStateManager.enableRescaleNormal();
		else
			GlStateManager.disableRescaleNormal();

		if (state.depthTest)
			GlStateManager.enableDepth();
		else
			GlStateManager.disableDepth();

		if (state.texture2D)
			GlStateManager.enableTexture2D();
		else
			GlStateManager.disableTexture2D();

		GlStateManager.depthMask(state.depthMask);
	}

}
