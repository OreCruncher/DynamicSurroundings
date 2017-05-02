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

package org.blockartistry.lib.gui;

import java.io.StringWriter;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.blockartistry.lib.LibLog;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ShaderUtils {

	private ShaderUtils() {

	}

	public static boolean useShaders() {
		// TODO: Option to turn off shaders
		return false; //OpenGlHelper.shadersSupported;
	}

	// Most of the code taken from the LWJGL wiki
	// http://wiki.lwjgl.org/wiki/GLSL_Shaders_with_LWJGL.html

	public static int createProgram(@Nullable final ResourceLocation vert, @Nullable final ResourceLocation frag) {
		int vertId = 0, fragId = 0, program;
		if (vert != null)
			vertId = createShader(vert, ARBVertexShader.GL_VERTEX_SHADER_ARB);
		if (frag != null)
			fragId = createShader(frag, ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);

		program = OpenGlHelper.glCreateProgram();
		if (program == 0)
			return 0;

		if (vert != null)
			OpenGlHelper.glAttachShader(program, vertId);
		if (frag != null)
			OpenGlHelper.glAttachShader(program, fragId);

		OpenGlHelper.glLinkProgram(program);
		if (OpenGlHelper.glGetProgrami(program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
			LibLog.log().error(getLogInfoProgram(program), null);
			return 0;
		}

		GL20.glValidateProgram(program);
		if (OpenGlHelper.glGetProgrami(program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
			LibLog.log().error(getLogInfoProgram(program), null);
			return 0;
		}

		OpenGlHelper.glDeleteShader(vertId);
		OpenGlHelper.glDeleteShader(fragId);

		OpenGlHelper.glUseProgram(0);
		
		return program;
	}

	private static int createShader(@Nullable final ResourceLocation location, final int shaderType) {
		int shader = 0;
		try {

			shader = OpenGlHelper.glCreateShader(shaderType);

			if (shader == 0)
				return 0;

			OpenGlHelper.glShaderSource(shader, readFileAsString(location));
			OpenGlHelper.glCompileShader(shader);

			if (OpenGlHelper.glGetProgrami(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
				throw new RuntimeException("Error creating shader: " + getShaderLogInfo(shader));

			return shader;
		} catch (final Exception e) {
			OpenGlHelper.glDeleteProgram(shader);
			e.printStackTrace();
			return -1;
		}
	}

	private static String getLogInfoProgram(int obj) {
		return OpenGlHelper.glGetProgramInfoLog(obj,
				OpenGlHelper.glGetProgrami(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}

	private static String getShaderLogInfo(final int obj) {
		return OpenGlHelper.glGetShaderInfoLog(obj,
				OpenGlHelper.glGetShaderi(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}

	private static ByteBuffer readFileAsString(@Nonnull final ResourceLocation location) throws Exception {
		final StringWriter strBuf = new StringWriter();
		IOUtils.copy(Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream(), strBuf,
				"UTF-8");
		final byte[] bytes = strBuf.toString().getBytes();
		final ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
		buffer.put(bytes);
		buffer.position(0);
		return buffer;
	}

}
