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

package org.blockartistry.DynSurround.asm;

import java.util.Iterator;

import org.blockartistry.lib.asm.Transmorgrifier;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraftforge.fml.common.Loader;

// Based on patches by CreativeMD
public class SoundCrashFixStreamThread extends Transmorgrifier {

	private static final String[] classNames = { "paulscode.sound.StreamThread" };

	public SoundCrashFixStreamThread() {
		super(classNames);
	}

	@Override
	public boolean isEnabled() {
		return !Loader.isModLoaded("ambientsounds");
	}
	
	@Override
	public String name() {
		return "removeSource";
	}

	@Override
	public boolean transmorgrify(final ClassNode cn) {
		final MethodNode m = findMethod(cn, "run", "()V");
		if (m != null) {
			for (Iterator<?> iterator = m.instructions.iterator(); iterator.hasNext();) {
				AbstractInsnNode insn = (AbstractInsnNode) iterator.next();
				if (insn instanceof MethodInsnNode && ((MethodInsnNode) insn).owner.equals("java/util/ListIterator")
						&& ((MethodInsnNode) insn).name.equals("next")) {
					insn = insn.getNext().getNext();

					final LocalVariableNode var = findLocalVariable(m, "src");
					if (var != null) {
						m.instructions.insert(insn, new VarInsnNode(Opcodes.ASTORE, var.index));
						m.instructions.insert(insn,
								new MethodInsnNode(Opcodes.INVOKESTATIC,
										"org/blockartistry/DynSurround/client/sound/fix/SoundFixMethods",
										"removeSource", "(Lpaulscode/sound/Source;)Lpaulscode/sound/Source;", false));
						m.instructions.insert(insn, new VarInsnNode(Opcodes.ALOAD, var.index));
						return true;
					}
				}
			}
		}

		return false;

	}

}
