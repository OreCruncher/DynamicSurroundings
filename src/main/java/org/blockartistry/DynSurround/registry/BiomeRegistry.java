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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.data.xface.BiomeConfig;
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
	public static final FakeBiome BATTLE_MUSIC = new FakeBiome("BattleMusic");

	public BiomeInfo VILLAGE_INFO;
	public BiomeInfo PLAYER_INFO;
	public BiomeInfo UNDERGROUND_INFO;
	public BiomeInfo CLOUDS_INFO;
	public BiomeInfo OUTERSPACE_INFO;
	public BiomeInfo WTF_INFO;
	public BiomeInfo BATTLE_MUSIC_INFO;

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

		this.registry.put(BATTLE_MUSIC, BATTLE_MUSIC_INFO = new BiomeInfo(BATTLE_MUSIC));
	}

	@Override
	public void initComplete() {
		if (ModOptions.enableDebugLogging) {
			DSurround.log().info("*** BIOME REGISTRY ***");
			final List<BiomeInfo> info = new ArrayList<BiomeInfo>(this.registry.values());
			Collections.sort(info);
			for (final BiomeInfo entry : info)
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
			// Two possibilities:
			// - A biome could have been dynamically added after the mod's init
			// phase. Open Terrain Generation can do this.
			//
			// - A mod replaced a vanilla biome in the biome registry but did
			// not update the identity instance in Biome. An example of this
			// is Biome.PLAINS. It is the default biome returned and a
			// chunk isn't fully loaded. If a mod updates the PLAINS entry
			// in the REGISTRY and did NOT update Biome.PLAINS with the new
			// object reference things have become inconsistent.
			final int id = Biome.getIdForBiome(biome);
			if (id == -1) {
				final Biome tryBiome;
				// Check for the second case
				if ("Ocean".equals(biome.getBiomeName()))
					tryBiome = Biome.getBiomeForId(0);
				else if ("Plains".equals(biome.getBiomeName()))
					tryBiome = Biome.getBiomeForId(1);
				else
					tryBiome = null;

				if (tryBiome == null) {
					final String err = String.format("Unknown biome [%s] (%s)", biome.getBiomeName(),
							biome.getClass().getName());
					throw new RuntimeException(err);
				} else {
					result = this.registry.get(tryBiome);
				}
			}

			// Handle the first case
			if (result == null) {
				DSurround.log().warn("Biome [%s] not detected during initialization - forcing reload (%s)",
						biome.getBiomeName(), biome.getClass());
				RegistryManager.reloadResources(this.side);
				result = this.registry.get(biome);
			}

			if (result == null) {
				throw new RuntimeException("What's going on with biomes?");
			}
		}
		return result;
	}

	final boolean isBiomeMatch(@Nonnull final BiomeConfig entry, @Nonnull final BiomeInfo info) {
		if (Pattern.matches(entry.biomeName, info.getBiomeName()))
			return true;
		final String alias = this.biomeAliases.get(info.getBiomeName());
		return alias == null ? false : Pattern.matches(entry.biomeName, alias);
	}

	public void registerBiomeAlias(@Nonnull final String alias, @Nonnull final String biome) {
		this.biomeAliases.put(alias, biome);
	}

	public void register(@Nonnull final BiomeConfig entry) {
		final BiomeMatcher matcher = BiomeMatcher.getMatcher(entry);
		for (final BiomeInfo biomeEntry : this.registry.values()) {
			if (matcher.match(biomeEntry))
				biomeEntry.update(entry);
		}
	}
}
