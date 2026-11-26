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

package org.orecruncher.dsurround.client.aurora;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The classic aurora: the night's curtains ({@link Aurora}) drawn by {@link AuroraRenderer}, fading in as it appears
 * and out as it goes ({@link AuroraLifeTracker}). Ported from the 26.x line, in place of the old banded auroras (the
 * immediate-mode one and the shader band, which cost about 5 ms of GPU a frame at 1920x1080).
 */
@SideOnly(Side.CLIENT)
public final class AuroraClassic implements IAurora {

	private final Aurora aurora;
	private final AuroraLifeTracker tracker;

	public AuroraClassic(final long seed) {
		this.aurora = Aurora.create(seed, ModOptions.aurora.maxBands);
		this.tracker = new AuroraLifeTracker(AuroraUtils.AURORA_PEAK_AGE, AuroraUtils.AURORA_AGE_RATE);
	}

	@Override
	public void setFading(final boolean flag) {
		this.tracker.setFading(flag);
	}

	@Override
	public boolean isDying() {
		return this.tracker.isFading();
	}

	@Override
	public void update() {
		this.tracker.update();
	}

	@Override
	public boolean isComplete() {
		return !this.tracker.isAlive();
	}

	@Override
	public void render(final float partialTick) {
		AuroraRenderer.render(this.aurora, this.tracker.ageRatio(), AuroraUtils.getTimeSeconds());
	}

	@Override
	@Nonnull
	public String toString() {
		return String.format("<CLASSIC> bands %d, brightness %.2f, heading %.0f, strength %.2f%s", this.aurora.bands,
				this.aurora.brightness, Math.toDegrees(this.aurora.heading), this.tracker.ageRatio(),
				this.tracker.isFading() ? ", fading" : "");
	}
}
