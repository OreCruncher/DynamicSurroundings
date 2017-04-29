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

package org.blockartistry.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.ModLog;

import com.google.gson.Gson;

public final class JsonUtils {

	private JsonUtils() {
		
	}
	
	@SuppressWarnings("unused")
	@Nonnull
	public static <T> T load(@Nonnull final File file, @Nonnull final Class<T> clazz) throws Exception {
		try(final InputStream stream = new FileInputStream(file)) {
			if (stream != null)
				return load(stream, clazz);
		}
		return (T) clazz.newInstance();
	}

	@Nonnull
	public static <T> T load(@Nonnull final String modId, @Nonnull final Class<T> clazz) throws Exception {
		final String fileName = modId.replaceAll("[^a-zA-Z0-9.-]", "_");
		try(final InputStream stream = clazz.getResourceAsStream("/assets/dsurround/data/" + fileName + ".json")) {
			if (stream != null)
				return load(stream, clazz);
		}
		return (T) clazz.newInstance();
	}

	@Nullable
	public static <T> T load(@Nonnull final Reader stream, @Nonnull final Class<T> clazz) throws Exception {
		try {
			return new Gson().fromJson(stream, clazz);
		} catch (final Throwable t) {
			ModLog.error("Unable to process Json from stream", t);;
		}
		return (T) clazz.newInstance();
	}

	@Nullable
	public static <T> T load(@Nonnull final InputStream stream, @Nonnull final Class<T> clazz) throws Exception {
		try (final InputStreamReader reader = new InputStreamReader(stream)) {
			return new Gson().fromJson(reader, clazz);
		} catch (final Throwable t) {
			ModLog.error("Unable to process Json from stream", t);;
		}
		return (T) clazz.newInstance();
	}

}
