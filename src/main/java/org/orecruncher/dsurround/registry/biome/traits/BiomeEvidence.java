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

package org.orecruncher.dsurround.registry.biome.traits;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * What is known of a biome to work out its types from, with nothing of the game in it (so tests need no game): its
 * id's path and name, its biome dictionary types by name, its climate and its top block's id.
 */
public final class BiomeEvidence {

	// Biome.BiomeProperties' own: a biome that sets neither gives no climate worth reading
	static final float DEFAULT_TEMPERATURE = 0.5F;
	static final float DEFAULT_RAINFALL = 0.5F;

	public final String path;
	public final String name;
	public final Set<String> types;
	public final float temperature;
	public final float rainfall;
	public final boolean canRain;
	public final boolean snows;
	public final boolean highHumidity;
	public final String topBlock;

	public BiomeEvidence(@Nonnull final String path, @Nonnull final String name, @Nonnull final Set<String> types,
			final float temperature, final float rainfall, final boolean canRain, final boolean snows,
			final boolean highHumidity, @Nonnull final String topBlock) {
		this.path = path;
		this.name = name;
		this.types = Collections.unmodifiableSet(new LinkedHashSet<>(types));
		this.temperature = temperature;
		this.rainfall = rainfall;
		this.canRain = canRain;
		this.snows = snows;
		this.highHumidity = highHumidity;
		this.topBlock = topBlock;
	}

	/**
	 * Whether the biome was given a climate of its own, rather than left at the defaults
	 */
	public boolean hasClimate() {
		return this.temperature != DEFAULT_TEMPERATURE || this.rainfall != DEFAULT_RAINFALL;
	}
}
