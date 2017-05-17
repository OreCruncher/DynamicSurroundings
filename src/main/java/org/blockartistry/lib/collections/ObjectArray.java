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

package org.blockartistry.lib.collections;

import javax.annotation.Nonnull;

import com.google.common.base.Predicate;

public class ObjectArray<T> {

	private static final int DEFAULT_SIZE = 16;

	protected Object[] data;
	protected int insertionIdx;

	public ObjectArray() {
		this(DEFAULT_SIZE);
	}

	public ObjectArray(final int size) {
		this.data = new Object[size];
	}

	private void resize() {
		final Object[] t = new Object[this.data.length * 2];
		System.arraycopy(this.data, 0, t, 0, this.data.length);
		this.data = t;
	}

	public void add(@Nonnull final T e) {
		if (e == null)
			return;

		if (this.data.length == this.insertionIdx)
			resize();

		this.data[this.insertionIdx++] = e;
	}

	public int size() {
		return this.insertionIdx;
	}

	@SuppressWarnings("unchecked")
	public T get(final int idx) {
		if (idx >= 0 && idx < this.insertionIdx)
			return (T) this.data[idx];
		return null;
	}

	@SuppressWarnings("unchecked")
	public T remove(final int idx) {
		T removed = null;
		if (idx >= 0 && idx < this.insertionIdx) {
			removed = (T) this.data[idx];
			final Object m = this.data[--this.insertionIdx];
			this.data[this.insertionIdx] = null;
			if (idx != this.insertionIdx)
				this.data[idx] = m;
		}

		return removed;
	}

	@SuppressWarnings("unchecked")
	public void removeIf(@Nonnull final Predicate<T> pred) {
		for (int i = this.insertionIdx - 1; i >= 0; i--) {
			if (pred.apply((T) this.data[i]))
				this.remove(i);
		}
	}

}
