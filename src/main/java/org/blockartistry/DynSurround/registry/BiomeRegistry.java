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

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.data.xface.BiomeConfig;
import org.blockartistry.DynSurround.data.xface.SoundConfig;
import org.blockartistry.DynSurround.data.xface.SoundType;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.MyUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;

public final class BiomeRegistry extends Registry {

	public static final FakeBiome UNDERGROUND = new FakeBiome("Underground");
	public static final FakeBiome PLAYER = new FakeBiome("Player");
	public static final FakeBiome UNDERWATER = new FakeBiome("Underwater");
	public static final FakeBiome UNDEROCEAN = new FakeBiome("UnderOCN");
	public static final FakeBiome UNDERDEEPOCEAN = new FakeBiome("UnderDOCN");
	public static final FakeBiome UNDERRIVER = new FakeBiome("UnderRVR");
	public static final FakeBiome OUTERSPACE = new FakeBiome("OuterSpace");
	public static final FakeBiome CLOUDS = new FakeBiome("Clouds");
	public static final FakeBiome VILLAGE = new FakeBiome("Village");

	public BiomeInfo VILLAGE_INFO;
	public BiomeInfo PLAYER_INFO;
	public BiomeInfo UNDERGROUND_INFO;
	public BiomeInfo CLOUDS_INFO;
	public BiomeInfo OUTERSPACE_INFO;
	public BiomeInfo WTF_INFO;

	// This is for cases when the biome coming in doesn't make sense
	// and should default to something to avoid crap.
	private static final FakeBiome WTF = new WTFFakeBiome();

	private final Map<Biome, BiomeInfo> registry = new IdentityHashMap<Biome, BiomeInfo>();
	private final Map<String, String> biomeAliases = new HashMap<String, String>();

	BiomeRegistry(@Nonnull final Side side) {
		super(side);
	}

	@Override
	public void init() {
		this.biomeAliases.clear();
		this.registry.clear();

		for (final String entry : ModOptions.biomeAliases) {
			final String[] parts = StringUtils.split(entry, "=");
			if (parts.length == 2) {
				this.biomeAliases.put(parts[0], parts[1]);
			}
		}

		for (Iterator<Biome> itr = Biome.REGISTRY.iterator(); itr.hasNext();) {
			final BiomeInfo e = new BiomeInfo(itr.next());
			this.registry.put(e.biome, e);
		}

		// Add our fake biomes
		this.registry.put(UNDERWATER, new BiomeInfo(UNDERWATER));
		this.registry.put(UNDEROCEAN, new BiomeInfo(UNDEROCEAN));
		this.registry.put(UNDERDEEPOCEAN, new BiomeInfo(UNDERDEEPOCEAN));
		this.registry.put(UNDERRIVER, new BiomeInfo(UNDERRIVER));

		this.registry.put(WTF, WTF_INFO = new BiomeInfo(WTF));
		this.registry.put(PLAYER, PLAYER_INFO = new BiomeInfo(PLAYER));
		this.registry.put(VILLAGE, VILLAGE_INFO = new BiomeInfo(VILLAGE));
		this.registry.put(UNDERGROUND, UNDERGROUND_INFO = new BiomeInfo(UNDERGROUND));
		this.registry.put(CLOUDS, CLOUDS_INFO = new BiomeInfo(CLOUDS));
		this.registry.put(OUTERSPACE, OUTERSPACE_INFO = new BiomeInfo(OUTERSPACE));
	}

	@Override
	public void initComplete() {
		if (ModOptions.enableDebugLogging) {
			DSurround.log().info("*** BIOME REGISTRY ***");
			for (final BiomeInfo entry : registry.values())
				DSurround.log().info(entry.toString());
		}

		// Free memory because we no longer need
		this.biomeAliases.clear();
	}

	@Override
	public void fini() {

	}

	@Nonnull
	public BiomeInfo get(@Nonnull final Biome biome) {
		// This shouldn't happen, but...
		if (biome == null)
			return WTF_INFO;

		BiomeInfo result = this.registry.get(biome);
		if (result == null) {
			// Open Terrain Generation can trigger this...
			DSurround.log().warn("Biome [%s] not detected during initialization - forcing reload (%s)", biome.getBiomeName(),
					biome.getClass());
			RegistryManager.reloadResources(this.side);
			result = this.registry.get(biome);
			if (result == null) {
				throw new RuntimeException("What's going on with biomes?");
			}
		}
		return result;
	}

	final boolean isBiomeMatch(@Nonnull final BiomeConfig entry, @Nonnull final String biomeName) {
		if (Pattern.matches(entry.biomeName, biomeName))
			return true;
		final String alias = this.biomeAliases.get(biomeName);
		return alias == null ? false : Pattern.matches(entry.biomeName, alias);
	}

	public void registerBiomeAlias(@Nonnull final String alias, @Nonnull final String biome) {
		this.biomeAliases.put(alias, biome);
	}

	public void register(@Nonnull final BiomeConfig entry) {
		final SoundRegistry soundRegistry = RegistryManager.getManager().getRegistry(RegistryType.SOUND);

		for (final BiomeInfo biomeEntry : this.registry.values()) {
			if (isBiomeMatch(entry, biomeEntry.getBiomeName())) {
				if (entry.hasPrecipitation != null)
					biomeEntry.setHasPrecipitation(entry.hasPrecipitation.booleanValue());
				if (entry.hasAurora != null)
					biomeEntry.setHasAurora(entry.hasAurora.booleanValue());
				if (entry.hasDust != null)
					biomeEntry.setHasDust(entry.hasDust.booleanValue());
				if (entry.hasFog != null)
					biomeEntry.setHasFog(entry.hasFog.booleanValue());
				if (entry.fogDensity != null)
					biomeEntry.setFogDensity(entry.fogDensity.floatValue());
				if (entry.fogColor != null) {
					final int[] rgb = MyUtils.splitToInts(entry.fogColor, ',');
					if (rgb.length == 3)
						biomeEntry.setFogColor(new Color(rgb[0], rgb[1], rgb[2]));
				}
				if (entry.dustColor != null) {
					final int[] rgb = MyUtils.splitToInts(entry.dustColor, ',');
					if (rgb.length == 3)
						biomeEntry.setDustColor(new Color(rgb[0], rgb[1], rgb[2]));
				}
				if (entry.soundReset != null && entry.soundReset.booleanValue()) {
					biomeEntry.resetSounds();
				}

				if (entry.spotSoundChance != null)
					biomeEntry.setSpotSoundChance(entry.spotSoundChance.intValue());

				for (final SoundConfig sr : entry.sounds) {
					if (soundRegistry.isSoundBlocked(sr.sound))
						continue;
					final SoundEffect.Builder b = new SoundEffect.Builder(sr);
					if (sr.soundCategory == null)
						b.setSoundCategory(SoundCategory.AMBIENT);
					final SoundEffect s = b.build();
					if (s.getSoundType() == SoundType.SPOT)
						biomeEntry.addSpotSound(s);
					else
						biomeEntry.addSound(s);
				}
			}
		}
	}
}
