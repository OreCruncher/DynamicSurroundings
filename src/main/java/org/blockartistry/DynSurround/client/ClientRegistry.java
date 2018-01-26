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
package org.blockartistry.DynSurround.client;

import org.blockartistry.DynSurround.registry.BiomeRegistry;
import org.blockartistry.DynSurround.registry.BlockRegistry;
import org.blockartistry.DynSurround.registry.DimensionRegistry;
import org.blockartistry.DynSurround.registry.FootstepsRegistry;
import org.blockartistry.DynSurround.registry.ItemRegistry;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.DynSurround.registry.SeasonRegistry;
import org.blockartistry.DynSurround.registry.SoundRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Common references to the various registries used by the client side
 * of the mod.  Consolidate the various reference needs in one place for
 * better management.
 */
@SideOnly(Side.CLIENT)
public final class ClientRegistry {
	
	private ClientRegistry() {
		
	}

	public static final SoundRegistry SOUND = RegistryManager.<SoundRegistry>get(RegistryType.SOUND); 
	public static final BiomeRegistry BIOME = RegistryManager.<BiomeRegistry>get(RegistryType.BIOME);
	public static final BlockRegistry BLOCK = RegistryManager.<BlockRegistry>get(RegistryType.BLOCK);
	public static final DimensionRegistry DIMENSION = RegistryManager.<DimensionRegistry>get(RegistryType.DIMENSION);
	public static final SeasonRegistry SEASON = RegistryManager.<SeasonRegistry>get(RegistryType.SEASON);
	public static final ItemRegistry ITEMS = RegistryManager.<ItemRegistry>get(RegistryType.ITEMS);
	public static final FootstepsRegistry FOOTSTEPS = RegistryManager.<FootstepsRegistry>get(RegistryType.FOOTSTEPS);
}
