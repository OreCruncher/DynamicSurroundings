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

package org.orecruncher.dsurround.event;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fires when there is an update to an entities emoji state. Will only fire
 * client side.
 */
public class EntityDataEvent extends Event {

	/**
	 * Persistent ID of the entity this event is associated with.
	 */
	public final int entityId;

	/**
	 * Indicates if the entity is attacking something
	 */
	public final boolean isAttacking;
	
	/**
	 * Indicates if the entity is fleeing something
	 */
	public final boolean isFleeing;

	public EntityDataEvent(@Nonnull final int id, final boolean isAttacking, final boolean isFleeing) {
		this.entityId = id;
		this.isAttacking = isAttacking;
		this.isFleeing = isFleeing;
	}

}
