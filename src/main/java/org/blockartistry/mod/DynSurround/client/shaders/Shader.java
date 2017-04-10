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

package org.blockartistry.mod.DynSurround.client.shaders;

import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.util.gui.ShaderUtils;
import org.blockartistry.mod.DynSurround.util.gui.ShaderUtils.ParameterBindings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum Shader {

	LIGHT_SOURCE(null, "halo.frag");

	private int id = 0;
	private final String fragment;
	private final String vertex;

	private Shader(@Nullable final String vert, @Nullable final String frag) {
		this.vertex = vert == null ? null : "/assets/dsurround/shader/" + vert;
		this.fragment = frag == null ? null : "/assets/dsurround/shader/" + frag;
	}

	public ParameterBindings aquire() {
		return ShaderUtils.useShader(this.id);
	}

	public void release() {
		ShaderUtils.closeShader();
	}

	private void load() {
		this.id = ShaderUtils.createProgram(this.vertex, this.fragment);
	}

	public static void init() {
		if (ShaderUtils.useShaders())
			for (final Shader s : Shader.values())
				s.load();
	}

}
