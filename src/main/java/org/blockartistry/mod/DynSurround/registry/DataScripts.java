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
import java.util.ArrayList;
import java.util.List;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.Module;
import org.blockartistry.mod.DynSurround.scripts.ScriptingEngine;

import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public final class DataScripts {
	
	public static interface IDependent {
		void clear();
	}
	
	private static List<IDependent> dependents = new ArrayList<IDependent>();
	public static void registerDependent(final IDependent dep) {
		dependents.add(dep);
	}
	
	private static void clearDependents() {
		for(final IDependent dep: dependents)
			dep.clear();
	}
	

	// Module.dataDirectory()
	private File dataDirectory;

	// "/assets/dsurround/data/"
	private String assetDirectory;
	private ScriptingEngine exe;

	public DataScripts(final File file, final String assetDirectory) {
		this.dataDirectory = file;
		this.assetDirectory = assetDirectory;
	}

	public static void initialize(final IResourceManager resources) {
		clearDependents();
		final DataScripts scripts = new DataScripts(Module.dataDirectory(), "/assets/dsurround/data/");
		scripts.init();

		for (final ModContainer mod : Loader.instance().getActiveModList()) {
			scripts.runFromArchive(mod.getModId());
		}
		
		if(resources != null) {
			
		}
	}

	private boolean init() {
		this.exe = new ScriptingEngine();
		return this.exe.initialize();
	}

	private void runFromArchive(final String dataFile) {
		final String fileName = dataFile.replaceAll("[^a-zA-Z0-9.-]", "_");
		InputStream stream = null;

		try {
			stream = DataScripts.class.getResourceAsStream(assetDirectory + fileName + ".scala");
			if (stream != null) {
				ModLog.info("Executing script for mod [%s]", dataFile);
				runFromStream(stream);
			}
		} catch (final Throwable t) {
			ModLog.error("Unable to run script!", t);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (final Throwable t) {
				;
			}
		}
	}

	@SuppressWarnings("unused")
	private void runFromDirectory(final String dataFile) {
		final File file = new File(dataDirectory, dataFile);
		InputStream stream = null;

		try {
			stream = new FileInputStream(file);
			if (stream != null) {
				ModLog.info("Executing script [%s] from directory", dataFile);
				runFromStream(stream);
			}
		} catch (final Throwable t) {
			ModLog.error("Unable to run script!", t);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (final Throwable t) {
				;
			}
		}
	}

	private void runFromStream(final InputStream stream) {
		try {
			if (stream != null)
				this.exe.eval(new InputStreamReader(stream));
		} catch (final Throwable t) {
			ModLog.error("Unable to run script!", t);
		}
	}
}
