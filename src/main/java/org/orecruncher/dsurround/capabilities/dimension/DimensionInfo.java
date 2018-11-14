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

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.dimension.DimensionData;
import org.orecruncher.dsurround.registry.dimension.DimensionRegistry;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class DimensionInfo implements IDimensionInfoEx {

	public final static float MIN_INTENSITY = 0.0F;
	public final static float MAX_INTENSITY = 1.0F;

	private static final DecimalFormat FORMATTER = new DecimalFormat("0");

	private final Random RANDOM = XorShiftRandom.current();

	protected final World world;
	protected WeakReference<DimensionData> ref;

	// Used server side for its stuff
	private float intensity = 0.0F;
	private float currentIntensity = 0.0F;
	private float minIntensity = ModOptions.rain.defaultMinRainStrength;
	private float maxIntensity = ModOptions.rain.defaultMaxRainStrength;
	private int thunderTimer = 0;

	public DimensionInfo() {
		this.world = null;
	}

	public DimensionInfo(@Nonnull final World world) {
		this.world = world;
		this.ref = new WeakReference<>(null);
	}

	protected DimensionData data() {
		DimensionData result = this.ref.get();
		if (result == null) {
			final DimensionRegistry reg = RegistryManager.get().get(DimensionRegistry.class);
			result = reg.getData(this.world);
			this.ref = new WeakReference<>(result);
		}
		return result;
	}

	//===================================
	//
	//  IDimensionInfo
	//
	//===================================
	@Override
	public int getId() {
		return this.world.provider.getDimension();
	}

	@Override
	@Nonnull
	public String getName() {
		return data().getName();
	}

	@Override
	public int getSeaLevel() {
		return data().getSeaLevel();
	}

	@Override
	public int getSkyHeight() {
		return data().getSkyHeight();
	}

	@Override
	public int getCloudHeight() {
		return data().getCloudHeight();
	}

	@Override
	public int getSpaceHeight() {
		return data().getSpaceHeight();
	}

	@Override
	public boolean hasHaze() {
		return data().getHasHaze();
	}

	@Override
	public boolean hasAuroras() {
		return data().getHasAuroras();
	}

	@Override
	public boolean hasWeather() {
		return data().getHasWeather();
	}

	@Override
	public boolean hasFog() {
		return data().getHasFog();
	}

	@Override
	public boolean playBiomeSounds() {
		return data().getPlayBiomeSounds();
	}

	//===================================
	//
	//  IDimensionInfoEx
	//
	//===================================
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

	private final class NBT {
		public final static String INTENSITY = "i";
		public final static String CURRENT_INTENSITY = "ci";
		public final static String MIN_INTENSITY = "min";
		public final static String MAX_INTENSITY = "max";
		public final static String THUNDER_TIMER = "th";
	};

}
