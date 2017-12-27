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

import static org.objectweb.asm.Opcodes.ASM5;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;

public abstract class MyTransformer implements IClassTransformer {

	protected final Logger logger;

	private final List<Transmorgrifier> morgers = new ArrayList<Transmorgrifier>();

	public MyTransformer(final Logger logger) {
		this.logger = logger;
		this.initTransmorgrifiers();
	}

	protected abstract void initTransmorgrifiers();

	public void addTransmorgrifier(final Transmorgrifier t) {
		this.morgers.add(t);
	}

	@Override
	public byte[] transform(final String name, final String transformedName, byte[] classBytes) {

		for (final Transmorgrifier t : this.morgers)
			if (t.matches(transformedName) && t.isEnabled()) {

				try {
					final ClassReader cr = new ClassReader(classBytes);
					final ClassNode cn = new ClassNode(ASM5);
					cr.accept(cn, 0);

					this.logger.info(String.format("Transmorgrifying [%s]: %s", transformedName, t.name()));
					final boolean modified = t.transmorgrify(cn);

					final ClassWriter cw = new ClassWriter(t.classWriterFlags());
					cn.accept(cw);
					classBytes = cw.toByteArray();

					if (modified) {
						this.logger.info(String.format("Transmorgrified [%s]: %s", transformedName, t.name()));
					}
					
				} catch (final Throwable ex) {
					ex.printStackTrace();
					throw ex;
				}
			}

		return classBytes;
	}

}
