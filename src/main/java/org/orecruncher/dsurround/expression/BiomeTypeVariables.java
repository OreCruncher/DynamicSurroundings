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
package org.orecruncher.dsurround.expression;

import java.util.Set;

import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.lib.BiomeUtils;
import org.orecruncher.lib.expression.Dynamic;
import org.orecruncher.lib.expression.DynamicVariantList;

import net.minecraftforge.common.BiomeDictionary;

public class BiomeTypeVariables extends DynamicVariantList {

	public BiomeTypeVariables() {
		// Scan the BiomeDictionary adding the the types
		final Set<BiomeDictionary.Type> types = BiomeUtils.getBiomeTypes();
		for (final BiomeDictionary.Type t : types)
			add(new Dynamic.DynamicBoolean("biome.is" + t.getName()) {
				@Override
				public void update() {
					this.value = EnvironState.getTruePlayerBiome().isBiomeType(t);
				}
			});
	}
}
