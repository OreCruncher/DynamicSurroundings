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

package org.orecruncher.dsurround.client.footsteps.interfaces;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum EventType {
	//@formatter:off
	WALK(null), // final form
	WANDER(null), // final form
	SWIM(null), // final form
	RUN(WALK), // -> walk
	JUMP(WANDER), // -> wander
	LAND(RUN), // -> run
	CLIMB(WALK), // -> walk
	CLIMB_RUN(RUN), // -> run
	DOWN(WALK), // -> walk
	DOWN_RUN(RUN), // -> run
	UP(WALK), // -> walk
	UP_RUN(RUN);// -> run
	//@formatter:on

	private final EventType destination;
	private final String jsonName;

	EventType(@Nonnull final EventType dest) {
		this.destination = dest;
		this.jsonName = name().toLowerCase();
	}

	@Nonnull
	public String jsonName() {
		return this.jsonName;
	}

	public boolean canTransition() {
		return this.destination != null;
	}

	public EventType getTransitionDestination() {
		return this.destination;
	}
}