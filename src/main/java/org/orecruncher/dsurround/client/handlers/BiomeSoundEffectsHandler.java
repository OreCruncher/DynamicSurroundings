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

package org.orecruncher.dsurround.client.handlers;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.client.handlers.scanners.BiomeScanner;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.biome.BiomeInfo;
import org.orecruncher.lib.collections.ObjectArray;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BiomeSoundEffectsHandler extends EffectHandlerBase {

	public static final int SCAN_INTERVAL = 4;

	protected final BiomeScanner biomes = new BiomeScanner();

	public BiomeSoundEffectsHandler() {
		super("Biome Sound Effects");
	}

	@Override
	public boolean doTick(final int tick) {
		return ModOptions.sound.enableBiomeSounds && (tick % SCAN_INTERVAL) == 0
				&& EnvironState.getDimensionInfo().getPlayBiomeSounds()
				&& EnvironState.getWorld().isBlockLoaded(EnvironState.getPlayerPosition());
	}

	private boolean doBiomeSounds() {
		return EnvironState.isPlayerUnderground() || EnvironState.getDimensionInfo().getAlwaysOutside() || !EnvironState.isPlayerInside();
	}

	private void getBiomeSounds(@Nonnull final Object2FloatOpenHashMap<SoundEffect> result) {
		// Need to collect sounds from all the applicable biomes
		// along with their weights.
		this.biomes.getBiomes().reference2FloatEntrySet().stream()
			.forEach(e -> e.getKey().findSoundMatches().forEach(fx -> result.addTo(fx, e.getFloatValue())));

		// Scale the volumes in the resulting list based on the weights
		final float area = this.biomes.getBiomeArea();
		result.replaceAll((fx, v) -> 0.1F + 0.9F * (v / area));
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {

		this.biomes.update();

		final Object2FloatOpenHashMap<SoundEffect> sounds = new Object2FloatOpenHashMap<>();
		sounds.defaultReturnValue(0);

		// Only gather data if the player is alive. If the player is dead the biome
		// sounds will cease playing.
		if (player.isEntityAlive()) {

			if (doBiomeSounds())
				getBiomeSounds(sounds);

			final ObjectArray<SoundEffect> playerSounds = new ObjectArray<>();
			RegistryManager.BIOME.PLAYER_INFO.findSoundMatches(playerSounds);
			RegistryManager.BIOME.BATTLE_MUSIC_INFO.findSoundMatches(playerSounds);
			if (EnvironState.inVillage())
				RegistryManager.BIOME.VILLAGE_INFO.findSoundMatches(playerSounds);

			playerSounds.forEach(fx -> sounds.put(fx, 1.0F));

			if (doBiomeSounds()) {
				final BiomeInfo playerBiome = EnvironState.getPlayerBiome();
				final SoundEffect sound = playerBiome.getSpotSound(this.RANDOM);
				if (sound != null)
					SoundEffectHandler.INSTANCE.playSoundAtPlayer(player, sound);
			}

			final SoundEffect sound = RegistryManager.BIOME.PLAYER_INFO.getSpotSound(this.RANDOM);
			if (sound != null)
				SoundEffectHandler.INSTANCE.playSoundAtPlayer(player, sound);
		}

		SoundEffectHandler.INSTANCE.queueAmbientSounds(sounds);
	}

}
