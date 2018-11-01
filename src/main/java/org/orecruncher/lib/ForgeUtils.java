/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.orecruncher.lib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class ForgeUtils {

	private ForgeUtils() {

	}

	@Nullable
	public static ModContainer findModContainer(@Nonnull final String modId) {
		if (!StringUtils.isEmpty(modId))
			for (final ModContainer mod : Loader.instance().getActiveModList()) {
				if (mod.getModId().equalsIgnoreCase(modId))
					return mod;
			}
		return null;
	}

	@Nullable
	public static ModMetadata getModMetadata(@Nonnull final String modId) {
		final ModContainer container = findModContainer(modId);
		return container != null ? container.getMetadata() : null;
	}

	@Nonnull
	public static String getModName(@Nonnull final String modId) {
		if ("minecraft".equalsIgnoreCase(modId))
			return "Minecraft";
		final ModContainer cont = findModContainer(modId);
		return cont != null ? cont.getName() : "UNKNOWN";
	}

	@Nonnull
	public static String getModName(@Nonnull final ResourceLocation resource) {
		return getModName(resource.getNamespace());
	}

	@Nonnull
	public static String getForgeVersion() {
		final ModContainer mod = findModContainer("forge");
		return mod != null ? mod.getVersion() : StringUtils.EMPTY;
	}

	@Nullable
	public static Item getItem(@Nonnull final String resourceName) {
		return ForgeRegistries.ITEMS.getValue(new ResourceLocation(resourceName));
	}
}
