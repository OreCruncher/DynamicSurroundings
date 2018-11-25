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

import static org.objectweb.asm.Opcodes.ALOAD;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

public class PatchSoundManagerClampVolume  extends Transmorgrifier {

	public PatchSoundManagerClampVolume() {
		super("net.minecraft.client.audio.SoundManager");
	}

	@Override
	public String name() {
		return "SoundManager getClampedVolume";
	}

	@Override
	public int classWriterFlags() {
		return ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES;
	}

	@Override
	public boolean transmorgrify(final ClassNode cn) {
		final String names[] = { "getClampedVolume", "func_188770_e" };
		final String sig = "(Lnet/minecraft/client/audio/ISound;)F";

		final MethodNode m = findMethod(cn, sig, names);
		if (m != null) {
			logMethod(Transformer.log(), m, "Found!");

			final String owner = "org/orecruncher/dsurround/client/sound/SoundEngine";
			final String targetName = "getClampedVolume";
			final String sig1 = "(Lnet/minecraft/client/audio/ISound;)F";
			
			// Need to wrap in try catch in case of stillborn client startup
			final LabelNode tryStart = new LabelNode();
			final LabelNode tryEnd = new LabelNode();
			final LabelNode tryHandler = new LabelNode();
			final TryCatchBlockNode tryCatch = new TryCatchBlockNode(tryStart, tryEnd, tryHandler, "java/lang/Throwable"); 
			m.tryCatchBlocks.add(tryCatch);
			
			final InsnList list = new InsnList();
			list.add(tryStart);
			list.add(new VarInsnNode(ALOAD, 1));
			list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, targetName, sig1, false));
			list.add(new InsnNode(Opcodes.FRETURN));
			list.add(tryEnd);
			list.add(tryHandler);
			m.instructions.insert(m.instructions.getFirst(), list);
			
			return true;
		} else {
			Transformer.log().error("Unable to locate method {}{}", names[0], sig);
		}

		Transformer.log().info("Unable to patch [{}]!", getClassName());

		return false;
	}

}
