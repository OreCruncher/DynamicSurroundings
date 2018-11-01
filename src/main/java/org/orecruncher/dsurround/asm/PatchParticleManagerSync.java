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

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class PatchParticleManagerSync extends Transmorgrifier {

	public PatchParticleManagerSync() {
		super("net.minecraft.client.particle.ParticleManager");
	}

	@Override
	public String name() {
		return "ParticleManager synchronization";
	}

	@Override
	public boolean isEnabled() {
		return TransformLoader.config.getBoolean("Enable synchronized for ParticleManager", "asm", true,
				StringUtils.EMPTY);
	}

	@Override
	public boolean transmorgrify(final ClassNode cn) {
		// Loop through the method nodes setting the synchronized bit
		for (final MethodNode m : cn.methods) {
			if (!m.name.startsWith("<") && (m.access & Opcodes.ACC_PUBLIC) != 0
					&& (m.access & Opcodes.ACC_SYNCHRONIZED) == 0) {
				logMethod(Transformer.log(), m, "Synchronized!");
				m.access |= Opcodes.ACC_SYNCHRONIZED;
			}
		}

		return true;
	}

}
