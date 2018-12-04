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
package org.orecruncher.dsurround.client.handlers.fog;

import java.util.Map;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.capabilities.CapabilitySeasonInfo;
import org.orecruncher.dsurround.capabilities.season.ISeasonInfo;
import org.orecruncher.dsurround.capabilities.season.SeasonType;
import org.orecruncher.dsurround.capabilities.season.SeasonType.SubType;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SeasonFogRangeCalculator extends MorningFogRangeCalculator {

	private static final Map<SeasonKey, FogType> MAPPING = new Object2ReferenceOpenHashMap<>();
	static {
		MAPPING.put(new SeasonKey(SeasonType.AUTUMN, SubType.EARLY), FogType.NORMAL);
		MAPPING.put(new SeasonKey(SeasonType.AUTUMN, SubType.MID), FogType.MEDIUM);
		MAPPING.put(new SeasonKey(SeasonType.AUTUMN, SubType.LATE), FogType.HEAVY);

		MAPPING.put(new SeasonKey(SeasonType.WINTER, SubType.EARLY), FogType.MEDIUM);
		MAPPING.put(new SeasonKey(SeasonType.WINTER, SubType.MID), FogType.LIGHT);
		MAPPING.put(new SeasonKey(SeasonType.WINTER, SubType.LATE), FogType.NORMAL);

		MAPPING.put(new SeasonKey(SeasonType.SPRING, SubType.EARLY), FogType.MEDIUM);
		MAPPING.put(new SeasonKey(SeasonType.SPRING, SubType.MID), FogType.HEAVY);
		MAPPING.put(new SeasonKey(SeasonType.SPRING, SubType.LATE), FogType.NORMAL);

		MAPPING.put(new SeasonKey(SeasonType.SUMMER, SubType.EARLY), FogType.LIGHT);
		MAPPING.put(new SeasonKey(SeasonType.SUMMER, SubType.MID), FogType.NONE);
		MAPPING.put(new SeasonKey(SeasonType.SUMMER, SubType.LATE), FogType.LIGHT);
	}

	@Override
	public FogType getFogType() {
		final World world = EnvironState.getWorld();
		final ISeasonInfo cap = CapabilitySeasonInfo.getCapability(world);
		if (cap != null) {
			final SeasonType t = cap.getSeasonType(world);
			final SeasonType.SubType st = cap.getSeasonSubType(world);
			final FogType type = MAPPING.get(new SeasonKey(t, st));
			if (type != null)
				return type;
		}
		return super.getFogType();
	}

	private static class SeasonKey implements Map.Entry<SeasonType, SubType> {

		private final SeasonType season;
		private final SubType subType;

		public SeasonKey(@Nonnull final SeasonType s, @Nonnull final SubType st) {
			this.season = s;
			this.subType = st;
		}

		@Override
		@Nonnull
		public SeasonType getKey() {
			return this.season;
		}

		@Override
		@Nonnull
		public SubType getValue() {
			return this.subType;
		}

		@Override
		public SubType setValue(@Nonnull final SubType value) {
			return null;
		}

		@Override
		public int hashCode() {
			return this.season.hashCode() ^ (31 * this.subType.hashCode());
		}

		@Override
		public boolean equals(@Nonnull final Object key) {
			final SeasonKey sk = (SeasonKey) key;
			return this.season == sk.season && this.subType == sk.subType;
		}

	}

}
