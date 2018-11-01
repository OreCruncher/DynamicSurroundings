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

package org.orecruncher.dsurround.entity;

import javax.annotation.Nonnull;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Describes how an Entity "feels". Though an ActionState has a default
 * EmotionalState, the EntityAI may select a different EmotionalState based on
 * other factors.
 */
public enum EmotionalState {

	/** The Entity has no real opinion at the moment. */
	NEUTRAL(0),
	/** The Entity is happy, like when being tempted with food. */
	HAPPY(1),
	/** The entity is sad. */
	SAD(2),
	/** The Entity is angry such as when attacking. */
	ANGRY(3),
	/** The Entity is afraid, such as when fleeing. */
	AFRAID(4),
	/** The Entity is busy with an activity, like Farming. */
	BUSY(5),
	/** The Entity feels sick because of a negative potion effect. */
	SICK(6),
	/** The Entity feels hurt because it's health has been reduced. */
	HURT(7);

	private static final TIntObjectHashMap<EmotionalState> lookup = new TIntObjectHashMap<>();
	static {
		for (final EmotionalState state : EmotionalState.values()) {
			lookup.put(state.getId(), state);
		}
	}

	private final int id;

	EmotionalState(final int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	@Nonnull
	public static EmotionalState get(int id) {
		EmotionalState result = lookup.get(id);
		if (result == null)
			result = EmotionalState.NEUTRAL;
		return result;
	}

	public static int getId(@Nonnull final EmotionalState type) {
		return type.getId();
	}

}
