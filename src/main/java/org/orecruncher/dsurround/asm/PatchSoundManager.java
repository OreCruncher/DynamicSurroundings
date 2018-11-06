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
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class PatchSoundManager extends Transmorgrifier {

	public PatchSoundManager() {
		super("net.minecraft.client.audio.SoundManager");
	}

	@Override
	public String name() {
		return "Sound Caching";
	}

	@Override
	public boolean isEnabled() {
		return TransformLoader.config.getBoolean("Enable Sound Caching", "asm", true, StringUtils.EMPTY);
	}

	@Override
	public int classWriterFlags() {
		return ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES;
	}

	@Override
	public boolean transmorgrify(final ClassNode cn) {
		final String names[] = { "getURLForSoundResource", "func_148612_a" };
		final String sig = "(Lnet/minecraft/util/ResourceLocation;)Ljava/net/URL;";

		final MethodNode m = findMethod(cn, sig, names);
		if (m != null) {
			logMethod(Transformer.log(), m, "Found!");

			final InsnList list = new InsnList();
			list.add(new VarInsnNode(ALOAD, 0));

			final String owner = "org/orecruncher/dsurround/lib/sound/SoundCache";
			final String targetName = "getURLForSoundResource";
			final String sig1 = "(Lnet/minecraft/util/ResourceLocation;)Ljava/net/URL;";

			list.add(new MethodInsnNode(INVOKESTATIC, owner, targetName, sig1, false));
			list.add(new InsnNode(ARETURN));
			m.instructions.insert(m.instructions.getFirst(), list);
			return true;
		} else {
			Transformer.log().error("Unable to locate method {}{}", names[0], sig);
		}

		Transformer.log().info("Unable to patch [{}]!", getClassName());

		return false;
	}

}
