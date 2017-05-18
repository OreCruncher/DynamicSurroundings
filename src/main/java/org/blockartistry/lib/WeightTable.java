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

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.lib.collections.ObjectArray;
import org.blockartistry.lib.random.XorShiftRandom;

public class WeightTable<T> extends ObjectArray<WeightTable.IItem<T>> {

	protected final Random RANDOM = XorShiftRandom.current();
	protected int totalWeight = 0;

	public static interface IItem<T> {
		
		int getWeight();

		T getItem();
	}

	public WeightTable() {
	}

	@SafeVarargs
	public WeightTable(@Nonnull final IEntrySource<T>... src) {
		for (int i = 0; i < src.length; i++)
			if (src[i].matches())
				this.add(src[i].getEntry());
	}

	@Override
	public boolean add(@Nonnull final WeightTable.IItem<T> entry) {
		assert entry != null;
		this.totalWeight += entry.getWeight();
		return super.add(entry);
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public T next() {
		if (this.totalWeight <= 0)
			return null;

		int targetWeight = this.RANDOM.nextInt(this.totalWeight);

		WeightTable.IItem<T> selected = null;
		int i = -1;
		do {
			selected = (WeightTable.IItem<T>) this.data[++i];
			targetWeight -= selected.getWeight();
		} while (targetWeight >= 0);

		return selected.getItem();
	}

	public static interface IEntrySource<T> {
		WeightTable.IItem<T> getEntry();

		boolean matches();
	}
}
