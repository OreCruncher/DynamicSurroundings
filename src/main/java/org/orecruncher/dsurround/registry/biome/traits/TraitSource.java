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

import javax.annotation.Nonnull;

/**
 * Where a biome's type came from, in the order they are looked at. The biome dictionary's are taken as given; the
 * guesses only fill in what those leave out (see {@link TraitGroup}).
 */
public enum TraitSource {

	/**
	 * Forge's biome dictionary: what the biome's mod registered (or, for a biome registered with none, what Forge
	 * guessed from its climate)
	 */
	DICTIONARY("biome dictionary", false),
	/**
	 * Follows from types taken as given (snowy is cold)
	 */
	DERIVED("derived", false),
	/**
	 * The biome's climate: temperature, rainfall, whether it rains
	 */
	CLIMATE("climate", true),
	/**
	 * Words in the biome's id and name
	 */
	NAME("name", true),
	/**
	 * The block its ground is topped with
	 */
	TOP_BLOCK("top block", true),
	/**
	 * Follows from guessed types
	 */
	DERIVED_FROM_GUESS("derived from guesses", true);

	private final String label;
	private final boolean guess;

	TraitSource(@Nonnull final String label, final boolean guess) {
		this.label = label;
		this.guess = guess;
	}

	@Nonnull
	public String label() {
		return this.label;
	}

	public boolean isGuess() {
		return this.guess;
	}
}
