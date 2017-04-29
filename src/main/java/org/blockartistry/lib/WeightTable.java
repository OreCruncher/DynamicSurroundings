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

package org.blockartistry.lib;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import org.blockartistry.lib.random.XorShiftRandom;

public class WeightTable<T> {

	protected final List<Item<T>> items = new ArrayList<Item<T>>(16);
	protected int totalWeight = 0;

	public static class Item<T> {

		public final int itemWeight;
		public final T item;

		public Item(@Nonnull final T item, final int weight) {
			this.itemWeight = weight;
			this.item = item;
		}
	}

	public WeightTable() {
	}
	
	@SafeVarargs
	public WeightTable(@Nonnull final IEntrySource<T>... src) {
		for (int i = 0; i < src.length; i++)
			if (src[i].matches())
				this.add(src[i].getItem(), src[i].getWeight());
	}

	public WeightTable<T> add(@Nonnull final T entry, final int itemWeight) {
		assert itemWeight > 0;
		assert entry != null;

		this.totalWeight += itemWeight;
		this.items.add(new Item<T>(entry, itemWeight));
		return this;
	}

	public WeightTable<T> add(@Nonnull final Item<T> entry) {
		assert entry != null;
		assert entry.itemWeight > 0;
		assert entry.item != null;

		this.totalWeight += entry.itemWeight;
		this.items.add(entry);
		return this;
	}

	@Nonnull
	public T next() {
		if(this.totalWeight <= 0)
			return null;
		
		int targetWeight = XorShiftRandom.current().nextInt(this.totalWeight);

		int i = -1;
		do {
			targetWeight -= this.items.get(++i).itemWeight;
		} while (targetWeight >= 0);

		return this.items.get(i).item;
	}

	public static interface IEntrySource<T> {
		int getWeight();

		T getItem();

		boolean matches();
	}
}
