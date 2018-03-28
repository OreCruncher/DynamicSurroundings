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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.handlers.BiomeSoundEffectsHandler;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.data.xface.BiomeConfig;
import org.blockartistry.DynSurround.data.xface.SoundConfig;
import org.blockartistry.DynSurround.data.xface.SoundType;
import org.blockartistry.lib.BiomeUtils;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.MyUtils;
import org.blockartistry.lib.WeightTable;
import org.blockartistry.lib.collections.ObjectArray;
import org.blockartistry.lib.compat.ModEnvironment;

import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class BiomeInfo implements Comparable<BiomeInfo> {

	private static Class<?> bopBiome = null;
	private static Field bopBiomeFogDensity = null;
	private static Field bopBiomeFogColor = null;

	static {

		if (ModEnvironment.BiomesOPlenty.isLoaded())
			try {
				bopBiome = Class.forName("biomesoplenty.common.biome.BOPBiome");
				bopBiomeFogDensity = ReflectionHelper.findField(bopBiome, "fogDensity");
				bopBiomeFogColor = ReflectionHelper.findField(bopBiome, "fogColor");
			} catch (final Throwable t) {
				bopBiome = null;
				bopBiomeFogDensity = null;
				bopBiomeFogColor = null;
			}
	}

	public final static int DEFAULT_SPOT_CHANCE = 1000 / BiomeSoundEffectsHandler.SCAN_INTERVAL;
	public final static SoundEffect[] NO_SOUNDS = {};

	protected final IBiome biome;

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

	protected final List<String> comments = Lists.newArrayList();

	protected final boolean isRiver;
	protected final boolean isOcean;
	protected final boolean isDeepOcean;

	public BiomeInfo(@Nonnull final IBiome biome) {
		this.biome = biome;

		if (!isFake()) {
			this.hasPrecipitation = canRain() || getEnableSnow();
		}

		// If it is a BOP biome initialize from the BoP Biome
		// instance. May be overwritten by DS config.
		if (bopBiome != null && !biome.isFake()) {
			final Biome b = biome.getBiome();
			if (bopBiome.isInstance(b)) {
				try {
					final int color = bopBiomeFogColor.getInt(b);
					if (color > 0) {
						this.hasFog = true;
						this.fogColor = new Color(color);
						this.fogDensity = bopBiomeFogDensity.getFloat(b);
					}
				} catch (final Exception ex) {

				}
			}
		}

		this.isRiver = this.biome.getTypes().contains(Type.RIVER);
		this.isOcean = this.biome.getTypes().contains(Type.OCEAN);
		this.isDeepOcean = this.isOcean && getBiomeName().matches("(?i).*deep.*ocean.*|.*abyss.*");
	}

	public boolean isRiver() {
		return this.isRiver;
	}

	public boolean isOcean() {
		return this.isOcean;
	}

	public boolean isDeepOcean() {
		return this.isDeepOcean;
	}

	public ResourceLocation getKey() {
		return this.biome.getKey();
	}

	public int getBiomeId() {
		return this.biome.getId();
	}

	public Set<Type> getBiomeTypes() {
		return this.biome.getTypes();
	}

	void addComment(@Nonnull final String comment) {
		if (!StringUtils.isEmpty(comment))
			this.comments.add(comment);
	}

	public List<String> getComments() {
		return this.comments;
	}

	public String getBiomeName() {
		return this.biome.getName();
	}

	public boolean hasWeatherEffect() {
		return getHasPrecipitation() || getHasDust();
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

	void setHasPrecipitation(final boolean flag) {
		this.hasPrecipitation = flag;
	}

	public boolean getHasDust() {
		return this.hasDust;
	}

	void setHasDust(final boolean flag) {
		this.hasDust = flag;
	}

	public boolean getHasAurora() {
		return this.hasAurora;
	}

	void setHasAurora(final boolean flag) {
		this.hasAurora = flag;
	}

	public boolean getHasFog() {
		return this.hasFog;
	}

	void setHasFog(final boolean flag) {
		this.hasFog = flag;
	}

	public Color getDustColor() {
		return this.dustColor;
	}

	void setDustColor(final Color color) {
		this.dustColor = color;
	}

	public Color getFogColor() {
		return this.fogColor;
	}

	void setFogColor(@Nonnull final Color color) {
		this.fogColor = color;
	}

	public float getFogDensity() {
		return this.fogDensity;
	}

	void setFogDensity(final float density) {
		this.fogDensity = density;
	}

	void setSpotSoundChance(final int chance) {
		this.spotSoundChance = chance;
	}

	void addSound(final SoundEffect sound) {
		this.sounds = MyUtils.append(this.sounds, sound);
	}

	void addSpotSound(final SoundEffect sound) {
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
	public Collection<SoundEffect> findSoundMatches() {
		return findSoundMatches(new ObjectArray<SoundEffect>(8));
	}

	@Nonnull
	public Collection<SoundEffect> findSoundMatches(@Nonnull final Collection<SoundEffect> results) {
		for (int i = 0; i < this.sounds.length; i++) {
			final SoundEffect sound = this.sounds[i];
			if (sound.matches())
				results.add(sound);
		}
		return results;
	}

	@Nullable
	public SoundEffect getSpotSound(@Nonnull final Random random) {
		return this.spotSounds != NO_SOUNDS && random.nextInt(this.spotSoundChance) == 0
				? new WeightTable<>(this.spotSounds).next()
				: null;
	}

	void resetSounds() {
		this.sounds = NO_SOUNDS;
		this.spotSounds = NO_SOUNDS;
		this.spotSoundChance = DEFAULT_SPOT_CHANCE;
	}

	public boolean isBiomeType(@Nonnull final BiomeDictionary.Type type) {
		return getBiomeTypes().contains(type);
	}

	public boolean areBiomesSameClass(@Nonnull final Biome biome) {
		return BiomeUtils.areBiomesSimilar(this.biome.getBiome(), biome);
	}

	// Internal to the package
	void update(@Nonnull final BiomeConfig entry) {
		addComment(entry.comment);
		if (entry.hasPrecipitation != null)
			setHasPrecipitation(entry.hasPrecipitation.booleanValue());
		if (entry.hasAurora != null)
			setHasAurora(entry.hasAurora.booleanValue());
		if (entry.hasDust != null)
			setHasDust(entry.hasDust.booleanValue());
		if (entry.hasFog != null)
			setHasFog(entry.hasFog.booleanValue());
		if (entry.fogDensity != null)
			setFogDensity(entry.fogDensity.floatValue());
		if (entry.fogColor != null) {
			final int[] rgb = MyUtils.splitToInts(entry.fogColor, ',');
			if (rgb.length == 3)
				setFogColor(new Color(rgb[0], rgb[1], rgb[2]));
		}
		if (entry.dustColor != null) {
			final int[] rgb = MyUtils.splitToInts(entry.dustColor, ',');
			if (rgb.length == 3)
				setDustColor(new Color(rgb[0], rgb[1], rgb[2]));
		}
		if (entry.soundReset != null && entry.soundReset.booleanValue()) {
			addComment("> Sound Reset");
			resetSounds();
		}

		if (entry.spotSoundChance != null)
			setSpotSoundChance(entry.spotSoundChance.intValue());

		for (final SoundConfig sr : entry.sounds) {
			if (ClientRegistry.SOUND.isSoundBlocked(sr.sound))
				continue;
			final SoundEffect.Builder b = new SoundEffect.Builder(sr);
			final SoundEffect s = b.build();
			if (s.getSoundType() == SoundType.SPOT)
				addSpotSound(s);
			else
				addSound(s);
		}

	}

	@Override
	@Nonnull
	public String toString() {
		final ResourceLocation rl = this.biome.getKey();
		final String registryName = rl == null ? (isFake() ? "FAKE" : "UNKNOWN") : rl.toString();

		final StringBuilder builder = new StringBuilder();
		builder.append("Biome [").append(getBiomeName()).append('/').append(registryName).append("] (")
				.append(getBiomeId()).append("):");
		if (!isFake()) {
			builder.append("\n+ ").append('<');
			boolean comma = false;
			for (final BiomeDictionary.Type t : getBiomeTypes()) {
				if (comma)
					builder.append(',');
				else
					comma = true;
				builder.append(t.getName());
			}
			builder.append('>').append('\n');
			builder.append("+ temp: ").append(getTemperature()).append(" (").append(getTemperatureRating().getValue())
					.append(")");
			builder.append(" rain: ").append(getRainfall());
		}

		if (this.hasPrecipitation)
			builder.append(" PRECIPITATION");
		if (this.hasDust)
			builder.append(" DUST");
		if (this.hasAurora)
			builder.append(" AURORA");
		if (this.hasFog)
			builder.append(" FOG");
		if (this.dustColor != null)
			builder.append(" dustColor:").append(this.dustColor.toString());
		if (this.fogColor != null) {
			builder.append(" fogColor:").append(this.fogColor.toString());
			builder.append(" fogDensity:").append(this.fogDensity);
		}

		if (this.sounds.length > 0) {
			builder.append("\n+ sounds [\n");
			for (final SoundEffect sound : this.sounds)
				builder.append("+   ").append(sound.toString()).append('\n');
			builder.append("+ ]");
		}

		if (this.spotSounds.length > 0) {
			builder.append("\n+ spot sound chance:").append(this.spotSoundChance);
			builder.append("\n+ spot sounds [\n");
			for (final SoundEffect sound : this.spotSounds)
				builder.append("+   ").append(sound.toString()).append('\n');
			builder.append("+ ]");
		}

		if (this.comments.size() > 0) {
			builder.append("\n+ comments:\n");
			for (final String c : this.comments)
				builder.append("+   ").append(c).append('\n');
		}

		return builder.toString();
	}

	@Override
	public int compareTo(@Nonnull final BiomeInfo o) {
		return getBiomeName().compareTo(o.getBiomeName());
	}
}
