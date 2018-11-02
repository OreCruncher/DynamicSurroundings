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

package org.orecruncher.dsurround.registry.config;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

/**
 * Iterator that traverses the compressed memory block that contains all the
 * cached Json config files for the session.
 */
class MCFIterator implements Iterator<ModConfiguration>, Closeable {

	private final Gson gson;
	private final JsonReader reader;

	protected MCFIterator(@Nonnull final byte[] bits) throws IOException {
		this.gson = new GsonBuilder().create();
		this.reader = new JsonReader(new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(bits))));
		this.reader.beginArray();
	}

	@Override
	public boolean hasNext() {
		try {
			return this.reader.hasNext();
		} catch (@Nonnull final IOException ex) {
			ModBase.log().error("Unable to read from memory!", ex);
		}
		return false;
	}

	@Override
	@Nullable
	public ModConfiguration next() {
		String source = null;
		try {
			// Should be a string followed by our Json object
			source = this.gson.fromJson(this.reader, String.class);
			final ModConfiguration mcf = this.gson.fromJson(this.reader, ModConfiguration.class);
			mcf.source = source;
			return mcf;
		} catch (@Nonnull final JsonSyntaxException | JsonIOException ex) {
			ModBase.log().error(source != null ? source : "Unable to parse Json from memory!", ex);
			return null;
		}
	}

	@Override
	public void close() throws IOException {
		this.reader.close();
	}

}
