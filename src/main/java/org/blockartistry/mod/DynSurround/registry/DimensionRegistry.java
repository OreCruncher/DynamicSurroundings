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

package org.blockartistry.mod.DynSurround.registry;

import java.util.ArrayList;
import java.util.List;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.data.xface.DimensionConfig;
import org.blockartistry.mod.DynSurround.registry.DataScripts.IDependent;
import org.blockartistry.mod.DynSurround.util.DiurnalUtils;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

public final class DimensionRegistry implements IDependent {

	private final static DimensionRegistry INSTANCE = new DimensionRegistry();

	private DimensionRegistry() {
		DataScripts.registerDependent(this);
	}

	@Override
	public void preInit() {
		cache.clear();
		dimensionData.clear();
	}

	@Override
	public void postInit() {
		if (ModOptions.enableDebugLogging) {
			ModLog.info("*** DIMENSION REGISTRY (cache) ***");
			for (final DimensionConfig reg : cache)
				ModLog.info(reg.toString());
		}
	}

	private static final List<DimensionConfig> cache = new ArrayList<DimensionConfig>();
	private static final TIntObjectHashMap<DimensionInfo> dimensionData = new TIntObjectHashMap<DimensionInfo>();
	private static boolean isFlatWorld = false;

	public static void initialize() {
		INSTANCE.preInit();
	}

	// TODO: Need to hook into world load to get FLAT?
	public static void loading(final World world) {
		getData(world).initialize(world.provider);
		if (world.provider.getDimension() == 0) {
			isFlatWorld = world.getWorldInfo().getTerrainType() == WorldType.FLAT;
		}
	}

	private static DimensionConfig getData(final DimensionConfig entry) {
		for (final DimensionConfig e : cache)
			if ((e.dimensionId != null && e.dimensionId.equals(entry.dimensionId))
					|| (e.name != null && e.name.equals(entry.name)))
				return e;
		cache.add(entry);
		return entry;
	}

	public static void register(final DimensionConfig entry) {
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

	@SuppressWarnings("unused")
	private static void process(final DimensionFile config) {
		for (final DimensionConfig entry : config.entries) {
			register(entry);
		}
	}

	public static DimensionInfo getData(final World world) {
		DimensionInfo data = dimensionData.get(world.provider.getDimension());
		if (data == null) {
			DimensionConfig entry = null;
			for (final DimensionConfig e : cache)
				if ((e.dimensionId != null && e.dimensionId == world.provider.getDimension())
						|| (e.name != null && e.name.equals(world.provider.getDimensionType()))) {
					entry = e;
					break;
				}
			if (entry == null) {
				data = new DimensionInfo(world);
			} else {
				data = new DimensionInfo(world, entry);
			}
			dimensionData.put(world.provider.getDimension(), data);
		}
		return data;
	}

	public static boolean hasHaze(final World world) {
		return getData(world).getHasHaze();
	}

	public static int getSeaLevel(final World world) {
		if (world.provider.getDimension() == 0 && isFlatWorld)
			return 0;
		return getData(world).getSeaLevel();
	}

	public static int getSkyHeight(final World world) {
		return getData(world).getSkyHeight();
	}

	public static int getCloudHeight(final World world) {
		return getData(world).getCloudHeight();
	}

	public static int getSpaceHeight(final World world) {
		return getData(world).getSpaceHeight();
	}

	public static boolean hasAuroras(final World world) {
		return getData(world).getHasAuroras();
	}

	public static boolean hasWeather(final World world) {
		return getData(world).getHasWeather();
	}

	public static String getSeason(final World world) {
		return getData(world).getSeason();
	}

	private static final String CONDITION_TOKEN_RAINING = "raining";
	private static final String CONDITION_TOKEN_DAY = "day";
	private static final String CONDITION_TOKEN_NIGHT = "night";
	private static final char CONDITION_SEPARATOR = '#';

	public static String getConditions(final World world) {
		final StringBuilder builder = new StringBuilder();
		builder.append(CONDITION_SEPARATOR);
		if (DiurnalUtils.isDaytime(world))
			builder.append(CONDITION_TOKEN_DAY);
		else
			builder.append(CONDITION_TOKEN_NIGHT);
		builder.append(CONDITION_SEPARATOR).append(world.provider.getDimensionType());
		if (world.getRainStrength(1.0F) > 0.0F)
			builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_RAINING);
		builder.append(CONDITION_SEPARATOR).append(getSeason(world));
		builder.append(CONDITION_SEPARATOR);
		return builder.toString();
	}
}
