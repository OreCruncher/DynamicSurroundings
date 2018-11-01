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

package org.orecruncher.lib.collections;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

public class CollectionUtils {

	private CollectionUtils() {

	}

	public static <K, V> void removeIf(@Nonnull final Map<K, V> theMap, @Nonnull final Predicate<? super Entry<K, V>> thePred) {
		final ArrayList<K> keys = new ArrayList<>();
		for (final Entry<K, V> entry : theMap.entrySet())
			if (thePred.test(entry))
				keys.add(entry.getKey());
		for (final K key : keys)
			theMap.remove(key);
	}

}
