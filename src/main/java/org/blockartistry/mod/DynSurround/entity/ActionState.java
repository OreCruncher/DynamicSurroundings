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

package org.blockartistry.mod.DynSurround.entity;

import gnu.trove.map.hash.TObjectIntHashMap;

public enum ActionState {

	NONE(0, EmotionalState.NEUTRAL),
	IDLE(1, EmotionalState.NEUTRAL),
	ANGRY(2, EmotionalState.ANGRY),
	ATTACKING(1000, EmotionalState.ANGRY),
	EXPLODE(2000, EmotionalState.ANGRY),
	LOOKING(3, EmotionalState.NEUTRAL),
	FARMING(4, EmotionalState.BUSY),
	PANIC(5, EmotionalState.AFRAID),
	TEMPT(6, EmotionalState.HAPPY),
	MATING(7, EmotionalState.HAPPY),
	CRAZY(8, EmotionalState.AFRAID),
	TRADING(9, EmotionalState.HAPPY),
	PLAYING(10, EmotionalState.HAPPY),
	BEGGING(11, EmotionalState.HAPPY),
	WORKING(12, EmotionalState.BUSY),
	EATING(13, EmotionalState.HAPPY),
	FOLLOWING(14, EmotionalState.NEUTRAL),
	MOVING(15, EmotionalState.NEUTRAL);

	private static final TObjectIntHashMap<ActionState> map = new TObjectIntHashMap<ActionState>();

	private final int priority;
	private final EmotionalState state;

	ActionState(final int priority, final EmotionalState state) {
		this.priority = priority;
		this.state = state;
	}

	public int getPriority() {
		return this.priority;
	}

	public EmotionalState getState() {
		return this.state;
	}

	public static ActionState get(int id) {
		final ActionState[] v = ActionState.values();
		if (id > v.length || id < 0)
			return null;
		return v[id];
	}

	public static int getId(final ActionState state) {
		if (map.size() == 0) {
			for (int x = 0; x < ActionState.values().length; x++)
				map.put(ActionState.values()[x], x);
		}
		return map.get(state);
	}

}
