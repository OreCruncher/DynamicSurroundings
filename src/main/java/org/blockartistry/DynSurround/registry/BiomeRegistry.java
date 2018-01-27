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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.data.xface.BiomeConfig;
import org.blockartistry.DynSurround.data.xface.ModConfigurationFile;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
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

	private final Map<ResourceLocation, BiomeInfo> registry = new HashMap<ResourceLocation, BiomeInfo>();
	private final Map<String, String> biomeAliases = new HashMap<String, String>();

	public BiomeRegistry(@Nonnull final Side side) {
		super(side);
	}

	private void register(@Nonnull final Biome biome) {
		final BiomeInfo e = new BiomeInfo(biome);
		this.registry.put(e.getKey(), e);
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

		final List<Biome> biomes = ForgeRegistries.BIOMES.getValues();
		for (final Biome b : biomes)
			register(b);

		// Add our fake biomes
		register(UNDERWATER);
		register(UNDEROCEAN);
		register(UNDERDEEPOCEAN);
		register(UNDERRIVER);
		register(PLAYER);
		register(VILLAGE);
		register(UNDERGROUND);
		register(CLOUDS);
		register(OUTERSPACE);
		register(BATTLE_MUSIC);

		this.PLAYER_INFO = resolve(PLAYER);
		this.VILLAGE_INFO = resolve(VILLAGE);
		this.UNDERGROUND_INFO = resolve(UNDERGROUND);
		this.CLOUDS_INFO = resolve(CLOUDS);
		this.OUTERSPACE_INFO = resolve(OUTERSPACE);
		this.BATTLE_MUSIC_INFO = resolve(BATTLE_MUSIC);

		// WTF is a strange animal
		register(WTF);
		this.WTF_INFO = resolve(WTF);
	}

	@Override
	public void configure(@Nonnull final ModConfigurationFile cfg) {
		for (final Entry<String, String> entry : cfg.biomeAlias.entrySet())
			this.registerBiomeAlias(entry.getKey(), entry.getValue());

		for (final BiomeConfig biome : cfg.biomes)
			this.register(biome);
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

	@Nullable
	private BiomeInfo resolve(@Nonnull final Biome biome) {
		return this.registry.get(BiomeInfo.getKey(biome));
	}

	@Nonnull
	public BiomeInfo get(@Nonnull final Biome biome) {
		// This shouldn't happen, but...
		if (biome == null)
			return WTF_INFO;

		BiomeInfo result = resolve(biome);
		if (result == null) {
			DSurround.log().warn("Biome [%s] not detected during initialization - forcing reload (%s)",
					biome.getBiomeName(), biome.getClass());
			RegistryManager.reloadResources(this.side);
			result = resolve(biome);

			if (result == null) {
				DSurround.log().warn("Unable to locate biome [%s] (%s) after reload", biome.getBiomeName(),
						biome.getClass().getName());
				result = WTF_INFO;
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
