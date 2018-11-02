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
package org.orecruncher.dsurround.client;

import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.biome.BiomeRegistry;
import org.orecruncher.dsurround.registry.block.BlockRegistry;
import org.orecruncher.dsurround.registry.dimension.DimensionRegistry;
import org.orecruncher.dsurround.registry.effect.EffectRegistry;
import org.orecruncher.dsurround.registry.footstep.FootstepsRegistry;
import org.orecruncher.dsurround.registry.item.ItemRegistry;
import org.orecruncher.dsurround.registry.season.SeasonRegistry;
import org.orecruncher.dsurround.registry.sound.SoundRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Common references to the various registries used by the client side of the
 * mod. Consolidate the various reference needs in one place for better
 * management.
 */
@SideOnly(Side.CLIENT)
public final class ClientRegistry {

	private ClientRegistry() {

	}

	public static final SoundRegistry SOUND;
	public static final BiomeRegistry BIOME;
	public static final BlockRegistry BLOCK;
	public static final DimensionRegistry DIMENSION;
	public static final SeasonRegistry SEASON;
	public static final ItemRegistry ITEMS;
	public static final FootstepsRegistry FOOTSTEPS;
	public static final EffectRegistry EFFECTS;

	static {

		DIMENSION = new DimensionRegistry(Side.CLIENT);
		SOUND = new SoundRegistry(Side.CLIENT);
		BIOME = new BiomeRegistry(Side.CLIENT);
		BLOCK = new BlockRegistry(Side.CLIENT);
		SEASON = new SeasonRegistry(Side.CLIENT);
		ITEMS = new ItemRegistry(Side.CLIENT);
		FOOTSTEPS = new FootstepsRegistry(Side.CLIENT);
		EFFECTS = new EffectRegistry(Side.CLIENT);

		final RegistryManager rm = RegistryManager.get();
		rm.register(DIMENSION);
		rm.register(SOUND);
		rm.register(BIOME);
		rm.register(BLOCK);
		rm.register(SEASON);
		rm.register(ITEMS);
		rm.register(FOOTSTEPS);
		rm.register(EFFECTS);

		rm.reload();
	}
}
