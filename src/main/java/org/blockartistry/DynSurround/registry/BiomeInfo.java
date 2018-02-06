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
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModEnvironment;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.handlers.AreaSoundEffectHandler;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.data.xface.BiomeConfig;
import org.blockartistry.DynSurround.data.xface.SoundConfig;
import org.blockartistry.DynSurround.data.xface.SoundType;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.MyUtils;
import org.blockartistry.lib.WeightTable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class BiomeInfo implements Comparable<BiomeInfo> {

	private static Field biomeName = null;
	private static Class<?> bopBiome = null;
	private static Field bopBiomeFogDensity = null;
	private static Field bopBiomeFogColor = null;

	static {
		try {
			biomeName = ReflectionHelper.findField(Biome.class, "biomeName", "field_76791_y");
		} catch (final Throwable t) {
			DSurround.log().error("Unable to obtain Biome::biomeName field reference!", t);
		}

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

	public final static int DEFAULT_SPOT_CHANCE = 1000 / AreaSoundEffectHandler.SCAN_INTERVAL;
	public final static SoundEffect[] NO_SOUNDS = {};

	protected final Biome biome;
	protected final int biomeId;

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

	protected final List<String> comments = Lists.newArrayList();

	public BiomeInfo(@Nonnull final Biome biome) {
		this.biome = biome;

		if (!this.isFake()) {
			this.hasPrecipitation = canRain() || getEnableSnow();
			this.biomeTypes = Sets.newIdentityHashSet();
			for (final BiomeDictionary.Type t : BiomeDictionary.getTypesForBiome(this.biome))
				this.biomeTypes.add(t);
			this.biomeId = Biome.getIdForBiome(this.biome);
		} else {
			this.biomeTypes = ImmutableSet.of();
			this.biomeId = ((FakeBiome) this.biome).getBiomeId();
		}

		// If it is a BOP biome initialize from the BoP Biome
		// instance. May be overwritten by DS config.
		if (bopBiome != null && bopBiome.isInstance(biome)) {
			try {
				final int color = bopBiomeFogColor.getInt(biome);
				if (color > 0) {
					this.hasFog = true;
					this.fogColor = new Color(color);
					this.fogDensity = bopBiomeFogDensity.getFloat(biome);
				}
			} catch (final Exception ex) {

			}
		}
	}

	public static ResourceLocation getKey(@Nonnull final Biome biome) {
		ResourceLocation res = biome.getRegistryName();
		if (res == null) {
			final String name = biome.getClass().getName() + "_" + biome.getBiomeName().replace(' ', '_').toLowerCase();
			res = new ResourceLocation(DSurround.RESOURCE_ID, name);
		}
		return res;
	}

	public ResourceLocation getKey() {
		return getKey(this.biome);
	}

	public int getBiomeId() {
		return this.biomeId;
	}

	void addComment(@Nonnull final String comment) {
		if (!StringUtils.isEmpty(comment))
			this.comments.add(comment);
	}

	public List<String> getComments() {
		return this.comments;
	}

	public String getBiomeName() {
		try {
			return (String) biomeName.get(this.biome);
		} catch (final Throwable t) {
			return "UNKNOWN";
		}
	}

	public boolean hasWeatherEffect() {
		return this.getHasPrecipitation() || this.getHasDust();
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
				? new WeightTable<SoundEffect>(this.spotSounds).next()
				: null;
	}

	void resetSounds() {
		this.sounds = NO_SOUNDS;
		this.spotSounds = NO_SOUNDS;
		this.spotSoundChance = DEFAULT_SPOT_CHANCE;
	}

	public boolean isBiomeType(@Nonnull final BiomeDictionary.Type type) {
		return this.biomeTypes.contains(type);
	}

	public boolean areBiomesSameClass(@Nonnull final Biome biome) {
		return BiomeDictionary.areBiomesEquivalent(this.biome, biome);
	}

	// Internal to the package
	void update(@Nonnull final BiomeConfig entry) {
		this.addComment(entry.comment);
		if (entry.hasPrecipitation != null)
			this.setHasPrecipitation(entry.hasPrecipitation.booleanValue());
		if (entry.hasAurora != null)
			this.setHasAurora(entry.hasAurora.booleanValue());
		if (entry.hasDust != null)
			this.setHasDust(entry.hasDust.booleanValue());
		if (entry.hasFog != null)
			this.setHasFog(entry.hasFog.booleanValue());
		if (entry.fogDensity != null)
			this.setFogDensity(entry.fogDensity.floatValue());
		if (entry.fogColor != null) {
			final int[] rgb = MyUtils.splitToInts(entry.fogColor, ',');
			if (rgb.length == 3)
				this.setFogColor(new Color(rgb[0], rgb[1], rgb[2]));
		}
		if (entry.dustColor != null) {
			final int[] rgb = MyUtils.splitToInts(entry.dustColor, ',');
			if (rgb.length == 3)
				this.setDustColor(new Color(rgb[0], rgb[1], rgb[2]));
		}
		if (entry.soundReset != null && entry.soundReset.booleanValue()) {
			this.addComment("> Sound Reset");
			this.resetSounds();
		}

		if (entry.spotSoundChance != null)
			this.setSpotSoundChance(entry.spotSoundChance.intValue());

		for (final SoundConfig sr : entry.sounds) {
			if (ClientRegistry.SOUND.isSoundBlocked(sr.sound))
				continue;
			final SoundEffect.Builder b = new SoundEffect.Builder(sr);
			final SoundEffect s = b.build();
			if (s.getSoundType() == SoundType.SPOT)
				this.addSpotSound(s);
			else
				this.addSound(s);
		}

	}

	@Override
	@Nonnull
	public String toString() {
		final ResourceLocation rl = this.biome.getRegistryName();
		final String registryName = rl == null ? (this.isFake() ? "FAKE" : "UNKNOWN") : rl.toString();

		final StringBuilder builder = new StringBuilder();
		builder.append("Biome [").append(this.getBiomeName()).append('/').append(registryName).append("] (")
				.append(this.getBiomeId()).append("):");
		if (!this.isFake()) {
			builder.append("\n+ ").append('<');
			boolean comma = false;
			for (final BiomeDictionary.Type t : this.biomeTypes) {
				if (comma)
					builder.append(',');
				else
					comma = true;
				builder.append(t.name());
			}
			builder.append('>').append('\n');
			builder.append("+ temp: ").append(this.getTemperature()).append(" (")
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
		return this.getBiomeName().compareTo(o.getBiomeName());
	}
}
