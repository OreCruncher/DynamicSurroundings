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

package org.blockartistry.DynSurround.registry;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.data.xface.DimensionConfig;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

public final class DimensionInfo {

	public static final DimensionInfo NONE = new DimensionInfo();

	private static final int SPACE_HEIGHT_OFFSET = 32;

	protected final int dimensionId;
	protected final String name;

	protected int seaLevel;
	protected int skyHeight;
	protected int cloudHeight;
	protected int spaceHeight;
	protected boolean hasHaze = false;
	protected boolean hasAuroras = false;
	protected boolean hasWeather = false;
	protected boolean hasFog = false;

	private DimensionInfo() {
		this.dimensionId = Integer.MIN_VALUE;
		this.name = "<NOT SET>";
	}

	public DimensionInfo(@Nonnull final World world) {
		this.dimensionId = world.provider.getDimension();
		this.name = world.provider.getDimensionType().getName();
		this.seaLevel = world.getSeaLevel();
		this.skyHeight = world.getHeight();
		this.cloudHeight = this.skyHeight;
		this.spaceHeight = this.skyHeight + SPACE_HEIGHT_OFFSET;

		// Force sea level based on known world types that give heartburn
		final WorldType wt = world.getWorldType();

		if (wt == WorldType.FLAT)
			this.seaLevel = 0;
		else if (this.dimensionId == 0 && ModOptions.worldSealevelOverride > 0)
			this.seaLevel = ModOptions.worldSealevelOverride;
	}

	public DimensionInfo(@Nonnull final World world, @Nonnull final DimensionConfig entry) {
		this(world);

		if (entry.seaLevel != null)
			this.seaLevel = entry.seaLevel;
		if (entry.skyHeight != null)
			this.skyHeight = entry.skyHeight;
		if (entry.hasHaze != null)
			this.hasHaze = entry.hasHaze;
		if (entry.hasAurora != null)
			this.hasAuroras = entry.hasAurora;
		if (entry.hasWeather != null)
			this.hasWeather = entry.hasWeather;
		if (entry.cloudHeight != null)
			this.cloudHeight = entry.cloudHeight;
		else
			this.cloudHeight = this.hasHaze ? this.skyHeight / 2 : this.skyHeight;
		if (entry.hasFog != null)
			this.hasFog = entry.hasFog;

		this.spaceHeight = this.skyHeight + SPACE_HEIGHT_OFFSET;
	}

	public int getDimensionId() {
		return this.dimensionId;
	}

	public String getName() {
		return this.name;
	}

	public int getSeaLevel() {
		return this.seaLevel;
	}

	public int getSkyHeight() {
		return this.skyHeight;
	}

	public int getCloudHeight() {
		return this.cloudHeight;
	}

	public int getSpaceHeight() {
		return this.spaceHeight;
	}

	public boolean getHasHaze() {
		return this.hasHaze;
	}

	public boolean getHasAuroras() {
		return this.hasAuroras;
	}

	public boolean getHasWeather() {
		return this.hasWeather;
	}

	public boolean getHasFog() {
		return this.hasFog;
	}

	@Override
	@Nonnull
	public String toString() {
		final ToStringHelper builder = MoreObjects.toStringHelper(this);
		builder.add("id", this.dimensionId);
		builder.add("name", this.name);
		builder.add("seaLevel", this.seaLevel);
		builder.add("cloudHeight", this.cloudHeight);
		builder.add("skyHeight", this.skyHeight);
		builder.add("haze", this.hasHaze);
		builder.add("aurora", this.hasAuroras);
		builder.add("weather", this.hasWeather);
		builder.add("fog", this.hasFog);
		return builder.toString();
	}

}
