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

package org.blockartistry.mod.DynSurround.api.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Describes how an Entity "feels".  Though an ActionState has a default
 * EmotionalState, the EntityAI may select a different EmotionalState based
 * on other factors.
 */
public enum EmotionalState {

	/** The Entity has no real opinion at the moment. */
	NEUTRAL,
	/** The Entity is happy, like when being tempted with food. */
	HAPPY,
	/** The entity is sad. */
	SAD,
	/** The Entity is angry such as when attacking. */
	ANGRY,
	/** The Entity is afraid, such as when fleeing. */
	AFRAID,
	/** The Entity is busy with an activity, like Farming. */
	BUSY,
	/** The Entity feels sick because of a negative potion effect. */
	SICK,
	/** The Entity feels hurt because it's health has been reduced. */
	HURT;

	@Nullable
	public static EmotionalState get(int id) {
		final EmotionalState[] v = EmotionalState.values();
		if (id > v.length || id < 0)
			return null;
		return v[id];
	}

	public static int getId(@Nonnull final EmotionalState state) {
		return state.ordinal();
	}

}
