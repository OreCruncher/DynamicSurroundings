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

import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.lib.gfx.shaders.ShaderProgram;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

public final class Shaders {

	public static final ShaderProgram AURORA;
	public static final ShaderProgram AURORA_CINEMATIC;
	public static final ShaderProgram AURORA_COMPOSITE;
	public static final ShaderProgram DROPS;
	public static final ShaderProgram FROST;

	static {

		AURORA = register("Aurora", new ResourceLocation(ModInfo.MOD_ID, "shaders/aurora_classic.vert"),
				new ResourceLocation(ModInfo.MOD_ID, "shaders/aurora_classic.frag"));
		AURORA_CINEMATIC = register("Cinematic Aurora", new ResourceLocation(ModInfo.MOD_ID, "shaders/aurora_cinematic.vert"),
				new ResourceLocation(ModInfo.MOD_ID, "shaders/aurora_cinematic.frag"));
		AURORA_COMPOSITE = register("Aurora Composite", new ResourceLocation(ModInfo.MOD_ID, "shaders/aurora_composite.vert"),
				new ResourceLocation(ModInfo.MOD_ID, "shaders/aurora_composite.frag"));
		DROPS = register("Particle Weather", new ResourceLocation(ModInfo.MOD_ID, "shaders/drops.vert"),
				new ResourceLocation(ModInfo.MOD_ID, "shaders/drops.frag"));
		FROST = register("Window Frost", new ResourceLocation(ModInfo.MOD_ID, "shaders/frost.vert"),
				new ResourceLocation(ModInfo.MOD_ID, "shaders/frost.frag"));
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
