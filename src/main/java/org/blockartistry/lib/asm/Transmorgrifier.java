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

package org.blockartistry.lib.asm;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class Transmorgrifier {

	private final String className;

	public Transmorgrifier(final String className) {
		this.className = className;
	}

	public int classWriterFlags() {
		return ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES;
	}

	/*
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

	/*
	 * Indicates whether the transmorgrification process should executed. Override
	 * to provide custom behavior, such as configuration control.
	 */
	public boolean isEnabled() {
		return true;
	}

	public boolean matches(final String name) {
		return this.className.equals(name);
	}

	/*
	 * Override to provide ASM logic.
	 */
	public abstract boolean transmorgrify(final ClassNode cn);

	// Helper methods
	protected MethodNode findMethod(final ClassNode cn, final String name, final String signature) {
		for (final MethodNode m : cn.methods)
			if (m.name.equals(name) && m.desc.equals(signature))
				return m;
		return null;
	}

	protected MethodNode findMethod(final ClassNode cn, final String[] name, final String[] signature) {
		for (int i = 0; i < name.length; i++) {
			final MethodNode m = findMethod(cn, name[i], signature[i]);
			if (m != null)
				return m;
		}
		return null;
	}

	protected MethodNode findCTOR(final ClassNode cn, final String signature) {
		return findMethod(cn, "<init>", signature);
	}

	protected LocalVariableNode findLocalVariable(final MethodNode m, final String name) {
		for (final LocalVariableNode v : m.localVariables)
			if (v.name.equals(name))
				return v;
		return null;
	}

	protected void logMethod(final Logger log, final MethodNode node, final String message) {
		final String text = String.format("%s%s: %s", node.name, node.desc, message);
		log.info(text);
	}
}
