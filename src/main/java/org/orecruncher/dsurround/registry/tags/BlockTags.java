/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.orecruncher.dsurround.registry.tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The mod's block tags, named as the 26.x line's {@code BlockEffectTags} ({@code dsurround:effects/<name>}), defined
 * by {@code assets/<namespace>/dsconfigs/tags/block/effects/<name>.json} files (see {@link TagRegistry})
 */
public final class BlockTags {

	private static final List<String> TAGS = new ArrayList<>();

	// Wooden floors that squeak now and then underfoot
	public static final String FLOOR_SQUEAKS = effects("floor_squeaks");

	/**
	 * Every tag the mod uses, whose files are read
	 */
	public static final List<String> ALL = Collections.unmodifiableList(TAGS);

	private static String effects(final String name) {
		final String tag = TagResolver.normalize("effects/" + name);
		TAGS.add(tag);
		return tag;
	}

	private BlockTags() {
	}
}
