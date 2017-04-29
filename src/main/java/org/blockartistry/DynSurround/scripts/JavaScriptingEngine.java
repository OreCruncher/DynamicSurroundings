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

package org.blockartistry.DynSurround.scripts;

import java.io.Reader;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.blockartistry.DynSurround.ModLog;

import scala.Some;
import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.IMain;
import scala.tools.nsc.settings.MutableSettings.BooleanSetting;

public final class JavaScriptingEngine implements IScriptingEngine {

	private IMain engine;

	public JavaScriptingEngine() {
		this.engine = (IMain) new ScriptEngineManager().getEngineByName("scala");
		final Settings settings = this.engine.settings();
		((BooleanSetting) settings.usejavacp()).value_$eq(true);
		settings.explicitParentLoader_$eq(new Some<ClassLoader>(new MyClassLoader()));
	}

	public String getEngineName() {
		return this.engine != null ? this.engine.getFactory().getEngineName() : "UNKNOWN";
	}

	public String getEngineVersion() {
		return this.engine != null ? this.engine.getFactory().getEngineVersion() : "UNKNOWN";
	}
	
	public String preferredExtension() {
		return ".ds";
	}

	public boolean initialize() {
		if (this.engine == null)
			return false;

		ModLog.debug("ScriptEngine: %s %s", getEngineName(), getEngineVersion());
		return true;
	}

	public Object eval(final String command) {
		Object result = null;
		try {
			result = this.engine.eval(command);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return result;
	}

	public Object eval(final Reader reader) {
		Object result = null;
		try {
			result = this.engine.eval(reader);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return result;
	}
}
