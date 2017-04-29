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

package org.blockartistry.DynSurround.api;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.api.entity.ActionState;
import org.blockartistry.DynSurround.entity.EmojiDataTables;

import net.minecraft.entity.ai.EntityAIBase;

/**
 * Encapsulates the configuration API available with Dynamic Surroundings.
 */
public final class Configure {
	private Configure() {
		
	}

	/**
	 * Dynamic Surroundings determines what ActionState to set for an Entity based
	 * on it's currently executing EntityAI tasks.  If a mod adds custom AI for a mob
	 * it can be registered using this API so that it can be recognized by the
	 * ActionState routines.
	 * 
	 * @param clazz EntityAIBase class for which an ActionState is being set.
	 * @param state  ActionState to set.
	 */
	public static void addEntityAIMapping(@Nonnull final Class<? extends EntityAIBase> clazz, @Nonnull final ActionState state) {
		EmojiDataTables.add(clazz, state);
	}
}
