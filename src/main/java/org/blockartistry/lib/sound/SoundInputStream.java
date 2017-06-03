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

package org.blockartistry.lib.sound;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

public class SoundInputStream extends InputStream {

	protected final InputStream stream;
	
	public SoundInputStream(@Nonnull final InputStream stream) {
		this.stream = stream;
	}
	
	@Override
	public int read() throws IOException {
		try {
			return this.stream.read();
		} catch(@Nonnull final Throwable t) {
			return -1;
		}
	}
	
	@Override
	public int available() throws IOException {
		try {
			return this.stream.available();
		} catch(@Nonnull final Throwable t) {
			return 0;
		}
	}
	
	@Override
	public void close() {
		try {
			this.stream.close();
		} catch(@Nonnull final Throwable t) {
			;
		}
	}
	
	@Override
	public void mark(final int readlimit) {
		this.stream.mark(readlimit);
	}
	
	@Override
	public boolean markSupported() {
		return this.stream.markSupported();
	}
	
	@Override
	public void reset() throws IOException {
		this.stream.reset();
	}

}
