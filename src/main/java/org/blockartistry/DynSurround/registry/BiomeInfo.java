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

package org.blockartistry.DynSurround.registry;

import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.client.handlers.AreaSoundEffectHandler;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.MyUtils;
import org.blockartistry.lib.WeightTable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraftforge.common.BiomeDictionary;

public final class BiomeInfo {

	public final static int DEFAULT_SPOT_CHANCE = 1200 / AreaSoundEffectHandler.SCAN_INTERVAL;
	public final static SoundEffect[] NO_SOUNDS = {};

	protected final Biome biome;

	protected boolean hasPrecipitation;
	protected boolean hasDust;
	protected boolean hasAurora;
	protected boolean hasFog;

	private Color dustColor;
	private Color fogColor;
	private float fogDensity;

	protected SoundEffect[] sounds = NO_SOUNDS;
	protected SoundEffect[] spotSounds = NO_SOUNDS;
	protected int spotSoundChance = DEFAULT_SPOT_CHANCE;

	protected final Set<BiomeDictionary.Type> biomeTypes;

	public BiomeInfo(@Nonnull final Biome biome) {
		this.biome = biome;

		if (!this.isFake()) {
			this.hasPrecipitation = canRain() || getEnableSnow();
			this.biomeTypes = BiomeDictionary.getTypes(this.biome);
		} else {
			this.biomeTypes = ImmutableSet.of();
		}
	}

	public String getBiomeName() {
		return this.biome.getBiomeName();
	}

	public boolean getHasPrecipitation() {
		return this.hasPrecipitation;
	}

	public boolean canRain() {
		return this.biome.canRain();
	}

	public boolean getEnableSnow() {
		return this.biome.getEnableSnow();
	}

	public void setHasPrecipitation(final boolean flag) {
		this.hasPrecipitation = flag;
	}

	public boolean getHasDust() {
		return this.hasDust;
	}

	public void setHasDust(final boolean flag) {
		this.hasDust = flag;
	}

	public boolean getHasAurora() {
		return this.hasAurora;
	}

	public void setHasAurora(final boolean flag) {
		this.hasAurora = flag;
	}

	public boolean getHasFog() {
		return this.hasFog;
	}

	public void setHasFog(final boolean flag) {
		this.hasFog = flag;
	}

	public Color getDustColor() {
		return this.dustColor;
	}

	public void setDustColor(final Color color) {
		this.dustColor = color;
	}

	public Color getFogColor() {
		return this.fogColor;
	}

	public void setFogColor(@Nonnull final Color color) {
		this.fogColor = color;
	}

	public float getFogDensity() {
		return this.fogDensity;
	}

	public void setFogDensity(final float density) {
		this.fogDensity = density;
	}

	public void setSpotSoundChance(final int chance) {
		this.spotSoundChance = chance;
	}

	public void addSound(final SoundEffect sound) {
		this.sounds = MyUtils.append(this.sounds, sound);
	}

	public void addSpotSound(final SoundEffect sound) {
		this.spotSounds = MyUtils.append(this.spotSounds, sound);
	}

	public boolean isFake() {
		return this.biome instanceof FakeBiome;
	}

	public float getFloatTemperature(@Nonnull final BlockPos pos) {
		return this.biome.getFloatTemperature(pos);
	}

	public float getTemperature() {
		return this.biome.getTemperature();
	}

	public TempCategory getTempCategory() {
		return this.biome.getTempCategory();
	}

	public TemperatureRating getTemperatureRating() {
		return TemperatureRating.fromTemp(getTemperature());
	}

	public boolean isHighHumidity() {
		return this.biome.isHighHumidity();
	}

	public float getRainfall() {
		return this.biome.getRainfall();
	}

	@Nonnull
	public void findSoundMatches(@Nonnull final List<SoundEffect> results) {
		for (int i = 0; i < this.sounds.length; i++) {
			final SoundEffect sound = this.sounds[i];
			if (sound.matches())
				results.add(sound);
		}
	}

	@Nullable
	public SoundEffect getSpotSound(@Nonnull final Random random) {
		return this.spotSounds != NO_SOUNDS && random.nextInt(this.spotSoundChance) == 0
				? new WeightTable<SoundEffect>(this.spotSounds).next() : null;
	}

	public void resetSounds() {
		this.sounds = NO_SOUNDS;
		this.spotSounds = NO_SOUNDS;
		this.spotSoundChance = DEFAULT_SPOT_CHANCE;
	}

	public boolean isBiomeType(@Nonnull final BiomeDictionary.Type type) {
		return this.biomeTypes.contains(type);
	}
	
	@Override
	@Nonnull
	public String toString() {
		final ResourceLocation rl = this.biome.getRegistryName();
		final String registryName = rl == null ? "UNKNOWN" : rl.toString();

		final StringBuilder builder = new StringBuilder();
		builder.append("Biome [").append(this.getBiomeName()).append('/').append(registryName).append("]:");
		if (this.isFake()) {
			builder.append(" FAKE ");
		} else {
			builder.append('<');
			boolean comma = false;
			for (final BiomeDictionary.Type t : this.biomeTypes) {
				if (comma)
					builder.append(',');
				else
					comma = true;
				builder.append(t.getName());
			}
			builder.append('>');
			builder.append(" temp: ").append(this.getTemperature()).append(" (")
					.append(getTemperatureRating().getValue()).append(")");
			builder.append(" rain: ").append(this.getRainfall());
		}

		if (this.hasPrecipitation)
			builder.append(" PRECIPITATION");
		if (this.hasDust)
			builder.append(" DUST");
		if (this.hasAurora)
			builder.append(" AURORA");
		if (this.hasFog)
			builder.append(" FOG");
		if (!this.hasPrecipitation && !this.hasDust && !this.hasAurora && !this.hasFog)
			builder.append(" NONE");
		if (this.dustColor != null)
			builder.append(" dustColor:").append(this.dustColor.toString());
		if (this.fogColor != null) {
			builder.append(" fogColor:").append(this.fogColor.toString());
			builder.append(" fogDensity:").append(this.fogDensity);
		}

		if (this.sounds.length > 0) {
			builder.append("; sounds [");
			for (final SoundEffect sound : this.sounds)
				builder.append(sound.toString()).append(',');
			builder.append(']');
		}

		if (this.spotSounds.length > 0) {
			builder.append("; spot sound chance:").append(this.spotSoundChance);
			builder.append(" spot sounds [");
			for (final SoundEffect sound : this.spotSounds)
				builder.append(sound.toString()).append(',');
			builder.append(']');
		}
		return builder.toString();
	}
}
