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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class PatchSoundManagerPlayTime extends Transmorgrifier {

	public PatchSoundManagerPlayTime() {
		super("net.minecraft.client.audio.SoundManager");
	}

	@Override
	public String name() {
		return "SoundManager playTime";
	}

	@Override
	public boolean transmorgrify(final ClassNode cn) {
		final String names[] = { "playSound", "func_148611_c" };
		final String sig = "(Lnet/minecraft/client/audio/ISound;)V";

		final MethodNode m = findMethod(cn, sig, names);
		if (m != null) {
			logMethod(Transformer.log(), m, "Found!");

			for (int i = 0; i < m.instructions.size(); i++) {
				final AbstractInsnNode node = m.instructions.get(i);
				if (node instanceof IntInsnNode) {
					final IntInsnNode intNode = (IntInsnNode) node;
					if (intNode.operand == 20) {
						m.instructions.set(node, new IntInsnNode(Opcodes.BIPUSH, 3));
						return true;
					}
				}
			}
		} else {
			Transformer.log().error("Unable to locate method {}{}", names[0], sig);
		}

		Transformer.log().info("Unable to patch [{}]!", getClassName());

		return false;
	}

}
