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

package org.orecruncher.dsurround.client.fx.particle.system;

/**
 * How many splashes a waterfall throws ({@link ParticleWaterSplash}), from the 26.x line's WaterfallEffect. Apart from
 * the splash, whose particle system reads the game's settings as it loads, so it can be tested without the game.
 */
final class WaterfallSplashes {

	// Beyond this distance from the camera, half the splashes
	static final double PARTICLE_FULL_DISTANCE_SQ = 16D * 16D;
	// Beyond vanilla's particle distance, a quarter; beyond twice it, none
	static final double PARTICLE_HALF_DISTANCE_SQ = 32D * 32D;
	static final double PARTICLE_RANGE_SQ = 64D * 64D;

	private WaterfallSplashes() {
	}

	/**
	 * How many splashes to throw this time, by the Particles option: none at Minimal, half at Decreased; between
	 * half and all of that, at random, so they don't come in a fixed beat
	 */
	static int count(final int limit, final int particleSetting, final int roll) {
		final int count = particleSetting >= 2 ? 0 : particleSetting == 1 ? limit / 2 : limit;
		if (count < 4)
			return count;
		final int half = count / 2;
		return half + Math.floorMod(roll, count - half);
	}

	/**
	 * Fewer splashes far from the camera: half beyond 16 blocks, a quarter beyond 32, none beyond 64
	 */
	static int byDistance(final int count, final double cameraDistanceSq) {
		if (cameraDistanceSq > PARTICLE_RANGE_SQ)
			return 0;
		if (cameraDistanceSq > PARTICLE_HALF_DISTANCE_SQ)
			return count / 4;
		if (cameraDistanceSq > PARTICLE_FULL_DISTANCE_SQ)
			return count / 2;
		return count;
	}
}
