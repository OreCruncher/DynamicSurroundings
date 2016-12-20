/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) Abastro, OreCruncher
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

package org.blockartistry.mod.DynSurround.util;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.Module;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class SoundUtils {

	private SoundUtils() {
		
	}
	
	@Nonnull
	public static SoundEvent getOrRegisterSound(@Nonnull final String location) {
		final ResourceLocation rl;
		if (location.contains(":")) {
			rl = new ResourceLocation(location);
		} else {
			rl = new ResourceLocation(Module.RESOURCE_ID, location);
		}
		return getOrRegisterSound(rl);
	}

	@Nonnull
	public static SoundEvent getOrRegisterSound(@Nonnull final ResourceLocation location) {
		if (SoundEvent.REGISTRY.containsKey(location))
			return SoundEvent.REGISTRY.getObject(location);

		final SoundEvent sound = new SoundEvent(location).setRegistryName(location);
		GameRegistry.register(sound);
		return sound;
	}

}