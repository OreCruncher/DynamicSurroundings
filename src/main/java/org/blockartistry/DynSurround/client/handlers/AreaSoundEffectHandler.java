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

package org.blockartistry.DynSurround.client.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.registry.BiomeInfo;
import org.blockartistry.DynSurround.registry.BiomeRegistry;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;

import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectFloatHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class AreaSoundEffectHandler extends EffectHandlerBase {

	public static final int SCAN_INTERVAL = 4;

	private static boolean doBiomeSounds() {
		return EnvironState.isPlayerUnderground() || !EnvironState.isPlayerInside();
	}

	private static void getBiomeSounds(@Nonnull final TObjectFloatHashMap<SoundEffect> result) {
		// Need to collect sounds from all the applicable biomes
		// along with their weights.
		final TObjectIntIterator<BiomeInfo> info = AreaSurveyHandler.getBiomes().iterator();
		while (info.hasNext()) {
			info.advance();
			final List<SoundEffect> bs = new ArrayList<SoundEffect>();
			info.key().findSoundMatches(bs);
			for (final SoundEffect sound : bs) {
				final int w = info.value();
				result.adjustOrPutValue(sound, w, w);
			}
		}

		// Scale the volumes in the resulting list based on the weights
		final int area = AreaSurveyHandler.getBiomeArea();
		final TObjectFloatIterator<SoundEffect> itr = result.iterator();
		while (itr.hasNext()) {
			itr.advance();
			final float scale = 0.1F + 0.9F * ((float) itr.value() / (float) area);
			itr.setValue(scale);
		}
	}

	protected final BiomeRegistry registry;
	
	public AreaSoundEffectHandler() {
		super("AreaSoundEffectHandler");
		
		this.registry = RegistryManager.<BiomeRegistry>get(RegistryType.BIOME);
	}
	
	private static boolean skipTick(@Nonnull final EntityPlayer player) {
		// Skip processing this tick IF:
		// * Option is disabled
		// * It's not the appropriate tick interval
		// * The chunk the player is in is not loaded
		return !(ModOptions.enableBiomeSounds && (EnvironState.getTickCounter() % SCAN_INTERVAL) == 0
				&& EnvironState.getWorld().isBlockLoaded(EnvironState.getPlayerPosition()));
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {

		if (skipTick(player))
			return;

		final TObjectFloatHashMap<SoundEffect> sounds = new TObjectFloatHashMap<SoundEffect>();
		if (doBiomeSounds())
			getBiomeSounds(sounds);

		final List<SoundEffect> playerSounds = new ArrayList<SoundEffect>();
		this.registry.PLAYER_INFO.findSoundMatches(playerSounds);
		if (ModOptions.enableBattleMusic)
			this.registry.BATTLE_MUSIC_INFO.findSoundMatches(playerSounds);
		if (EnvironState.inVillage())
			this.registry.VILLAGE_INFO.findSoundMatches(playerSounds);

		for (final SoundEffect effect : playerSounds)
			sounds.put(effect, 1.0F);

		SoundEffectHandler.INSTANCE.queueAmbientSounds(sounds);

		if (doBiomeSounds()) {
			final BiomeInfo playerBiome = EnvironState.getPlayerBiome();
			final SoundEffect sound = playerBiome.getSpotSound(this.RANDOM);
			if (sound != null)
				SoundEffectHandler.INSTANCE.playSoundAtPlayer(player, sound);
		}

		final SoundEffect sound = this.registry.PLAYER_INFO.getSpotSound(this.RANDOM);
		if (sound != null)
			SoundEffectHandler.INSTANCE.playSoundAtPlayer(player, sound);
	}

}
