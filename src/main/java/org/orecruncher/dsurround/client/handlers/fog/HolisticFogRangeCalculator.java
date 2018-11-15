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
package org.orecruncher.dsurround.client.handlers.fog;

import javax.annotation.Nonnull;

import org.orecruncher.lib.collections.ObjectArray;

import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Consults various different fog calculators and aggregates the results into a
 * single set.
 */
@SideOnly(Side.CLIENT)
public class HolisticFogRangeCalculator implements IFogRangeCalculator {

	protected final ObjectArray<IFogRangeCalculator> calculators = new ObjectArray<>(8);
	protected final FogResult cached = new FogResult();

	public void add(@Nonnull final IFogRangeCalculator calc) {
		this.calculators.add(calc);
	}

	@Override
	@Nonnull
	public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
		float start = event.getFarPlaneDistance();
		float end = event.getFarPlaneDistance();
		for (int i = 0; i < this.calculators.size(); i++) {
			final FogResult result = this.calculators.get(i).calculate(event);
			start = Math.min(start, result.getStart());
			end = Math.min(end, result.getEnd());
		}
		this.cached.set(start, end);
		return this.cached;
	}

	@Override
	public void tick() {
		this.calculators.forEach(IFogRangeCalculator::tick);
	}

	@Override
	@Nonnull
	public String toString() {
		return this.cached.toString();
	}
}
