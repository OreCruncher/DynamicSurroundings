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

package org.blockartistry.mod.DynSurround.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.util.random.XorShiftRandom;

public class WeightTable<T extends WeightTable.Item> {

	protected final Random random = new XorShiftRandom();
	protected final List<T> items = new ArrayList<T>();
	protected int totalWeight = 0;

	public abstract static class Item {

		public final int itemWeight;
		protected Random rnd;

		public Item(final int weight) {
			assert weight > 0;
			this.itemWeight = weight;
		}
	}

	public WeightTable() {
	}

	public void add(@Nonnull final T entry) {
		this.totalWeight += entry.itemWeight;
		entry.rnd = this.random;
		this.items.add(entry);
	}

	public void remove(@Nonnull final T entry) {
		if(this.items.remove(entry))
			this.totalWeight -= entry.itemWeight;
	}

	@Nonnull
	public T next() {
		if(this.totalWeight < 1 || this.items.isEmpty())
			return null;
		
		int targetWeight = this.random.nextInt(this.totalWeight);

		int i = 0;
		for (i = this.items.size(); (targetWeight -= this.items.get(i - 1).itemWeight) >= 0; i--)
			;

		return this.items.get(i - 1);
	}

	@Nonnull
	public List<T> getEntries() {
		return Collections.unmodifiableList(items);
	}

	public int getTotalWeight() {
		return totalWeight;
	}
}
