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

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.Module;

import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.util.ResourceLocation;

public enum EmojiType {

	NONE("none"),
	ATTACK("emoji_attack"),
	PANIC("emoji_panic"),
	HAPPY("emoji_happy"),
	SAD("emoji_sad"),
	SICK("emoji_sick"),
	WATCH("emoji_watch"),
	FARM("emoji_farm"),
	WORK("emoji_work"),
	TRADE("emoji_trade"),
	ANGRY("emoji_angry"),
	EAT("emoji_eat");

	private static final TObjectIntHashMap<EmojiType> map = new TObjectIntHashMap<EmojiType>();

	private final ResourceLocation resource;

	private EmojiType(@Nonnull final String texture) {
		this.resource = new ResourceLocation(Module.RESOURCE_ID, "textures/particles/" + texture + ".png");
	}

	@Nonnull
	public ResourceLocation getResource() {
		return this.resource;
	}

	public static EmojiType get(int id) {
		final EmojiType[] v = EmojiType.values();
		if (id > v.length || id < 0)
			return null;
		return v[id];
	}

	public static int getId(final EmojiType state) {
		if (map.size() == 0) {
			for (int x = 0; x < EmojiType.values().length; x++)
				map.put(EmojiType.values()[x], x);
		}
		return map.get(state);
	}

}
