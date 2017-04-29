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

import org.blockartistry.DynSurround.DSurround;

import net.minecraft.util.ResourceLocation;

public enum EmojiType {

	/** No emoji will be displayed */
	NONE("none"),
	/** The attack emoji will be displayed */
	ATTACK("emoji_attack"),
	/** The flee emoji will be displayed */
	FLEE("emoji_flee"),
	/** The happy emoji will be displayed */
	HAPPY("emoji_happy"),
	/** The sad emoji will be displayed */
	SAD("emoji_sad"),
	/** The sick emoji will be displayed */
	SICK("emoji_sick"),
	/** The hurt emoji will be displayed */
	HURT("emoji_hurt"),
	/** The watch emoji will be displayed */
	WATCH("emoji_watch"),
	/** The farm farm will be displayed */
	FARM("emoji_farm"),
	/** The work emoji will be displayed */
	WORK("emoji_work"),
	/** The trade emoji will be displayed */
	TRADE("emoji_trade"),
	/** The angry emoji will be displayed */
	ANGRY("emoji_angry"),
	/** The eat emoji will be displayed */
	EAT("emoji_eat");

	private final ResourceLocation resource;

	private EmojiType(@Nonnull final String texture) {
		this.resource = new ResourceLocation(DSurround.RESOURCE_ID, "textures/particles/" + texture + ".png");
	}

	@Nonnull
	public ResourceLocation getResource() {
		return this.resource;
	}

	@Nullable
	public static EmojiType get(int id) {
		final EmojiType[] v = EmojiType.values();
		if (id > v.length || id < 0)
			return null;
		return v[id];
	}

	public static int getId(@Nonnull final EmojiType state) {
		return state.ordinal();
	}

}
