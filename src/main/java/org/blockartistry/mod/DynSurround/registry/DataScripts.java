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
import java.io.Reader;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.scripts.IScriptingEngine;
import org.blockartistry.mod.DynSurround.scripts.JsonScriptingEngine;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;

public final class DataScripts {

	// DSurround.dataDirectory()
	private File dataDirectory;

	// "/assets/dsurround/data/"
	private String assetDirectory;
	private IScriptingEngine exe;
	private Side side;

	public DataScripts(final Side side) {
		this(side, DSurround.dataDirectory(), "/assets/dsurround/data/");
	}

	public DataScripts(@Nonnull Side side, @Nonnull final File file, @Nonnull final String assetDirectory) {
		this.side = side;
		this.dataDirectory = file;
		this.assetDirectory = assetDirectory;
	}

	public boolean execute(@Nonnull final List<InputStream> resources) {

		if (!this.init())
			return false;

		for (final ModContainer mod : Loader.instance().getActiveModList()) {
			runFromArchive(mod.getModId());
		}

		for(final InputStream stream: resources) {
			try(final InputStreamReader reader = new InputStreamReader(stream)) {
				runFromStream(reader);
			} catch (final Throwable t) {
				ModLog.error("Unable to read script from resource pack!", t);
			}
		}
		
		// Load scripts specified in the configuration
		final String[] configFiles = ModOptions.externalScriptFiles;
		for (final String file : configFiles) {
			runFromDirectory(file);
		}

		return true;
	}

	private boolean init() {
		this.exe = new JsonScriptingEngine(this.side);
		return this.exe.initialize();
	}

	private void runFromStream(@Nonnull final Reader reader) {
		if(reader != null)
			this.exe.eval(reader);
	}
	
	private void runFromArchive(@Nonnull final String dataFile) {
		final String fileName = StringUtils.appendIfMissing(
				StringUtils.appendIfMissing(assetDirectory, "/") + dataFile.replaceAll("[^a-zA-Z0-9.-]", "_"),
				this.exe.preferredExtension()).toLowerCase();

		try (final InputStream stream = DataScripts.class.getResourceAsStream(fileName)) {
			if (stream != null) {
				ModLog.info("%s: Executing script for mod [%s]", this.side.toString(), dataFile);
				this.exe.eval(new InputStreamReader(stream));
			}
		} catch (final Throwable t) {
			ModLog.error("Unable to run script!", t);
		}
	}

	private void runFromDirectory(@Nonnull final String dataFile) {
		final String workingFile = StringUtils.appendIfMissing(Paths.get(dataFile).getFileName().toString(),
				this.exe.preferredExtension());
		final File file = new File(dataDirectory, workingFile);
		if (!file.exists()) {
			ModLog.warn("Could not locate script file [%s]", file.toString());
			return;
		}

		if (!file.isFile()) {
			ModLog.warn("Script file [%s] is not a file", file.toString());
			return;
		}

		try (final InputStream stream = new FileInputStream(file)) {
			ModLog.info("%s: Executing script [%s]", this.side.toString(), file.toString());
			this.exe.eval(new InputStreamReader(stream));
		} catch (final Throwable t) {
			ModLog.error("Unable to run script!", t);
		}
	}
}
