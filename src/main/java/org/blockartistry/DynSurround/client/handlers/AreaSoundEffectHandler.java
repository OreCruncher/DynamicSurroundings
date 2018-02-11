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
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.registry.BiomeInfo;
import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectFloatHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class AreaSoundEffectHandler extends EffectHandlerBase {

	public static final int SCAN_INTERVAL = 4;

	private boolean doBiomeSounds() {
		return EnvironState.isPlayerUnderground() || !EnvironState.isPlayerInside();
	}

	private void getBiomeSounds(@Nonnull final TObjectFloatHashMap<SoundEffect> result) {
		// Need to collect sounds from all the applicable biomes
		// along with their weights.
		AreaSurveyHandler.getBiomes().forEachEntry((biome, w) -> {
			final List<SoundEffect> bs = new ArrayList<>();
			biome.findSoundMatches(bs);
			bs.forEach(fx -> result.adjustOrPutValue(fx, w, w));
			return true;
		});

		// Scale the volumes in the resulting list based on the weights
		final float area = AreaSurveyHandler.getBiomeArea();
		result.transformValues(v -> {
			return 0.1F + 0.9F * (v / area);
		});
	}

	public AreaSoundEffectHandler() {
		super("Area Sound Effects");
	}

	@Override
	public boolean doTick(final int tick) {
		return ModOptions.sound.enableBiomeSounds && (tick % SCAN_INTERVAL) == 0
				&& EnvironState.getWorld().isBlockLoaded(EnvironState.getPlayerPosition());
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {

		final TObjectFloatHashMap<SoundEffect> sounds = new TObjectFloatHashMap<SoundEffect>(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1F);
		if (doBiomeSounds())
			getBiomeSounds(sounds);

		final List<SoundEffect> playerSounds = new ArrayList<SoundEffect>();
		ClientRegistry.BIOME.PLAYER_INFO.findSoundMatches(playerSounds);
		if (ModOptions.sound.enableBattleMusic)
			ClientRegistry.BIOME.BATTLE_MUSIC_INFO.findSoundMatches(playerSounds);
		if (EnvironState.inVillage())
			ClientRegistry.BIOME.VILLAGE_INFO.findSoundMatches(playerSounds);

		playerSounds.forEach(fx -> sounds.put(fx, 1.0F));

		SoundEffectHandler.INSTANCE.queueAmbientSounds(sounds);

		if (doBiomeSounds()) {
			final BiomeInfo playerBiome = EnvironState.getPlayerBiome();
			final SoundEffect sound = playerBiome.getSpotSound(this.RANDOM);
			if (sound != null)
				SoundEffectHandler.INSTANCE.playSoundAtPlayer(player, sound);
		}

		final SoundEffect sound = ClientRegistry.BIOME.PLAYER_INFO.getSpotSound(this.RANDOM);
		if (sound != null)
			SoundEffectHandler.INSTANCE.playSoundAtPlayer(player, sound);
	}

}
