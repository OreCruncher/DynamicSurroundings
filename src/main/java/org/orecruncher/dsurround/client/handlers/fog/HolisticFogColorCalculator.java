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

import org.orecruncher.lib.Color;
import org.orecruncher.lib.collections.ObjectArray;

import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HolisticFogColorCalculator implements IFogColorCalculator {

	protected ObjectArray<IFogColorCalculator> calculators = new ObjectArray<>(4);
	protected Color cached;

	public void add(@Nonnull final IFogColorCalculator calc) {
		this.calculators.add(calc);
	}

	@Nonnull
	@Override
	public Color calculate(@Nonnull final EntityViewRenderEvent.FogColors event) {
		Color result = null;
		for (int i = 0; i < this.calculators.size(); i++) {
			final Color color = this.calculators.get(i).calculate(event);
			if (result == null)
				result = color;
			else
				result = result.mix(color);

		}

		// Possible if there are no calculators defined (the user disabled fog options without turning off
		// the master switch).
		if (result == null)
		{
			result = new Color(event.getRed(), event.getGreen(), event.getBlue());
		}

		return this.cached = result;
	}

	@Override
	public void tick() {
		this.calculators.forEach(IFogColorCalculator::tick);
	}

	@Override
	public String toString() {
		return this.cached != null ? this.cached.toString() : "<NOT SET>";
	}

}
