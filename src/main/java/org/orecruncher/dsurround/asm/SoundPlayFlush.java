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

import java.util.Iterator;

import javax.annotation.Nonnull;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.orecruncher.dsurround.client.sound.SoundEngine;

/**
 * ASM patch to force the flush of the sound queue when playing a sound.
 */
public class SoundPlayFlush extends Transmorgrifier {

	public SoundPlayFlush() {
		super("net.minecraft.client.audio.SoundManager");
	}

	@Override
	public String name() {
		return "SoundManager playSound flush";
	}

	@Override
	public boolean transmorgrify(final ClassNode cn) {
		final String names[] = { "playSound", "func_148611_c" };
		final String sig = "(Lnet/minecraft/client/audio/ISound;)V";

		final MethodNode m = findMethod(cn, sig, names);
		if (m != null) {
			logMethod(Transformer.log(), m, "Found!");

			final String owner = "org/orecruncher/dsurround/asm/SoundPlayFlush";
			final String targetName = "flush";
			final String sig1 = "()V";

			for (final Iterator<?> iterator = m.instructions.iterator(); iterator.hasNext();) {
				final AbstractInsnNode insn = (AbstractInsnNode) iterator.next();
				if (insn instanceof MethodInsnNode) {
					final MethodInsnNode mn = (MethodInsnNode) insn;
					if (mn.owner.equals("net/minecraft/client/audio/SoundManager$SoundSystemStarterThread")
							&& mn.name.equals("play")) {

						final InsnList list = new InsnList();
						list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, targetName, sig1, false));

						m.instructions.insert(insn, list);
						return true;
					}
				}
			}

			return false;
		} else {
			Transformer.log().error("Unable to locate method {}{}", names[0], sig);
		}

		Transformer.log().info("Unable to patch [{}]!", getClassName());

		return false;
	}

	public static void flush() {
		try {
			SoundEngine.flushSound();
		} catch (@Nonnull final Throwable t) {

		}
	}

}
