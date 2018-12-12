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

import org.apache.logging.log4j.Logger;

public class Transformer extends MyTransformer {

	private static final Logger logger = TransformLoader.logger;

	public static Logger log() {
		return logger;
	}

	public Transformer() {
		super(logger);
	}

	@Override
	protected void initTransmorgrifiers() {
		addTransmorgrifier(new PatchEntityRenderer());
		addTransmorgrifier(new PatchSoundManager());
		addTransmorgrifier(new PatchSoundManagerPlayTime());
		addTransmorgrifier(new PatchSoundManagerClampVolume());
		addTransmorgrifier(new PatchSoundManagerSync());
		addTransmorgrifier(new PatchParticleManagerSync());
		addTransmorgrifier(new PatchEntityArrow());
		addTransmorgrifier(new SoundCategoryAdditions());
		addTransmorgrifier(new BiomeInfoHook());
		addTransmorgrifier(new BlockStateInfoHook());
		addTransmorgrifier(new ItemInfoHook());
	}

}
