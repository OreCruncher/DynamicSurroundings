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
package org.orecruncher.dsurround.client.handlers.scanners;

import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.biome.BiomeInfo;
import org.orecruncher.lib.chunk.ClientChunkCache;
import org.orecruncher.lib.chunk.IBlockAccessEx;

import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Performs an area scan around the to calculate the relative weights of the
 * biomes in the local area.
 */
@SideOnly(Side.CLIENT)
public final class BiomeScanner implements ITickable {

	private static final int BIOME_SURVEY_RANGE = 20;
	private static final int MAX_BIOME_AREA = (int) Math.pow(BIOME_SURVEY_RANGE * 2 + 1, 2);

	private int biomeArea;
	private final Reference2FloatOpenHashMap<BiomeInfo> weights = new Reference2FloatOpenHashMap<>();
	private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

	// "Finger print" of the last area survey.
	private BiomeInfo surveyedBiome = null;
	private int surveyedDimension = 0;
	private BlockPos surveyedPosition = BlockPos.ORIGIN;

	@Override
	public void update() {
		final BlockPos position = EnvironState.getPlayerPosition();

		if (this.surveyedBiome != EnvironState.getPlayerBiome()
				|| this.surveyedDimension != EnvironState.getDimensionId()
				|| this.surveyedPosition.compareTo(position) != 0) {

			this.surveyedBiome = EnvironState.getPlayerBiome();
			this.surveyedDimension = EnvironState.getDimensionId();
			this.surveyedPosition = position;

			this.biomeArea = 0;
			this.weights.clear();

			if (EnvironState.getPlayerBiome().isFake()) {
				this.biomeArea = 1;
				this.weights.put(EnvironState.getPlayerBiome(), 1);
			} else {
				final IBlockAccessEx provider = ClientChunkCache.instance();

				// Collect raw biome data before mapping to BiomeInfo - saves lookups
				final Reference2IntOpenHashMap<Biome> scratch = new Reference2IntOpenHashMap<>();
				scratch.defaultReturnValue(-1);

				for (int dX = -BIOME_SURVEY_RANGE; dX <= BIOME_SURVEY_RANGE; dX++)
					for (int dZ = -BIOME_SURVEY_RANGE; dZ <= BIOME_SURVEY_RANGE; dZ++) {
						this.mutable.setPos(this.surveyedPosition.getX() + dX, 0, this.surveyedPosition.getZ() + dZ);
						final Biome biome = provider.getBiome(this.mutable);
						final int v = scratch.getInt(biome);
						if (v == -1)
							scratch.put(biome, 1);
						else
							scratch.put(biome, v + 1);
					}

				this.biomeArea = MAX_BIOME_AREA;
				scratch.reference2IntEntrySet().forEach(
						entry -> this.weights.put(RegistryManager.BIOME.get(entry.getKey()), entry.getIntValue()));
			}
		}
	}

	public int getBiomeArea() {
		return this.biomeArea;
	}

	public Reference2FloatOpenHashMap<BiomeInfo> getBiomes() {
		return this.weights;
	}

}
