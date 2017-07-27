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

import java.lang.reflect.Method;
import java.net.URL;

import javax.annotation.Nonnull;
import javax.sound.sampled.AudioFormat;

import org.blockartistry.lib.LibLog;

import net.minecraftforge.fml.relauncher.ReflectionHelper;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.codecs.CodecJOrbis;

public final class CodecJOrbisWrapper implements ICodec {

	private static Method eos;

	static {
		try {
			eos = ReflectionHelper.findMethod(CodecJOrbis.class, "endOfStream", null, boolean.class,
					boolean.class);
		} catch (final Throwable t) {
			throw new RuntimeException("Unable to locate CodecJOrbis::endOfStream()!");
		}
	}

	private final ICodec wrapped = new CodecJOrbis();
	private boolean isEOS;

	private void terminateStream() {
		try {
			this.isEOS = true;
			eos.invoke(this.wrapped, true, true);
		} catch (final Throwable t) {
			LibLog.log().error("Unable to terminate sound stream!", t);
		}
	}

	@Override
	public void reverseByteOrder(final boolean b) {
		this.wrapped.reverseByteOrder(b);
	}

	@Override
	public boolean initialize(@Nonnull final URL url) {
		return this.wrapped.initialize(url);
	}

	@Override
	public boolean initialized() {
		return this.wrapped.initialized();
	}

	@Override
	public SoundBuffer read() {
		try {
			return this.wrapped.read();
		} catch (final Throwable t) {
			LibLog.log().error("trapping CodecJOrbisWrapper::read() error", t);
			terminateStream();
		}
		return null;
	}

	@Override
	public SoundBuffer readAll() {
		try {
			return this.wrapped.readAll();
		} catch (final Throwable t) {
			LibLog.log().error("trapping CodecJOrbisWrapper::readAll() error", t);
			terminateStream();
		}
		return null;
	}

	@Override
	public boolean endOfStream() {
		return this.isEOS || this.wrapped.endOfStream();
	}

	@Override
	public void cleanup() {
		this.wrapped.cleanup();
	}

	@Override
	public AudioFormat getAudioFormat() {
		return this.wrapped.getAudioFormat();
	}

}
