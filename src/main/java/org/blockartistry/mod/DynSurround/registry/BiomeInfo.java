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
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.util.Color;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.TempCategory;

public final class BiomeInfo {

	private final int biomeId;
	private final String biomeName;
	private final Biome biome;

	public int getBiomeId() {
		return this.biomeId;
	}

	public String getBiomeName() {
		return this.biomeName;
	}

	private boolean hasPrecipitation;
	private boolean hasDust;
	private boolean hasAurora;
	private boolean hasFog;
	
	public boolean getHasPrecipitation() {
		return this.hasPrecipitation;
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

	private Color dustColor;
	private Color fogColor;
	private float fogDensity;
	
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
	
	private List<SoundEffect> sounds;
	private int spotSoundChance;
	private List<SoundEffect> spotSounds;
	
	@Nonnull
	public List<SoundEffect> getSounds() {
		return this.sounds;
	}
	
	public int getSpotSoundChance() {
		return this.spotSoundChance;
	}
	
	public void setSpotSoundChance(final int chance) {
		this.spotSoundChance = chance;
	}
	
	@Nonnull
	public List<SoundEffect> getSpotSounds() {
		return this.spotSounds;
	}
	
	public BiomeInfo(final int biomeId, @Nonnull final String biomeName) {
		this.biome = null;
		this.biomeId = biomeId;
		this.biomeName = biomeName;
		
		this.sounds = new ArrayList<SoundEffect>();
		this.spotSounds = new ArrayList<SoundEffect>();
		this.spotSoundChance = 1200;
	}

	public BiomeInfo(@Nonnull final Biome biome) {

		this.biome = biome;
		this.biomeId = Biome.getIdForBiome(biome);
		this.biomeName = biome.getBiomeName();

		this.hasPrecipitation = biome.canRain() || biome.getEnableSnow();
		this.sounds = new ArrayList<SoundEffect>();
		this.spotSounds = new ArrayList<SoundEffect>();
		this.spotSoundChance = 1200;
	}

	public boolean isFake() {
		return this.biome == null;
	}
	
	public float getFloatTemperature(@Nonnull final BlockPos pos) {
		return isFake() ? 0.0F : this.biome.getFloatTemperature(pos);
	}

	public TempCategory getTempCategory() {
		return isFake() ? TempCategory.COLD : this.biome.getTempCategory();
	}

	public boolean isHighHumidity() {
		return isFake() ? false : this.biome.isHighHumidity();
	}

	public float getRainfall() {
		return isFake() ? 0.0F : this.biome.getRainfall();
	}

	@Nullable
	public SoundEffect findSoundMatch(@Nonnull final String conditions) {
		for (final SoundEffect sound : this.sounds)
			if (sound.matches(conditions))
				return sound;
		return null;
	}

	@Nonnull
	public List<SoundEffect> findSoundMatches(@Nonnull final String conditions) {
		final List<SoundEffect> results = new ArrayList<SoundEffect>();
		for (final SoundEffect sound : this.sounds)
			if (sound.matches(conditions))
				results.add(sound);
		return results;
	}
	
	@Nullable
	public SoundEffect getSpotSound(@Nonnull final String conditions, @Nonnull final Random random) {
		if (this.getSpotSounds().isEmpty() || random.nextInt(this.getSpotSoundChance()) != 0)
			return null;

		int totalWeight = 0;
		final List<SoundEffect> candidates = new ArrayList<SoundEffect>();
		for (final SoundEffect s : getSpotSounds())
			if (s.matches(conditions)) {
				candidates.add(s);
				totalWeight += s.weight;
			}
		if (totalWeight <= 0)
			return null;

		if (candidates.size() == 1)
			return candidates.get(0);

		int targetWeight = random.nextInt(totalWeight);
		int i = 0;
		for (i = candidates.size(); (targetWeight -= candidates.get(i - 1).weight) >= 0; i--)
			;

		return candidates.get(i - 1);
	}
	
	public void resetSounds() {
		this.sounds.clear();
		this.spotSounds.clear();
		this.spotSoundChance = 1200;
	}

	@Override
	@Nonnull
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(String.format("Biomes %d [%s]:", this.biomeId, this.biomeName));
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

		if (!this.sounds.isEmpty()) {
			builder.append("; sounds [");
			for (final SoundEffect sound : this.sounds)
				builder.append(sound.toString()).append(',');
			builder.append(']');
		}

		if (!this.spotSounds.isEmpty()) {
			builder.append("; spot sound chance:").append(this.spotSoundChance);
			builder.append(" spot sounds [");
			for (final SoundEffect sound : this.spotSounds)
				builder.append(sound.toString()).append(',');
			builder.append(']');
		}
		return builder.toString();
	}
}
