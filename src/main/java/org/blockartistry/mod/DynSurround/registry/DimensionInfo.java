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

package org.blockartistry.mod.DynSurround.registry;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.data.xface.DimensionConfig;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

public final class DimensionInfo {

	private static final int SPACE_HEIGHT_OFFSET = 32;

	protected final int dimensionId;
	protected boolean initialized;
	protected String name = "<NOT SET>";
	protected Integer seaLevel;
	protected Integer skyHeight;
	protected Integer cloudHeight;
	protected Integer spaceHeight;
	protected Boolean hasHaze;
	protected Boolean hasAuroras;
	protected Boolean hasWeather;

	public DimensionInfo(@Nonnull final World world) {
		this.dimensionId = world.provider.getDimension();
		initialize(world.provider);
	}

	public DimensionInfo(@Nonnull final World world, @Nonnull final DimensionConfig entry) {
		this.dimensionId = world.provider.getDimension();
		this.name = world.provider.getDimensionType().getName();
		this.seaLevel = entry.seaLevel;
		this.skyHeight = entry.skyHeight;
		this.hasHaze = entry.hasHaze;
		this.hasAuroras = entry.hasAurora;
		this.hasWeather = entry.hasWeather;
		this.cloudHeight = entry.cloudHeight;
		initialize(world.provider);
	}

	public DimensionInfo initialize(@Nonnull final WorldProvider provider) {
		if (!this.initialized) {
			this.name = provider.getDimensionType().getName();
			if (this.seaLevel == null)
				this.seaLevel = provider.getAverageGroundLevel();
			if (this.skyHeight == null)
				this.skyHeight = provider.getHeight();
			if (this.hasHaze == null)
				this.hasHaze = !provider.getHasNoSky();
			if (this.hasAuroras == null)
				this.hasAuroras = !provider.getHasNoSky();
			if (this.hasWeather == null)
				this.hasWeather = !provider.getHasNoSky();
			if (this.cloudHeight == null)
				this.cloudHeight = this.hasHaze ? this.skyHeight / 2 : this.skyHeight;
			if (this.spaceHeight == null)
				this.spaceHeight = this.skyHeight + SPACE_HEIGHT_OFFSET;
			this.initialized = true;
			ModLog.info("Dimension initialized " + this.toString());
		}
		return this;
	}

	public int getDimensionId() {
		return this.dimensionId;
	}

	public String getName() {
		return this.name;
	}

	public int getSeaLevel() {
		return this.seaLevel.intValue();
	}

	public int getSkyHeight() {
		return this.skyHeight.intValue();
	}

	public int getCloudHeight() {
		return this.cloudHeight.intValue();
	}

	public int getSpaceHeight() {
		return this.spaceHeight.intValue();
	}

	public boolean getHasHaze() {
		return this.hasHaze.booleanValue();
	}

	public boolean getHasAuroras() {
		return this.hasAuroras.booleanValue();
	}

	public boolean getHasWeather() {
		return this.hasWeather.booleanValue();
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
		return builder.toString();
	}

}
