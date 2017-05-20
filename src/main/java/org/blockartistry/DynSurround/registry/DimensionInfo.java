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

import net.minecraft.world.World;
import net.minecraft.world.WorldType;

public final class DimensionInfo {
	
	public static final DimensionInfo NONE = new DimensionInfo();

	private static final int SPACE_HEIGHT_OFFSET = 32;

	protected final int dimensionId;
	protected String name = "<NOT SET>";
	protected int seaLevel;
	protected int skyHeight;
	protected int cloudHeight;
	protected int spaceHeight;
	protected boolean hasHaze;
	protected boolean hasAuroras;
	protected boolean hasWeather;
	protected boolean hasFog;

	private DimensionInfo() {
		this.dimensionId = Integer.MIN_VALUE;
	}
	
	public DimensionInfo(@Nonnull final World world) {
		this.dimensionId = world.provider.getDimension();
		this.name = world.provider.getDimensionType().getName();
		this.seaLevel = world.getSeaLevel();
		this.skyHeight = world.getHeight();
		this.hasHaze = !world.provider.getHasNoSky();
		this.hasAuroras = !world.provider.getHasNoSky();
		this.hasWeather = !world.provider.getHasNoSky();
		this.cloudHeight = this.hasHaze ? this.skyHeight / 2 : this.skyHeight;
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
		final StringBuilder builder = new StringBuilder();
		builder.append(this.dimensionId).append('/').append(this.name).append(':');
		builder.append(" seaLevel:").append(this.seaLevel);
		builder.append(" cloudH:").append(this.cloudHeight);
		builder.append(" skyH:").append(this.skyHeight);
		builder.append(" haze:").append(Boolean.toString(this.hasHaze));
		builder.append(" aurora:").append(Boolean.toString(this.hasAuroras));
		builder.append(" weather:").append(Boolean.toString(this.hasWeather));
		builder.append(" fog:").append(Boolean.toString(this.hasFog));
		return builder.toString();
	}

}
