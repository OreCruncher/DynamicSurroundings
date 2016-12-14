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

package org.blockartistry.mod.DynSurround.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.blockartistry.mod.DynSurround.ModLog;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class JsonUtils {

	@SuppressWarnings({ "unused" })
	public static <T> T load(final File file, final Class<T> clazz) throws Exception {
		InputStream stream = null;

		try {
			stream = new FileInputStream(file);
			if (stream != null)
				return load(stream, clazz);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (final Throwable t) {
				;
			}
		}
		return (T) clazz.newInstance();
	}

	public static <T> T load(final String modId, final Class<T> clazz) throws Exception {
		final String fileName = modId.replaceAll("[^a-zA-Z0-9.-]", "_");
		InputStream stream = null;

		try {
			stream = clazz.getResourceAsStream("/assets/dsurround/data/" + fileName + ".json");
			if (stream != null)
				return load(stream, clazz);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (final Throwable t) {
				;
			}
		}
		return (T) clazz.newInstance();
	}

	public static <T> T load(final Reader stream, final Class<T> clazz) {

		T result = null;
		try (final JsonReader reader = new JsonReader(stream)) {
			result = new Gson().fromJson(reader, clazz);
		} catch (final Throwable t) {
			ModLog.error("Unable to process Json from stream", t);;
		}
		return result;
	}

	public static <T> T load(final InputStream stream, final Class<T> clazz) {

		T result = null;

		try (final InputStreamReader reader = new InputStreamReader(stream)) {
			result = load(reader, clazz);
		} catch (final Throwable t) {
			ModLog.error("Unable to process Json from stream", t);;
		}

		return result;
	}

}
