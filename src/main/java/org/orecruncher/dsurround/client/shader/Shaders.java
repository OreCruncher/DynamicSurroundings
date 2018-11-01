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
package org.orecruncher.dsurround.client.shader;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.lib.gfx.shaders.ShaderProgram;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

public final class Shaders {

	public static final ShaderProgram AURORA;

	static {

		AURORA = register("Aurora", new ResourceLocation(ModBase.MOD_ID, "shaders/aurora.vert"),
				new ResourceLocation(ModBase.MOD_ID, "shaders/aurora.frag"));
	}

	public static boolean areShadersSupported() {
		return OpenGlHelper.areShadersSupported();
	}

	private static ShaderProgram register(final String name, final ResourceLocation vertex,
			final ResourceLocation fragment) {
		try {
			return ShaderProgram.createProgram(name, vertex, fragment);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}
}
