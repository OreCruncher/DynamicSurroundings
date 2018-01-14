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

package org.blockartistry.lib.shaders;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import net.minecraft.client.renderer.GLAllocation;

public class ShaderUtils {

	private ShaderUtils() {

	}

	private static final FloatBuffer SCRATCH = GLAllocation.createDirectFloatBuffer(16);

	// Gets the model/view matrix into the shader
	public static void setModelViewMatrix(final String name, final ShaderProgram program) throws ShaderException {
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, SCRATCH);
		program.setMatrix4(name, SCRATCH);
		SCRATCH.clear();
	}

	// Gets the projection matrix into the shader
	public static void setProjectionMatrix(final String name, final ShaderProgram program) throws ShaderException {
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, SCRATCH);
		program.setMatrix4(name, SCRATCH);
		SCRATCH.clear();
	}

	public static void setMVPMatrix(final String name, final ShaderProgram program) throws ShaderException {
		final Matrix4f mv = new Matrix4f();
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, SCRATCH);
		mv.load(SCRATCH);
		SCRATCH.clear();

		final Matrix4f p = new Matrix4f();
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, SCRATCH);
		p.load(SCRATCH);
		SCRATCH.clear();

		final Matrix4f mvp = new Matrix4f();
		Matrix4f.mul(p, mv, mvp);
		mvp.store(SCRATCH);
		SCRATCH.clear();
		program.setMatrix4(name, SCRATCH);
	}
}
