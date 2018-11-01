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

package org.orecruncher.lib.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.lib.LibLog;

import com.google.common.io.ByteStreams;

import net.minecraft.util.ResourceLocation;

public class Streams {

	public static void copy(@Nonnull final InputStream iStream, @Nonnull final OutputStream oStream)
			throws IOException {
		ByteStreams.copy(iStream, oStream);
	}

	public static void copy(@Nonnull final InputStream stream, @Nonnull final File outFile) {
		try (final FileOutputStream oFile = new FileOutputStream(outFile, false)) {
			copy(stream, oFile);
		} catch (final Throwable t) {
			LibLog.log().error("Unable to copy stream to file " + outFile.getName(), t);
		}
	}

	public static String readResourceAsString(final ResourceLocation resource) throws Exception {
		final String assetPath = String.format("/assets/%s/%s", resource.getNamespace(),
				resource.getPath());
		try (final InputStream stream = Streams.class.getResourceAsStream(assetPath)) {
			if (stream != null) {
				try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
					final StringBuilder source = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null)
						source.append(line).append('\n');
					return source.toString();
				}
			}
		}

		return StringUtils.EMPTY;
	}
}
