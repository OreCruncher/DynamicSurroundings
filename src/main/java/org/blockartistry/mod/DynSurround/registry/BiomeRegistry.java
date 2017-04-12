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

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.data.xface.BiomeConfig;
import org.blockartistry.mod.DynSurround.data.xface.SoundConfig;
import org.blockartistry.mod.DynSurround.data.xface.SoundType;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.mod.DynSurround.util.Color;
import org.blockartistry.mod.DynSurround.util.MyUtils;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;

public final class BiomeRegistry extends Registry {

	BiomeRegistry(@Nonnull final Side side) {
		super(side);
	}
	
	@Override
	public void init() {
		biomeAliases.clear();
		registry.clear();

		for (final String entry : ModOptions.biomeAliases) {
			final String[] parts = StringUtils.split(entry, "=");
			if (parts.length == 2) {
				biomeAliases.put(parts[0], parts[1]);
			}
		}

		for (Iterator<Biome> itr = Biome.REGISTRY.iterator(); itr.hasNext();) {
			final BiomeInfo e = new BiomeInfo(itr.next());
			registry.put(e.biome, e);
		}

		// Add our fake biomes
		registry.put(UNDERWATER, new BiomeInfo(UNDERWATER));
		registry.put(UNDEROCEAN, new BiomeInfo(UNDEROCEAN));
		registry.put(UNDERDEEPOCEAN, new BiomeInfo(UNDERDEEPOCEAN));
		registry.put(UNDERRIVER, new BiomeInfo(UNDERRIVER));
		registry.put(WTF, new BiomeInfo(WTF));

		registry.put(PLAYER, PLAYER_INFO = new BiomeInfo(PLAYER));
		registry.put(VILLAGE, VILLAGE_INFO = new BiomeInfo(VILLAGE));
		registry.put(UNDERGROUND, UNDERGROUND_INFO = new BiomeInfo(UNDERGROUND));
		registry.put(CLOUDS, CLOUDS_INFO = new BiomeInfo(CLOUDS));
		registry.put(OUTERSPACE, OUTERSPACE_INFO = new BiomeInfo(OUTERSPACE));
	}

	@Override
	public void initComplete() {
		if (ModOptions.enableDebugLogging) {
			ModLog.info("*** BIOME REGISTRY ***");
			for (final BiomeInfo entry : registry.values())
				ModLog.info(entry.toString());
		}

		// Free memory because we no longer need
		biomeAliases.clear();
	}

	@Override
	public void fini() {
		
	}

	private final Map<Biome, BiomeInfo> registry = new IdentityHashMap<Biome, BiomeInfo>();
	private final Map<String, String> biomeAliases = new HashMap<String, String>();

	public static final FakeBiome UNDERGROUND = new FakeBiome("Underground");
	public static final FakeBiome PLAYER = new FakeBiome("Player");
	public static final FakeBiome UNDERWATER = new FakeBiome("Underwater");
	public static final FakeBiome UNDEROCEAN = new FakeBiome("UnderOCN");
	public static final FakeBiome UNDERDEEPOCEAN = new FakeBiome("UnderDOCN");
	public static final FakeBiome UNDERRIVER = new FakeBiome("UnderRVR");
	public static final FakeBiome OUTERSPACE = new FakeBiome("OuterSpace");
	public static final FakeBiome CLOUDS = new FakeBiome("Clouds");
	public static final FakeBiome VILLAGE = new FakeBiome("Village");
	
	public static BiomeInfo VILLAGE_INFO;
	public static BiomeInfo PLAYER_INFO;
	public static BiomeInfo UNDERGROUND_INFO;
	public static BiomeInfo CLOUDS_INFO;
	public static BiomeInfo OUTERSPACE_INFO;
	
	// This is for cases when the biome coming in doesn't make sense
	// and should default to something to avoid crap.
	private static final FakeBiome WTF = new FakeBiome("(FooBar)");

	@Nonnull
	public static String resolveName(@Nullable final Biome biome) {
		if (biome == null)
			return "(Bad Biomes)";
		return biome.getBiomeName();
	}

	@Nullable
	public BiomeInfo get(@Nonnull final Biome biome) {
		BiomeInfo entry = registry.get(biome == null ? WTF : biome);
		if (entry == null) {
			ModLog.warn("Biome [%s] was not detected during scan! Explicitly adding at defaults", resolveName(biome));
			entry = new BiomeInfo(biome);
			this.registry.put(entry.biome, entry);
		}
		return entry;
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
					biomeEntry.getSounds().clear();
					biomeEntry.getSpotSounds().clear();
				}

				if (entry.spotSoundChance != null)
					biomeEntry.setSpotSoundChance(entry.spotSoundChance.intValue());

				for (final SoundConfig sr : entry.sounds) {
					if (soundRegistry.isSoundBlocked(sr.sound))
						continue;
					final SoundEffect s = new SoundEffect(sr);
					if (s.type == SoundType.SPOT)
						biomeEntry.getSpotSounds().add(s);
					else
						biomeEntry.getSounds().add(s);
				}
			}
		}
	}
}
