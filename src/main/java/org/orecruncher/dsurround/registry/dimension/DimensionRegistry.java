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

package org.orecruncher.dsurround.registry.dimension;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.config.DimensionConfig;
import org.orecruncher.dsurround.registry.config.ModConfiguration;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

public final class DimensionRegistry extends Registry {

	public DimensionRegistry(@Nonnull final Side side) {
		super(side);
	}

	@Override
	public void init() {
		this.cache.clear();
		this.dimensionData.clear();
	}

	@Override
	public void configure(@Nonnull final ModConfiguration cfg) {
		cfg.dimensions.forEach(dim -> register(dim));
	}

	@Override
	public void initComplete() {
		if (ModOptions.logging.enableDebugLogging) {
			ModBase.log().info("*** DIMENSION REGISTRY (cache) ***");
			this.cache.forEach(dim -> ModBase.log().info(dim.toString()));
		}
	}

	@Override
	public void fini() {

	}

	private final List<DimensionConfig> cache = new ArrayList<>();
	private final TIntObjectHashMap<DimensionInfo> dimensionData = new TIntObjectHashMap<>();

	public void loading(@Nonnull final World world) {
		getData(world);
	}

	@Nonnull
	private DimensionConfig getData(@Nonnull final DimensionConfig entry) {
		for (final DimensionConfig e : this.cache)
			if (e.equals(entry))
				return e;
		this.cache.add(entry);
		return entry;
	}

	public void register(@Nonnull final DimensionConfig entry) {
		if (entry.dimensionId != null || entry.name != null) {
			final DimensionConfig data = getData(entry);
			if (data == entry)
				return;
			if (data.dimensionId == null)
				data.dimensionId = entry.dimensionId;
			if (data.name == null)
				data.name = entry.name;
			if (entry.hasAurora != null)
				data.hasAurora = entry.hasAurora;
			if (entry.hasHaze != null)
				data.hasHaze = entry.hasHaze;
			if (entry.hasWeather != null)
				data.hasWeather = entry.hasWeather;
			if (entry.cloudHeight != null)
				data.cloudHeight = entry.cloudHeight;
			if (entry.seaLevel != null)
				data.seaLevel = entry.seaLevel;
			if (entry.skyHeight != null)
				data.skyHeight = entry.skyHeight;
		}
	}

	@Nonnull
	public DimensionInfo getData(@Nonnull final World world) {
		DimensionInfo data = this.dimensionData.get(world.provider.getDimension());
		if (data == null) {
			DimensionConfig entry = null;
			for (final DimensionConfig e : this.cache)
				if ((e.dimensionId != null && e.dimensionId == world.provider.getDimension())
						|| (e.name != null && e.name.equals(world.provider.getDimensionType().getName()))) {
					entry = e;
					break;
				}
			if (entry == null) {
				data = new DimensionInfo(world);
			} else {
				data = new DimensionInfo(world, entry);
			}

			this.dimensionData.put(world.provider.getDimension(), data);
			ModBase.log().info(data.toString());
		}
		return data;
	}

	public boolean hasHaze(@Nonnull final World world) {
		return getData(world).getHasHaze();
	}

	public int getSeaLevel(@Nonnull final World world) {
		return getData(world).getSeaLevel();
	}

	public int getSkyHeight(@Nonnull final World world) {
		return getData(world).getSkyHeight();
	}

	public int getCloudHeight(@Nonnull final World world) {
		return getData(world).getCloudHeight();
	}

	public int getSpaceHeight(@Nonnull final World world) {
		return getData(world).getSpaceHeight();
	}

	public boolean hasAuroras(@Nonnull final World world) {
		return getData(world).getHasAuroras();
	}

	public boolean hasWeather(@Nonnull final World world) {
		return getData(world).getHasWeather();
	}

	public boolean hasFog(@Nonnull final World world) {
		return getData(world).getHasFog();
	}
	
	public boolean getPlayBiomeSounds(@Nonnull final World world) {
		return getData(world).getPlayBiomeSounds();
	}

}
