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

package org.blockartistry.mod.DynSurround.registry;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.Module;
import org.blockartistry.mod.DynSurround.scripts.ScriptingEngine;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public final class DataScripts {
	
	// Module.dataDirectory()
	private File dataDirectory;
	
	// "/assets/dsurround/data/"
	private String assetDirectory;
	private ScriptingEngine exe;
	
	public DataScripts(final File file, final String assetDirectory) {
		this.dataDirectory = file;
		this.assetDirectory = assetDirectory;
	}
	
	public static void initialize() {
		final DataScripts scripts = new DataScripts(Module.dataDirectory(),"/assets/dsurround/data/");
		scripts.init();
		
		for (final ModContainer mod : Loader.instance().getActiveModList()) {
			scripts.runFromArchive(mod.getModId());
		}
	}

	public boolean init() {
		this.exe = new ScriptingEngine();
		return this.exe.initialize();
	}
	
	public void runFromArchive(final String dataFile) {
		final String fileName = dataFile.replaceAll("[^a-zA-Z0-9.-]", "_");
		InputStream stream = null;

		try {
			stream = DataScripts.class.getResourceAsStream(assetDirectory + fileName + ".scala");
			runFromStream(stream);
		} catch(final Throwable t) {
			ModLog.error("Unable to run scala script!", t);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (final Throwable t) {
				;
			}
		}
	}
	
	public void runFromDirectory(final String dataFile) {
		final File file = new File(dataDirectory, dataFile);
		InputStream stream = null;

		try {
			stream = new FileInputStream(file);
			runFromStream(stream);
		} catch(final Throwable t) {
			ModLog.error("Unable to run scala script!", t);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (final Throwable t) {
				;
			}
		}
	}
	
	public void runFromStream(final InputStream stream) {
		try {
			if (stream != null)
				this.exe.eval(new InputStreamReader(stream));
		} catch(final Throwable t) {
			ModLog.error("Unable to run scala script!", t);
		}
	}
}
