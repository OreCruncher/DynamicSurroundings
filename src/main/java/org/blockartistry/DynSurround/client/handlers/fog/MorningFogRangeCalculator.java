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
package org.blockartistry.DynSurround.client.handlers.fog;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.math.MathStuff;

import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MorningFogRangeCalculator extends VanillaFogRangeCalculator {

	protected static final float START = 0.630F;
	protected static final float MID = 0.730F;
	protected static final float END = 0.830F;
	protected static final float RESERVE = 10F;

	protected final FogResult cache = new FogResult();

	@Override
	@Nonnull
	public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
		this.cache.set(event);
		if (this.cache.getStart() > RESERVE) {
			if (EnvironState.getDimensionId() != 1 && EnvironState.getDimensionId() != -1) {
				final float ca = EnvironState.getWorld().getCelestialAngle((float) event.getRenderPartialTicks());
				if (ca >= START && ca <= END) {
					final float factor = 1F - MathStuff.abs(ca - MID) / (MID - START);
					final float shift = this.cache.getStart() * factor;
					final float newEnd = this.cache.getEnd() - shift;
					final float newStart = MathStuff.clamp(this.cache.getStart() - shift * 2, RESERVE, newEnd);
					this.cache.set(newStart, newEnd);
				}
			}
		}
		return this.cache;
	}
}
