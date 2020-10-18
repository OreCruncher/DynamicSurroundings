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
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.config.DimensionConfig;
import org.orecruncher.dsurround.registry.config.ModConfiguration;

import net.minecraft.world.World;

public final class DimensionRegistry extends Registry {

	public DimensionRegistry() {
		super("Dimension Registry");
	}

	@Override
	protected void preInit() {
		this.cache.clear();
	}

	@Override
	protected void init(@Nonnull final ModConfiguration cfg) {
		cfg.dimensions.forEach(this::register);
	}

	@Override
	protected void complete() {
		if (ModOptions.logging.enableDebugLogging) {
			ModBase.log().info("*** DIMENSION REGISTRY (cache) ***");
			this.cache.stream().map(Object::toString).forEach(ModBase.log()::info);
		}
	}

	private final List<DimensionConfig> cache = new ArrayList<>();

	public void loading(@Nonnull final World world) {
		getData(world);
	}

	@Nonnull
	private DimensionConfig getData(@Nonnull final DimensionConfig entry) {
		final Optional<DimensionConfig> result = this.cache.stream().filter(e -> e.equals(entry)).findFirst();
		if (result.isPresent())
			return result.get();
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

	@Nullable
	public DimensionConfig getData(@Nonnull final World world) {
		// Dimension registry is shared so we need to guard access in case
		// the client reloads.
		synchronized (this) {
			for (final DimensionConfig e : this.cache)
				if ((e.dimensionId != null && e.dimensionId == world.provider.getDimension())
						|| (e.name != null && e.name.equals(world.provider.getDimensionType().getName()))) {
					return e;
				}
			return null;
			/*
			 * this.dimensionData.put(world.provider.getDimension(), data);
			 * ModBase.log().info(data.toString()); final WorldBorder border =
			 * world.getWorldBorder(); if (border != null) { final StringBuilder builder =
			 * new StringBuilder(); builder.append("x: ").append((long)
			 * border.minX()).append('/').append((long) border.maxX()) .append(", ");
			 * builder.append("z: ").append((long) border.minZ()).append('/').append((long)
			 * border.maxZ()) .append(", "); builder.append("center: (").append((long)
			 * border.getCenterX()).append(',') .append((long)
			 * border.getCenterZ()).append(')'); ModBase.log().info(builder.toString()); }
			 *
			 * return data;
			 */
		}
	}
}
