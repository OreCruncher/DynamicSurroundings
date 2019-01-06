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

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MorningFogRangeCalculator extends VanillaFogRangeCalculator {

	protected static final float START = 0.630F;
	protected static final float END = 0.830F;
	protected static final float RESERVE = 10F;

	public static enum FogType {
		//@formatter:off
		NONE(0F, 0F, 0F),
		NORMAL(START, END, RESERVE),
		LIGHT(START+ 0.1F, END - 0.1F, RESERVE + 5F),
		MEDIUM(START - 0.1F, END +0.1F, RESERVE),
		HEAVY(START - 0.1F, END + 0.2F, RESERVE -5F);
		//@formatter:on

		private final float start;
		private final float end;
		private final float reserve;

		private FogType(final float start, final float end, final float reserve) {
			this.start = start;
			this.end = end;
			this.reserve = reserve;
		}

		public float getStart() {
			return this.start;
		}

		public float getEnd() {
			return this.end;
		}

		public float getReserve() {
			return this.reserve;
		}
	}

	protected int fogDay = -1;
	protected boolean doFog = false;
	protected FogType type = FogType.NORMAL;

	protected final FogResult cache = new FogResult();

	@Override
	@Nonnull
	public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
		this.cache.set(event);
		if (this.type != FogType.NONE && this.cache.getStart() > this.type.getReserve()) {
			final float ca = EnvironState.getWorld().getCelestialAngle((float) event.getRenderPartialTicks());
			if (ca >= this.type.getStart() && ca <= this.type.getEnd()) {
				final float mid = (this.type.getStart() + this.type.getEnd()) / 2F;
				final float factor = 1F - MathStuff.abs(ca - mid) / (mid - this.type.getStart());
				final float shift = this.cache.getStart() * factor;
				final float newEnd = this.cache.getEnd() - shift;
				final float newStart = MathStuff.clamp(this.cache.getStart() - shift * 2, this.type.getReserve() + 1,
						newEnd);
				this.cache.set(newStart, newEnd);
			}
		}
		return this.cache;
	}

	@Override
	public void tick() {
		// Determine if fog is going to be done this Minecraft day
		final int day = EnvironState.getClock().getDay();
		if (this.fogDay != day) {
			final int dim = EnvironState.getDimensionId();
			this.fogDay = day;
			//@formatter:off
			final boolean doFog =
				(dim != -1 && dim != 1)
				&& (
					ModOptions.fog.morningFogChance < 2
					|| XorShiftRandom.current().nextInt(ModOptions.fog.morningFogChance) == 0
				);
			//@formatter:on
			this.type = doFog ? getFogType() : FogType.NONE;
		}
	}

	protected FogType getFogType() {
		return FogType.NORMAL;
	}

}
