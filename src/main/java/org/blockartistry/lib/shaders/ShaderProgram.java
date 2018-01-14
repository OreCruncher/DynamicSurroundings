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

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.io.Streams;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class ShaderProgram {

	protected final String name;
	protected int programId = -1;
	protected boolean isDrawing = false;

	protected int textureIndex = 0;

	public ShaderProgram(final String name) {
		if (StringUtils.isEmpty(name))
			this.name = "UNKNOWN";
		else
			this.name = name;
	}

	protected void initialize(final String vertexSrc, final String fragmentSrc) throws ShaderException {

		int vertShader = 0;
		int fragShader = 0;

		vertShader = createShader(vertexSrc, ARBVertexShader.GL_VERTEX_SHADER_ARB);
		fragShader = createShader(fragmentSrc, ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);

		if (vertShader == 0 || fragShader == 0)
			throw new ShaderException(this.name, "Unable to intialize shader!");

		this.programId = ARBShaderObjects.glCreateProgramObjectARB();

		if (this.programId != 0) {
			ARBShaderObjects.glAttachObjectARB(this.programId, vertShader);
			ARBShaderObjects.glAttachObjectARB(this.programId, fragShader);

			ARBShaderObjects.glLinkProgramARB(this.programId);
			if (ARBShaderObjects.glGetObjectParameteriARB(this.programId,
					ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
				throw new ShaderException(this.name, getLogInfo(this.programId));
			}

			ARBShaderObjects.glValidateProgramARB(this.programId);
			if (ARBShaderObjects.glGetObjectParameteriARB(this.programId,
					ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
				throw new ShaderException(this.name, getLogInfo(this.programId));
			}
		}
	}

	public final String getName() {
		return this.name;
	}

	public final int getProgramId() {
		return this.programId;
	}

	public final boolean isValid() {
		return this.programId != 0;
	}

	public final void use() throws ShaderException {
		this.use(null);
	}

	public final void unUse() throws ShaderException {
		try {
			final String log = getLogInfo(this.programId);
			if (!StringUtils.isEmpty(log))
				throw new ShaderException(this.name, log);
		} finally {
			ARBShaderObjects.glUseProgramObjectARB(0);
		}
	}
	
	public final void delete() {
		if(this.isValid()) {
			ARBShaderObjects.glUseProgramObjectARB(0);
			ARBShaderObjects.glDeleteObjectARB(this.programId);
		}
	}

	public final void use(final IShaderUseCallback callback) throws ShaderException {

		this.textureIndex = 0;
		ARBShaderObjects.glUseProgramObjectARB(this.programId);

		if (callback != null)
			callback.call(this);
	}

	protected final int getUniform(final String name) throws ShaderException {
		if (!this.isValid())
			throw new ShaderException(this.name, "ShaderProgram is not valid!");

		final int id = ARBShaderObjects.glGetUniformLocationARB(this.programId, name);
		if (id == -1)
			throw new ShaderException(this.name, String.format("Unknown uniform '%s'", name));

		return id;
	}

	public void set(final String name, final float... value) throws ShaderException {
		if (value.length == 0 || value.length > 4)
			throw new ShaderException(this.name, "Invalid number of elements");

		final int id = getUniform(name);
		if (value.length == 1) {
			ARBShaderObjects.glUniform1fARB(getUniform(name), value[0]);
		} else {
			final FloatBuffer buff = BufferUtils.createFloatBuffer(value.length);
			for (int i = 0; i < value.length; i++)
				buff.put(value[i]);
			buff.flip();
			switch (value.length) {
			case 1:
				ARBShaderObjects.glUniform1ARB(id, buff);
				break;
			case 2:
				ARBShaderObjects.glUniform2ARB(id, buff);
				break;
			case 3:
				ARBShaderObjects.glUniform3ARB(id, buff);
				break;
			case 4:
				ARBShaderObjects.glUniform4ARB(id, buff);
				break;
			}

		}
	}
	
	public void setMatrix4(final String name, final FloatBuffer matrix) throws ShaderException {
		ARBShaderObjects.glUniformMatrix4ARB(getUniform(name), false, matrix);
	}

	public void set(final String name, final Color color) throws ShaderException {
		this.set(name, color, 1.0F);
	}

	public void set(final String name, final Color color, final float alpha) throws ShaderException {
		this.set(name, color.red, color.green, color.blue, alpha);
	}

	public void set(final String name, final Vec3d vector) throws ShaderException {
		this.set(name, (float) vector.x, (float) vector.y, (float) vector.z);
	}

	public void set(final String name, final Vec3i vector) throws ShaderException {
		this.set(name, vector.getX(), vector.getY(), vector.getZ());
	}

	public void set(final String name, final Vec2f vector) throws ShaderException {
		this.set(name, vector.x, vector.y);
	}

	public void set(final String name, final int value) throws ShaderException {
		final int id = getUniform(name);
		ARBShaderObjects.glUniform1iARB(id, value);
	}

	public void set(final String name, final ResourceLocation texture) throws ShaderException {
		final int textureId = this.textureIndex++;
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit + textureId);
		GlStateManager.enableTexture2D();
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
	}

	protected void initialize(final ResourceLocation vertexResource, final ResourceLocation fragmentResource)
			throws Exception {
		final String vertex = Streams.readResourceAsString(vertexResource);
		final String fragment = Streams.readResourceAsString(fragmentResource);
		this.initialize(vertex, fragment);
	}
	
	public static ShaderProgram createProgram(final String name, final ResourceLocation vertex, final ResourceLocation fragment) throws Exception {
		final ShaderProgram prog = new ShaderProgram(name);
		prog.initialize(vertex, fragment);
		return prog;
	}

	private int createShader(final String src, final int shaderType) throws ShaderException {
		int shader = 0;
		try {
			shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

			if (shader == 0)
				return 0;

			ARBShaderObjects.glShaderSourceARB(shader, src);
			ARBShaderObjects.glCompileShaderARB(shader);

			if (ARBShaderObjects.glGetObjectParameteriARB(shader,
					ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
				throw new ShaderException(this.name, getLogInfo(shader));

			return shader;
		} catch (final Exception exc) {
			ARBShaderObjects.glDeleteObjectARB(shader);
			throw exc;
		}
	}

	private static String getLogInfo(final int obj) {
		return ARBShaderObjects.glGetInfoLogARB(obj,
				ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}

	@FunctionalInterface
	public static interface IShaderUseCallback {
		void call(final ShaderProgram program) throws ShaderException;
	}
}
