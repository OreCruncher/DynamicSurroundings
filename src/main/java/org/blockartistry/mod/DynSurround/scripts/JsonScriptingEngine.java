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

package org.blockartistry.mod.DynSurround.scripts;

import java.io.Reader;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.Module;
import org.blockartistry.mod.DynSurround.scripts.json.ConfigurationScript;

import net.minecraftforge.fml.relauncher.Side;

public class JsonScriptingEngine implements IScriptingEngine {

	private final Side side;
	
	public JsonScriptingEngine(@Nonnull final Side side) {
		this.side = side;
	}
	
	@Override
	public String getEngineName() {
		return "Json Scripting Engine";
	}

	@Override
	public String getEngineVersion() {
		return Module.VERSION;
	}
	
	@Override
	public String preferredExtension() {
		return ".json";
	}

	@Override
	public boolean initialize() {
		return true;
	}

	@Override
	public Object eval(@Nonnull final Reader reader) {
		ConfigurationScript.process(this.side, reader);
		return null;
	}

}
