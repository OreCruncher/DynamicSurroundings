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

package org.blockartistry.mod.DynSurround.scanner;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.util.BlockPosHelper;

import net.minecraft.util.math.BlockPos;

public class CuboidPointIterator implements IPointIterator {

	protected final BlockPos minPoint;
	protected final BlockPos maxPoint;

	protected Iterator<BlockPos> itr;
	protected BlockPos peeked;

	public CuboidPointIterator(@Nonnull final Cuboid other) {
		this(other.minimum(), other.maximum());
	}

	public CuboidPointIterator(@Nonnull final BlockPos p1, @Nonnull final BlockPos p2) {
		this.minPoint = BlockPosHelper.createMinPoint(p1, p2);
		this.maxPoint = BlockPosHelper.createMaxPoint(p1, p2);
		reset();
	}

	@Override
	@Nullable
	public BlockPos next() {

		// If there is a peek value the iterator already advanced
		// so just return the cached result.
		if (this.peeked != null) {
			final BlockPos result = this.peeked;
			this.peeked = null;
			return result;
		}

		// Go to the well.  If nothing, return null
		if (!this.itr.hasNext())
			return null;

		// Peel off the point and return
		return this.itr.next();
	}

	@Override
	@Nullable
	public BlockPos peek() {
		
		// If it was peeked already return that
		if (this.peeked != null)
			return this.peeked;
		
		// If there isn't anything left return null
		if (!this.itr.hasNext())
			return null;
		
		// Stash the peeked value and return it
		return this.peeked = this.itr.next();
	}

	@Override
	@Nullable
	public void reset() {
		this.itr = BlockPos.getAllInBox(this.minPoint, this.maxPoint).iterator();
		this.peeked = null;
	}
}
