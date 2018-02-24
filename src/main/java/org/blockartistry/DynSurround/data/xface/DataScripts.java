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

package org.blockartistry.DynSurround.data.xface;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.lib.JsonUtils;

public final class DataScripts {

	private static final File dataDirectory = DSurround.dataDirectory();
	private static final String assetDirectory = "/assets/" + DSurround.MOD_ID + "/data/";

	@Nullable
	public static ModConfigurationFile loadFromStream(@Nonnull final Reader reader) {
		try {
			return JsonUtils.load(reader, ModConfigurationFile.class);
		} catch (@Nonnull final Exception ex) {
			DSurround.log().error("Unable to read stream!", ex);
		}
		return null;
	}

	@Nullable
	public static ModConfigurationFile loadFromArchive(@Nonnull final String modName) {
		final String fileName = StringUtils.appendIfMissing(
				StringUtils.appendIfMissing(assetDirectory, "/") + modName.replaceAll("[^a-zA-Z0-9.-]", "_"), ".json")
				.toLowerCase();

		try (final InputStream stream = DataScripts.class.getResourceAsStream(fileName)) {
			if (stream != null)
				return loadFromStream(new InputStreamReader(stream));
		} catch (@Nonnull final Throwable ex) {
			DSurround.log().error("Unable to run script!", ex);
		}
		return null;
	}

	@Nullable
	public static ModConfigurationFile loadFromDirectory(@Nonnull final String dataFile) {
		final String workingFile = StringUtils.appendIfMissing(Paths.get(dataFile).getFileName().toString(), ".json");
		final File file = new File(dataDirectory, workingFile);
		if (!file.exists()) {
			DSurround.log().warn("Could not locate script file [%s]", file.toString());
			return null;
		}

		if (!file.isFile()) {
			DSurround.log().warn("Script file [%s] is not a file", file.toString());
			return null;
		}

		try (final InputStream stream = new FileInputStream(file)) {
			return loadFromStream(new InputStreamReader(stream));
		} catch (@Nonnull final Throwable ex) {
			DSurround.log().error("Unable to run script!", ex);
		}

		return null;
	}
}
