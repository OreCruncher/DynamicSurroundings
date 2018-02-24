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

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;

import java.util.ListIterator;

import org.blockartistry.lib.asm.Transmorgrifier;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class PatchSoundHandler extends Transmorgrifier {

	public PatchSoundHandler() {
		super("net.minecraft.client.audio.SoundHandler");
	}

	@Override
	public int classWriterFlags() {
		return ClassWriter.COMPUTE_MAXS;
	}

	@Override
	public String name() {
		return "SoundManager Replace";
	}

	@Override
	public boolean transmorgrify(final ClassNode cn) {

		final String name = "<init>";
		final String sig = "(Lnet/minecraft/client/resources/IResourceManager;Lnet/minecraft/client/settings/GameSettings;)V";

		final String managerToReplace = "net/minecraft/client/audio/SoundManager";
		final String newManager = "org/blockartistry/DynSurround/client/sound/SoundManagerReplacement";

		boolean modified = false;

		final MethodNode m = findCTOR(cn, sig);
		if (m != null) {
			logMethod(Transformer.log(), m, "Found!");
			final ListIterator<AbstractInsnNode> itr = m.instructions.iterator();
			boolean foundNew = false;
			while (itr.hasNext()) {
				final AbstractInsnNode node = itr.next();
				if (node.getOpcode() == NEW) {
					final TypeInsnNode theNew = (TypeInsnNode) node;
					if (managerToReplace.equals(theNew.desc)) {
						m.instructions.set(node, new TypeInsnNode(NEW, newManager));
						modified = true;
						foundNew = true;
					}
				} else if (node.getOpcode() == INVOKESPECIAL) {
					final MethodInsnNode theInvoke = (MethodInsnNode) node;
					if (managerToReplace.equals(theInvoke.owner)) {
						if (foundNew) {
							Transformer.log().info("{}.{}{}", theInvoke.owner, theInvoke.name, theInvoke.desc);
							m.instructions.set(node, new MethodInsnNode(INVOKESPECIAL, newManager, theInvoke.name,
									theInvoke.desc, false));
							modified = true;
							foundNew = false;
						}
					}
				}
			}
		} else {
			Transformer.log().error("Unable to locate method {}{}", name, sig);
		}

		if (!modified)
			Transformer.log().info("Unable to patch [{}]!", getClassName());

		return modified;
	}

}
