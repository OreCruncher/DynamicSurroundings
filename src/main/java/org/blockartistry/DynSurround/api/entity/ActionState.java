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

package org.blockartistry.DynSurround.api.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Describes the current action that the entity is taking.
 */
public enum ActionState {

	/** Not doing anything */
	NONE(0, EmotionalState.NEUTRAL),
	/** Moving various model parts around, like shifting head orientation */
	IDLE(1, EmotionalState.NEUTRAL),
	/** Entity is angry and doing something related to that anger.  Enderman agitation is an example. */
	ANGRY(2, EmotionalState.ANGRY),
	/** The entity has a target and is attempting to attack. */
	ATTACKING(1000, EmotionalState.ANGRY),
	/** The entity is in the process of exploding.  Creeper swell is an example. */
	EXPLODE(2000, EmotionalState.ANGRY),
	/** The entity is looking at something, either a player, mob, block, etc. */
	LOOKING(3, EmotionalState.NEUTRAL),
	/** The villager is attempting to farm mature crops. */
	FARMING(4, EmotionalState.BUSY),
	/** The entity is fleeing, such as a villager from a zombie. */
	PANIC(5, EmotionalState.AFRAID),
	/** The entity is being tempted.  Usually applies to passive mobs when player holds out food. */
	TEMPT(6, EmotionalState.HAPPY),
	/** Needs no explanation. */
	MATING(7, EmotionalState.HAPPY),
	/** When a mob runs around like crazy. */
	CRAZY(8, EmotionalState.AFRAID),
	/** Villager is engaged in a trade with a player. */
	TRADING(9, EmotionalState.HAPPY),
	/** Child villager is playing. */
	PLAYING(10, EmotionalState.HAPPY),
	/** Pet is begging */
	BEGGING(11, EmotionalState.HAPPY),
	/** Currently not used */
	WORKING(12, EmotionalState.BUSY),
	/** The entity is eating, such as a sheep cropping grass. */
	EATING(13, EmotionalState.HAPPY),
	/** The entity is following, like a leashed passive. */
	FOLLOWING(14, EmotionalState.NEUTRAL),
	/** The entity is on the move. */
	MOVING(15, EmotionalState.NEUTRAL);

	private final int priority;
	private final EmotionalState state;

	ActionState(final int priority, @Nonnull final EmotionalState state) {
		this.priority = priority;
		this.state = state;
	}

	public int getPriority() {
		return this.priority;
	}

	@Nonnull
	public EmotionalState getEmotionalState() {
		return this.state;
	}

	@Nullable
	public static ActionState get(int id) {
		final ActionState[] v = ActionState.values();
		if (id > v.length || id < 0)
			return null;
		return v[id];
	}

	public static int getId(@Nonnull final ActionState state) {
		return state.ordinal();
	}

}
