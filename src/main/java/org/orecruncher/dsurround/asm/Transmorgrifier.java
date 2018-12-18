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

package org.orecruncher.dsurround.asm;

import javax.annotation.Nonnull;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Handles ASM modification for a class.
 */
public abstract class Transmorgrifier {

	private final String className;

	public Transmorgrifier(@Nonnull final String className) {
		this.className = className;
	}

	/**
	 * ClassWriter flags to use when processing the byte code.
	 *
	 * @return Set of flags to use
	 */
	public int classWriterFlags() {
		return ClassWriter.COMPUTE_MAXS;
	}

	/**
	 * Name of the Transmorgrifier. Used in logging to identify which set of logic
	 * was operating.
	 */
	@Nonnull
	public abstract String name();

	/**
	 * Obtains the name of the class this Transmorgrifier is targeting
	 *
	 * @return Target class name
	 */
	@Nonnull
	public String getClassName() {
		return this.className;
	}

	/**
	 * Indicates whether the transmorgrification process should executed. Override
	 * to provide custom behavior, such as configuration control.
	 */
	public boolean isEnabled() {
		return true;
	}

	/**
	 * Used by the Transformer to determine if a class that is being processed
	 * matches what the Transmorgifier is looking for.
	 *
	 * @param name Name of the class being processed
	 * @return true if the Transmorgrifier is interested, false otherwise
	 */
	public boolean matches(@Nonnull final String name) {
		return this.className.equals(name);
	}

	/**
	 * Override to provide ASM logic.
	 */
	public abstract boolean transmorgrify(final ClassNode cn);

	protected MethodNode findMethod(@Nonnull final ClassNode cn, @Nonnull final String[] signatures,
			@Nonnull final String[] names) {
		for (final MethodNode m : cn.methods)
			if (isOneOf(m.name, names) && isOneOf(m.desc, signatures))
				return m;
		return null;
	}

	protected boolean isOneOf(@Nonnull final String s, @Nonnull final String... possibles) {
		for (final String p : possibles)
			if (s.equals(p))
				return true;
		return false;
	}

	protected void logMethod(@Nonnull final Logger log, @Nonnull final MethodNode node, @Nonnull final String message) {
		final String text = String.format("%s%s: %s", node.name, node.desc, message);
		log.info(text);
	}
}
