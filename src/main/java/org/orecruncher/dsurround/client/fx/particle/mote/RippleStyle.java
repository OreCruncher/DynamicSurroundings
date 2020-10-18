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
package org.orecruncher.dsurround.client.fx.particle.mote;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.util.ResourceLocation;

public enum RippleStyle {

	//@formatter:off
	ORIGINAL("textures/particles/ripple.png"),
	CIRCLE("textures/particles/ripple1.png"),
	SQUARE("textures/particles/ripple2.png"),
	PIXELATED("textures/particles/pixel_ripples.png") {
		private final int FRAMES = 7;
		private final float DELTA = 1F / this.FRAMES;

		@Override
		public float getU1(final int age) {
			return (age / 2.0F) * this.DELTA;
		}

		@Override
		public float getU2(final int age) {
			return getU1(age) + this.DELTA;
		}

		@Override
		public boolean doScaling() {
			return false;
		}

		@Override
		public int getMaxAge() {
			return this.FRAMES * 2;
		}
	};
	//@formatter:on

	private final ResourceLocation resource;

	RippleStyle(@Nonnull final String texture) {
		this.resource = new ResourceLocation(ModInfo.RESOURCE_ID, texture);
	}

	@Nonnull
	public ResourceLocation getTexture() {
		return this.resource;
	}

	public float getU1(final int age) {
		return 0F;
	}

	public float getU2(final int age) {
		return 1F;
	}

	public float getV1(final int age) {
		return 0F;
	}

	public float getV2(final int age) {
		return 1F;
	}

	public boolean doScaling() {
		return true;
	}

	public boolean doAlpha() {
		return true;
	}

	public int getMaxAge() {
		return 12 + XorShiftRandom.current().nextInt(8);
	}

	@Nonnull
	public static RippleStyle getStyle(final int v) {
		if (v >= values().length)
			return CIRCLE;
		return values()[v];
	}

	@Nonnull
	public static RippleStyle get() {
		return getStyle(ModOptions.rain.rainRippleStyle);
	}
}
