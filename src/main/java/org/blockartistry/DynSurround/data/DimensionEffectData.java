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

package org.blockartistry.DynSurround.data;

import java.text.DecimalFormat;
import java.util.Random;
import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.lib.MathStuff;
import org.blockartistry.lib.random.XorShiftRandom;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

/**
 * Per world effect data for effects
 */
public final class DimensionEffectData extends WorldSavedData {

	private static final DecimalFormat FORMATTER = new DecimalFormat("0");

	public final static float MIN_INTENSITY = 0.0F;
	public final static float MAX_INTENSITY = 1.0F;

	private final class NBT {
		public final static String DIMENSION = "d";
		public final static String INTENSITY = "s";
		public final static String CURRENT_INTENSITY = "ci";
		public final static String MIN_INTENSITY = "min";
		public final static String MAX_INTENSITY = "max";
		public final static String THUNDER_TIMER = "th";
	};

	private final Random RANDOM = XorShiftRandom.current();
	private int dimensionId = 0;
	private float intensity = 0.0F;
	private float currentIntensity = 0.0F;
	private float minIntensity = ModOptions.defaultMinRainStrength;
	private float maxIntensity = ModOptions.defaultMaxRainStrength;
	private int thunderTimer = 0;

	private DimensionEffectData(final int dimension) {
		this(DSurround.MOD_ID);
		this.dimensionId = dimension;
	}

	public DimensionEffectData(@Nonnull final String identifier) {
		super(identifier);
	}

	public int getDimensionId() {
		return this.dimensionId;
	}

	public float getRainIntensity() {
		return this.intensity;
	}

	public float getCurrentRainIntensity() {
		return this.currentIntensity;
	}

	public void setRainIntensity(final float intensity) {
		final float i = MathStuff.clamp(intensity, MIN_INTENSITY, MAX_INTENSITY);
		if (this.intensity != i) {
			this.intensity = i;
			this.markDirty();
		}
	}

	public void setCurrentRainIntensity(final float intensity) {
		final float i = MathStuff.clamp(intensity, 0, this.intensity);
		if (this.currentIntensity != i) {
			this.currentIntensity = i;
			this.markDirty();
		}
	}

	public float getMinRainIntensity() {
		return this.minIntensity;
	}

	public void setMinRainIntensity(final float intensity) {
		final float i = MathStuff.clamp(intensity, MIN_INTENSITY, this.maxIntensity);
		if (this.minIntensity != i) {
			this.minIntensity = i;
			this.markDirty();
		}
	}

	public float getMaxRainIntensity() {
		return this.maxIntensity;
	}

	public void setMaxRainIntensity(final float intensity) {
		final float i = MathStuff.clamp(intensity, this.minIntensity, MAX_INTENSITY);
		if (this.maxIntensity != i) {
			this.maxIntensity = i;
			this.markDirty();
		}
	}

	public int getThunderTimer() {
		return this.thunderTimer;
	}

	public void setThunderTimer(final int time) {
		final int t = MathStuff.clamp(time, 0, Integer.MAX_VALUE);
		if (this.thunderTimer != t) {
			this.thunderTimer = t;
			this.markDirty();
		}
	}

	public void randomizeRain() {
		final float result;
		final float delta = this.maxIntensity - this.minIntensity;
		if (delta <= 0.0F) {
			result = (float) this.minIntensity;
		} else {
			final float mid = delta / 2.0F;
			result = this.minIntensity + RANDOM.nextFloat() * mid + RANDOM.nextFloat() * mid;
		}
		setRainIntensity(MathStuff.clamp(result, 0.01F, MAX_INTENSITY));
		setCurrentRainIntensity(0.0F);
	}

	@Override
	public void readFromNBT(@Nonnull final NBTTagCompound nbt) {
		this.dimensionId = nbt.getInteger(NBT.DIMENSION);
		this.setRainIntensity(nbt.getFloat(NBT.INTENSITY));
		if (nbt.hasKey(NBT.CURRENT_INTENSITY))
			this.setCurrentRainIntensity(nbt.getFloat(NBT.CURRENT_INTENSITY));
		if (nbt.hasKey(NBT.MIN_INTENSITY))
			this.setMinRainIntensity(nbt.getFloat(NBT.MIN_INTENSITY));
		if (nbt.hasKey(NBT.MAX_INTENSITY))
			this.setMaxRainIntensity(nbt.getFloat(NBT.MAX_INTENSITY));
		if (nbt.hasKey(NBT.THUNDER_TIMER))
			this.setThunderTimer(nbt.getInteger(NBT.THUNDER_TIMER));
	}

	@Override
	@Nonnull
	public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound nbt) {
		nbt.setInteger(NBT.DIMENSION, this.getDimensionId());
		nbt.setFloat(NBT.INTENSITY, this.getRainIntensity());
		nbt.setFloat(NBT.CURRENT_INTENSITY, this.getCurrentRainIntensity());
		nbt.setFloat(NBT.MIN_INTENSITY, this.getMinRainIntensity());
		nbt.setFloat(NBT.MAX_INTENSITY, this.getMaxRainIntensity());
		nbt.setInteger(NBT.THUNDER_TIMER, this.getThunderTimer());
		return nbt;
	}

	@Nonnull
	public String configString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("dim ").append(this.dimensionId).append(": ");
		builder.append("rainIntensity [").append(FORMATTER.format(this.minIntensity * 100));
		builder.append(",").append(FORMATTER.format(this.maxIntensity * 100));
		builder.append("]");
		return builder.toString();
	}

	@Override
	@Nonnull
	public String toString() {
		// Dump out some diagnostics for the current dimension
		final StringBuilder builder = new StringBuilder();
		builder.append("dim ").append(this.dimensionId).append(": ");
		builder.append("rainIntensity: ").append(FORMATTER.format(this.intensity * 100));
		builder.append('/').append(FORMATTER.format(this.currentIntensity * 100));
		builder.append(" [").append(FORMATTER.format(this.minIntensity * 100));
		builder.append(",").append(FORMATTER.format(this.maxIntensity * 100));
		builder.append("], thunderTimer: ").append(this.thunderTimer);
		return builder.toString();
	}

	@Nonnull
	public static DimensionEffectData get(@Nonnull final World world) {
		final MapStorage storage = world.getPerWorldStorage();
		DimensionEffectData data = (DimensionEffectData) storage.getOrLoadData(DimensionEffectData.class,
				DSurround.MOD_ID);
		if (data == null) {
			data = new DimensionEffectData(world.provider.getDimension());
			storage.setData(DSurround.MOD_ID, data);
		}
		return data;
	}

}
