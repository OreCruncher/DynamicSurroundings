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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.DSurround;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

class RegistryManagerClient extends RegistryManager {

	private static final ResourceLocation SCRIPT = new ResourceLocation(DSurround.RESOURCE_ID, "configure.json");

	RegistryManagerClient() {
		super(Side.CLIENT);
		this.registries[RegistryType.BLOCK] = new BlockRegistry(Side.CLIENT);
		this.registries[RegistryType.FOOTSTEPS] = new FootstepsRegistry(Side.CLIENT);
		this.registries[RegistryType.ITEMS] = new ItemRegistry(Side.CLIENT);
	}

	private boolean checkCompatible(@Nonnull final ResourcePackRepository.Entry pack) {
		return pack.getResourcePack().resourceExists(SCRIPT);
	}

	@Nullable
	private InputStream openScript(@Nonnull final IResourcePack pack) throws IOException {
		return pack.getInputStream(SCRIPT);
	}

	@Nonnull
	public List<InputStream> getAdditionalScripts() {
		final List<ResourcePackRepository.Entry> repo = Minecraft.getMinecraft().getResourcePackRepository()
				.getRepositoryEntries();

		final List<InputStream> streams = new ArrayList<InputStream>();

		// Look in other resource packs for more configuration data
		for (final ResourcePackRepository.Entry pack : repo) {
			if (checkCompatible(pack)) {
				ModLog.debug("Found script in resource pack: %s", pack.getResourcePackName());
				try {
					final InputStream stream = openScript(pack.getResourcePack());
					if (stream != null)
						streams.add(stream);
				} catch (final Throwable t) {
					ModLog.error("Unable to open script in resource pack", t);
				}
			}
		}
		return streams;
	}

}
