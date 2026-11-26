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

package org.orecruncher.dsurround.profiling;

import javax.annotation.Nonnull;

/**
 * A recording's headline, for chat as it finishes (see {@link ProfileSummary#headline}): the frame rate and the effect
 * that cost the largest share of drawing a frame. Uses nothing from {@code jdk.jfr}, so {@link Profiler} can hold one.
 */
final class ProfileHeadline {

	// Frames a second
	final int fps;
	// The costliest effect, as its render timer names it
	final String effect;
	// Its share of the GPU's and the CPU's time drawing a frame, 0 to 1
	final double gpuShare;
	final double cpuShare;

	ProfileHeadline(final int fps, @Nonnull final String effect, final double gpuShare, final double cpuShare) {
		this.fps = fps;
		this.effect = effect;
		this.gpuShare = gpuShare;
		this.cpuShare = cpuShare;
	}
}
