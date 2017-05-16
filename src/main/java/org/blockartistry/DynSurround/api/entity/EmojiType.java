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
import gnu.trove.map.hash.TIntObjectHashMap;

public enum EmojiType {

	/** No emoji will be displayed */
	NONE(0),
	/** The attack emoji will be displayed */
	ATTACK(1),
	/** The flee emoji will be displayed */
	FLEE(2),
	/** The happy emoji will be displayed */
	HAPPY(3),
	/** The sad emoji will be displayed */
	SAD(4),
	/** The sick emoji will be displayed */
	SICK(5),
	/** The hurt emoji will be displayed */
	HURT(6),
	/** The watch emoji will be displayed */
	WATCH(7),
	/** The farm farm will be displayed */
	FARM(8),
	/** The work emoji will be displayed */
	WORK(9),
	/** The trade emoji will be displayed */
	TRADE(10),
	/** The angry emoji will be displayed */
	ANGRY(11),
	/** The eat emoji will be displayed */
	EAT(12);

	private static final TIntObjectHashMap<EmojiType> lookup = new TIntObjectHashMap<EmojiType>();
	static {
		for (final EmojiType state : EmojiType.values()) {
			lookup.put(state.getId(), state);
		}
	}

	private final int id;

	EmojiType(final int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	@Nonnull
	public static EmojiType get(int id) {
		EmojiType result = lookup.get(id);
		if (result == null)
			result = EmojiType.NONE;
		return result;
	}

	public static int getId(@Nonnull final EmojiType type) {
		return type.getId();
	}

}
