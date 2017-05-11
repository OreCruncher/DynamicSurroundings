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

public enum EmojiType {

	/** No emoji will be displayed */
	NONE,
	/** The attack emoji will be displayed */
	ATTACK,
	/** The flee emoji will be displayed */
	FLEE,
	/** The happy emoji will be displayed */
	HAPPY,
	/** The sad emoji will be displayed */
	SAD,
	/** The sick emoji will be displayed */
	SICK,
	/** The hurt emoji will be displayed */
	HURT,
	/** The watch emoji will be displayed */
	WATCH,
	/** The farm farm will be displayed */
	FARM,
	/** The work emoji will be displayed */
	WORK,
	/** The trade emoji will be displayed */
	TRADE,
	/** The angry emoji will be displayed */
	ANGRY,
	/** The eat emoji will be displayed */
	EAT;

	@Nullable
	public static EmojiType get(int id) {
		final EmojiType[] v = EmojiType.values();
		if (id > v.length || id < 0)
			return NONE;
		return v[id];
	}
	
	public static int getId(@Nonnull final EmojiType type) {
		return type.ordinal();
	}

}
