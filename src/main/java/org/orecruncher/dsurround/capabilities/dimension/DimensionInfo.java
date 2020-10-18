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

package org.orecruncher.dsurround.capabilities.dimension;

import java.text.DecimalFormat;
import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.config.DimensionConfig;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

public final class DimensionInfo implements IDimensionInfoEx {

	public final static IDimensionInfo NONE = new DimensionInfo();

	public final static float MIN_INTENSITY = 0.0F;
	public final static float MAX_INTENSITY = 1.0F;

	private static final int SPACE_HEIGHT_OFFSET = 32;
	private static final DecimalFormat FORMATTER = new DecimalFormat("0");

	protected final Random RANDOM = XorShiftRandom.current();
	protected final World world;

	// Rain/weather tracking data. Some of the data is synchronized from the server.
	private float intensity = 0.0F;
	private float currentIntensity = 0.0F;
	private float minIntensity = ModOptions.rain.defaultMinRainStrength;
	private float maxIntensity = ModOptions.rain.defaultMaxRainStrength;
	private int thunderTimer = 0;

	// Attributes about the dimension. This is information is loaded from
	// local configs.
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
	protected boolean alwaysOutside = false;
	protected boolean playBiomeSounds = true;

	public DimensionInfo() {
		this.world = null;
		this.dimensionId = Integer.MIN_VALUE;
		this.name = "<DEFAULT NONE>";
	}

	public DimensionInfo(@Nonnull final World world) {
		this.world = world;

		// Attributes that come from the world object itself. Set
		// now because the config may override.
		this.dimensionId = world.provider.getDimension();
		this.name = world.provider.getDimensionType().getName();
		this.seaLevel = world.getSeaLevel();
		this.skyHeight = world.getActualHeight();
		this.cloudHeight = this.skyHeight;
		this.spaceHeight = this.skyHeight + SPACE_HEIGHT_OFFSET;

		if (world.provider.isSurfaceWorld() && world.provider.hasSkyLight()) {
			this.hasWeather = true;
			this.hasAuroras = true;
			this.hasFog = true;
		}

		// Force sea level based on known world types that give heartburn
		final WorldType wt = world.getWorldType();

		if (wt == WorldType.FLAT)
			this.seaLevel = 0;
		else if (this.dimensionId == 0 && ModOptions.biomes.worldSealevelOverride > 0)
			this.seaLevel = ModOptions.biomes.worldSealevelOverride;

		final String dim = Integer.toString(this.dimensionId);
		for (int i = 0; i < ModOptions.biomes.dimensionBlacklist.length; i++)
			if (dim.equals(ModOptions.biomes.dimensionBlacklist[i])) {
				this.playBiomeSounds = false;
				break;
			}

		// Override based on player config settings
		final DimensionConfig entry = RegistryManager.DIMENSION.getData(this.world);
		if (entry != null) {
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
			if (entry.alwaysOutside != null)
				this.alwaysOutside = entry.alwaysOutside;

			this.spaceHeight = this.skyHeight + SPACE_HEIGHT_OFFSET;
		}
	}

	// ===================================
	//
	// IDimensionInfo
	//
	// ===================================
	@Override
	public int getId() {
		return this.dimensionId;
	}

	@Nonnull
	public String getName() {
		return this.name;
	}

	@Override
	public int getSeaLevel() {
		return this.seaLevel;
	}

	@Override
	public int getSkyHeight() {
		return this.skyHeight;
	}

	@Override
	public int getCloudHeight() {
		return this.cloudHeight;
	}

	@Override
	public int getSpaceHeight() {
		return this.spaceHeight;
	}

	@Override
	public boolean hasHaze() {
		return this.hasHaze;
	}

	@Override
	public boolean hasAuroras() {
		return this.hasAuroras;
	}

	@Override
	public boolean hasWeather() {
		return this.hasWeather;
	}

	@Override
	public boolean hasFog() {
		return this.hasFog;
	}

	@Override
	public boolean playBiomeSounds() {
		return this.playBiomeSounds;
	}

	@Override
	public boolean alwaysOutside() {
		return this.alwaysOutside;
	}

	// ===================================
	//
	// IDimensionInfoEx
	//
	// ===================================
	@Override
	public float getRainIntensity() {
		return this.intensity;
	}

	@Override
	public float getCurrentRainIntensity() {
		return this.currentIntensity;
	}

	@Override
	public void setRainIntensity(final float intensity) {
		this.intensity = MathStuff.clamp(intensity, MIN_INTENSITY, MAX_INTENSITY);
	}

	@Override
	public void setCurrentRainIntensity(final float intensity) {
		this.currentIntensity = MathStuff.clamp(intensity, 0, this.intensity);
	}

	@Override
	public float getMinRainIntensity() {
		return this.minIntensity;
	}

	@Override
	public void setMinRainIntensity(final float intensity) {
		this.minIntensity = MathStuff.clamp(intensity, MIN_INTENSITY, this.maxIntensity);
	}

	@Override
	public float getMaxRainIntensity() {
		return this.maxIntensity;
	}

	@Override
	public void setMaxRainIntensity(final float intensity) {
		this.maxIntensity = MathStuff.clamp(intensity, this.minIntensity, MAX_INTENSITY);
	}

	@Override
	public int getThunderTimer() {
		return this.thunderTimer;
	}

	@Override
	public void setThunderTimer(final int time) {
		this.thunderTimer = MathStuff.clamp(time, 0, Integer.MAX_VALUE);
	}

	@Override
	public void randomizeRain() {
		final float result;
		final float delta = this.maxIntensity - this.minIntensity;
		if (delta <= 0.0F) {
			result = this.minIntensity;
		} else {
			final float mid = delta / 2.0F;
			result = this.minIntensity + this.RANDOM.nextFloat() * mid + this.RANDOM.nextFloat() * mid;
		}
		setRainIntensity(MathStuff.clamp(result, 0.01F, MAX_INTENSITY));
		setCurrentRainIntensity(0.0F);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		final NBTTagCompound nbt = new NBTTagCompound();
		nbt.setFloat(NBT.INTENSITY, getRainIntensity());
		nbt.setFloat(NBT.CURRENT_INTENSITY, getCurrentRainIntensity());
		nbt.setFloat(NBT.MIN_INTENSITY, getMinRainIntensity());
		nbt.setFloat(NBT.MAX_INTENSITY, getMaxRainIntensity());
		nbt.setInteger(NBT.THUNDER_TIMER, getThunderTimer());
		return nbt;
	}

	@Override
	public void deserializeNBT(@Nonnull final NBTTagCompound nbt) {
		setRainIntensity(nbt.getFloat(NBT.INTENSITY));
		setCurrentRainIntensity(nbt.getFloat(NBT.CURRENT_INTENSITY));
		setMinRainIntensity(nbt.getFloat(NBT.MIN_INTENSITY));
		setMaxRainIntensity(nbt.getFloat(NBT.MAX_INTENSITY));
		setThunderTimer(nbt.getInteger(NBT.THUNDER_TIMER));
	}

	@Override
	@Nonnull
	public String configString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("dim ").append(getId()).append(": ");
		builder.append("rainIntensity [").append(FORMATTER.format(getMinRainIntensity() * 100));
		builder.append(",").append(FORMATTER.format(getMaxRainIntensity() * 100));
		builder.append("]");
		return builder.toString();
	}

	@Override
	@Nonnull
	public String toString() {
		// Dump out some diagnostics for the current dimension
		final StringBuilder builder = new StringBuilder();
		builder.append("dim ").append(getId()).append(": ");
		builder.append("rainIntensity: ").append(FORMATTER.format(getRainIntensity() * 100));
		builder.append('/').append(FORMATTER.format(getCurrentRainIntensity() * 100));
		builder.append(" [").append(FORMATTER.format(getMinRainIntensity() * 100));
		builder.append(",").append(FORMATTER.format(getMaxRainIntensity() * 100));
		builder.append("], thunderTimer: ").append(getThunderTimer());
		return builder.toString();
	}

	private static final class NBT {
		public final static String INTENSITY = "i";
		public final static String CURRENT_INTENSITY = "ci";
		public final static String MIN_INTENSITY = "min";
		public final static String MAX_INTENSITY = "max";
		public final static String THUNDER_TIMER = "th";
	}

}
