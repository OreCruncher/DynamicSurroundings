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

package org.blockartistry.DynSurround.internal.entity;

import javax.annotation.Nonnull;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Describes the current action that the entity is taking.
 */
public enum ActionState {

	/** Not doing anything */
	NONE(0, 0, EmotionalState.NEUTRAL),
	/** Moving various model parts around, like shifting head orientation */
	IDLE(1, 1, EmotionalState.NEUTRAL),
	/**
	 * Entity is angry and doing something related to that anger. Enderman
	 * agitation is an example.
	 */
	ANGRY(2, 2, EmotionalState.ANGRY),
	/** The entity has a target and is attempting to attack. */
	ATTACKING(3, 1000, EmotionalState.ANGRY),
	/**
	 * The entity is in the process of exploding. Creeper swell is an example.
	 */
	EXPLODE(4, 2000, EmotionalState.ANGRY),
	/** The entity is looking at something, either a player, mob, block, etc. */
	LOOKING(5, 3, EmotionalState.NEUTRAL),
	/** The villager is attempting to farm mature crops. */
	FARMING(6, 4, EmotionalState.BUSY),
	/** The entity is fleeing, such as a villager from a zombie. */
	PANIC(7, 5, EmotionalState.AFRAID),
	/**
	 * The entity is being tempted. Usually applies to passive mobs when player
	 * holds out food.
	 */
	TEMPT(8, 6, EmotionalState.HAPPY),
	/** Needs no explanation. */
	MATING(9, 7, EmotionalState.HAPPY),
	/** When a mob runs around like crazy. */
	CRAZY(10, 8, EmotionalState.AFRAID),
	/** Villager is engaged in a trade with a player. */
	TRADING(11, 9, EmotionalState.HAPPY),
	/** Child villager is playing. */
	PLAYING(12, 10, EmotionalState.HAPPY),
	/** Pet is begging */
	BEGGING(13, 11, EmotionalState.HAPPY),
	/** Currently not used */
	WORKING(14, 12, EmotionalState.BUSY),
	/** The entity is eating, such as a sheep cropping grass. */
	EATING(15, 13, EmotionalState.HAPPY),
	/** The entity is following, like a leashed passive. */
	FOLLOWING(16, 14, EmotionalState.NEUTRAL),
	/** The entity is on the move. */
	MOVING(17, 15, EmotionalState.NEUTRAL);

	private static final TIntObjectHashMap<ActionState> lookup = new TIntObjectHashMap<ActionState>();
	static {
		for (final ActionState state : ActionState.values()) {
			lookup.put(state.getId(), state);
		}
	}

	private final int id;
	private final int priority;
	private final EmotionalState state;

	ActionState(final int id, final int priority, @Nonnull final EmotionalState state) {
		this.id = id;
		this.priority = priority;
		this.state = state;
	}

	public int getId() {
		return this.id;
	}

	public int getPriority() {
		return this.priority;
	}

	@Nonnull
	public EmotionalState getEmotionalState() {
		return this.state;
	}

	@Nonnull
	public static ActionState get(int id) {
		ActionState result = lookup.get(id);
		if (result == null)
			result = ActionState.NONE;
		return result;
	}

	public static int getId(@Nonnull final ActionState state) {
		return state.getId();
	}

}
